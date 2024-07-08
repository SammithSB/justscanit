package com.dwarkakhali.justscanit

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.justscanit.R

@Composable
fun StartScreen(onStartCamera: () -> Unit) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Placeholder for a drawable related to QR codes
        Image(
            painter = painterResource(id = R.drawable.ic_qr_code), // Replace with your drawable
            contentDescription = "QR Code",
            modifier = Modifier
                .size(128.dp)
                .padding(16.dp)
        )

        Text(
            text = "Welcome to JustScanIt",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )

        Text(
            text = "JustScanIt is your go-to QR code scanner app! With JustScanIt, you can:\n\n" +
                    "• Quickly scan QR codes with your camera\n" +
                    "• Manage all your scanned QR codes in one place\n" +
                    "• Copy scanned data to your clipboard\n" +
                    "• Open URLs directly from scanned QR codes\n\n" +
                    "Tap the button below to start scanning!",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onStartCamera() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            Text("Start Camera")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

