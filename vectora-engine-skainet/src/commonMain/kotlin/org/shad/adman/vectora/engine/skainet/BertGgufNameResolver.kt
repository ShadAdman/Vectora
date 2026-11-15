package org.shad.adman.vectora.engine.skainet

import sk.ainet.io.weights.WeightNameResolver

/**
 * Maps the BERT DSL's module-tree paths to llama.cpp's GGUF-BERT tensor
 * names, so `createBertEncoderRuntime(resolver = BertGgufNameResolver(tensorNames))`
 * can load a GGUF checkpoint the same way it loads safetensors.
 *
 * GGUF-BERT tensor naming is NOT standardized across converters — verified
 * directly against two independent conversions of the same model:
 *   `leliuga/all-MiniLM-L6-v2-GGUF`  -> token_embd_norm / attn_output_norm / layer_output_norm
 *   `caliex/all-MiniLM-L6-v2-f16.gguf` -> output_norm / attn_norm / ffn_norm
 * Everything except the three norm-layer names agrees between them. [tensorNames]
 * is queried (with [candidates] as fallback aliases) so both variants resolve
 * without guessing which convention a given file uses.
 *
 * DSL module path -> GGUF tensor name
 *   MLP/embeddings/word_embeddings        (word_embeddings.weight) -> token_embd.weight
 *   MLP/embeddings                        (position_embeddings.weight) -> position_embd.weight
 *   MLP/embeddings                        (token_type_embeddings.weight) -> token_types.weight
 *   MLP/embeddings/LayerNorm              (LayerNorm.weight/bias) -> token_embd_norm.{w,b} | output_norm.{w,b}
 *   MLP/encoder.layer.N.attn/attention    (attention.{q,k,v,o}_proj.weight/bias) -> blk.N.attn_{q,k,v,output}.{w,b}
 *   MLP/encoder.layer.N.attn/attn_ln      (attn_ln.weight/bias) -> blk.N.attn_output_norm.{w,b} | blk.N.attn_norm.{w,b}
 *   MLP/encoder.layer.N.ffn/intermediate  (intermediate.weight/bias) -> blk.N.ffn_up.{w,b}
 *   MLP/encoder.layer.N.ffn/output        (output.weight/bias) -> blk.N.ffn_down.{w,b}
 *   MLP/encoder.layer.N.ffn/output_ln     (output_ln.weight/bias) -> blk.N.layer_output_norm.{w,b} | blk.N.ffn_norm.{w,b}
 *
 * The returned name carries the "bert." prefix `BertNetworkLoader.normalizeTensorNames`
 * adds to every loaded tensor (GGUF names never start with "bert." on their own,
 * so normalization always fires).
 *
 * Every 2D tensor above is stored by GGUF with dimensions in reverse (`ne[]`)
 * order relative to what this resolver's names get matched against — a shape
 * relabel only, not a data permutation. See [transposeGgufWeightTensors].
 */
internal class BertGgufNameResolver(private val tensorNames: Set<String>) : WeightNameResolver {

    private val layerRegex = Regex("""encoder\.layer\.(\d+)\.(?:attn|ffn)""")

    /** First candidate present in [tensorNames] (bert.-prefixed), or the first as a last resort. */
    private fun candidates(vararg names: String): String? {
        val prefixed = names.map { "bert.$it" }
        return prefixed.firstOrNull { it in tensorNames } ?: prefixed.firstOrNull()
    }

    override fun resolve(modulePath: String, paramName: String): String? {
        val blk = layerRegex.find(modulePath)?.let { "blk.${it.groupValues[1]}" }
        val w = paramName.endsWith(".weight")

        return when {
            modulePath.endsWith("/word_embeddings") -> candidates("token_embd.weight")
            paramName == "position_embeddings.weight" -> candidates("position_embd.weight")
            paramName == "token_type_embeddings.weight" -> candidates("token_types.weight")
            modulePath.endsWith("/LayerNorm") ->
                if (w) candidates("token_embd_norm.weight", "output_norm.weight")
                else candidates("token_embd_norm.bias", "output_norm.bias")

            blk == null -> null

            paramName.endsWith("q_proj.weight") -> candidates("$blk.attn_q.weight")
            paramName.endsWith("q_proj.bias") -> candidates("$blk.attn_q.bias")
            paramName.endsWith("k_proj.weight") -> candidates("$blk.attn_k.weight")
            paramName.endsWith("k_proj.bias") -> candidates("$blk.attn_k.bias")
            paramName.endsWith("v_proj.weight") -> candidates("$blk.attn_v.weight")
            paramName.endsWith("v_proj.bias") -> candidates("$blk.attn_v.bias")
            paramName.endsWith("o_proj.weight") -> candidates("$blk.attn_output.weight")
            paramName.endsWith("o_proj.bias") -> candidates("$blk.attn_output.bias")

            modulePath.endsWith("/attn_ln") ->
                if (w) candidates("$blk.attn_output_norm.weight", "$blk.attn_norm.weight")
                else candidates("$blk.attn_output_norm.bias", "$blk.attn_norm.bias")
            modulePath.endsWith("/intermediate") ->
                if (w) candidates("$blk.ffn_up.weight") else candidates("$blk.ffn_up.bias")
            // Order matters: "/output_ln" must not be caught by an "/output" check.
            modulePath.endsWith("/output_ln") ->
                if (w) candidates("$blk.layer_output_norm.weight", "$blk.ffn_norm.weight")
                else candidates("$blk.layer_output_norm.bias", "$blk.ffn_norm.bias")
            modulePath.endsWith("/output") ->
                if (w) candidates("$blk.ffn_down.weight") else candidates("$blk.ffn_down.bias")

            else -> null
        }
    }
}
