package dev.forint.deafmute.cameraxdemo

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dev.forint.deafmute.cameraxdemo.camera.CameraFrameAnalyzer
import dev.forint.deafmute.cameraxdemo.camera.CameraXManager
import dev.forint.deafmute.cameraxdemo.databinding.ActivityMainBinding
import dev.forint.deafmute.cameraxdemo.mediapipe.HandLandmarkerManager
import dev.forint.deafmute.cameraxdemo.model.dto.DetectedHandFrame
import dev.forint.deafmute.cameraxdemo.ui.RecognitionUiState
import dev.forint.deafmute.cameraxdemo.ui.RecognitionViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.math.ceil

class MainActivity : AppCompatActivity(), HandLandmarkerManager.Listener {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: RecognitionViewModel by viewModels()

    private lateinit var cameraXManager: CameraXManager
    private lateinit var handLandmarkerManager: HandLandmarkerManager

    private var usingFrontCamera: Boolean = false
    private var shouldRefreshTtsOnResume: Boolean = false

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            bindCamera()
        } else {
            Toast.makeText(this, "需要相机权限才能继续采集 landmarks", Toast.LENGTH_LONG).show()
        }
    }

    private val ttsSettingsLauncher = registerForActivityResult(StartActivityForResult()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraXManager = CameraXManager(this)
        handLandmarkerManager = HandLandmarkerManager(this, this)

        bindActions()
        observeUiState()
        viewModel.initialize()
        ensureCameraPermission()
    }

    override fun onDestroy() {
        if (isFinishing && !isChangingConfigurations) {
            viewModel.onPageExit()
        }
        handLandmarkerManager.close()
        cameraXManager.release()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        if (shouldRefreshTtsOnResume) {
            shouldRefreshTtsOnResume = false
            viewModel.retryTtsDetection()
        }
    }

    override fun onHandFrameDetected(frame: DetectedHandFrame) {
        viewModel.onHandLandmarksDetected(frame)
    }

    override fun onHandLandmarkerError(message: String) {
        viewModel.onHandLandmarkerError(message)
    }

    private fun bindActions() {
        binding.buttonSaveRestart.setOnClickListener {
            viewModel.onSettingsSubmitted(
                baseUrl = binding.editBaseUrl.text?.toString().orEmpty(),
                authToken = binding.editToken.text?.toString().orEmpty(),
            )
        }

        binding.buttonSwitchCamera.setOnClickListener {
            usingFrontCamera = !usingFrontCamera
            viewModel.onCameraFacingChanged(usingFrontCamera)
            handLandmarkerManager.setFrontCamera(usingFrontCamera)
            bindCamera()
        }

        binding.buttonClear.setOnClickListener {
            viewModel.onClearBufferClicked()
        }

        binding.buttonSpeak.setOnClickListener {
            viewModel.onSpeakClicked()
        }

        binding.buttonCloseSession.setOnClickListener {
            viewModel.onCloseSessionClicked()
        }

        binding.buttonOpenTtsSettings.setOnClickListener {
            openTtsSettings()
        }

        binding.buttonRetryTtsDetection.setOnClickListener {
            viewModel.retryTtsDetection()
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: RecognitionUiState) {
        if (!binding.editBaseUrl.isFocused && binding.editBaseUrl.text?.toString() != state.baseUrl) {
            binding.editBaseUrl.setText(state.baseUrl)
        }
        if (!binding.editToken.isFocused && binding.editToken.text?.toString() != state.authToken) {
            binding.editToken.setText(state.authToken)
        }

        val countdownSeconds = if (state.countdownMillis > 0) {
            ceil(state.countdownMillis / 1000.0).toInt()
        } else {
            0
        }

        binding.textPreviewOverlay.text = buildString {
            append("Session: ")
            append(if (state.sessionId.isBlank()) "--" else state.sessionId)
            append("\n")
            append("Gesture: ${state.currentGesture} | Output: ${state.displayText}")
            append("\n")
            append("Buffer: ${state.bufferText.ifBlank { "（空）" }}")
            append("\n")
            append("Countdown: ")
            append(if (countdownSeconds > 0) "${countdownSeconds}s" else "--")
        }

        binding.textSessionStatus.text = buildString {
            append("状态：${state.sessionStatus}")
            append("\nSessionId：${state.sessionId.ifBlank { "--" }}")
            append("\nStartedAt：${state.sessionStartedAt.ifBlank { "--" }}")
            append("\nActiveConfig：${state.activeConfigName}")
            append("\nTTS：${state.ttsUserStatusText}")
            append("\nTTS Engine：${state.ttsDefaultEngine}")
        }

        binding.textBuffer.text = "句子缓冲：${state.bufferText.ifBlank { "（空）" }}"

        binding.textResult.text = buildString {
            append("当前输出：${state.displayText}")
            append("\noutputType：${state.outputType}")
            append("\ncontrolAction：${state.controlAction.ifBlank { "--" }}")
            append("\nmessage：${state.lastMessage}")
            state.errorMessage?.takeIf { it.isNotBlank() }?.let {
                append("\nerror：$it")
            }
        }

        binding.textTtsStatus.text = buildString {
            append("当前状态：${state.ttsUserStatusText}")
            append("\n默认引擎：${state.ttsDefaultEngine}")
            append("\n问题原因：${state.ttsProblemReason}")
            append("\n建议操作：${state.ttsActionSuggestion}")
        }

        binding.textDebug.text = buildString {
            append("frameNo：${state.frameNo}")
            append("\ninputType：${state.inputType.ifBlank { "--" }}")
            append("\nstableCount / requiredHits：${state.stableCount} / ${state.requiredHits}")
            append("\ndebounced：${state.debounced}")
            append("\n候选基础手势：${state.detectedGestureCandidates.joinToString().ifBlank { "--" }}")
            append("\nhandedness：${state.handedness}")
            append("\ncameraFacing：${state.cameraFacing}")
            append("\nmirrored：${state.mirrored}")
            append("\ntraceId：${state.latestTraceId.ifBlank { "--" }}")
            append("\n")
            append("\n[TTS]")
            append("\nstatusCode：${state.ttsUserStatusCode}")
            append("\nphase：${state.ttsPhase}")
            append("\ndefaultEngine：${state.ttsDefaultEngine}")
            append("\ninitStatusCode：${state.ttsInitStatusCode ?: "--"}")
            append("\nlanguageStatusCode：${state.ttsLanguageStatusCode ?: "--"}")
            append("\npendingText：${state.ttsPendingText ?: "--"}")
            append("\nlastSpokenText：${state.ttsLastSpokenText ?: "--"}")
            append("\nlastError：${state.ttsLastError ?: "--"}")
            append("\ndiscoveredServices：${state.ttsDiscoveredServices.joinToString().ifBlank { "--" }}")
            append("\ninstalledEngines：${state.ttsInstalledEngines.joinToString().ifBlank { "--" }}")
            append("\nrecentLogs：")
            if (state.ttsLogLines.isEmpty()) {
                append("\n- --")
            } else {
                state.ttsLogLines.forEach { line ->
                    append("\n- ")
                    append(line)
                }
            }
        }

        binding.textContract.text = buildString {
            append("pointCount：${state.landmarkPointCount}")
            append("\ncoordinateMode：${state.landmarkCoordinateMode.ifBlank { "--" }}")
            append("\norigin：${state.landmarkOrigin.ifBlank { "--" }}")
            append("\norder：${state.landmarkOrder.ifBlank { "--" }}")
            append("\nhandMode：${state.landmarkHandMode.ifBlank { "--" }}")
            append("\nsupportedGestureCodes：${state.supportedLandmarkGestureCodes.joinToString().ifBlank { "--" }}")
            append("\nassumptions：${state.landmarkAssumptions.ifBlank { "--" }}")
        }
    }

    private fun ensureCameraPermission() {
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA,
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            bindCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun bindCamera() {
        handLandmarkerManager.setFrontCamera(usingFrontCamera)
        cameraXManager.bindCamera(
            lifecycleOwner = this,
            previewView = binding.previewView,
            analyzer = CameraFrameAnalyzer(handLandmarkerManager),
            lensFacing = if (usingFrontCamera) {
                CameraSelector.LENS_FACING_FRONT
            } else {
                CameraSelector.LENS_FACING_BACK
            },
            onError = { throwable ->
                Toast.makeText(
                    this,
                    "相机绑定失败：${throwable.message}",
                    Toast.LENGTH_LONG,
                ).show()
                if (usingFrontCamera) {
                    usingFrontCamera = false
                    viewModel.onCameraFacingChanged(false)
                    bindCamera()
                }
            },
        )
    }

    private fun openTtsSettings() {
        val primaryIntent = Intent("com.android.settings.TTS_SETTINGS")
        val fallbackIntent = Intent(Settings.ACTION_SETTINGS)
        val launched = launchSettingsIntent(primaryIntent) || launchSettingsIntent(fallbackIntent)
        if (!launched) {
            Toast.makeText(this, "无法打开系统设置，请手动检查语音播报设置", Toast.LENGTH_LONG).show()
        }
    }

    private fun launchSettingsIntent(intent: Intent): Boolean {
        return try {
            shouldRefreshTtsOnResume = true
            ttsSettingsLauncher.launch(intent)
            true
        } catch (_: ActivityNotFoundException) {
            shouldRefreshTtsOnResume = false
            false
        } catch (_: Throwable) {
            shouldRefreshTtsOnResume = false
            false
        }
    }
}
