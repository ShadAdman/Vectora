package org.shad.adman.vectora.engine.embedding

import org.shad.adman.vectora.engine.R

internal actual object ModelLoader {
    actual fun loadModel(): ByteArray {
        val context = appContext ?: error("Context not initialized. VectoraInitializer should have been called.")
        return context.resources.openRawResource(R.raw.all_minilm).use { it.readBytes() }
    }

    actual fun loadVocab(): List<String> {
        val context = appContext ?: error("Context not initialized. VectoraInitializer should have been called.")
        return context.resources.openRawResource(R.raw.vocab).use { inputStream ->
            inputStream.bufferedReader().readLines()
        }
    }
}
