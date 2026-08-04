# Vectora

**The on-device lightning-fast semantic search engine for the next generation of Mobile applications.**

Vectora is a Kotlin Multiplatform (KMP) library that brings high-performance semantic search directly to your mobile devices. By leveraging on-device embeddings (MiniLM), Vectora allows you to perform intent-based searches that go far beyond simple keyword matching, all while keeping user data private and ensuring sub-millisecond response times.

---

## Key Features

*   **On-Device Semantic Search:** Convert text into high-dimensional vectors and perform similarity searches locally using the `all-MiniLM-L6-v2` model.
*   **Blazingly Fast:** Optimized for mobile; indexing 1000+ items typically takes only a few milliseconds.
*   **Query Export (Backend-Assisted):** Extract structured data from natural language queries on-device to power precise backend filters.
*   **Progressive Indexing:** Real-time progress tracking for large datasets with customizable chunking.
*   **Local Caching:** Optional persistent storage for indexed vectors to ensure instant availability across app restarts.
*   **Kotlin Multiplatform:** First-class support for Android and iOS.

---

## Installation

Add the dependency to your `build.gradle.kts`:

```kotlin
// For KMP projects
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.shadadman:vectora-search:1.0.0")
        }
    }
}

// For Android-only projects
dependencies {
    implementation("io.github.shadadman:vectora-search:1.0.0")
}
```

---

## Getting Started

### 1. Define Your Data Model

```kotlin
data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double
)
```

### 2. Initialize and Index

```kotlin
// Create a VectoraSearch instance
val vectora = VectoraSearch.create<Product>()

// Index your items
// chunkSize (default 100) controls progress granularity
vectora.index(products, chunkSize = 50) { it.name + " " + it.description }
```

### 3. Perform Semantic Search

```kotlin
// Search with natural language
vectora.search("lightweight running shoes", topK = 5)

// Collect and display results
vectora.searchResults.collect { results ->
    // results is List<SearchResult<Product>>
    val items = results.map { it.item }
    updateUi(items)
}
```

---

## Advanced: Query Export

Query Export bridges natural language and structured parameters for backend-assisted search. Define a shared schema, and Vectora will populate it on-device.

```kotlin
@Serializable
data class ProductFilters(
    val brand: String = "",
    val color: String = "",
    val maxPrice: Double = 0.0
)

// Extract filters locally
val query = "blue nike shoes under 150 dollars"
val filters = VectoraSearch.parseQuery(query, ProductFilters())

// Send precise, structured data to your backend
viewModelScope.launch {
    val results = repository.fetchProducts(
        brand = filters.brand,
        color = filters.color,
        maxPrice = filters.maxPrice
    )
    _uiState.value = results
}
```

---

## Platforms

*   **Android:** API 24+
*   **iOS:** iOS 13+ (ARM64 & Simulator)
*   **Kotlin Multiplatform:** commonMain support

---

## License

Vectora is available under the Apache 2.0 license. See the [LICENSE.txt](./LICENSE.txt) file for more info.
