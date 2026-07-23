package com.vectora.doc

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import vectora.doc.generated.resources.Res
import vectora.doc.generated.resources.vectora_logo

@Composable
fun App() {
    var currentScreen by remember { mutableStateOf("home") }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFBB86FC),
            background = Color(0xFF000000),
            surface = Color(0xFF121212)
        ),
        typography = LatoTypography()
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
                }
            ) { screen ->
                if (screen == "home") {
                    HomeScreen(onNavigateToApi = { currentScreen = "api" })
                } else {
                    ApiSection(onBack = { currentScreen = "home" })
                }
            }
        }
    }
}

@Composable
fun HomeScreen(onNavigateToApi: () -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isMobile = maxWidth < 900.dp
        
        if (isMobile) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Animation on top
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(650.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IPhoneFrame(modifier = Modifier.scale(0.85f))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Text below
                HomeScreenText(onNavigateToApi = onNavigateToApi, isMobile = true)
                
                Spacer(modifier = Modifier.height(48.dp))
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize().padding(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Side: Text
                HomeScreenText(
                    onNavigateToApi = onNavigateToApi,
                    isMobile = false,
                    modifier = Modifier.weight(1.2f)
                )

                // Right Side: Animation
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    IPhoneFrame()
                }
            }
        }
    }
}

@Composable
fun HomeScreenText(
    onNavigateToApi: () -> Unit,
    isMobile: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.then(if (isMobile) Modifier else Modifier.padding(end = 48.dp)),
        horizontalAlignment = if (isMobile) Alignment.CenterHorizontally else Alignment.Start
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(Res.drawable.vectora_logo),
                contentDescription = "Vectora Logo",
                modifier = Modifier.size(if (isMobile) 80.dp else 120.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Vectora",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isMobile) 64.sp else 110.sp,
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFFBB86FC), Color(0xFF03DAC6))
                    ),
                    letterSpacing = (-2).sp
                )
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "The on-device lightning-fast semantic search engine for the next generation of Mobile applications.",
            style = MaterialTheme.typography.headlineSmall.copy(
                color = Color.White.copy(alpha = 0.8f),
                lineHeight = if (isMobile) 32.sp else 36.sp,
                fontWeight = FontWeight.Light,
                textAlign = if (isMobile) TextAlign.Center else TextAlign.Start
            )
        )
        Spacer(modifier = Modifier.height(if (isMobile) 32.dp else 48.dp))
        UseVectoraButton(onClick = onNavigateToApi)
    }
}

@Composable
fun IPhoneFrame(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(360.dp)
            .height(720.dp)
            .border(8.dp, Color(0xFF222222), RoundedCornerShape(48.dp))
            .padding(8.dp)
            .clip(RoundedCornerShape(40.dp))
            .background(Color(0xFF0A0A0A))
    ) {
        ChatAnimation()
    }
}

