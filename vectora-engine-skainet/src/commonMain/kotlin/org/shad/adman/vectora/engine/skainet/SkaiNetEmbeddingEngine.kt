package org.shad.adman.vectora.engine.skainet

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.shad.adman.vectora.core.embedding.NormalizedEmbeddingEngine
import org.shad.adman.vectora.core.model.Vector
import sk.ainet.apps.llm.weights.BertSafeTensorsNameResolver
import sk.ainet.context.DirectCpuExecutionContext
import sk.ainet.frontend.text.Encoding
import sk.ainet.frontend.text.Padding
import sk.ainet.frontend.text.Truncation
import sk.ainet.frontend.tokenizer.TextFrontEnds
import sk.ainet.frontend.tokenizer.WordPieceTokenizer
import sk.ainet.io.ByteArrayRandomAccessSource
import sk.ainet.io.ParametersLoader
import sk.ainet.io.gguf.StreamingGgufParametersLoader
import sk.ainet.io.safetensors.SafeTensorsParametersLoader
import sk.ainet.lang.types.FP32
import sk.ainet.models.bert.BertConfigParser
import sk.ainet.models.bert.BertEncoderRuntime
import sk.ainet.models.bert.BertNetworkLoader
import sk.ainet.models.bert.PooledExecutionContext
import sk.ainet.models.bert.createBertEncoderRuntime

/**
 * [EmbeddingEngine] backed by SKaiNET's BERT encoder runtime.
 *
 * Instance-scoped: every engine owns its own runtime and execution context,
 * so multiple engines (e.g. two different models) coexist in one process and
 * closing one never affects another.
 *
 * Output vectors are mean/CLS-pooled, optionally dense-projected, and
 * L2-normalized — identical semantics to Python sentence-transformers with
 * `normalize_embeddings=True`, so cosine similarity equals the dot product.
 */
public class SkaiNetEmbeddingEngine private constructor(
    private val runtime: BertEncoderRuntime<FP32>,
    private val tokenizer: WordPieceTokenizer,
    private val config: SkaiNetEngineConfig,
) : NormalizedEmbeddingEngine {

    /** Dimensionality of produced vectors (hidden size or projection size). */
    public val dimensions: Int get() = runtime.dimensions

    // BertEncoderRuntime is not thread-safe; serialize inference per engine.
    private val inferenceLock = Mutex()
    private var closed = false

    override suspend fun embed(text: String): Vector = withContext(config.dispatcher) {
        embedLocked(text)
    }

    override suspend fun embed(texts: List<String>): List<Vector> = withContext(config.dispatcher) {
        if (texts.size <= 1 || config.batchSize <= 1) {
            return@withContext texts.map { text -> embedLocked(text).also { yield() } }
        }
        // Batch texts of similar length together so padding to the longest
        // row of each batch wastes as little compute as possible.
        val vectors = arrayOfNulls<Vector>(texts.size)
        texts.indices.sortedBy { texts[it].length }.chunked(config.batchSize).forEach { indices ->
            val embedded = embedBatchLocked(indices.map { texts[it] })
            indices.forEachIndexed { position, index -> vectors[index] = embedded[position] }
            yield()
        }
        vectors.map { checkNotNull(it) }
    }

    /** Embeds a search query, applying [SkaiNetEngineConfig.queryPrefix] if configured. */
    public suspend fun embedQuery(text: String): Vector =
        embed(config.queryPrefix?.plus(text) ?: text)

    private suspend fun embedLocked(text: String): Vector = inferenceLock.withLock {
        check(!closed) { "SkaiNetEmbeddingEngine is closed" }
        val encoded = tokenizer.encodeForBert(text, config.maxSeqLen)
        Vector(runtime.encode(encoded.ids, encoded.attentionMask))
    }

    private suspend fun embedBatchLocked(texts: List<String>): List<Vector> = inferenceLock.withLock {
        check(!closed) { "SkaiNetEmbeddingEngine is closed" }
        val batch = tokenizer.encodeBatch(texts, Padding.Longest, Truncation.Longest(config.maxSeqLen))
        runtime.encodeBatch(batch.ids, batch.seqLen, batch.attentionMask).map(::Vector)
    }

    override fun close() {
        closed = true
    }

    public companion object {

        private val FLOAT_DTYPES = setOf("F64", "F32", "F16", "BF16")

        /**
         * HuggingFace BERT checkpoints carry an I64 `embeddings.position_ids`
         * buffer the encoder never reads (positions are derived arithmetically);
         * loading only float tensors keeps the single-dtype load valid.
         */
        private fun floatSafeTensorsLoader(bytes: ByteArray) = SafeTensorsParametersLoader(
            sourceProvider = { ByteArrayRandomAccessSource(bytes) },
            tensorFilter = { it.dtype in FLOAT_DTYPES },
        )

        /**
         * Loads a model and builds a ready-to-use engine. Heavy work (weight
         * materialization, name mapping) runs on [SkaiNetEngineConfig.dispatcher],
         * never blocking the caller's thread.
         */
        public suspend fun create(
            model: ModelSource,
            vocabText: String,
            config: SkaiNetEngineConfig = SkaiNetEngineConfig(),
        ): SkaiNetEmbeddingEngine = withContext(config.dispatcher) {
            val ctx = PooledExecutionContext(DirectCpuExecutionContext())
            val bertConfig = when (model) {
                is ModelSource.SafeTensors -> BertConfigParser.parse(model.configJson, model.denseConfigJson)
                is ModelSource.Gguf -> BertConfigParser.parse(model.configJson)
            }
            val pooling = BertConfigParser.parsePooling(model.poolingConfigJson)

            val loaders = buildList<ParametersLoader> {
                when (model) {
                    is ModelSource.SafeTensors -> {
                        add(floatSafeTensorsLoader(model.model))
                        model.denseProjection?.let { dense -> add(floatSafeTensorsLoader(dense)) }
                    }
                    is ModelSource.Gguf ->
                        add(StreamingGgufParametersLoader(sourceProvider = { ByteArrayRandomAccessSource(model.model) }))
                }
            }

            val loaded = BertNetworkLoader.loadWeightTensors(loaders, ctx, FP32::class)
            val tensors = when (model) {
                is ModelSource.SafeTensors -> loaded
                is ModelSource.Gguf -> transposeGgufWeightTensors(loaded, ctx, config.keepQuantizedWeightsPacked)
            }
            val runtime = createBertEncoderRuntime<FP32>(
                config = bertConfig,
                tensors = tensors,
                ctx = ctx,
                resolver = if (model is ModelSource.Gguf) {
                    BertGgufNameResolver(tensors.map { it.name }.toSet())
                } else {
                    BertSafeTensorsNameResolver()
                },
                mode = config.executionMode,
                pooling = pooling,
            )
            SkaiNetEmbeddingEngine(runtime, TextFrontEnds.fromVocabTxt(vocabText), config)
        }
    }
}

/**
 * Tokenizes like HuggingFace does for uncased BERT (normalization, WordPiece,
 * [CLS]/[SEP]) and truncates to [maxSeqLen] tokens.
 */
internal fun WordPieceTokenizer.encodeForBert(text: String, maxSeqLen: Int): Encoding =
    encodeWithMetadata(text, truncation = Truncation.Longest(maxSeqLen))
