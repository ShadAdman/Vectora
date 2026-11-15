package org.shad.adman.vectora.engine.skainet

import sk.ainet.context.ExecutionContext
import sk.ainet.io.weights.WeightTensor
import sk.ainet.lang.tensor.Shape
import sk.ainet.lang.tensor.data.Q8_0BlockTensorData
import sk.ainet.lang.tensor.data.toFloatArray
import sk.ainet.lang.types.DType
import sk.ainet.lang.types.FP32

/**
 * GGUF's `ne[]` reports every 2D tensor's dimensions in reverse order
 * relative to safetensors/numpy convention — `ffn_up.weight` reads as
 * `[384, 1536]` where HF stores the same weight as `[1536, 384]`. This is
 * NOT a data permutation: the underlying byte buffer is already laid out
 * row-major matching the HF-shaped interpretation (verified directly by
 * dequantizing raw GGUF tensors and comparing against the fp32 safetensors
 * reference row-for-row, cosine > 0.9999 with the shape simply relabeled).
 * Reversing the shape label while keeping the flat array untouched is the
 * whole fix; 1D tensors (biases, LayerNorm weight/bias) need no change.
 *
 * `Q8_0BlockTensorData`'s native value type is `Byte` (raw quantized codes,
 * per `Q8_0TensorData : TensorData<DType, Byte>`) — its generic
 * `copyToFloatArray()`/`get()` correctly return those raw codes, NOT
 * dequantized floats (confirmed by reading skainet-lang-core's
 * Q8_0TensorData.kt directly). The real dequantizer is the
 * `Q8_0TensorData.toFloatArray()` extension (`code * blockScale`), used here.
 *
 * A reshape-only path that keeps the tensor packed (same `packedData` bytes,
 * new [Shape], wired straight into the DSL — no dequant at all) would
 * preserve the JNI NEON packed-matmul speed path on Android, but crashes
 * under a plain `DirectCpuExecutionContext`: some kernel step there falls
 * back to a generic `Float`-typed `.get()`, and `Byte` cannot be cast to
 * `Float` (ClassCastException, confirmed). Dequantizing up front is correct
 * everywhere; keeping the packed path Android-only is a follow-up that
 * needs on-device validation of `skainet-backend-jni-cpu`'s kernel dispatch.
 */
internal fun transposeGgufWeightTensors(
    tensors: List<WeightTensor<FP32, Float>>,
    ctx: ExecutionContext,
): List<WeightTensor<FP32, Float>> = tensors.map { wt ->
    if (wt.shape.size != 2) return@map wt
    val (d0, d1) = wt.shape[0] to wt.shape[1]
    val floats = when (val data = wt.tensor.data) {
        is Q8_0BlockTensorData -> data.toFloatArray()
        else -> data.copyToFloatArray()
    }
    val newTensor = ctx.fromFloatArray<FP32, Float>(Shape(d1, d0), FP32::class, floats)
    wt.copy(shape = listOf(d1, d0), tensor = newTensor)
}
