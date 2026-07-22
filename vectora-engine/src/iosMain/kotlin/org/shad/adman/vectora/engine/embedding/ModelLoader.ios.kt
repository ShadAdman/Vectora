package org.shad.adman.vectora.engine.embedding

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.*
import platform.posix.memcpy

internal actual object ModelLoader {
    @OptIn(ExperimentalForeignApi::class)
    actual fun loadModel(): ByteArray {
        val path = NSBundle.mainBundle.pathForResource("all-minilm", "tflite")
            ?: NSBundle.allBundles.map { it as NSBundle }.firstNotNullOfOrNull { it.pathForResource("all-minilm", "tflite") }
            ?: error("Model file not found: all-minilm.tflite")

        val data = NSData.dataWithContentsOfFile(path) ?: error("Failed to read model file at $path")
        val size = data.length.toInt()
        val byteArray = ByteArray(size)
        if (size > 0) {
            byteArray.usePinned { pinned ->
                memcpy(pinned.addressOf(0), data.bytes, data.length)
            }
        }
        return byteArray
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun loadVocab(): List<String> {
        val path = NSBundle.mainBundle.pathForResource("vocab", "txt")
            ?: NSBundle.allBundles.map { it as NSBundle }.firstNotNullOfOrNull { it.pathForResource("vocab", "txt") }
            ?: error("Vocab file not found: vocab.txt")

        val content = NSString.stringWithContentsOfFile(path, encoding = NSUTF8StringEncoding, error = null)
            ?: error("Failed to read vocab file at $path")
        
        return content.split("\n")
    }
}
