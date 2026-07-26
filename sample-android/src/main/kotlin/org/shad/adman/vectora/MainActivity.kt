package org.shad.adman.vectora

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.shad.adman.vectora.core.model.SearchResult
import org.shad.adman.vectora.search.VectoraSearch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    App()
                }
            }
        }
    }
}

@Composable
fun App() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var searchEngine by remember { mutableStateOf<VectoraSearch<Product>?>(null) }
    var results by remember { mutableStateOf<List<SearchResult<Product>>>(emptyList()) }
    var query by remember { mutableStateOf("hi. give me a list of your best nike shoes that are in black color") }
    var isReady by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Initializing...") }

    val products = remember {
        listOf(
            Product("1", "Air Max 270", "A bold lifestyle shoe with a large Air unit for all-day comfort.", 150.0, "Lifestyle", "Black"),
            Product("2", "Air Force 1 '07", "The b-ball icon that puts a fresh spin on what you know best.", 110.0, "Basketball", "Black"),
            Product("3", "Zoom Pegasus 40", "Highly responsive running shoe with breathable mesh.", 130.0, "Running", "Black/Anthracite"),
            Product("4", "Revolution 6", "Simple, classic design made with at least 20% recycled content.", 70.0, "Running", "Triple Black"),
            Product("5", "Court Vision Low", "80s-inspired basketball style meets modern materials.", 75.0, "Lifestyle", "Black/Black"),
            Product("6", "Blazer Mid '77", "Vintage basketball look with a modern twist.", 105.0, "Lifestyle", "Black/White"),
            Product("7", "Air Max Excee", "Inspired by the Air Max 90, a celebration of a classic through a new lens.", 95.0, "Lifestyle", "Black/White"),
            Product("8", "Tanjun", "Simplicity at its best, named after the Japanese word for 'Simplicity'.", 70.0, "Lifestyle", "Black/White"),
            Product("9", "Downshifter 12", "Supportive and durable for your daily runs.", 75.0, "Running", "Black/Dark Smoke Grey"),
            Product("10", "Air Max Dawn", "Rooted in track DNA, made from at least 20% recycled material.", 115.0, "Lifestyle", "Black/White"),
            Product("11", "Air Max 90", "Nothing as fly, nothing as comfortable, nothing as proven.", 130.0, "Lifestyle", "Black/Black/White"),
            Product("12", "Air Max 97", "Iconic ripple design inspired by Japanese bullet trains.", 175.0, "Lifestyle", "Black/White"),
            Product("13", "Air Max Plus", "Tuned Air experience that offers premium stability and cushioning.", 175.0, "Lifestyle", "Black/Black"),
            Product("14", "Waffle Debut", "Retro style meets modern comfort with a waffle outsole.", 75.0, "Lifestyle", "Black/White"),
            Product("15", "React Vision", "Uninterrupted comfort with Nike React technology.", 140.0, "Lifestyle", "Black/White/Grey"),
            Product("16", "SB Dunk Low Pro", "Skateboard-ready with Zoom Air cushioning.", 115.0, "Skateboarding", "Black/White"),
            Product("17", "Air Huarache", "Built to fit your foot and designed for comfort.", 125.0, "Lifestyle", "Triple Black"),
            Product("18", "Air Presto", "The 'T-shirt for your feet', sleek and comfortable.", 135.0, "Lifestyle", "Black/Black"),
            Product("19", "Air VaporMax Plus", "Floating cage and cushioned upper for a secure fit.", 210.0, "Lifestyle", "Triple Black"),
            Product("20", "Renew Ride 3", "Soft, smooth, and stable for your everyday miles.", 80.0, "Running", "Black/White"),
            Product("21", "Quest 5", "Lightweight and breathable for neutral runners.", 80.0, "Running", "Black/White"),
            Product("22", "Legend Essential 3", "Durable enough for the rigors of a fast-paced group class.", 65.0, "Training", "Black"),
            Product("23", "SuperRep Go 3", "Flexible Flyknit upper made from recycled materials.", 100.0, "Training", "Black"),
            Product("24", "MC Trainer 2", "Versatility from the weight room to the turf.", 75.0, "Training", "Black/White"),
            Product("25", "Juniper Trail 2", "Tough traction for off-road adventures.", 85.0, "Trail Running", "Black"),
            Product("26", "Ultraboost 22", "High-performance running shoe with incredible energy return.", 190.0, "Running", "Core Black", brand = "Adidas"),
            Product("27", "Suede Classic XXI", "The most iconic Puma sneaker of all time.", 75.0, "Lifestyle", "Puma Black", brand = "Puma"),
            Product("28", "Classic Leather", "Timeless style that never goes out of fashion.", 85.0, "Lifestyle", "Black", brand = "Reebok"),
            Product("29", "574", "The most 'New Balance' shoe ever.", 90.0, "Lifestyle", "Black/White", brand = "New Balance"),
            Product("30", "Gel-Kayano 29", "Premium stability and energized cushioning.", 160.0, "Running", "Black/White", brand = "Asics")
        )
    }

    LaunchedEffect(Unit) {
        try {
            status = "Loading model..."
            val search = VectoraSearch.create<Product>()
            
            status = "Indexing products..."
            search.index(products) { p ->
                "${p.brand} ${p.name} ${p.description} ${p.category} ${p.color}"
            }
            searchEngine = search
            isReady = true
            status = "Ready"

            search.searchResults.collect { searchResults: List<SearchResult<Product>> ->
                results = searchResults
            }
        } catch (e: Exception) {
            status = "Error: ${e.message}"
            e.printStackTrace()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).systemBarsPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = status, style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = {
                context.startActivity(Intent(context, QueryUnderstandingActivity::class.java))
            }) {
                Text("Query Understanding Demo")
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search Products") }
        )
        Button(
            onClick = {
                scope.launch {
                    searchEngine?.search(query)
                }
            },
            enabled = isReady,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Search")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn {
            items(results) { result ->
                ListItem(
                    headlineContent = { 
                        Text("${result.item.brand} ${result.item.name}") 
                    },
                    supportingContent = { 
                        Column {
                            Text(result.item.description, style = MaterialTheme.typography.bodySmall)
                            Text("Price: $${result.item.price} | Color: ${result.item.color}", style = MaterialTheme.typography.bodySmall)
                            Text("Score: ${result.score}", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
            }
        }
    }
}
