package com.vectora.doc

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CodeBlock(code: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        Text(
            text = code,
            fontFamily = FontFamily.Monospace,
            fontSize = 16.sp,
            color = Color(0xFFE0E0E0),
            lineHeight = 23.sp
        )
    }
}
