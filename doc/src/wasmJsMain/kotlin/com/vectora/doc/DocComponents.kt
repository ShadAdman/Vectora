package com.vectora.doc

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CodeBlock(
    code: String,
    language: String = "kotlin",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0D0D0D))
            .border(1.dp, Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
    ) {
        // Header bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1A1A))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = language.lowercase(),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            )
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy code",
                tint = Color.Gray.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
        }

        // Code area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = highlightCode(code),
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                softWrap = false
            )
        }
    }
}

private fun highlightCode(code: String) = buildAnnotatedString {
    val keywords = listOf(
        "val", "var", "fun", "class", "object", "interface", "import", "package",
        "return", "if", "else", "when", "for", "while", "do", "break", "continue",
        "try", "catch", "finally", "throw", "null", "true", "false", "in", "is",
        "as", "override", "public", "private", "protected", "internal", "expect", "actual",
        "dependencies", "implementation", "kotlin", "sourceSets"
    )

    var index = 0
    while (index < code.length) {
        val remaining = code.substring(index)
        
        when {
            // Comments
            remaining.startsWith("//") -> {
                val endOfLine = remaining.indexOf('\n').let { if (it == -1) remaining.length else it }
                withStyle(SpanStyle(color = Color(0xFF6A9955))) {
                    append(remaining.substring(0, endOfLine))
                }
                index += endOfLine
            }
            // Strings
            remaining.startsWith("\"") -> {
                val nextQuote = remaining.indexOf('\"', 1)
                val endOfString = if (nextQuote == -1) remaining.length else nextQuote + 1
                withStyle(SpanStyle(color = Color(0xFFCE9178))) {
                    append(remaining.substring(0, endOfString))
                }
                index += endOfString
            }
            // Keywords and words
            else -> {
                val match = Regex("^[a-zA-Z0-9_]+").find(remaining)
                if (match != null) {
                    val word = match.value
                    val style = when {
                        word in keywords -> SpanStyle(color = Color(0xFF569CD6))
                        word.matches(Regex("\\d+")) -> SpanStyle(color = Color(0xFFB5CEA8))
                        word.matches(Regex("[A-Z][a-zA-Z0-9_]*")) -> SpanStyle(color = Color(0xFF4EC9B0))
                        else -> SpanStyle(color = Color(0xFFD4D4D4))
                    }
                    withStyle(style) { append(word) }
                    index += word.length
                } else {
                    append(remaining[0])
                    index++
                }
            }
        }
    }
}
