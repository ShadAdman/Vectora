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
fun SearchUsageSection() {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Using VectoraSearch",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFFBB86FC)
            )
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Indexing is the process of converting your data into high-dimensional vectors (embeddings) that represent the semantic meaning of the text. This allows Vectora to perform lightning-fast similarity searches based on intent rather than just keywords.",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 16.sp,
            lineHeight = 24.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Vectora is highly optimized for mobile devices. indexing 1000 products/items/models typically takes only a few milliseconds, making it suitable for real-time local updates and an excellent user experience.",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 16.sp,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "1. Your Data Model Example",
            style = MaterialTheme.typography.titleLarge.copy(color = Color(0xFF03DAC6), fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(12.dp))
        CodeBlock(
            """
            data class Product(
                val id: String,
                val name: String,
                val description: String,
                val price: Double
            )
            """.trimIndent()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "2. Initialize and Index",
            style = MaterialTheme.typography.titleLarge.copy(color = Color(0xFF03DAC6), fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "The 'products' variable is a simple List<Product> containing your application data. This can be any list of your items or data that comes from remote or your local database. If you do the indexing before sending your users to search section, it makes the search ux very smooth and blazingly fast",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        CodeBlock(
            """
            // Create an instance with the SKaiNET engine (recommended):
            // pure-Kotlin BERT runtime, full vocab, L2-normalized vectors.
            // Model weights: all-MiniLM-L6-v2 safetensors + config.json + vocab.txt

            val engine = SkaiNetEmbeddingEngine.create(
                model = AndroidAssets.loadSafeTensors(context),   // or any ModelSource
                vocabText = AndroidAssets.loadVocab(context),
            )
            val vectora = VectoraSearch.create<Product>(engine = engine)

            // (Legacy) zero-arg create() still works but is deprecated —
            // it uses the KFlite/TFLite backend.

            // Index your list of products
            // The lambda defines which fields are used for semantic search

            vectora.index(products) { it.name + " " + it.description + " " + it.price }
            """.trimIndent()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "3. Search and Observe",
            style = MaterialTheme.typography.titleLarge.copy(color = Color(0xFF03DAC6), fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Perform a search by passing a natural language query. You can also specify 'topK' to limit the number of results returned (defaults to 10).",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        CodeBlock(
            """
            // Search with natural language and optional topK
            
            vectora.search("lightweight running shoes", topK = 5)

            // Collect results in your UI
            
            vectora.searchResults.collect { results ->
                // results is a list of SearchResult<Product> containing 
                // the original item and its similarity `score`
                
                adapter.submitList(results.map { it.item })
            }
            """.trimIndent()
        )
    }
}



