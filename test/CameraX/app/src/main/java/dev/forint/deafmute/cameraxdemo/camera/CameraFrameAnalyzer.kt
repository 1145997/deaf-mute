package dev.forint.deafmute.cameraxdemo.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import dev.forint.deafmute.cameraxdemo.mediapipe.HandLandmarkerManager

class CameraFrameAnalyzer(
    private val handLandmarkerManager: HandLandmarkerManager,
) : ImageAnalysis.Analyzer {
    override fun analyze(image: ImageProxy) {
        handLandmarkerManager.detectLiveStream(image)
    }
}

