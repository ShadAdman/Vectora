package org.shad.adman.vectora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable
import org.shad.adman.vectora.search.VectoraSearch

@Serializable
data class ProductSearchSchema(
    val query: String = "",
    val filters: ProductFilters = ProductFilters()
)

@Serializable
data class ProductFilters(
    val brand: String = "",
    val color: String = "",
    val minPrice: Double = 0.0,
    val maxPrice: Double = 0.0
)

class QueryUnderstandingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    QueryUnderstandingScreen()
                }
            }
        }
    }
}

@Composable
fun QueryUnderstandingScreen() {
    var query by remember { mutableStateOf("Show me black Nike shoes under 100") }
    var parsedResult by remember { mutableStateOf<ProductSearchSchema?>(null) }
    var rawResult by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Vectora Query Understanding",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Model-free, on-device natural language parsing",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Natural Language Query") },
            placeholder = { Text("e.g. Black Nike shoes under 100") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val schema = ProductSearchSchema()
                parsedResult = VectoraSearch.parseQuery(query, schema)
                // For demonstration, we also show the internal JSON mapping
                // Note: VectoraSearch.queryParser is internal, but we can re-serialize
                // the result or just use the parsed object.
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Parse Query")
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (parsedResult != null) {
            Text(text = "Parsed Results", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            ResultItem("Main Query", parsedResult?.query ?: "")
            ResultItem("Brand", parsedResult?.filters?.brand ?: "")
            ResultItem("Color", parsedResult?.filters?.color ?: "")
            ResultItem("Min Price", parsedResult?.filters?.minPrice?.toString() ?: "0.0")
            ResultItem("Max Price", parsedResult?.filters?.maxPrice?.toString() ?: "0.0")

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(text = "Structured Output (Serialized)", style = MaterialTheme.typography.titleSmall)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF0F0F0))
                    .padding(16.dp)
            ) {
                Text(
                    text = "{\n" +
                            "  \"query\": \"${parsedResult?.query}\",\n" +
                            "  \"filters\": {\n" +
                            "    \"brand\": \"${parsedResult?.filters?.brand}\",\n" +
                            "    \"color\": \"${parsedResult?.filters?.color}\",\n" +
                            "    \"minPrice\": ${parsedResult?.filters?.minPrice},\n" +
                            "    \"maxPrice\": ${parsedResult?.filters?.maxPrice}\n" +
                            "  }\n" +
                            "}",
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun ResultItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
        Text(text = if (value.isEmpty()) "-" else value, style = MaterialTheme.typography.bodyLarge)
    }
}