@Composable
fun ChatAnimation() {
    val fullQuery = "hi. give me 2026 nike jordan shoes with blue or black color mixed that are under 200$"
    val secondQuery = "Filter them by manufacturing year between 2023 and 2026"
    val thirdQuery = "give me the one that have above 10 percent off"
    
    var displayedQuery by remember { mutableStateOf("") }
    var isSent by remember { mutableStateOf(false) }
    var showResults by remember { mutableStateOf(false) }
    
    var displayedSecondQuery by remember { mutableStateOf("") }
    var isSecondSent by remember { mutableStateOf(false) }
    var showSecondResults by remember { mutableStateOf(false) }

    var displayedThirdQuery by remember { mutableStateOf("") }
    var isThirdSent by remember { mutableStateOf(false) }
    var showThirdResults by remember { mutableStateOf(false) }

    val products2026 = remember {
        listOf(
            Product("Jordan Retro '26", "$195", "Blue/Black", Color(0xFF1A237E), "2026"),
            Product("Jordan Air Max", "$180", "Black/Cyan", Color(0xFF006064), "2026"),
            Product("Jordan Flight", "$189", "Deep Blue", Color(0xFF0D47A1), "2026"),
            Product("Jordan Low-Top", "$165", "Obsidian", Color(0xFF311B92), "2026"),
            Product("Jordan High '26", "$199", "Midnight Black", Color(0xFF212121), "2026"),
            Product("Jordan Zoom", "$175", "Blue Mist", Color(0xFF1565C0), "2026"),
            Product("Jordan Classic", "$150", "Black Volt", Color(0xFF1B5E20), "2026"),
            Product("Jordan Swift", "$185", "Dark Blue", Color(0xFF01579B), "2026"),
            Product("Jordan Prime", "$190", "Shadow Black", Color(0xFF424242), "2026"),
            Product("Jordan Elite", "$199", "Electric Blue", Color(0xFF2962FF), "2026")
        )
    }

    val productsMixed = remember {
        listOf(
            Product("Jordan Retro '26", "$195", "Blue/Black", Color(0xFF1A237E), "2026"),
            Product("Jordan 1 '23", "$170", "University Blue", Color(0xFF42A5F5), "2023"),
            Product("Jordan 4 '24", "$210", "Navy Grey", Color(0xFF3F51B5), "2024"),
            Product("Jordan 11 '25", "$225", "Space Blue", Color(0xFF1A237E), "2025"),
            Product("Jordan Low-Top", "$165", "Obsidian", Color(0xFF311B92), "2026"),
            Product("Jordan High '26", "$199", "Midnight Black", Color(0xFF212121), "2026")
        )
    }

    val productsDiscounted = remember {
        listOf(
            Product("Jordan 1 '23", "$136", "University Blue", Color(0xFF42A5F5), "2023", "20% OFF"),
            Product("Jordan Classic", "$127", "Black Volt", Color(0xFF1B5E20), "2026", "15% OFF"),
            Product("Jordan Zoom", "$148", "Blue Mist", Color(0xFF1565C0), "2026", "15% OFF")
        )
    }

    val infiniteTransition = rememberInfiniteTransition()
    val xOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val brush = Brush.linearGradient(
        colors = listOf(Color(0xFFBB86FC), Color(0xFF03DAC6), Color(0xFFBB86FC)),
        start = Offset(xOffset - 500f, 0f),
        end = Offset(xOffset, 500f)
    )

    LaunchedEffect(Unit) {
        while(true) {
            // Reset
            displayedQuery = ""
            isSent = false
            showResults = false
            displayedSecondQuery = ""
            isSecondSent = false
            showSecondResults = false
            displayedThirdQuery = ""
            isThirdSent = false
            showThirdResults = false
            delay(1500)

            // Typing first query
            fullQuery.forEach { char ->
                displayedQuery += char
                delay(40)
            }
            delay(800)
            isSent = true
            delay(600)
            showResults = true
            delay(4000)

            // Typing second query
            secondQuery.forEach { char ->
                displayedSecondQuery += char
                delay(40)
            }
            delay(800)
            isSecondSent = true
            delay(600)
            showSecondResults = true
            delay(4000)

            // Typing third query
            thirdQuery.forEach { char ->
                displayedThirdQuery += char
                delay(40)
            }
            delay(800)
            isThirdSent = true
            delay(600)
            showThirdResults = true
            delay(8000)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search Input at Top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(4.dp, brush, RoundedCornerShape(28.dp))
                .background(Color(0xFF1A1A1A), RoundedCornerShape(28.dp))
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val inputText = when {
                    displayedThirdQuery.isNotEmpty() || isThirdSent -> displayedThirdQuery
                    displayedSecondQuery.isNotEmpty() || isSecondSent -> displayedSecondQuery
                    else -> displayedQuery
                }

                Text(
                    text = if (inputText.isEmpty()) "Search by describing..." else inputText,
                    color = if (inputText.isEmpty()) Color.Gray else Color.White,
                    modifier = Modifier.weight(1f).animateContentSize(),
                    fontSize = 15.sp
                )
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = if (isThirdSent || (isSecondSent && !showThirdResults && displayedThirdQuery.isEmpty())) Color(0xFFBB86FC) else Color.Gray.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Chat History (Results only)
        Box(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
            Column(modifier = Modifier.fillMaxSize().padding(vertical = 16.dp)) {
                if (showThirdResults) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(1f)) {
                            ProductGrid(productsDiscounted)
                        }
                    }
                } else if (showSecondResults) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(1f)) {
                            ProductGrid(productsMixed)
                        }
                    }
                } else if (showResults) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            "Here are 20 results matching your criteria that are currently in stock:",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            ProductGrid(products2026)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UseVectoraButton(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition()
    val xOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val brush = Brush.linearGradient(
        colors = listOf(Color(0xFFBB86FC), Color(0xFF03DAC6), Color(0xFFBB86FC)),
        start = Offset(xOffset - 500f, 0f),
        end = Offset(xOffset, 500f)
    )

    Button(
        onClick = onClick,
        modifier = Modifier
            .height(58.dp)
            .width(240.dp)
            .border(4.dp, brush, RoundedCornerShape(29.dp)),
        shape = RoundedCornerShape(29.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            "Use Vectora",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

data class Product(val name: String, val price: String, val color: String, val accentColor: Color, val year: String = "2026", val discount: String? = null)

@Composable
fun ProductGrid(products: List<Product>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(products) { index, product ->
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(product) {
                delay(index * 60L)
                visible = true
            }
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(400)) + slideInVertically(
                    initialOffsetY = { 20 },
                    animationSpec = tween(400)
                )
            ) {
                ProductItem(product)
            }
        }
    }
}

@Composable
fun ProductItem(product: Product) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1A1A1A))
            .border(1.dp, Color(0xFF333333), RoundedCornerShape(16.dp))
            .padding(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.verticalGradient(listOf(product.accentColor.copy(alpha = 0.4f), product.accentColor.copy(alpha = 0.8f)))),
            contentAlignment = Alignment.Center
        ) {
            // "Graphic" representation of a shoe
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "JORDAN",
                    color = Color.White.copy(alpha = 0.3f),
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                )
                Text(
                    product.year,
                    color = Color.White.copy(alpha = 0.2f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(product.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(product.color, color = Color.Gray, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(product.price, color = Color(0xFFBB86FC), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            if (product.discount != null) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    product.discount,
                    color = Color(0xFF03DAC6),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(Color(0xFF03DAC6).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}
