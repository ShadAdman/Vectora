package org.shad.adman.vectora.engine.skainet

import sk.ainet.context.ExecutionContext
import sk.ainet.io.weights.WeightTensor
import sk.ainet.lang.tensor.Shape
import sk.ainet.lang.tensor.data.Q8_0BlockTensorData
import sk.ainet.lang.tensor.data.TensorData
import sk.ainet.lang.tensor.data.toFloatArray
import sk.ainet.lang.types.FP32

/**
 * Adapts GGUF-BERT weights to the layout SKaiNET's BERT network expects.
 *
 * GGUF's `ne[]` reports every 2D tensor's dimensions in reverse order
 * relative to the safetensors/numpy convention (`ffn_up.weight` reads as
 * `[384, 1536]` where HF stores `[1536, 384]`). The bytes are already
 * row-major for the HF shape, so only the shape label is reversed; 1D
 * tensors (biases, LayerNorm) need no change.
 *
 * Q8_0 weights: with [keepPacked] the encoder's matmul weights stay in their
 * packed 8-bit blocks (about a quarter of the FP32 size) and run through
 * SKaiNET's quantized matmul kernels. Embedding tables are always expanded
 * to FP32, because the embedding lookup (`gather`) cannot read packed blocks
 * as of SKaiNET 0.64.0. Without [keepPacked] every weight is expanded, which
 * is faster on the JVM but uses as much memory as an FP32 model.
 */
internal fun transposeGgufWeightTensors(
    tensors: List<WeightTensor<FP32, Float>>,
    ctx: ExecutionContext,
    keepPacked: Boolean,
): List<WeightTensor<FP32, Float>> = tensors.map { wt ->
    if (wt.shape.size != 2) return@map wt
    val (d0, d1) = wt.shape[0] to wt.shape[1]
    val data = wt.tensor.data
    val newTensor = if (keepPacked && data is Q8_0BlockTensorData && !wt.isEmbeddingTable()) {
        @Suppress("UNCHECKED_CAST")
        val packed = Q8_0BlockTensorData(Shape(d1, d0), data.packedData) as TensorData<FP32, Float>
        ctx.fromData(packed, FP32::class)
    } else {
        val floats = when (data) {
            is Q8_0BlockTensorData -> data.toFloatArray()
            else -> data.copyToFloatArray()
        }
        ctx.fromFloatArray<FP32, Float>(Shape(d1, d0), FP32::class, floats)
    }
    wt.copy(shape = listOf(d1, d0), tensor = newTensor)
}

/** llama.cpp GGUF-BERT names: `token_embd`, `position_embd`, `token_types`. */
private fun WeightTensor<FP32, Float>.isEmbeddingTable(): Boolean =
    name.contains("embd") || name.contains("token_types")
