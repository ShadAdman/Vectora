package org.shad.adman.vectora.engine.skainet

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private val FLOAT_DTYPES = setOf("F64", "F32", "F16", "BF16")

/**
 * Drops non-float tensors (e.g. the I64 `embeddings.position_ids` buffer in
 * HuggingFace BERT checkpoints) from a safetensors header. SKaiNET's
 * SafeTensorsParametersLoader loads a file with one target dtype and fails on
 * mixed integer buffers, and the BERT DSL derives positions arithmetically so
 * these tensors are dead weight anyway.
 *
 * The rewritten header is space-padded to its original byte length (allowed by
 * the safetensors spec), so tensor data offsets stay valid and the payload is
 * untouched.
 */
internal fun stripNonFloatTensors(safetensors: ByteArray): ByteArray {
    require(safetensors.size >= 8) { "not a safetensors file (${safetensors.size} bytes)" }
    var headerLen = 0L
    for (i in 7 downTo 0) headerLen = (headerLen shl 8) or (safetensors[i].toLong() and 0xFF)
    require(headerLen > 0 && headerLen <= safetensors.size - 8) { "corrupt safetensors header length $headerLen" }

    val headerBytes = safetensors.copyOfRange(8, 8 + headerLen.toInt())
    val header = Json.parseToJsonElement(headerBytes.decodeToString()).jsonObject
    val kept = header.filter { (name, meta) ->
        name == "__metadata__" ||
            meta.jsonObject["dtype"]?.jsonPrimitive?.content in FLOAT_DTYPES
    }
    if (kept.size == header.size) return safetensors

    val newHeader = Json.encodeToString(JsonObject.serializer(), JsonObject(kept)).encodeToByteArray()
    require(newHeader.size <= headerLen) { "sanitized header grew unexpectedly" }

    val result = safetensors.copyOf()
    newHeader.copyInto(result, 8)
    for (i in 8 + newHeader.size until 8 + headerLen.toInt()) result[i] = ' '.code.toByte()
    return result
}
