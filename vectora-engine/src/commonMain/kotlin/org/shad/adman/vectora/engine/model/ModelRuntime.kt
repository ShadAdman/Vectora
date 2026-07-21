package org.shad.adman.vectora.engine.model

/**
 * Internal abstraction representing the model execution layer.
 */
interface ModelRuntime : AutoCloseable {
    fun run(input: ModelInput): ModelOutput
}
