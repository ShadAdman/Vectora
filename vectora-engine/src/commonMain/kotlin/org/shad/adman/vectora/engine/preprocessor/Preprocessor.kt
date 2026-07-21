package org.shad.adman.vectora.engine.preprocessor

/**
 * Defines a preprocessing abstraction capable of converting the public input into the model's expected input.
 * @param I Input type (e.g., String)
 * @param M Model input type (e.g., ModelInput)
 */
interface Preprocessor<I, M> {
    fun process(input: I): M
}