package org.shad.adman.vectora.engine.skainet

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Serializable
private data class ReferenceCase(val text: String, val vector: List<Float>)

/**
 * Parity against Python sentence-transformers (`normalize_embeddings=True`)
 * for all-MiniLM-L6-v2, plus the instance-lifecycle guarantees the KFlite
 * singleton could not provide.
 *
 * Needs the model cached at ~/.cache/vectora-tests/all-MiniLM-L6-v2/
 * (model.safetensors + config.json; see TASKS.md M3) — tests no-op when absent.
 */
class EmbeddingParityTest {

    private val modelDir: Path =
        Path.of(System.getProperty("user.home"), ".cache", "vectora-tests", "all-MiniLM-L6-v2")

    private fun modelAvailable() = Files.exists(modelDir.resolve("model.safetensors"))

    private fun resource(name: String): String =
        checkNotNull(javaClass.classLoader.getResourceAsStream(name)) { "missing test resource $name" }
            .readBytes().decodeToString()

    private fun loadModelSource() = ModelSource.SafeTensors(
        model = Files.readAllBytes(modelDir.resolve("model.safetensors")),
        configJson = Files.readString(modelDir.resolve("config.json")),
        poolingConfigJson = modelDir.resolve("pooling_config.json")
            .takeIf { Files.exists(it) }?.let { Files.readString(it) },
    )

    private suspend fun createEngine() =
        SkaiNetEmbeddingEngine.create(loadModelSource(), resource("vocab.txt"))

    @Test
    fun matchesPythonSentenceTransformers() = runTest(timeout = kotlin.time.Duration.parse("10m")) {
        if (!modelAvailable()) return@runTest
        val fixture = javaClass.classLoader.getResourceAsStream("embedding_reference.json") ?: return@runTest
        val references = Json.decodeFromString<List<ReferenceCase>>(fixture.readBytes().decodeToString())
        val engine = createEngine()
        engine.use {
            assertEquals(384, engine.dimensions)
            for (case in references) {
                val actual = engine.embed(case.text).values
                assertEquals(case.vector.size, actual.size)
                val cos = cosine(actual, case.vector.toFloatArray())
                assertTrue(cos >= 0.999f, "cosine $cos < 0.999 for: ${case.text}")
                val norm = sqrt(actual.sumOf { (it * it).toDouble() })
                assertTrue(abs(norm - 1.0) < 1e-3, "output not L2-normalized (|v|=$norm)")
            }
        }
    }

    @Test
    fun twoEnginesCoexistAndCloseIndependently() = runTest(timeout = kotlin.time.Duration.parse("10m")) {
        if (!modelAvailable()) return@runTest
        val first = createEngine()
        val second = createEngine()
        val before = second.embed("independent lifecycles").values
        first.close()
        // The exact scenario the KFlite global singleton fails: closing one
        // engine must leave the other fully functional.
        val after = second.embed("independent lifecycles").values
        assertTrue(cosine(before, after) > 0.9999f)
        second.close()
    }

    private fun cosine(a: FloatArray, b: FloatArray): Float {
        var dot = 0f
        var na = 0f
        var nb = 0f
        for (i in a.indices) {
            dot += a[i] * b[i]
            na += a[i] * a[i]
            nb += b[i] * b[i]
        }
        return dot / (sqrt(na) * sqrt(nb))
    }
}
