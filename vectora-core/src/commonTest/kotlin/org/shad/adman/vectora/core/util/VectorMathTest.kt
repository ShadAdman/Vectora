package org.shad.adman.vectora.core.util

import org.shad.adman.vectora.core.model.Vector
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class VectorMathTest {

    @Test
    fun worksForAnyDimension() {
        for (dim in listOf(3, 384, 768)) {
            val v = Vector(FloatArray(dim) { if (it == 0) 1f else 0f })
            assertEquals(1f, VectorMath.cosineSimilarity(v, v), 1e-6f)
            assertEquals(1f, VectorMath.dotProduct(v, v), 1e-6f)
        }
    }

    @Test
    fun rejectsMismatchedDimensions() {
        // e.g. querying a 768-dim (mdbr-leaf) index with a 384-dim (MiniLM) vector
        val miniLm = Vector(FloatArray(384) { 1f })
        val leaf = Vector(FloatArray(768) { 1f })
        assertFailsWith<IllegalArgumentException> { VectorMath.cosineSimilarity(miniLm, leaf) }
        assertFailsWith<IllegalArgumentException> { VectorMath.dotProduct(leaf, miniLm) }
    }
}
