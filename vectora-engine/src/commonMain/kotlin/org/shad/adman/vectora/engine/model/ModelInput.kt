package org.shad.adman.vectora.engine.model

/**
 * Represent the model's expected input.
 */
data class ModelInput(
    val tensors: Map<Int, Any>
)
