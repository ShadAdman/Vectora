package org.shad.adman.vectora.search

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.shad.adman.vectora.caching.RealmVectoraCache
import org.shad.adman.vectora.caching.VectoraCache
import org.shad.adman.vectora.core.embedding.EmbeddingEngine
import org.shad.adman.vectora.core.model.IndexedItem
import org.shad.adman.vectora.core.model.SearchResult
import org.shad.adman.vectora.core.util.VectorMath
import org.shad.adman.vectora.engine.embedding.KFliteEmbeddingEngine
import org.shad.adman.vectora.query.QueryParser
import kotlin.random.Random

/**
 * Public API for Vectora search operations.
 */
class VectoraSearch<T> @PublishedApi internal constructor(
    private val engine: EmbeddingEngine,
    private val cache: VectoraCache? = null,
    private val itemSerializer: KSerializer<T>? = null
) : AutoCloseable {
    private val _indexedItems = MutableStateFlow<List<IndexedItem<T>>>(emptyList())
    val indexedItems: StateFlow<List<IndexedItem<T>>> = _indexedItems.asStateFlow()

    private val _searchResults = MutableSharedFlow<List<SearchResult<T>>>()
    val searchResults: SharedFlow<List<SearchResult<T>>> = _searchResults.asSharedFlow()

    companion object {
        private val json = Json { ignoreUnknownKeys = true }
        
        @PublishedApi
        internal val queryParser = QueryParser()

        /**
         * Parses a natural language query and populates a generic consumer-defined schema.
         */
        inline fun <reified T> parseQuery(query: String, schema: T): T {
            return queryParser.parse(query, schema)
        }

        /**
         * Creates a [VectoraSearch] instance using the all-MiniLM-L6-v2 model.
         * Automatically captures the serializer for [T].
         */
        inline fun <reified T> create(enableCache: Boolean = false): VectoraSearch<T> {
            val engine = KFliteEmbeddingEngine.createMiniLM()
            val cache = if (enableCache) RealmVectoraCache.create() else null
            val itemSerializer = if (enableCache) serializer<T>() else null
            return VectoraSearch(engine, cache, itemSerializer)
        }

        /**
         * Creates a [VectoraSearch] instance using the all-MiniLM-L6-v2 model with provided bytes.
         * Automatically captures the serializer for [T].
         */
        inline fun <reified T> create(modelBytes: ByteArray, enableCache: Boolean = false): VectoraSearch<T> {
            val engine = KFliteEmbeddingEngine.createMiniLM(modelBytes)
            val cache = if (enableCache) RealmVectoraCache.create() else null
            val itemSerializer = if (enableCache) serializer<T>() else null
            return VectoraSearch(engine, cache, itemSerializer)
        }

        /**
         * Creates a [VectoraSearch] instance using a custom [EmbeddingEngine].
         * Automatically captures the serializer for [T].
         */
        inline fun <reified T> create(engine: EmbeddingEngine, enableCache: Boolean = false): VectoraSearch<T> {
            val cache = if (enableCache) RealmVectoraCache.create() else null
            val itemSerializer = if (enableCache) serializer<T>() else null
            return VectoraSearch(engine, cache, itemSerializer)
        }
        
        /**
         * Internal factory for advanced usage.
         */
        @PublishedApi
        internal fun <T> createInternal(
            engine: EmbeddingEngine,
            cache: VectoraCache?,
            itemSerializer: KSerializer<T>?
        ): VectoraSearch<T> {
            return VectoraSearch(engine, cache, itemSerializer)
        }
    }

    /**
     * Loads indexed items from the cache.
     * @throws IllegalStateException if cache is not enabled.
     */
    suspend fun loadFromCache() {
        val currentCache = cache ?: throw IllegalStateException("Caching is not enabled for this VectoraSearch instance.")
        val currentSerializer = itemSerializer ?: throw IllegalStateException("Serializer not initialized for this instance.")
        
        val items = currentCache.loadItems { metadata ->
            json.decodeFromString(currentSerializer, metadata)
        }
        _indexedItems.value += items
    }

    /**
     * Adds already indexed items (e.g. from manual source).
     */
    fun addIndexedItems(items: List<IndexedItem<T>>) {
        _indexedItems.value += items
    }

    /**
     * Indexes a list of items.
     *
     * @param items The items to index.
     * @param textExtractor Function to extract searchable text from the item.
     * @param saveToCache Whether to persist the indexed items to cache.
     * @throws IllegalStateException if [saveToCache] is true but caching is not enabled.
     */
    suspend fun index(
        items: List<T>,
        saveToCache: Boolean = false,
        textExtractor: (T) -> String
        ) {
        if (saveToCache && (cache == null || itemSerializer == null)) {
            throw IllegalStateException("Caching is not enabled or serializer is missing for this VectoraSearch instance.")
        }

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

        if (saveToCache && cache != null && itemSerializer != null) {
            cache.saveItems(newIndexedItems) { item ->
                json.encodeToString(itemSerializer, item)
            }
        }
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
