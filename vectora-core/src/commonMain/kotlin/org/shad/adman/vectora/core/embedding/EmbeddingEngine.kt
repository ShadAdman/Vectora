package org.shad.adman.vectora.core.embedding

import org.shad.adman.vectora.core.model.Vector

// Defines the contract for converting text to vectors
interface EmbeddingEngine : AutoCloseable {

    suspend fun embed(
        text: String
    ): Vector

    suspend fun embed(
        texts: List<String>
    ): List<Vector>
}