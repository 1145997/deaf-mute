package dev.forint.deafmute.cameraxdemo.tts

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TtsDiagnostics(
    val phase: String = "idle",
    val ready: Boolean = false,
    val initStatusCode: Int? = null,
    val languageStatusCode: Int? = null,
    val defaultEngine: String = "--",
    val installedEngines: List<String> = emptyList(),
    val discoveredServices: List<String> = emptyList(),
    val pendingText: String? = null,
    val lastSpokenText: String? = null,
    val lastError: String? = null,
    val logLines: List<String> = emptyList(),
)

enum class TtsSpeakAttemptResult {
    SPOKEN,
    QUEUED_UNAVAILABLE,
    ERROR,
}

class SpeechPlayer(context: Context) : TextToSpeech.OnInitListener {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready.asStateFlow()

    private val _diagnostics = MutableStateFlow(TtsDiagnostics())
    val diagnostics: StateFlow<TtsDiagnostics> = _diagnostics.asStateFlow()

    private var textToSpeech: TextToSpeech? = null
    private var pendingText: String? = null
    private var initTimeoutJob: Job? = null

    init {
        bootstrap()
    }

    override fun onInit(status: Int) {
        initTimeoutJob?.cancel()
        logEvent("onInit callback received, status=$status (${describeInitStatus(status)})")

        if (status != TextToSpeech.SUCCESS) {
            _ready.value = false
            _diagnostics.update {
                it.copy(
                    phase = "error",
                    ready = false,
                    initStatusCode = status,
                    defaultEngine = resolveDefaultEngine(),
                    installedEngines = resolveInstalledEngines(),
                    lastError = "onInit failed: ${describeInitStatus(status)}",
                )
            }
            return
        }

        val languageResult = try {
            textToSpeech?.setLanguage(Locale.CHINA) ?: TextToSpeech.ERROR
        } catch (throwable: Throwable) {
            logEvent("setLanguage(Locale.CHINA) threw ${throwable.javaClass.simpleName}: ${throwable.message}")
            TextToSpeech.ERROR
        }

        val ready = languageResult != TextToSpeech.LANG_MISSING_DATA &&
            languageResult != TextToSpeech.LANG_NOT_SUPPORTED &&
            languageResult != TextToSpeech.ERROR

        _ready.value = ready
        _diagnostics.update {
            it.copy(
                phase = if (ready) "ready" else "error",
                ready = ready,
                initStatusCode = status,
                languageStatusCode = languageResult,
                defaultEngine = resolveDefaultEngine(),
                installedEngines = resolveInstalledEngines(),
                pendingText = pendingText,
                lastError = if (ready) null else "setLanguage failed: ${describeLanguageStatus(languageResult)}",
            )
        }
        logEvent(
            buildString {
                append("Language setup finished, result=")
                append(languageResult)
                append(" (")
                append(describeLanguageStatus(languageResult))
                append("), defaultEngine=")
                append(resolveDefaultEngine())
            },
        )

        if (!ready) {
            return
        }

        pendingText?.let {
            logEvent("Flushing queued utterance after init")
            speak(it)
            pendingText = null
            _diagnostics.update { diagnostics ->
                diagnostics.copy(pendingText = null)
            }
        }
    }

    fun speak(text: String): TtsSpeakAttemptResult {
        val normalized = text.trim()
        if (normalized.isEmpty()) {
            logEvent("Ignoring empty TTS text")
            return TtsSpeakAttemptResult.ERROR
        }
        if (!_ready.value) {
            pendingText = normalized
            logEvent("TTS not ready, queued text: $normalized")
            _diagnostics.update {
                it.copy(
                    pendingText = normalized,
                    lastError = if (it.phase == "timeout") it.lastError else null,
                )
            }
            return TtsSpeakAttemptResult.QUEUED_UNAVAILABLE
        }

        val result = try {
            textToSpeech?.speak(normalized, TextToSpeech.QUEUE_FLUSH, null, "recognition-tts")
                ?: TextToSpeech.ERROR
        } catch (throwable: Throwable) {
            logEvent("speak threw ${throwable.javaClass.simpleName}: ${throwable.message}")
            TextToSpeech.ERROR
        }

        _diagnostics.update {
            it.copy(
                lastSpokenText = normalized,
                pendingText = null,
                lastError = if (result == TextToSpeech.ERROR) "speak returned ERROR" else null,
            )
        }
        logEvent("speak called, result=$result, text=$normalized")
        return if (result == TextToSpeech.ERROR) {
            TtsSpeakAttemptResult.ERROR
        } else {
            TtsSpeakAttemptResult.SPOKEN
        }
    }

