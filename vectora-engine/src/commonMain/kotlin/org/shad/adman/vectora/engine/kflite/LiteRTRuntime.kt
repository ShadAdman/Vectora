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
            Kflite.init(modelBytes, options = InterpreterOptions(runtime = RuntimeType.TFLITE))
        } catch (e: Exception) {
            throw ModelLoadException("Failed to load model: ${e.message}", e)
        }
    }

    override fun run(input: ModelInput): ModelOutput {
        try {
            // LiteRT requires pre-allocated output buffers.
            val outputs = outputConfig.mapValues { (_, shape) ->
                when (shape.size) {
                    1 -> FloatArray(shape[0])
                    2 -> Array(shape[0]) { FloatArray(shape[1]) }
                    else -> error("Unsupported output tensor rank: ${shape.size}")
                }
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
