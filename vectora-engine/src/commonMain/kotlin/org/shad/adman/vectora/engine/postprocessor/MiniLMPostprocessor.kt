package org.shad.adman.vectora.engine.postprocessor

import org.shad.adman.vectora.core.model.Vector
import org.shad.adman.vectora.engine.model.ModelOutput

/**
 * Postprocessor for the all-MiniLM-L6-v2 model.
 */
class MiniLMPostprocessor : Postprocessor<ModelOutput, Vector> {
    override fun process(output: ModelOutput): Vector {
        val tensor = output.tensors[0] as? Array<*>
            ?: throw IllegalArgumentException(
                "Missing output tensor with identifier 0"
            )

        val embedding = tensor[0] as? FloatArray
            ?: throw IllegalArgumentException(
                "Invalid output tensor format. Expected [1, 384] FloatArray."
            )

        return Vector(embedding)
    }
}
