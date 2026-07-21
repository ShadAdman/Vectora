package org.shad.adman.vectora.engine.postprocessor

/**
 * Defines a postprocessing abstraction.
 * @param M Model output type (e.g., ModelOutput)
 * @param O Output type (e.g., Vector)
 */
interface Postprocessor<M, O> {
    fun process(output: M): O
}