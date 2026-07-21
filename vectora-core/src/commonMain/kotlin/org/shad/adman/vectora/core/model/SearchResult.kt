package org.shad.adman.vectora.core.model

data class SearchResult<T>(
    val item: T,
    val score: Float
)