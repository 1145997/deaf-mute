package dev.forint.deafmute.cameraxdemo.mediapipe

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import dev.forint.deafmute.cameraxdemo.domain.RecognitionConstants
import dev.forint.deafmute.cameraxdemo.model.dto.DetectedHandFrame

class HandLandmarkerManager(
    context: Context,
    private val listener: Listener,
) {
    interface Listener {
        fun onHandFrameDetected(frame: DetectedHandFrame)
        fun onHandLandmarkerError(message: String)
    }

    private val appContext = context.applicationContext

    @Volatile
    private var isFrontCamera: Boolean = false

    private var handLandmarker: HandLandmarker? = null

    init {
        setup()
    }

    fun setFrontCamera(frontCamera: Boolean) {
        isFrontCamera = frontCamera
    }

    fun detectLiveStream(imageProxy: ImageProxy) {
        val frameTime = SystemClock.uptimeMillis()
        val bitmapBuffer = Bitmap.createBitmap(
            imageProxy.width,
            imageProxy.height,
            Bitmap.Config.ARGB_8888,
        )

        try {
            bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer)
            val matrix = Matrix().apply {
                postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
                if (isFrontCamera) {
                    postScale(-1f, 1f, imageProxy.width.toFloat(), imageProxy.height.toFloat())
                }
            }
            val rotatedBitmap = Bitmap.createBitmap(
                bitmapBuffer,
                0,
                0,
                bitmapBuffer.width,
                bitmapBuffer.height,
                matrix,
                true,
            )
            val mpImage = BitmapImageBuilder(rotatedBitmap).build()
            handLandmarker?.detectAsync(mpImage, frameTime)
        } catch (throwable: Throwable) {
            listener.onHandLandmarkerError(throwable.message ?: "MediaPipe detectAsync 失败")
        } finally {
            imageProxy.close()
        }
    }

    fun close() {
        handLandmarker?.close()
        handLandmarker = null
    }

    private fun setup() {
        try {
            val baseOptions = BaseOptions.builder()
                .setDelegate(Delegate.CPU)
                .setModelAssetPath(RecognitionConstants.MODEL_ASSET_NAME)
                .build()

            val options = HandLandmarker.HandLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setNumHands(1)
                .setMinHandDetectionConfidence(0.5f)
                .setMinHandPresenceConfidence(0.5f)
                .setMinTrackingConfidence(0.5f)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setResultListener(::handleResult)
                .setErrorListener { error ->
                    listener.onHandLandmarkerError(error.message ?: "MediaPipe Hand Landmarker 初始化失败")
                }
                .build()

            handLandmarker = HandLandmarker.createFromOptions(appContext, options)
        } catch (throwable: Throwable) {
            listener.onHandLandmarkerError(throwable.message ?: "MediaPipe Hand Landmarker 初始化失败")
        }
    }

    private fun handleResult(result: HandLandmarkerResult, input: MPImage) {
        val cameraFacing = if (isFrontCamera) "front" else "back"
        val frame = HandLandmarkResultMapper.mapFirstHand(
            result = result,
            cameraFacing = cameraFacing,
            mirrored = isFrontCamera,
            capturedAt = result.timestampMs(),
        ) ?: return
        listener.onHandFrameDetected(frame)
    }
}
