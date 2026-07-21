package org.shad.adman.vectora.engine.kflite

import org.kmp.playground.kflite.interpreter.InterpreterOptions
import org.kmp.playground.kflite.interpreter.RuntimeType
import org.kmp.playground.kflite.kflite.Kflite
import org.shad.adman.vectora.engine.exception.InferenceException
import org.shad.adman.vectora.engine.exception.ModelLoadException
import org.shad.adman.vectora.engine.model.ModelInput
import org.shad.adman.vectora.engine.model.ModelOutput
import org.shad.adman.vectora.engine.model.ModelRuntime

/**
 * LiteRT-based implementation of [ModelRuntime].
 */
class LiteRTRuntime(
    private val modelBytes: ByteArray,
    private val outputConfig: Map<Int, IntArray>,
) : ModelRuntime {

    init {
        try {
            Kflite.init(modelBytes, options = InterpreterOptions(runtime = RuntimeType.LITERT))
        } catch (e: Exception) {
            throw ModelLoadException("Failed to load model: ${e.message}", e)
        }
    }

    override fun run(input: ModelInput): ModelOutput {
        try {
            // LiteRT requires pre-allocated output buffers.
            val outputs = outputConfig.mapValues { (_, shape) ->
                val totalSize = shape.fold(1) { acc, i -> acc * i }
                FloatArray(totalSize)
            }

            Kflite.run(
                input.tensors.entries.sortedBy { it.key }.map { it.value },
                outputs
            )

            return ModelOutput(outputs)
        } catch (e: Exception) {
            throw InferenceException("Inference failed: ${e.message}", e)
        }
    }

    override fun close() {
        try {
            Kflite.close()
        } catch (e: Exception) {
            // Silently close
        }
    }
}
