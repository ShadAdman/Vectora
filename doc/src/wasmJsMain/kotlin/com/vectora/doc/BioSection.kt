package com.vectora.doc

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BioSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp)
    ) {
        Text(
            text = "About Vectora",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFFBB86FC)
            )
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Vectora is an on-device semantic search engine designed specifically for the next generation of mobile applications. It brings the power of vector embeddings and similarity search directly to the device, ensuring privacy, speed, and offline capability.",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color.White.copy(alpha = 0.8f),
                lineHeight = 28.sp
            )
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Vectora Mission",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF03DAC6)
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "To enable developers to build more intuitive and intelligent mobile experiences without compromising on user privacy or performance. We believe semantic search should be accessible, lightweight, and local.",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color.White.copy(alpha = 0.8f),
                lineHeight = 28.sp
            )
        )
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Best Usage",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF03DAC6)
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Vectora shines in product-based or commerce applications where natural language discovery is key. Instead of rigid keyword matching, users can find what they need using descriptive language, making the search experience feel more human and effective.",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color.White.copy(alpha = 0.8f),
                lineHeight = 28.sp
            )
        )
    }
}
