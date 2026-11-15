package org.shad.adman.vectora.engine.skainet

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import java.nio.file.Path
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * FP16 safetensors (~45 MB, half the fp32 asset) is the size-mitigation path
 * while GGUF-BERT is blocked upstream (llama.cpp tensor naming + transposed
 * layouts don't map through BertSafeTensorsNameResolver). The loader
 * dequantizes F16 -> FP32 at load time; outputs must track fp32 closely.
 */
class Fp16SmokeTest {

    private val modelDir: Path =
        Path.of(System.getProperty("user.home"), ".cache", "vectora-tests", "all-MiniLM-L6-v2")

    private fun resource(name: String): String =
        checkNotNull(javaClass.classLoader.getResourceAsStream(name)) { "missing test resource $name" }
            .readBytes().decodeToString()

    @Test
    fun fp16TracksFp32() = runTest(timeout = kotlin.time.Duration.parse("10m")) {
        val fp16Path = modelDir.resolve("model.fp16.safetensors")
        if (!Files.exists(fp16Path) || !Files.exists(modelDir.resolve("model.safetensors"))) return@runTest
        val configJson = Files.readString(modelDir.resolve("config.json"))
        val vocab = resource("vocab.txt")

        val fp16 = SkaiNetEmbeddingEngine.create(
            ModelSource.SafeTensors(Files.readAllBytes(fp16Path), configJson), vocab)
        val fp32 = SkaiNetEmbeddingEngine.create(
            ModelSource.SafeTensors(Files.readAllBytes(modelDir.resolve("model.safetensors")), configJson), vocab)

        fp16.use {
            fp32.use {
                for (text in listOf(
                    "wireless noise cancelling headphones",
                    "A man is eating a piece of bread.",
                    "on-device semantic search",
                )) {
                    val a = fp16.embed(text).values
                    val b = fp32.embed(text).values
                    var dot = 0f; var na = 0f; var nb = 0f
                    for (i in a.indices) { dot += a[i] * b[i]; na += a[i] * a[i]; nb += b[i] * b[i] }
                    val cos = dot / (sqrt(na) * sqrt(nb))
                    assertTrue(cos >= 0.999f, "fp16 vs fp32 cosine $cos < 0.999 for: $text")
                }
            }
        }
    }
}
