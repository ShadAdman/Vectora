package org.shad.adman.vectora.search

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import org.shad.adman.vectora.core.cache.VectoraCache
import org.shad.adman.vectora.core.embedding.EmbeddingEngine
import org.shad.adman.vectora.core.model.IndexedItem
import org.shad.adman.vectora.core.model.Vector
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class VectoraSearchTest {

    class MockCache : VectoraCache {
        var savedItems: List<IndexedItem<*>> = emptyList()
        var clearCalled = false

        override suspend fun <T> saveItems(items: List<IndexedItem<T>>, serializer: (T) -> String) {
            savedItems = items
        }

        override suspend fun <T> loadItems(deserializer: (String) -> T): List<IndexedItem<T>> {
            return emptyList()
        }

        override suspend fun clear() {
            clearCalled = true
        }
    }

    @Serializable
    data class TestProduct(
        val id: String,
        val name: String,
        val description: String,
        val price: Double,
        val category: String,
        val color: String,
        val brand: String = "Nike",
    )

    class MockEmbeddingEngine : EmbeddingEngine {
        var isClosed = false
        override suspend fun embed(text: String): Vector {
            // Simple mock: return a vector where the first element is the text length
            return Vector(floatArrayOf(text.length.toFloat(), 0f, 0f))
        }

        override suspend fun embed(texts: List<String>): List<Vector> {
            return texts.map { embed(it) }
        }

        override fun close() {
            isClosed = true
        }
    }

    @Test
    fun testClose() {
        val engine = MockEmbeddingEngine()
        val search = VectoraSearch.create<TestProduct>(engine)
        search.close()
        assertTrue(engine.isClosed)
    }

    @Test
    fun testIndexAndSearch() = runTest {
        val engine = MockEmbeddingEngine()
        val search = VectoraSearch.create<TestProduct>(engine)

        val products = listOf(
            TestProduct("1", "Air Max 270", "A bold lifestyle shoe", 150.0, "Lifestyle", "Black"),
            TestProduct("2", "Air Force 1", "The b-ball icon", 110.0, "Basketball", "Black")
        )

        // Index like in the sample app
        search.index(products, textExtractor = { p ->
            "${p.brand} ${p.name} ${p.description} ${p.category} ${p.color}"
        })

        assertEquals(2, search.indexedItems.value.size)

        // Search
        search.search("lifestyle")
        
        val results = search.searchResults.first()
        assertTrue(results.isNotEmpty())
        assertEquals(2, results.size)
        
        // The first result should be the one with "lifestyle" which makes the text longer/shorter 
        // depending on mock logic. In our mock, length is the first vector element.
        // Cosine similarity will compare (len1, 0, 0) and (len2, 0, 0) which should be 1.0 
        // as long as both lengths are > 0.
        assertEquals(1.0f, results[0].score, 0.01f)
    }

    @Test
    fun testEmptySearch() = runTest {
        val engine = MockEmbeddingEngine()
        val search = VectoraSearch.create<TestProduct>(engine)

        search.search("anything")
        val results = search.searchResults.first()
        assertTrue(results.isEmpty())
    }

    @Test
    fun testTopK() = runTest {
        val engine = MockEmbeddingEngine()
        val search = VectoraSearch.create<TestProduct>(engine)

        val products = (1..20).map {
            TestProduct(it.toString(), "Product $it", "Description $it", 10.0, "Cat", "Color")
        }

        search.index(products, textExtractor = { it.description })
        search.search("query", topK = 5)

        val results = search.searchResults.first()
        assertEquals(5, results.size)
    }

    @Test
    fun testIndexWithSaveToCache() = runTest {
        val engine = MockEmbeddingEngine()
        val cache = MockCache()
        // Use internal constructor for testing with mock cache
        val search = VectoraSearch<TestProduct>(engine, cache, TestProduct.serializer())

        val products = listOf(
            TestProduct("1", "Product 1", "Desc 1", 10.0, "Cat", "Color")
        )

        search.index(
            products,
            textExtractor = { it.description },
            saveToCache = true
        )

        assertEquals(1, cache.savedItems.size)
        val item = (cache.savedItems[0] as IndexedItem<TestProduct>).item
        assertNotNull(item)
        assertEquals("Product 1", item.name)
    }
}
