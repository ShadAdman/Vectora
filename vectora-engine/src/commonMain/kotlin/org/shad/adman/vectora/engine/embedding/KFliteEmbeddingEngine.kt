package org.shad.adman.vectora.engine.embedding

import org.shad.adman.vectora.core.embedding.EmbeddingEngine
import org.shad.adman.vectora.core.model.Vector
import org.shad.adman.vectora.engine.Res
import org.shad.adman.vectora.engine.kflite.LiteRTRuntime
import org.shad.adman.vectora.engine.model.ModelInput
import org.shad.adman.vectora.engine.model.ModelOutput
import org.shad.adman.vectora.engine.model.ModelRuntime
import org.shad.adman.vectora.engine.postprocessor.MiniLMPostprocessor
import org.shad.adman.vectora.engine.postprocessor.Postprocessor
import org.shad.adman.vectora.engine.preprocessor.MiniLMPreprocessor
import org.shad.adman.vectora.engine.preprocessor.Preprocessor
import org.shad.adman.vectora.engine.preprocessor.TextCleaningPreprocessor

/**
 * Implementation of EmbeddingEngine using KFlite.
 *
 * It follows the pipeline:
 * String -> Preprocessor -> ModelInput -> KFlite -> ModelOutput -> Postprocessor -> Vector
 */
class KFliteEmbeddingEngine(
    private val preprocessor: Preprocessor<String, ModelInput>,
    private val runtime: ModelRuntime,
    private val postprocessor: Postprocessor<ModelOutput, Vector>
) : EmbeddingEngine, AutoCloseable {

    companion object {
        /**
         * Factory method to create a KFliteEmbeddingEngine configured for the all-MiniLM-L6-v2 model.
         * Automatically loads the model from resources.
         */
        suspend fun createMiniLM(): KFliteEmbeddingEngine {

            val modelBytes = Res.readBytes("files/all_minilm.tflite")
            return createMiniLM(modelBytes)
        }

        /**
         * Factory method to create a KFliteEmbeddingEngine configured for the all-MiniLM-L6-v2 model.
         */
        fun createMiniLM(modelBytes: ByteArray): KFliteEmbeddingEngine {
            val basePreprocessor = MiniLMPreprocessor()
            val preprocessor = TextCleaningPreprocessor(basePreprocessor)
            val postprocessor = MiniLMPostprocessor()
            // Output tensor 701 has shape [-1, 384]. We assume batch size 1 for single embedding.
            val outputConfig = mapOf(701 to intArrayOf(1, 384))
            val runtime = LiteRTRuntime(modelBytes, outputConfig)
            return KFliteEmbeddingEngine(preprocessor, runtime, postprocessor)
        }
    }

    override suspend fun embed(text: String): Vector {
        val modelInput = preprocessor.process(text)
        val modelOutput = runtime.run(modelInput)
        return postprocessor.process(modelOutput)
    }

    override suspend fun embed(texts: List<String>): List<Vector> {
        // Ensuring output ordering matches input ordering.
        // For models supporting batching, this could be optimized by
        // implementing a batched version of preprocessor and runtime.
        return texts.map { embed(it) }
    }

    override fun close() {
        runtime.close()
    }
}