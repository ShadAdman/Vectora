package org.shad.adman.vectora.engine.skainet

import kotlinx.coroutines.test.runTest
import java.io.File
import java.lang.management.ManagementFactory
import java.lang.management.MemoryType
import java.nio.file.Files
import java.nio.file.Path
import kotlin.random.Random
import kotlin.test.Test

/**
 * Opt-in benchmark, not a correctness test. Measures one model format per JVM
 * so peak-memory numbers are not mixed between formats:
 *
 * ```
 * VECTORA_BENCH=fp32 ./gradlew :vectora-engine-skainet:jvmTest --tests '*EmbeddingBenchmark*' --rerun
 * ```
 *
 * Optional `VECTORA_BENCH_MODE=OPTIMIZED` selects the execution mode.
 * `VECTORA_BENCH_PACKED=false` expands Q8_0 weights to FP32 at load.
 * Formats: `fp32`, `fp16` (safetensors), `q8_0`, `f16` (GGUF). Needs the model
 * files in ~/.cache/vectora-tests/all-MiniLM-L6-v2/. Prints one
 * `VECTORA_BENCH {json}` line and writes build/bench/<format>.json.
 */
class EmbeddingBenchmark {

    private val modelDir: Path =
        Path.of(System.getProperty("user.home"), ".cache", "vectora-tests", "all-MiniLM-L6-v2")

    private fun resource(name: String): String =
        checkNotNull(javaClass.classLoader.getResourceAsStream(name)) { "missing test resource $name" }
            .readBytes().decodeToString()

    @Test
    fun run() = runTest(timeout = kotlin.time.Duration.parse("30m")) {
        val format = System.getenv("VECTORA_BENCH") ?: return@runTest
        val fileName = when (format) {
            "fp32" -> "model.safetensors"
            "fp16" -> "model.fp16.safetensors"
            "q8_0" -> "model.q8_0.gguf"
            "f16" -> "model.f16.gguf"
            else -> error("unknown VECTORA_BENCH format: $format")
        }
        val modelFile = modelDir.resolve(fileName)
        check(Files.exists(modelFile)) { "missing $modelFile" }
        val vocab = resource("vocab.txt")
        val configJson = Files.readString(modelDir.resolve("config.json"))
        val texts = corpus(TEXT_COUNT)

        System.gc()
        val heapBeforeMb = usedHeapMb()
        val rssBeforeMb = rssMb("VmRSS")

        val loadStart = System.nanoTime()
        val engine = run {
            val bytes = Files.readAllBytes(modelFile)
            val source = if (fileName.endsWith(".gguf")) {
                ModelSource.Gguf(bytes, configJson)
            } else {
                ModelSource.SafeTensors(bytes, configJson)
            }
            val mode = System.getenv("VECTORA_BENCH_MODE")
                ?.let { sk.ainet.models.bert.BertExecutionMode.valueOf(it) }
                ?: sk.ainet.models.bert.BertExecutionMode.DIRECT
            SkaiNetEmbeddingEngine.create(source, vocab, SkaiNetEngineConfig(
                    executionMode = mode,
                    keepQuantizedWeightsPacked = System.getenv("VECTORA_BENCH_PACKED") != "false",
                ))
        }
        val loadMs = (System.nanoTime() - loadStart) / 1e6
        val peakHeapAfterLoadMb = peakHeapMb()
        val peakRssAfterLoadMb = rssMb("VmHWM")

        engine.use {
            val firstStart = System.nanoTime()
            engine.embed(texts[0])
            val firstEmbedMs = (System.nanoTime() - firstStart) / 1e6

            repeat(WARMUP) { engine.embed(texts[it]) }

            val latencies = DoubleArray(LATENCY_SAMPLES) {
                val start = System.nanoTime()
                engine.embed(texts[it])
                (System.nanoTime() - start) / 1e6
            }.also { it.sort() }

            val batchStart = System.nanoTime()
            val vectors = engine.embed(texts)
            val batchSeconds = (System.nanoTime() - batchStart) / 1e9
            check(vectors.size == texts.size)

            System.gc()
            val steadyHeapMb = usedHeapMb()

            val json = buildString {
                append("{")
                append("\"format\":\"$format\",")
                append("\"modelFileMb\":${fmt(Files.size(modelFile) / MB)},")
                append("\"loadMs\":${fmt(loadMs)},")
                append("\"firstEmbedMs\":${fmt(firstEmbedMs)},")
                append("\"latencyP50Ms\":${fmt(latencies[latencies.size / 2])},")
                append("\"latencyP95Ms\":${fmt(latencies[(latencies.size * 95) / 100])},")
                append("\"batchTexts\":${texts.size},")
                append("\"batchTextsPerSec\":${fmt(texts.size / batchSeconds)},")
                append("\"heapBeforeMb\":${fmt(heapBeforeMb)},")
                append("\"peakHeapAfterLoadMb\":${fmt(peakHeapAfterLoadMb)},")
                append("\"peakHeapMb\":${fmt(peakHeapMb())},")
                append("\"steadyHeapMb\":${fmt(steadyHeapMb)},")
                append("\"rssBeforeMb\":${fmt(rssBeforeMb)},")
                append("\"peakRssAfterLoadMb\":${fmt(peakRssAfterLoadMb)},")
                append("\"peakRssMb\":${fmt(rssMb("VmHWM"))},")
                append("\"maxHeapMb\":${fmt(Runtime.getRuntime().maxMemory() / MB)},")
                append("\"processors\":${Runtime.getRuntime().availableProcessors()}")
                append("}")
            }
            println("VECTORA_BENCH $json")
            File("build/bench").apply { mkdirs() }.resolve("$format.json").writeText(json)
        }
    }

    /** Deterministic corpus with sentence lengths from 4 to 40 words. */
    private fun corpus(count: Int): List<String> {
        val random = Random(42)
        return List(count) {
            List(random.nextInt(4, 41)) { WORDS[random.nextInt(WORDS.size)] }.joinToString(" ")
        }
    }

    private fun usedHeapMb(): Double =
        ManagementFactory.getMemoryMXBean().heapMemoryUsage.used / MB

    private fun peakHeapMb(): Double =
        ManagementFactory.getMemoryPoolMXBeans()
            .filter { it.type == MemoryType.HEAP }
            .sumOf { it.peakUsage.used } / MB

    /** Reads a kB field from /proc/self/status (Linux); -1 elsewhere. */
    private fun rssMb(field: String): Double {
        val status = File("/proc/self/status")
        if (!status.exists()) return -1.0
        val line = status.readLines().firstOrNull { it.startsWith("$field:") } ?: return -1.0
        return line.removePrefix("$field:").trim().removeSuffix("kB").trim().toDouble() / 1024.0
    }

    private fun fmt(value: Double): String = String.format(java.util.Locale.ROOT, "%.2f", value)

    private companion object {
        const val MB = 1024.0 * 1024.0
        const val TEXT_COUNT = 200
        const val WARMUP = 10
        const val LATENCY_SAMPLES = 50
        val WORDS = (
            "wireless noise cancelling headphones battery life semantic search on device vector " +
                "embedding model quantized memory fast kotlin multiplatform android phone laptop " +
                "the a of and with for from quick brown fox jumps over lazy dog bread eating man " +
                "woman child city river mountain coffee music running shoes waterproof jacket " +
                "price review delivery return warranty screen camera keyboard charger cable"
            ).split(" ")
    }
}
