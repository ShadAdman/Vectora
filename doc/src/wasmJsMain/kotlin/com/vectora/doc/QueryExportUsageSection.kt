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
fun QueryExportUsageSection() {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Using VectoraSearch.parseQuery()",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFFBB86FC)
            )
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Query Export enables backend-assisted search by bridging the gap between natural language and structured parameters. By defining a shared schema between your client and backend, Vectora populates it locally, allowing you to send precise, structured requests to your server.",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 16.sp,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "This approach reduces server-side complexity and latency by handling the semantic extraction on-device, ensuring your backend receives exactly the filters it needs to process the search efficiently.",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 16.sp,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "1. Your Schema Example",
            style = MaterialTheme.typography.titleLarge.copy(color = Color(0xFF03DAC6), fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Define a data class that represents the filters or parameters you want to extract from a natural language query. Make sure it's annotated with @Serializable.",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        CodeBlock(
            """
            @Serializable
            data class ProductFilters(
                val brand: String = "",
                val color: String = "",
                val minPrice: Double = 0.0,
                val maxPrice: Double = 0.0
            )
            """.trimIndent()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "2. Parse Natural Language",
            style = MaterialTheme.typography.titleLarge.copy(color = Color(0xFF03DAC6), fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Use VectoraSearch.parseQuery() to convert a natural language string into your schema. This is useful for building advanced filter UIs or sending structured data to your backend.",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        CodeBlock(
            """
            val query = "blue nike shoes under 150 dollars"
            
            // Vectora extracts the relevant information into your object
            val filters = VectoraSearch.parseQuery(query, ProductFilters())
            
            println(filters.brand)    // "nike"
            println(filters.color)    // "blue"
            println(filters.maxPrice) // 150.0
            """.trimIndent()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "3. Send to Backend",
            style = MaterialTheme.typography.titleLarge.copy(color = Color(0xFF03DAC6), fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Once Vectora has filled your schema, you can send the structured object directly to your backend API. This combines the power of client-side semantic extraction with your existing server-side business logic.",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        CodeBlock(
            """
            // Perform extraction
            val filters = VectoraSearch.parseQuery(userInput, ProductFilters())

            // Send to your backend API
            viewModelScope.launch {
                val results = repository.fetchProducts(
                    brand = filters.brand,
                    color = filters.color,
                    maxPrice = filters.maxPrice
                )
                _uiState.value = results
            }
            """.trimIndent()
        )
    }
}



