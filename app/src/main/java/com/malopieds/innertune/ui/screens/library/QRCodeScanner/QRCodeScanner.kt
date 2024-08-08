package com.malopieds.innertune.ui.screens.library.QRCodeScanner

import android.util.Log
import android.util.Size
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraUnavailableException
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalGetImage::class)
@Composable
fun QRCodeScanner(
    onQRCodeScanned: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }

    AndroidView(
        { previewView },
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    )

    LaunchedEffect(cameraProviderFuture) {
        try {
            // Recupero del CameraProvider
            val cameraProvider = cameraProviderFuture.get()
            Log.d("QRCodeScanner", "Camera provider retrieved")

            // Configurazione della preview
            val preview = Preview.Builder()
                .setTargetResolution(Size(1280, 720))
                .build()
                .also { preview ->
                    preview.setSurfaceProvider(previewView.surfaceProvider)
                    Log.d("QRCodeScanner", "SurfaceProvider set")
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            // Configurazione dell'analisi delle immagini
            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                            val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)

                            CoroutineScope(Dispatchers.Default).launch {
                                Log.d("QRCodeScanner", "Processing image with rotation: $rotationDegrees")

                                val barcodeScanner: BarcodeScanner = BarcodeScanning.getClient()
                                barcodeScanner.process(image)
                                    .addOnSuccessListener { barcodes ->
                                        if (barcodes.isNotEmpty()) {
                                            val barcode = barcodes.first()
                                            barcode.rawValue?.let { qrCode ->
                                                onQRCodeScanned(qrCode)
                                                CoroutineScope(Dispatchers.Main).launch {
                                                    if (isActive && cameraProvider.isBound(preview)) {
                                                        cameraProvider.unbindAll()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("QRCodeScanner", "Barcode scanning failed", e)
                                    }
                                    .addOnCompleteListener {
                                        imageProxy.close()
                                    }

                            }
                        } else {
                            imageProxy.close()
                        }
                    }
                }

            // Assicurati di disconnettere qualsiasi risorsa precedentemente utilizzata
            cameraProvider.unbindAll()

            // Collega la fotocamera al ciclo di vita dell'app
            cameraProvider.bindToLifecycle(
                lifecycleOwner, cameraSelector, preview, imageAnalysis
            )

            Log.d("QRCodeScanner", "Camera bound to lifecycle")

        } catch (exc: Exception) {
            Log.e("QRCodeScanner", "Error binding camera", exc)
        }
    }
}
