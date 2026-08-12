package org.shad.adman.vectora.core.util

import org.shad.adman.vectora.core.model.Vector
import kotlin.math.sqrt

object VectorMath {
    private fun requireSameDimensions(v1: Vector, v2: Vector) {
        require(v1.values.size == v2.values.size) {
            "Vector dimensions differ (${v1.values.size} vs ${v2.values.size}) — " +
                "an index must be built and queried with the same embedding model"
        }
    }

    fun cosineSimilarity(v1: Vector, v2: Vector): Float {
        requireSameDimensions(v1, v2)
        var dotProduct = 0.0f
        var normA = 0.0f
        var normB = 0.0f
        for (i in v1.values.indices) {
            dotProduct += v1.values[i] * v2.values[i]
            normA += v1.values[i] * v1.values[i]
            normB += v2.values[i] * v2.values[i]
        }
        val denominator = sqrt(normA.toDouble()) * sqrt(normB.toDouble())
        return if (denominator == 0.0) 0.0f else (dotProduct / denominator).toFloat()
    }

    // For unit vectors cosine similarity reduces to this — skip the norms.
    fun dotProduct(v1: Vector, v2: Vector): Float {
        requireSameDimensions(v1, v2)
        var dot = 0.0f
        for (i in v1.values.indices) {
            dot += v1.values[i] * v2.values[i]
        }
        return dot
    }
}
