package org.shad.adman.vectora.caching

import org.shad.adman.vectora.core.model.IndexedItem
import org.shad.adman.vectora.core.model.Vector

interface VectoraCache {
    suspend fun <T> saveItems(items: List<IndexedItem<T>>, serializer: (T) -> String)
    suspend fun <T> loadItems(deserializer: (String) -> T): List<IndexedItem<T>>
    suspend fun clear()
}
