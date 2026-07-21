package org.shad.adman.vectora.search

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.shad.adman.vectora.core.embedding.EmbeddingEngine
import org.shad.adman.vectora.core.model.IndexedItem
import org.shad.adman.vectora.core.model.SearchResult
import org.shad.adman.vectora.core.util.VectorMath
import org.shad.adman.vectora.engine.embedding.KFliteEmbeddingEngine
import kotlin.random.Random

/**
 * Public API for Vectora search operations.
 */
class VectoraSearch<T> private constructor(
    private val engine: EmbeddingEngine
) : AutoCloseable {
    private val _indexedItems = MutableStateFlow<List<IndexedItem<T>>>(emptyList())
    val indexedItems: StateFlow<List<IndexedItem<T>>> = _indexedItems.asStateFlow()

    private val _searchResults = MutableSharedFlow<List<SearchResult<T>>>()
    val searchResults: SharedFlow<List<SearchResult<T>>> = _searchResults.asSharedFlow()

    companion object {
        /**
         * Creates a [VectoraSearch] instance using the all-MiniLM-L6-v2 model.
         * Automatically loads the model from the engine's resources.
         */
        suspend fun <T> createMiniLM(): VectoraSearch<T> {
            val engine = KFliteEmbeddingEngine.createMiniLM()
            return VectoraSearch(engine)
        }

        /**
         * Creates a [VectoraSearch] instance using the all-MiniLM-L6-v2 model with provided bytes.
         * @param modelBytes The raw bytes of the TFLite model.
         */
        fun <T> createMiniLM(modelBytes: ByteArray): VectoraSearch<T> {
            val engine = KFliteEmbeddingEngine.createMiniLM(modelBytes)
            return VectoraSearch(engine)
        }

        /**
         * Creates a [VectoraSearch] instance using a custom [EmbeddingEngine].
         */
        fun <T> create(engine: EmbeddingEngine): VectoraSearch<T> {
            return VectoraSearch(engine)
        }
    }

    /**
     * Indexes a list of items.
     * @param items The items to index.
     * @param textExtractor Function to extract searchable text from the item.
     */
    suspend fun index(items: List<T>, textExtractor: (T) -> String) {
        val texts = items.map(textExtractor)
        val vectors = engine.embed(texts)
        val newIndexedItems = items.zip(vectors).map { (item, vector) ->
            IndexedItem(
                id = Random.nextLong().toString(),
                item = item,
                vector = vector
            )
        }
        _indexedItems.value += newIndexedItems
    }

    /**
     * Searches for items similar to the query.
     * @param query The search query.
     * @param topK Number of results to return.
     */
    suspend fun search(query: String, topK: Int = 10) {
        val queryVector = engine.embed(query)
        val results = _indexedItems.value.map { indexedItem: IndexedItem<T> ->
            SearchResult(
                item = indexedItem.item,
                score = VectorMath.cosineSimilarity(queryVector, indexedItem.vector)
            )
        }.sortedByDescending { it.score }.take(topK)
        
        _searchResults.emit(results)
    }

    override fun close() {
        engine.close()
    }
}