    fun retryDetection() {
        logEvent("retryDetection called")
        initTimeoutJob?.cancel()
        try {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (throwable: Throwable) {
            logEvent("retryDetection shutdown old engine failed: ${throwable.message}")
        }
        textToSpeech = null
        _ready.value = false
        _diagnostics.update {
            it.copy(
                phase = "retrying",
                ready = false,
                initStatusCode = null,
                languageStatusCode = null,
                lastError = null,
                defaultEngine = "--",
                installedEngines = emptyList(),
                discoveredServices = emptyList(),
            )
        }
        bootstrap()
    }

    fun shutdown() {
        logEvent("shutdown called")
        initTimeoutJob?.cancel()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        _ready.value = false
        _diagnostics.update {
            it.copy(
                phase = "shutdown",
                ready = false,
            )
        }
        scope.coroutineContext.cancelChildren()
    }

    private fun bootstrap() {
        val discoveredServices = resolveDiscoveredServices()
        _diagnostics.update {
            it.copy(
                phase = "creating",
                discoveredServices = discoveredServices,
            )
        }
        logEvent(
            if (discoveredServices.isEmpty()) {
                "No TTS services discovered from PackageManager"
            } else {
                "Discovered TTS services: ${discoveredServices.joinToString()}"
            },
        )

        try {
            textToSpeech = TextToSpeech(appContext, this)
            _diagnostics.update {
                it.copy(
                    phase = "waiting_on_init",
                    installedEngines = resolveInstalledEngines(),
                    defaultEngine = resolveDefaultEngine(),
                )
            }
            logEvent("TextToSpeech instance created, waiting for onInit callback")
            initTimeoutJob = scope.launch {
                delay(TTS_INIT_TIMEOUT_MS)
                if (!_ready.value && _diagnostics.value.phase == "waiting_on_init") {
                    val timeoutMessage = "TTS init timeout after ${TTS_INIT_TIMEOUT_MS}ms"
                    logEvent(timeoutMessage)
                    _diagnostics.update {
                        it.copy(
                            phase = "timeout",
                            ready = false,
                            defaultEngine = resolveDefaultEngine(),
                            installedEngines = resolveInstalledEngines(),
                            pendingText = pendingText,
                            lastError = timeoutMessage,
                        )
                    }
                }
            }
        } catch (throwable: Throwable) {
            _ready.value = false
            val message = "TextToSpeech constructor failed: ${throwable.javaClass.simpleName}: ${throwable.message}"
            logEvent(message)
            _diagnostics.update {
                it.copy(
                    phase = "error",
                    ready = false,
                    discoveredServices = discoveredServices,
                    defaultEngine = "--",
                    installedEngines = emptyList(),
                    lastError = message,
                )
            }
        }
    }

    private fun resolveInstalledEngines(): List<String> {
        return try {
            textToSpeech?.engines
                ?.map { engineInfo ->
                    buildString {
                        append(engineInfo.name ?: "--")
                        engineInfo.label?.toString()?.takeIf { it.isNotBlank() }?.let {
                            append(" (")
                            append(it)
                            append(")")
                        }
                    }
                }
                .orEmpty()
        } catch (throwable: Throwable) {
            logEvent("resolveInstalledEngines failed: ${throwable.message}")
            emptyList()
        }
    }

    private fun resolveDiscoveredServices(): List<String> {
        return try {
            appContext.packageManager.queryIntentServices(
                Intent(TextToSpeech.Engine.INTENT_ACTION_TTS_SERVICE),
                0,
            ).mapNotNull(::formatResolveInfo)
        } catch (throwable: Throwable) {
            logEvent("resolveDiscoveredServices failed: ${throwable.message}")
            emptyList()
        }
    }

    private fun formatResolveInfo(resolveInfo: ResolveInfo): String? {
        val serviceInfo = resolveInfo.serviceInfo ?: return null
        return "${serviceInfo.packageName}/${serviceInfo.name}"
    }

    private fun resolveDefaultEngine(): String {
        return try {
            textToSpeech?.defaultEngine?.takeIf { it.isNotBlank() } ?: "--"
        } catch (throwable: Throwable) {
            logEvent("resolveDefaultEngine failed: ${throwable.message}")
            "--"
        }
    }

    private fun logEvent(message: String) {
        val line = "${timestamp()} $message"
        Log.d(TAG, line)
        _diagnostics.update { diagnostics ->
            diagnostics.copy(logLines = (listOf(line) + diagnostics.logLines).take(MAX_LOG_LINES))
        }
    }

    private fun timestamp(): String {
        return timestampFormat.format(Date())
    }

    private fun describeInitStatus(status: Int): String {
        return when (status) {
            TextToSpeech.SUCCESS -> "SUCCESS"
            TextToSpeech.ERROR -> "ERROR"
            else -> "UNKNOWN"
        }
    }

    private fun describeLanguageStatus(status: Int): String {
        return when (status) {
            TextToSpeech.LANG_AVAILABLE -> "LANG_AVAILABLE"
            TextToSpeech.LANG_COUNTRY_AVAILABLE -> "LANG_COUNTRY_AVAILABLE"
            TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE -> "LANG_COUNTRY_VAR_AVAILABLE"
            TextToSpeech.LANG_MISSING_DATA -> "LANG_MISSING_DATA"
            TextToSpeech.LANG_NOT_SUPPORTED -> "LANG_NOT_SUPPORTED"
            else -> "UNKNOWN"
        }
    }

    companion object {
        private const val TAG = "SpeechPlayer"
        private const val TTS_INIT_TIMEOUT_MS = 8_000L
        private const val MAX_LOG_LINES = 12
        private val timestampFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    }
}
