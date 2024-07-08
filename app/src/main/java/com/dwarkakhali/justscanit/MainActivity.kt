package com.dwarkakhali.justscanit

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker

class MainActivity : ComponentActivity() {
    private val viewModel: ScannerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissionsIfNeeded()

        setContent {
            var showCamera by remember { mutableStateOf(false) }

            MaterialTheme {
                if (showCamera) {
                    CameraPreviewScreen(viewModel, onBack = { showCamera = false })
                } else {
                    StartScreen(onStartCamera = { showCamera = true })
                }
            }
        }
    }

    private fun requestPermissionsIfNeeded() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PermissionChecker.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            setContent {
                var showCamera by remember { mutableStateOf(true) }
                MaterialTheme {
                    if (showCamera) {
                        CameraPreviewScreen(viewModel, onBack = { showCamera = false })
                    } else {
                        StartScreen(onStartCamera = { showCamera = true })
                    }
                }
            }
        } else {
            Log.e("MainActivity", "Camera permission denied.")
        }
    }
}






