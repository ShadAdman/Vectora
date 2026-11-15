package org.shad.adman.vectora.engine.skainet

import sk.ainet.models.bert.HuggingFaceTokenizer
import sk.ainet.models.bert.TokenizerOutput

/**
 * Normalize, tokenize, and truncate like HuggingFace does for uncased BERT:
 * truncation keeps [CLS] at the front and re-terminates with [SEP].
 */
internal fun HuggingFaceTokenizer.encodeForBert(text: String, maxSeqLen: Int): TokenizerOutput {
    val output = encodeWithMetadata(normalizeForBert(text))
    if (output.inputIds.size <= maxSeqLen) return output
    val ids = output.inputIds.copyOf(maxSeqLen)
    ids[maxSeqLen - 1] = eosTokenId
    return TokenizerOutput(
        inputIds = ids,
        attentionMask = IntArray(maxSeqLen) { 1 },
        tokenTypeIds = IntArray(maxSeqLen) { 0 },
    )
}
