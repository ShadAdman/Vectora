package org.shad.adman.vectora.core.util

import org.shad.adman.vectora.core.model.Vector
import kotlin.math.sqrt

object VectorMath {
    fun cosineSimilarity(v1: Vector, v2: Vector): Float {
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
}
