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
import kotlinx.coroutines.guava.await

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
            .background(Color.Black) // Assicurati che il PreviewView sia visibile
    )

    LaunchedEffect(cameraProviderFuture) {
        try {
            val cameraProvider = cameraProviderFuture.await()
            Log.d("QRCodeScanner", "Camera provider retrieved")

            val preview = Preview.Builder()
                .setTargetResolution(Size(1280, 720)) // Risoluzione adeguata
                .build()
                .also { preview ->
                    preview.setSurfaceProvider(previewView.surfaceProvider)
                    Log.d("QRCodeScanner", "SurfaceProvider set")
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(1280, 720)) // Risoluzione adeguata
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                            val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)
                            Log.d("QRCodeScanner", "Processing image with rotation: $rotationDegrees")

                            val barcodeScanner: BarcodeScanner = BarcodeScanning.getClient()
                            barcodeScanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    Log.d("QRCodeScanner", "Barcodes detected: ${barcodes.size}")
                                    for (barcode in barcodes) {
                                        Log.d("QRCodeScanner", "Barcode: ${barcode.displayValue}, Type: ${barcode.valueType}")
                                        barcode.rawValue?.let { qrCode ->
                                            Log.d("QRCodeScanner", "QR code scanned: $qrCode")
                                            onQRCodeScanned(qrCode) // Passa il testo del QR code
                                            imageProxy.close() // Chiudi il proxy dopo aver letto il QR code
                                            return@addOnSuccessListener
                                        }
                                    }
                                    imageProxy.close() // Chiudi il proxy se nessun codice QR è stato trovato
                                }
                                .addOnFailureListener { e ->
                                    Log.e("QRCodeScanner", "Barcode scanning failed", e)
                                    imageProxy.close() // Chiudi il proxy in caso di errore
                                }
                        } else {
                            Log.d("QRCodeScanner", "Media image is null")
                            imageProxy.close()
                        }
                    }
                }

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner, cameraSelector, preview, imageAnalysis
            )

            Log.d("QRCodeScanner", "Camera bound to lifecycle")

        } catch (exc: Exception) {
            Log.e("QRCodeScanner", "Error binding camera", exc)
        }
    }
}
