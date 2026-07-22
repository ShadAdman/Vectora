package com.vectora.doc

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ImportSection() {
    var selectedPlatform by remember { mutableStateOf("Android") }
    val platforms = listOf("Android", "iOS", "KMP")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp)
    ) {
        Text(
            text = "Importing Vectora",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFFBB86FC)
            )
        )
        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            platforms.forEach { platform ->
                TextButton(
                    onClick = { selectedPlatform = platform },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (selectedPlatform == platform) Color(0xFF03DAC6) else Color.Gray
                    )
                ) {
                    Text(platform, fontWeight = if (selectedPlatform == platform) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val code = when (selectedPlatform) {
            "Android" -> """
                // build.gradle.kts
                dependencies {
                    implementation("io.github.shadadman:vectora-search:1.0.0")
                }
            """.trimIndent()
            "iOS" -> """
                // Swift Package Manager
                // 1. File > Add Packages...
                // 2. URL: https://github.com/shadadman:Vectora
                // 3. Select version
            """.trimIndent()
            "KMP" -> """
                // build.gradle.kts
                kotlin {
                    sourceSets {
                        commonMain.dependencies {
                            implementation("io.github.shadadman:vectora-search:1.0.0")
                        }
                    }
                }
            """.trimIndent()
            else -> ""
        }

        CodeBlock(code)
    }
}
