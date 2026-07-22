package org.shad.adman.vectora.engine.preprocessor

import org.shad.adman.vectora.engine.model.ModelInput

/**
 * Preprocessor for the all-MiniLM-L6-v2 model.
 * Converts input text into model-ready tensors using WordPiece tokenization.
 */
class MiniLMPreprocessor(vocab: List<String>) : Preprocessor<String, ModelInput> {
    private val tokenizer = WordPieceTokenizer(vocab)
    private val maxSeqLength = 128

    override fun process(input: String): ModelInput {
        val tokens = tokenizer.tokenize(input).take(maxSeqLength - 2)
        val inputIds = IntArray(maxSeqLength) { tokenizer.getPadId() }
        val attentionMask = IntArray(maxSeqLength) { 0 }

        inputIds[0] = tokenizer.getClsId()
        attentionMask[0] = 1

        val tokenIds = tokenizer.convertTokensToIds(tokens)
        tokenIds.forEachIndexed { index, id ->
            inputIds[index + 1] = id
            attentionMask[index + 1] = 1
        }

        inputIds[tokens.size + 1] = tokenizer.getSepId()
        attentionMask[tokens.size + 1] = 1

        // Model expects [batch, sequence] which is [1, 128]
        val inputIds2D = Array(1) { inputIds }
        val attentionMask2D = Array(1) { attentionMask }

        return ModelInput(
            tensors = mapOf(
                0 to inputIds2D,
                1 to attentionMask2D
            )
        )
    }
}
