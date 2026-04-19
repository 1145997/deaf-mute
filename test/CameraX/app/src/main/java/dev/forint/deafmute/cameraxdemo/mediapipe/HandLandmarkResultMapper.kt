package dev.forint.deafmute.cameraxdemo.mediapipe

import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import dev.forint.deafmute.cameraxdemo.domain.RecognitionConstants
import dev.forint.deafmute.cameraxdemo.model.dto.DetectedHandFrame
import dev.forint.deafmute.cameraxdemo.model.dto.LandmarkDto

object HandLandmarkResultMapper {
    fun mapFirstHand(
        result: HandLandmarkerResult,
        cameraFacing: String,
        mirrored: Boolean,
        capturedAt: Long,
    ): DetectedHandFrame? {
        val hands = result.landmarks()
        val firstHand = hands.firstOrNull() ?: return null
        if (firstHand.size != 21) {
            return null
        }

        return DetectedHandFrame(
            landmarks = firstHand.map { LandmarkDto(x = it.x().toDouble(), y = it.y().toDouble(), z = it.z().toDouble()) },
            capturedAt = capturedAt,
            source = RecognitionConstants.SOURCE,
            handedness = extractHandedness(result),
            mirrored = mirrored,
            cameraFacing = cameraFacing,
        )
    }

    private fun extractHandedness(result: HandLandmarkerResult): String? {
        val handednessMethod = result.javaClass.methods.firstOrNull {
            it.name == "handedness" || it.name == "handednesses"
        } ?: return null
        val handednessGroups = handednessMethod.invoke(result) as? List<*> ?: return null
        val firstGroup = handednessGroups.firstOrNull() as? List<*> ?: return null
        val firstCategory = firstGroup.firstOrNull() ?: return null

        val labelMethod = firstCategory.javaClass.methods.firstOrNull {
            it.name == "categoryName" || it.name == "displayName" || it.name == "label"
        } ?: return null
        return labelMethod.invoke(firstCategory) as? String
    }
}

