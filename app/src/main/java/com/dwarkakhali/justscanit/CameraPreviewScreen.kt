package com.dwarkakhali.justscanit

import android.content.ActivityNotFoundException
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.zIndex

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraPreviewScreen(viewModel: ScannerViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val scannedDataList by viewModel.scannedDataList.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val uriHandler = LocalUriHandler.current
    val isCameraPaused by viewModel.isCameraPaused.collectAsState()
    val showScanMessage by viewModel.showScanMessage.collectAsState()
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            val imageAnalysis = ImageAnalysis.Builder().build().apply {
                setAnalyzer(viewModel.executor) { imageProxy ->
                    viewModel.processImageProxy(imageProxy)
                }
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                Log.e("CameraPreview", "Failed to bind camera use cases", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    BackHandler {
        onBack()
    }

    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        modifier = Modifier
            .zIndex(0f),
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Scanned Items",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.primary, thickness = 1.dp)

                scannedDataList.forEach { (scannedData, count) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.background)
                                .padding(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "$scannedData (x$count)",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                if (scannedData.startsWith("http://") || scannedData.startsWith("https://")) {
                                    TextButton(onClick = {
                                        try {
                                            uriHandler.openUri(scannedData)
                                        } catch (e: ActivityNotFoundException) {
                                            Toast.makeText(context, "No application can handle this request. Please install a web browser.", Toast.LENGTH_LONG).show()
                                            e.printStackTrace()
                                        }
                                    }) {
                                        Text("Open Link")
                                    }
                                }
                            }
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString(scannedData))
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = { viewModel.clearScannedDataList() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.onError)
                }
            }
        },
        sheetPeekHeight = 64.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { if (isCameraPaused) viewModel.resumeScanning() }
        ) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )

            if (showScanMessage) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(16.dp)
                        .zIndex(1f) // Ensure it is on top
                ) {
                    Text(
                        text = "QR code scanned! Tap to scan another.",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            LaunchedEffect(scannedDataList) {
                if (scannedDataList.isNotEmpty()) {
                    coroutineScope.launch {
                        bottomSheetScaffoldState.bottomSheetState.expand()
                    }
                }
            }
        }
    }
}












