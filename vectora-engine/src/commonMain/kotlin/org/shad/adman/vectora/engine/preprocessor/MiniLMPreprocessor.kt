package org.shad.adman.vectora.engine.preprocessor

import org.shad.adman.vectora.engine.model.ModelInput

/**
 * Preprocessor for the all-MiniLM-L6-v2 model.
 * Converts input text into model-ready tensors.
 */
class MiniLMPreprocessor : Preprocessor<String, ModelInput> {
    override fun process(input: String): ModelInput {
        // Identifier 1: serving_default_inputs:0 (input_ids)
        // Identifier 0: serving_default_inputs_1:0 (attention_mask)

        val maxSeqLength = 128
        val inputIds = IntArray(maxSeqLength) { 0 }
        val attentionMask = IntArray(maxSeqLength) { 0 }

        // Placeholder for BERT tokenization.
        // In a real implementation, this would use a WordPiece tokenizer and a vocab.txt.
        val tokens = input.split(" ").take(maxSeqLength - 2)

        inputIds[0] = 101 // [CLS]
        attentionMask[0] = 1

        tokens.forEachIndexed { index, token ->
            // Using a simple hash as a placeholder for token ID
            inputIds[index + 1] = token.hashCode().let { if (it < 0) -it else it } % 30000
            attentionMask[index + 1] = 1
        }

        inputIds[tokens.size + 1] = 102 // [SEP]
        attentionMask[tokens.size + 1] = 1

        return ModelInput(
            tensors = mapOf(
                1 to inputIds,
                0 to attentionMask
            )
        )
    }
}