package org.shad.adman.vectora.engine.embedding

import org.shad.adman.vectora.core.embedding.EmbeddingEngine
import org.shad.adman.vectora.core.model.Vector
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
 */
class KFliteEmbeddingEngine(
    private val preprocessor: Preprocessor<String, ModelInput>,
    private val runtime: ModelRuntime,
    private val postprocessor: Postprocessor<ModelOutput, Vector>
) : EmbeddingEngine, AutoCloseable {

    companion object {
        fun createMiniLM(): KFliteEmbeddingEngine {
            val modelBytes = ModelLoader.loadModel()
            val vocab = ModelLoader.loadVocab()
            return createMiniLM(modelBytes, vocab)
        }

        fun createMiniLM(modelBytes: ByteArray): KFliteEmbeddingEngine {
            val vocab = ModelLoader.loadVocab()
            return createMiniLM(modelBytes, vocab)
        }

        fun createMiniLM(modelBytes: ByteArray, vocab: List<String>): KFliteEmbeddingEngine {
            val basePreprocessor = MiniLMPreprocessor(vocab)
            val preprocessor = TextCleaningPreprocessor(basePreprocessor)
            val postprocessor = MiniLMPostprocessor()
            // Using indices 0 and 1 for inputs and 0 for output as per signature.
            // Adjust if model metadata differs.
            val outputConfig = mapOf(0 to intArrayOf(1, 384))
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
        return texts.map { embed(it) }
    }

    override fun close() {
        runtime.close()
    }
}
