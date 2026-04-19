package dev.forint.deafmute.cameraxdemo.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraXManager(
    private val context: Context,
) {
    private val providerFuture = ProcessCameraProvider.getInstance(context)
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var cameraProvider: ProcessCameraProvider? = null

    fun bindCamera(
        lifecycleOwner: androidx.lifecycle.LifecycleOwner,
        previewView: PreviewView,
        analyzer: ImageAnalysis.Analyzer,
        lensFacing: Int,
        onError: (Throwable) -> Unit,
    ) {
        providerFuture.addListener(
            {
                try {
                    val provider = providerFuture.get()
                    cameraProvider = provider

                    val preview = Preview.Builder()
                        .build()
                        .also { it.setSurfaceProvider(previewView.surfaceProvider) }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .build()
                        .also {
                            it.clearAnalyzer()
                            it.setAnalyzer(cameraExecutor, analyzer)
                        }

                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.Builder().requireLensFacing(lensFacing).build(),
                        preview,
                        imageAnalysis,
                    )
                } catch (throwable: Throwable) {
                    onError(throwable)
                }
            },
            ContextCompat.getMainExecutor(context),
        )
    }

    fun release() {
        cameraProvider?.unbindAll()
        cameraExecutor.shutdown()
    }
}
