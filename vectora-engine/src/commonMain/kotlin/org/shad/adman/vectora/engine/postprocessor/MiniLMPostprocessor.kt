package org.shad.adman.vectora.engine.postprocessor

import org.shad.adman.vectora.core.model.Vector
import org.shad.adman.vectora.engine.model.ModelOutput

/**
 * Postprocessor for the all-MiniLM-L6-v2 model.
 * Extracts the embedding from the model output.
 */
class MiniLMPostprocessor : Postprocessor<ModelOutput, Vector> {
    override fun process(output: ModelOutput): Vector {
        val tensor = output.tensors[701] as? FloatArray
            ?: throw IllegalArgumentException("Missing output tensor with identifier 701")
        return Vector(tensor)
    }
}