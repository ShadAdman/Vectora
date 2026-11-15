package org.shad.adman.vectora.engine.skainet

import kotlinx.coroutines.test.runTest
import sk.ainet.models.bert.BertExecutionMode
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * DIRECT (eager) and OPTIMIZED (traced + fused graph) must produce identical
 * embeddings. Note: sequence-length bucketing is deliberately NOT used —
 * BertEncoderRuntime applies the attention mask to pooling only, so padded
 * tokens would leak into self-attention and change the embedding. OPTIMIZED
 * re-traces per sequence length behind an upstream LRU cache instead.
 */
class ExecutionModeParityTest {

    private val modelDir: Path =
        Path.of(System.getProperty("user.home"), ".cache", "vectora-tests", "all-MiniLM-L6-v2")

    private fun resource(name: String): String =
        checkNotNull(javaClass.classLoader.getResourceAsStream(name)) { "missing test resource $name" }
            .readBytes().decodeToString()

    @Test
    fun optimizedMatchesDirect() = runTest(timeout = kotlin.time.Duration.parse("10m")) {
        if (!Files.exists(modelDir.resolve("model.safetensors"))) return@runTest
        val source = ModelSource.SafeTensors(
            model = Files.readAllBytes(modelDir.resolve("model.safetensors")),
            configJson = Files.readString(modelDir.resolve("config.json")),
        )
        val vocab = resource("vocab.txt")
        val texts = listOf(
            "wireless noise cancelling headphones",
            "The quick brown fox jumps over the lazy dog.",
            "on-device semantic search with SKaiNET",
        )
        val direct = SkaiNetEmbeddingEngine.create(
            source, vocab, SkaiNetEngineConfig(executionMode = BertExecutionMode.DIRECT))
        val optimized = SkaiNetEmbeddingEngine.create(
            source, vocab, SkaiNetEngineConfig(executionMode = BertExecutionMode.OPTIMIZED))
        direct.use {
            optimized.use {
                for (text in texts) {
                    val a = direct.embed(text).values
                    val b = optimized.embed(text).values
                    var maxDiff = 0f
                    for (i in a.indices) {
                        val d = kotlin.math.abs(a[i] - b[i])
                        if (d > maxDiff) maxDiff = d
                    }
                    assertTrue(maxDiff <= 1e-5f, "DIRECT vs OPTIMIZED diverge (maxDiff=$maxDiff) for: $text")
                }
            }
        }
    }
}
