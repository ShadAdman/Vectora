package org.shad.adman.vectora.engine.skainet

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import sk.ainet.models.bert.BertExecutionMode

/**
 * Configuration for [SkaiNetEmbeddingEngine].
 *
 * @param maxSeqLen hard cap on token sequence length including [CLS]/[SEP]
 *   (256 matches sentence-transformers' default for all-MiniLM-L6-v2)
 * @param executionMode DIRECT (eager) suits interactive queries; OPTIMIZED
 *   (traced + fused graph, bit-exact) pays off for bulk corpus indexing
 * @param queryPrefix optional prompt prepended to query texts — retrieval
 *   models like MongoDB/mdbr-leaf-ir use asymmetric query/document prompts
 * @param dispatcher where inference runs; single model instances are
 *   internally serialized regardless
 */
public data class SkaiNetEngineConfig(
    val maxSeqLen: Int = 256,
    val executionMode: BertExecutionMode = BertExecutionMode.DIRECT,
    val queryPrefix: String? = null,
    val dispatcher: CoroutineDispatcher = Dispatchers.Default,
)
