package org.shad.adman.vectora.engine.skainet

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import java.nio.file.Path
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * GGUF quality gate: quantized/narrow-float GGUF models must stay close to
 * the fp32 safetensors engine (cosine >= 0.99 per sentence), through
 * [BertGgufNameResolver] + [transposeGgufWeightTensors]. Covers both a
 * packed-block format (Q8_0, via community `leliuga/all-MiniLM-L6-v2-GGUF`)
 * and a dense narrow-float format (F16, via `caliex/all-MiniLM-L6-v2-f16.gguf`)
 * — the two exercise different code paths (Q8_0BlockTensorData reshape vs.
 * plain dequant-to-fp32).
 */
class GgufSmokeTest {

    private val modelDir: Path =
        Path.of(System.getProperty("user.home"), ".cache", "vectora-tests", "all-MiniLM-L6-v2")

    private fun resource(name: String): String =
        checkNotNull(javaClass.classLoader.getResourceAsStream(name)) { "missing test resource $name" }
            .readBytes().decodeToString()

    private suspend fun assertGgufTracksFp32(ggufFileName: String) {
        val gguf = modelDir.resolve(ggufFileName)
        if (!Files.exists(gguf) || !Files.exists(modelDir.resolve("model.safetensors"))) return
        val configJson = Files.readString(modelDir.resolve("config.json"))
        val vocab = resource("vocab.txt")

        val quantized = SkaiNetEmbeddingEngine.create(
            ModelSource.Gguf(Files.readAllBytes(gguf), configJson), vocab)
        val fp32 = SkaiNetEmbeddingEngine.create(
            ModelSource.SafeTensors(Files.readAllBytes(modelDir.resolve("model.safetensors")), configJson), vocab)

        quantized.use {
            fp32.use {
                for (text in listOf(
                    "wireless noise cancelling headphones",
                    "A man is eating a piece of bread.",
                    "on-device semantic search",
                )) {
                    val a = quantized.embed(text).values
                    val b = fp32.embed(text).values
                    var dot = 0f; var na = 0f; var nb = 0f
                    for (i in a.indices) { dot += a[i] * b[i]; na += a[i] * a[i]; nb += b[i] * b[i] }
                    val cos = dot / (sqrt(na) * sqrt(nb))
                    assertTrue(cos >= 0.99f, "$ggufFileName vs fp32 cosine $cos < 0.99 for: $text")
                }
            }
        }
    }

    @Test
    fun q8_0GgufTracksFp32SafeTensors() = runTest(timeout = kotlin.time.Duration.parse("10m")) {
        assertGgufTracksFp32("model.q8_0.gguf")
    }

    @Test
    fun f16GgufTracksFp32SafeTensors() = runTest(timeout = kotlin.time.Duration.parse("10m")) {
        assertGgufTracksFp32("model.f16.gguf")
    }
}
