package org.shad.adman.vectora.engine.skainet

/**
 * Weights + HuggingFace configuration for a BERT / sentence-transformers
 * checkpoint. The engine derives every model dimension from [configJson],
 * so any compatible checkpoint (all-MiniLM-L6-v2, MongoDB/mdbr-leaf-ir, …)
 * loads through the same code path.
 *
 * @property configJson the checkpoint's `config.json`
 * @property poolingConfigJson optional `1_Pooling/config.json` (defaults to MEAN pooling)
 */
public sealed class ModelSource {
    public abstract val configJson: String
    public abstract val poolingConfigJson: String?

    /**
     * HuggingFace safetensors checkpoint (`model.safetensors`).
     *
     * @property denseProjection optional `2_Dense/model.safetensors` —
     *   sentence-transformers projection head used by e.g. mdbr-leaf models
     * @property denseConfigJson optional `2_Dense/config.json` (declares the
     *   projection output dimension)
     */
    public class SafeTensors(
        public val model: ByteArray,
        override val configJson: String,
        public val denseProjection: ByteArray? = null,
        public val denseConfigJson: String? = null,
        override val poolingConfigJson: String? = null,
    ) : ModelSource()

    /** GGUF checkpoint (quantized weights stay packed where kernels allow). */
    public class Gguf(
        public val model: ByteArray,
        override val configJson: String,
        override val poolingConfigJson: String? = null,
    ) : ModelSource()
}
