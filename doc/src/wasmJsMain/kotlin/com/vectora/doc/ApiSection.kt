package com.vectora.doc

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ApiSection(onBack: () -> Unit) {
    var selectedSection by remember { mutableStateOf("Bio") }
    val sections = listOf("Bio", "Usage", "Query Export", "Import")

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isMobile = maxWidth < 800.dp

        if (isMobile) {
            Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                // Mobile Top Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0A0A0A))
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Vectora Doc",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        TextButton(onClick = onBack) {
                            Text("Back", color = Color(0xFFBB86FC))
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sections.forEach { section ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedSection == section) Color(0xFF1A1A1A) else Color.Transparent)
                                    .clickable { selectedSection = section }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = section,
                                    color = if (selectedSection == section) Color(0xFFBB86FC) else Color.Gray,
                                    fontWeight = if (selectedSection == section) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                // Content Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        when (selectedSection) {
                            "Bio" -> BioSection()
                            "Usage" -> SearchUsageSection()
                            "Query Export" -> QueryExportUsageSection()
                            "Import" -> ImportSection()
                        }
                        Spacer(modifier = Modifier.height(64.dp))
                    }
                }
            }
        } else {
            Row(modifier = Modifier.fillMaxSize()) {
                // Side Menu
                Column(
                    modifier = Modifier
                        .width(280.dp)
                        .fillMaxHeight()
                        .background(Color(0xFF0A0A0A))
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Vectora Doc",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    sections.forEach { section ->
                        SideMenuItem(
                            text = section,
                            isSelected = selectedSection == section,
                            onClick = { selectedSection = section }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    TextButton(onClick = onBack) {
                        Text("< Back to Home", color = Color(0xFFBB86FC))
                    }
                }

                // Content Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(Color.Black)
                        .verticalScroll(rememberScrollState())
                        .padding(64.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().widthIn(max = 800.dp)) {
                        when (selectedSection) {
                            "Bio" -> BioSection()
                            "Usage" -> SearchUsageSection()
                            "Query Export" -> QueryExportUsageSection()
                            "Import" -> ImportSection()
                        }
                        
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SideMenuItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0xFF1A1A1A) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color(0xFFBB86FC) else Color.Gray,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 16.sp
        )
    }
}
