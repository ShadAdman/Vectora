package org.shad.adman.vectora.engine.skainet

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import java.nio.file.Path
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * `embed(texts)` runs padded batches; padding must not change any embedding.
 * Texts of very different lengths share a batch here on purpose, and the
 * result order must match the input order.
 */
class BatchParityTest {

    private val modelDir: Path =
        Path.of(System.getProperty("user.home"), ".cache", "vectora-tests", "all-MiniLM-L6-v2")

    private fun resource(name: String): String =
        checkNotNull(javaClass.classLoader.getResourceAsStream(name)) { "missing test resource $name" }
            .readBytes().decodeToString()

    @Test
    fun batchedMatchesSingle() = runTest(timeout = kotlin.time.Duration.parse("10m")) {
        if (!Files.exists(modelDir.resolve("model.safetensors"))) return@runTest
        val source = ModelSource.SafeTensors(
            model = Files.readAllBytes(modelDir.resolve("model.safetensors")),
            configJson = Files.readString(modelDir.resolve("config.json")),
        )
        val texts = listOf(
            "headphones",
            "The quick brown fox jumps over the lazy dog, then keeps running through the forest " +
                "until it reaches the river where the old mill used to stand many years ago.",
            "on-device semantic search",
            "Grüße aus München!",
            "a",
            "wireless noise cancelling headphones with thirty hours of battery life",
        )
        val engine = SkaiNetEmbeddingEngine.create(source, resource("vocab.txt"), SkaiNetEngineConfig(batchSize = 4))
        engine.use {
            val batched = engine.embed(texts)
            assertEquals(texts.size, batched.size)
            texts.forEachIndexed { i, text ->
                val single = engine.embed(text).values
                var maxDiff = 0f
                for (d in single.indices) maxDiff = maxOf(maxDiff, abs(single[d] - batched[i].values[d]))
                assertTrue(maxDiff <= 1e-4f, "batched vs single diverge (maxDiff=$maxDiff) for: $text")
            }
        }
    }
}
