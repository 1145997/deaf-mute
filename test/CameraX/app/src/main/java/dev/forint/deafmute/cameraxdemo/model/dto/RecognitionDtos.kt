package dev.forint.deafmute.cameraxdemo.model.dto

data class RecognitionConfigDto(
    val id: Long? = null,
    val configName: String? = null,
    val requiredHits: Int? = null,
    val debounceMs: Int? = null,
    val maxIntervalMs: Int? = null,
)

data class GestureDto(
    val id: Long? = null,
    val gestureCode: String? = null,
    val gestureName: String? = null,
    val detectionKey: String? = null,
)

data class PhraseDto(
    val id: Long? = null,
    val phraseCode: String? = null,
    val phraseText: String? = null,
    val ttsText: String? = null,
)

data class FlowDto(
    val id: Long? = null,
    val flowCode: String? = null,
    val flowName: String? = null,
    val priority: Int? = null,
)

data class BootstrapResponseDto(
    val activeConfig: RecognitionConfigDto? = null,
    val gestureList: List<GestureDto>? = null,
    val phraseList: List<PhraseDto>? = null,
    val flowList: List<FlowDto>? = null,
    val supportedLandmarkGestureCodes: List<String>? = null,
    val landmarkPointCount: Int? = null,
    val supportsHandedness: Boolean? = null,
    val supportsMirrored: Boolean? = null,
    val landmarkCoordinateMode: String? = null,
    val landmarkOrigin: String? = null,
    val landmarkOrder: String? = null,
    val landmarkHandMode: String? = null,
    val landmarkAssumptions: String? = null,
)

data class SessionStartRequestDto(
    val clientType: String,
    val sceneCode: String,
    val engineType: String,
    val appVersion: String,
)

data class SessionInfoDto(
    val sessionId: String,
    val startedAt: String? = null,
    val activeConfigId: Long? = null,
)

data class LandmarkDto(
    val x: Double,
    val y: Double,
    val z: Double = 0.0,
)

data class PredictRequestDto(
    val sessionId: String,
    val frameNo: Int,
    val capturedAt: Long,
    val source: String,
    val handedness: String? = null,
    val mirrored: Boolean,
    val cameraFacing: String,
    val landmarks: List<LandmarkDto>,
)

data class PredictResponseDto(
    val gesture: String? = null,
    val code: String? = null,
    val label: String? = null,
    val locked: Boolean? = null,
    val state: String? = null,
    val outputType: String? = null,
    val displayText: String? = null,
    val ttsText: String? = null,
    val controlAction: String? = null,
    val matchedFlowCode: String? = null,
    val matchedNodeCode: String? = null,
    val traceId: String? = null,
    val inputType: String? = null,
    val stableCount: Int? = null,
    val requiredHits: Int? = null,
    val debounced: Boolean? = null,
    val detectedGestureCandidates: List<String>? = null,
)

data class SessionActionRequestDto(
    val sessionId: String,
    val reason: String,
)

data class SessionActionResponseDto(
    val ok: Boolean? = null,
    val message: String? = null,
)

data class DetectedHandFrame(
    val landmarks: List<LandmarkDto>,
    val capturedAt: Long,
    val source: String,
    val handedness: String?,
    val mirrored: Boolean,
    val cameraFacing: String,
)
