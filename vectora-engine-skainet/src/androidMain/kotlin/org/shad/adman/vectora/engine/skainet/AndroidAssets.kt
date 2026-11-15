package org.shad.adman.vectora.engine.skainet

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Loads model files from APK assets. The engine itself is asset-agnostic —
 * pass a [Context] explicitly; there is no hidden initializer.
 */
public object AndroidAssets {

    public suspend fun loadVocab(context: Context, path: String = "minilm/vocab.txt"): String =
        withContext(Dispatchers.IO) {
            context.assets.open(path).use { it.readBytes().decodeToString() }
        }

    public suspend fun loadSafeTensors(
        context: Context,
        modelPath: String = "minilm/model.safetensors",
        configPath: String = "minilm/config.json",
        poolingConfigPath: String? = "minilm/pooling_config.json",
    ): ModelSource.SafeTensors = withContext(Dispatchers.IO) {
        ModelSource.SafeTensors(
            model = context.assets.open(modelPath).use { it.readBytes() },
            configJson = context.assets.open(configPath).use { it.readBytes().decodeToString() },
            poolingConfigJson = poolingConfigPath?.let { path ->
                runCatching {
                    context.assets.open(path).use { it.readBytes().decodeToString() }
                }.getOrNull()
            },
        )
    }

    public suspend fun loadGguf(
        context: Context,
        modelPath: String,
        configPath: String,
        poolingConfigPath: String? = null,
    ): ModelSource.Gguf = withContext(Dispatchers.IO) {
        ModelSource.Gguf(
            model = context.assets.open(modelPath).use { it.readBytes() },
            configJson = context.assets.open(configPath).use { it.readBytes().decodeToString() },
            poolingConfigJson = poolingConfigPath?.let { path ->
                runCatching {
                    context.assets.open(path).use { it.readBytes().decodeToString() }
                }.getOrNull()
            },
        )
    }
}
