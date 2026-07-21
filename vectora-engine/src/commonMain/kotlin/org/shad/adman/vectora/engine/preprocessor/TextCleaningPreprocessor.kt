package org.shad.adman.vectora.engine.preprocessor

import org.shad.adman.vectora.engine.model.ModelInput

/**
 * A decorator preprocessor that cleans the input text before passing it to the delegate.
 */
class TextCleaningPreprocessor(
    private val delegate: Preprocessor<String, ModelInput>
) : Preprocessor<String, ModelInput> {
    override fun process(input: String): ModelInput {
        val cleaned = input.lowercase()
            .trim()
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
        return delegate.process(cleaned)
    }
}
