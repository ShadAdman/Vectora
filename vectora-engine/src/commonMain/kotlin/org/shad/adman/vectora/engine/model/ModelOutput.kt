package org.shad.adman.vectora.engine.model

/**
 * Represent the model's output.
 */
data class ModelOutput(
    val tensors: Map<Int, Any>
)
