package dev.forint.deafmute.cameraxdemo.ui

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.forint.deafmute.cameraxdemo.domain.BufferMutation
import dev.forint.deafmute.cameraxdemo.domain.CountdownManager
import dev.forint.deafmute.cameraxdemo.domain.RecognitionConstants
import dev.forint.deafmute.cameraxdemo.domain.SentenceBufferManager
import dev.forint.deafmute.cameraxdemo.model.dto.BootstrapResponseDto
import dev.forint.deafmute.cameraxdemo.model.dto.DetectedHandFrame
import dev.forint.deafmute.cameraxdemo.model.dto.PredictRequestDto
import dev.forint.deafmute.cameraxdemo.model.dto.PredictResponseDto
import dev.forint.deafmute.cameraxdemo.model.dto.SessionActionRequestDto
import dev.forint.deafmute.cameraxdemo.model.dto.SessionStartRequestDto
import dev.forint.deafmute.cameraxdemo.repository.RecognitionRepository
import dev.forint.deafmute.cameraxdemo.repository.RecognitionSettingsStore
import dev.forint.deafmute.cameraxdemo.tts.SpeechPlayer
import dev.forint.deafmute.cameraxdemo.tts.TtsSpeakAttemptResult
import dev.forint.deafmute.cameraxdemo.tts.TtsDiagnostics
import dev.forint.deafmute.cameraxdemo.tts.TtsStatusMapper
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecognitionViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsStore = RecognitionSettingsStore(application)
    private val speechPlayer = SpeechPlayer(application)
    private val sentenceBufferManager = SentenceBufferManager()
    private val countdownManager = CountdownManager(
        scope = viewModelScope,
        durationMs = RecognitionConstants.COUNTDOWN_TIMEOUT_MS,
        tickMs = RecognitionConstants.COUNTDOWN_TICK_MS,
        onFinished = ::handleCountdownFinished,
    )

    private val _uiState = MutableStateFlow(RecognitionUiState())
    val uiState: StateFlow<RecognitionUiState> = _uiState.asStateFlow()

    private var repository: RecognitionRepository
    private var lastPredictDispatchAt: Long = 0L
    private var initializeJob: Job? = null
    private var predictJob: Job? = null

    init {
        val settings = settingsStore.load()
        repository = RecognitionRepository(settings.baseUrl)
        _uiState.update {
            it.copy(
                baseUrl = settings.baseUrl,
                authToken = settings.authToken,
            )
        }

        viewModelScope.launch {
            countdownManager.remainingMillis.collect { remaining ->
                _uiState.update { state -> state.copy(countdownMillis = remaining) }
            }
        }

        viewModelScope.launch {
            speechPlayer.ready.collect { ready ->
                _uiState.update { state -> state.copy(ttsReady = ready) }
            }
        }

        viewModelScope.launch {
            speechPlayer.diagnostics.collect(::applyTtsDiagnostics)
        }
    }

    fun initialize(forceRestart: Boolean = false) {
        if (!forceRestart && _uiState.value.sessionId.isNotBlank() && _uiState.value.sessionStatus == "active") {
            return
        }
        if (initializeJob?.isActive == true) {
            return
        }
        initializeJob = viewModelScope.launch {
            setBusy(true)
            _uiState.update { it.copy(sessionStatus = "starting") }
            if (forceRestart) {
                closeSessionInternal(
                    reason = RecognitionConstants.CLOSE_REASON_RESTART,
                    clearSessionState = true,
                    silent = true,
                )
            }
            try {
                val bootstrap = repository.bootstrap(_uiState.value.authToken)
                applyBootstrap(bootstrap)
                val session = repository.startSession(
                    token = _uiState.value.authToken,
                    request = SessionStartRequestDto(
                        clientType = RecognitionConstants.CLIENT_TYPE,
                        sceneCode = RecognitionConstants.SCENE_CODE,
                        engineType = RecognitionConstants.ENGINE_TYPE,
                        appVersion = RecognitionConstants.APP_VERSION,
                    ),
                )
                _uiState.update {
                    it.copy(
                        sessionId = session.sessionId,
                        sessionStatus = "active",
                        sessionStartedAt = session.startedAt.orEmpty(),
                        errorMessage = null,
                        frameNo = 0,
                        lastMessage = "bootstrap + session/start 已完成",
                    )
                }
                lastPredictDispatchAt = 0L
            } catch (throwable: Throwable) {
                reportError("初始化失败：${throwable.message}")
                _uiState.update { it.copy(sessionStatus = "error") }
            } finally {
                setBusy(false)
            }
        }
    }

    fun onSettingsSubmitted(baseUrl: String, authToken: String) {
        val normalizedBaseUrl = baseUrl.trim()
        if (normalizedBaseUrl.isBlank()) {
            reportError("Recognition API Base 不能为空")
            return
        }
        settingsStore.save(normalizedBaseUrl, authToken.trim())
        try {
            repository = RecognitionRepository(normalizedBaseUrl)
        } catch (throwable: Throwable) {
            reportError("Recognition API Base 非法：${throwable.message}")
            return
        }
        _uiState.update {
            it.copy(
                baseUrl = normalizedBaseUrl,
                authToken = authToken.trim(),
                lastMessage = "已保存设置，准备重新拉起会话",
                errorMessage = null,
            )
        }
        initialize(forceRestart = true)
    }

    fun onCameraFacingChanged(usingFrontCamera: Boolean) {
        _uiState.update {
            it.copy(
                usingFrontCamera = usingFrontCamera,
                cameraFacing = if (usingFrontCamera) "front" else "back",
                mirrored = usingFrontCamera,
            )
        }
    }

    fun onHandLandmarksDetected(frame: DetectedHandFrame) {
        val sessionId = _uiState.value.sessionId
        if (sessionId.isBlank()) {
            return
        }
        if (predictJob?.isActive == true) {
            return
        }
        val now = SystemClock.elapsedRealtime()
        if (now - lastPredictDispatchAt < RecognitionConstants.PREDICT_INTERVAL_MS) {
            return
        }
        lastPredictDispatchAt = now
        predictJob = viewModelScope.launch {
            val nextFrameNo = _uiState.value.frameNo + 1
            _uiState.update {
                it.copy(
                    frameNo = nextFrameNo,
                    handedness = frame.handedness ?: "--",
                    cameraFacing = frame.cameraFacing,
                    mirrored = frame.mirrored,
                )
            }
            try {
                val response = repository.predict(
                    token = _uiState.value.authToken,
                    request = PredictRequestDto(
                        sessionId = sessionId,
                        frameNo = nextFrameNo,
                        capturedAt = frame.capturedAt,
                        source = frame.source,
                        handedness = frame.handedness,
                        mirrored = frame.mirrored,
                        cameraFacing = frame.cameraFacing,
                        landmarks = frame.landmarks,
                    ),
                )
                applyPrediction(response)
            } catch (throwable: Throwable) {
                reportError("predict 调用失败：${throwable.message}")
            }
        }
    }

    fun onHandLandmarkerError(message: String) {
        reportError("MediaPipe：$message")
    }

    fun onClearBufferClicked() {
        val mutation = sentenceBufferManager.clear()
        countdownManager.cancel()
        applyBufferMutation(mutation)
    }

    fun onSpeakClicked() {
        val text = sentenceBufferManager.timeoutCommitText()
        if (!text.isNullOrBlank()) {
            applySpeakResult(
                text = text,
                attemptResult = speechPlayer.speak(text),
                triggeredBy = "manual",
            )
        }
    }

    fun retryTtsDetection() {
        speechPlayer.retryDetection()
        _uiState.update {
            it.copy(
                lastMessage = "已重新触发 TTS 检测",
                errorMessage = null,
            )
        }
    }

    fun onCloseSessionClicked() {
        viewModelScope.launch {
            closeSessionInternal(
                reason = RecognitionConstants.CLOSE_REASON_MANUAL_CLOSE,
                clearSessionState = true,
                silent = false,
            )
        }
    }

    fun onPageExit() {
        viewModelScope.launch {
            closeSessionInternal(
                reason = RecognitionConstants.CLOSE_REASON_PAGE_CLOSE,
                clearSessionState = true,
                silent = true,
            )
        }
    }

    private fun applyBootstrap(bootstrap: BootstrapResponseDto) {
        _uiState.update {
            it.copy(
                activeConfigName = bootstrap.activeConfig?.configName ?: "--",
                landmarkPointCount = bootstrap.landmarkPointCount ?: 0,
                supportedLandmarkGestureCodes = bootstrap.supportedLandmarkGestureCodes.orEmpty(),
                landmarkCoordinateMode = bootstrap.landmarkCoordinateMode.orEmpty(),
                landmarkOrigin = bootstrap.landmarkOrigin.orEmpty(),
                landmarkOrder = bootstrap.landmarkOrder.orEmpty(),
                landmarkHandMode = bootstrap.landmarkHandMode.orEmpty(),
                landmarkAssumptions = bootstrap.landmarkAssumptions.orEmpty(),
                lastMessage = "bootstrap 成功，已拿到 landmarks 固定契约",
                errorMessage = null,
            )
        }
    }

    private fun applyPrediction(response: PredictResponseDto) {
        val mutation = sentenceBufferManager.apply(response)
        _uiState.update {
            it.copy(
                currentGesture = response.gesture ?: "--",
                displayText = response.displayText ?: response.label ?: "--",
                outputType = response.outputType ?: "NONE",
                controlAction = response.controlAction.orEmpty(),
                inputType = response.inputType.orEmpty(),
                stableCount = response.stableCount ?: 0,
                requiredHits = response.requiredHits ?: 0,
                debounced = response.debounced ?: false,
                detectedGestureCandidates = response.detectedGestureCandidates.orEmpty(),
                latestTraceId = response.traceId.orEmpty(),
                errorMessage = null,
            )
        }

        applyBufferMutation(mutation)

        if (mutation.immediateSpeakText.isNullOrBlank()) {
            if (mutation.restartCountdown && _uiState.value.bufferText.isNotBlank()) {
                countdownManager.restart()
            } else if (_uiState.value.bufferText.isBlank()) {
                countdownManager.cancel()
            }
        } else {
            countdownManager.cancel()
            speechPlayer.speak(mutation.immediateSpeakText)
            _uiState.update {
                it.copy(lastMessage = mutation.message ?: "控制动作已触发播报")
            }
        }
    }

    private fun applyBufferMutation(mutation: BufferMutation) {
        _uiState.update {
            it.copy(
                bufferText = mutation.text,
                lastMessage = mutation.message ?: it.lastMessage,
                errorMessage = null,
            )
        }
    }

    private fun handleCountdownFinished() {
        val text = sentenceBufferManager.timeoutCommitText() ?: return
        applySpeakResult(
            text = text,
            attemptResult = speechPlayer.speak(text),
            triggeredBy = "countdown",
        )
    }

    private fun applyTtsDiagnostics(diagnostics: TtsDiagnostics) {
        val presentation = TtsStatusMapper.map(diagnostics)
        _uiState.update {
            it.copy(
                ttsReady = diagnostics.ready,
                ttsUserStatusCode = presentation.code.code,
                ttsUserStatusText = presentation.statusText,
                ttsProblemReason = presentation.reason,
                ttsActionSuggestion = presentation.suggestion,
                ttsPhase = diagnostics.phase,
                ttsDefaultEngine = diagnostics.defaultEngine,
                ttsInitStatusCode = diagnostics.initStatusCode,
                ttsLanguageStatusCode = diagnostics.languageStatusCode,
                ttsInstalledEngines = diagnostics.installedEngines,
                ttsDiscoveredServices = diagnostics.discoveredServices,
                ttsPendingText = diagnostics.pendingText,
                ttsLastSpokenText = diagnostics.lastSpokenText,
                ttsLastError = diagnostics.lastError,
                ttsLogLines = diagnostics.logLines,
            )
        }
    }

    private fun applySpeakResult(
        text: String,
        attemptResult: TtsSpeakAttemptResult,
        triggeredBy: String,
    ) {
        val message = when (attemptResult) {
            TtsSpeakAttemptResult.SPOKEN -> {
                if (triggeredBy == "manual") {
                    "已手动播报：$text"
                } else {
                    "倒计时结束，已播报：$text"
                }
            }
            TtsSpeakAttemptResult.QUEUED_UNAVAILABLE -> {
                "已触发播报请求，但系统 TTS 不可用。当前状态：${_uiState.value.ttsUserStatusText}"
            }
            TtsSpeakAttemptResult.ERROR -> {
                "已触发播报请求，但系统 TTS 调用失败。当前状态：${_uiState.value.ttsUserStatusText}"
            }
        }
        _uiState.update {
            it.copy(
                lastMessage = message,
                errorMessage = if (attemptResult == TtsSpeakAttemptResult.SPOKEN) null else message,
            )
        }
    }

    private suspend fun closeSessionInternal(
        reason: String,
        clearSessionState: Boolean,
        silent: Boolean,
    ) {
        val sessionId = _uiState.value.sessionId
        if (sessionId.isNotBlank()) {
            try {
                repository.closeSession(
                    token = _uiState.value.authToken,
                    request = SessionActionRequestDto(
                        sessionId = sessionId,
                        reason = reason,
                    ),
                )
            } catch (throwable: Throwable) {
                if (!silent) {
                    reportError("session/close 失败：${throwable.message}")
                }
            }
        }

        if (clearSessionState) {
            countdownManager.cancel()
            val clearedBuffer = sentenceBufferManager.clear()
            _uiState.update {
                it.copy(
                    sessionId = "",
                    sessionStatus = "closed",
                    sessionStartedAt = "",
                    bufferText = clearedBuffer.text,
                    currentGesture = "--",
                    displayText = "--",
                    outputType = "NONE",
                    controlAction = "",
                    inputType = "",
                    frameNo = 0,
                    stableCount = 0,
                    requiredHits = 0,
                    detectedGestureCandidates = emptyList(),
                    latestTraceId = "",
                    lastMessage = if (silent) "页面退出，准备关闭会话" else "会话已关闭",
                )
            }
        }
    }

    private fun setBusy(busy: Boolean) {
        _uiState.update { it.copy(isBusy = busy) }
    }

    private fun reportError(message: String) {
        _uiState.update {
            it.copy(
                errorMessage = message,
                lastMessage = message,
            )
        }
    }

    override fun onCleared() {
        countdownManager.cancel()
        speechPlayer.shutdown()
        super.onCleared()
    }
}
