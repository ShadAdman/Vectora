package org.shad.adman.vectora.core.cache

import org.shad.adman.vectora.core.model.IndexedItem

// Contract for persisting indexed vectors; implementations live in separate
// modules (e.g. Realm-backed vectora-caching) so the search API carries no
// storage dependency.
interface VectoraCache {
    suspend fun <T> saveItems(items: List<IndexedItem<T>>, serializer: (T) -> String)
    suspend fun <T> loadItems(deserializer: (String) -> T): List<IndexedItem<T>>
    suspend fun clear()
}
