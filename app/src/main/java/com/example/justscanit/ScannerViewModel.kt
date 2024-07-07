package com.example.justscanit

import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class ScannerViewModel : ViewModel() {
    private val _scannedDataList = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val scannedDataList: StateFlow<List<Pair<String, Int>>> = _scannedDataList

    private val _showScanMessage = MutableStateFlow(false)
    val showScanMessage: StateFlow<Boolean> = _showScanMessage

    val executor = Executors.newSingleThreadExecutor()
    private val _isCameraPaused = MutableStateFlow(false)
    val isCameraPaused: StateFlow<Boolean> = _isCameraPaused

    @OptIn(ExperimentalGetImage::class)
    fun processImageProxy(imageProxy: ImageProxy) {
        if (_isCameraPaused.value) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val scanner = BarcodeScanning.getClient()
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        handleBarcode(barcode)
                    }
                }
                .addOnFailureListener {
                    Log.e("BarcodeScanning", "Barcode scanning failed", it)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }

    private fun handleBarcode(barcode: Barcode) {
        val valueType = barcode.valueType
        val scannedDataText = when (valueType) {
            Barcode.TYPE_WIFI -> {
                "SSID: ${barcode.wifi?.ssid}\nPassword: ${barcode.wifi?.password}\nType: ${barcode.wifi?.encryptionType}"
            }
            Barcode.TYPE_URL -> {
                "${barcode.url?.url}"
            }
            Barcode.TYPE_TEXT -> {
                "Text: ${barcode.displayValue}"
            }
            // Handle other types
            else -> "Value: ${barcode.displayValue}"
        }

        viewModelScope.launch {
            val currentList = _scannedDataList.value.toMutableList()
            val existingItem = currentList.find { it.first == scannedDataText }

            if (existingItem != null) {
                val updatedItem = existingItem.copy(second = existingItem.second + 1)
                currentList[currentList.indexOf(existingItem)] = updatedItem
            } else {
                currentList.add(scannedDataText to 1)
            }

            _scannedDataList.value = currentList
            _isCameraPaused.value = true
            _showScanMessage.value = true
        }
    }

    fun clearScannedDataList() {
        viewModelScope.launch {
            _scannedDataList.value = emptyList()
        }
    }

    fun resumeScanning() {
        viewModelScope.launch {
            _isCameraPaused.value = false
            _showScanMessage.value = false
        }
    }
}






