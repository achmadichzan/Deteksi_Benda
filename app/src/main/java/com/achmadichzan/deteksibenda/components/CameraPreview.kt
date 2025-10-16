package com.achmadichzan.deteksibenda.components

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.concurrent.Executors
import kotlin.use

@Composable
fun CameraPreview(modifier: Modifier = Modifier, onBitmapReady: (Bitmap) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                this.scaleType = PreviewView.ScaleType.FIT_CENTER
            }
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val resolutionSelector = ResolutionSelector.Builder()
                    .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
                    .build()

                val preview = Preview.Builder()
                    .setResolutionSelector(resolutionSelector)
                    .build()
                    .also { it.surfaceProvider = previewView.surfaceProvider }

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setResolutionSelector(resolutionSelector)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor) { imageProxy ->
                            val bitmapBuffer = createBitmap(imageProxy.width, imageProxy.height)
                            imageProxy.use { bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }

                            val matrix = Matrix().apply {
                                postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
                            }
                            val rotatedBitmap = Bitmap.createBitmap(
                                bitmapBuffer, 0, 0, bitmapBuffer.width, bitmapBuffer.height, matrix, true
                            )
                            onBitmapReady(rotatedBitmap)
                        }
                    }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner, cameraSelector, preview, imageAnalyzer
                    )
                } catch (exc: Exception) {
                    // Log error
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        }
    )
}