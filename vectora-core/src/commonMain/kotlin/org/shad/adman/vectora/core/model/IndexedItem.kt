package org.shad.adman.vectora.core.model

data class IndexedItem<T>(
    val id: String,
    val item: T,
    val vector: Vector
)