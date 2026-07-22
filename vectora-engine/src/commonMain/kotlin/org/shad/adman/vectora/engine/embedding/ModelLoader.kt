package org.shad.adman.vectora.engine.embedding

/**
 * Platform-independent model loader.
 */
internal expect object ModelLoader {
    /**
     * Loads the bundled model bytes.
     */
    fun loadModel(): ByteArray

    /**
     * Loads the bundled vocabulary.
     */
    fun loadVocab(): List<String>
}
