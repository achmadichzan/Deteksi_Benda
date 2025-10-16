package com.achmadichzan.deteksibenda

import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.achmadichzan.deteksibenda.components.BoundingBox
import com.achmadichzan.deteksibenda.components.BoundingBoxOverlay
import com.achmadichzan.deteksibenda.components.CameraPreview
import com.achmadichzan.deteksibenda.components.Constants
import com.achmadichzan.deteksibenda.components.Detector
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ObjectDetectionScreen() {
    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    var boxes by remember { mutableStateOf<List<BoundingBox>>(emptyList()) }
    var time by remember { mutableLongStateOf(0L) }

    val detector = remember {
        Detector(
            context = context,
            modelPath = Constants.MODEL_PATH,
            labelPath = Constants.LABELS_PATH,
            detectorListener = object : Detector.DetectorListener {
                override fun onEmptyDetect() {
                    boxes = emptyList()
                }

                override fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long) {
                    boxes = boundingBoxes
                    time = inferenceTime
                }
            }
        )
    }

    DisposableEffect(Unit) {
        detector.setup()
        onDispose {
            detector.clear()
        }
    }

    LaunchedEffect(key1 = true) {
        cameraPermissionState.launchPermissionRequest()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (cameraPermissionState.status.isGranted) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(), // Penting!
                    onBitmapReady = { detector.detect(it) }
                )
                BoundingBoxOverlay(
                    boundingBoxes = boxes,
                    modifier = Modifier.fillMaxSize() // Penting!
                )
                Text(
                    text = "${time}ms",
                    color = Color.White,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Izin Kamera dibutuhkan untuk menggunakan aplikasi ini.")
            }
        }
    }
}