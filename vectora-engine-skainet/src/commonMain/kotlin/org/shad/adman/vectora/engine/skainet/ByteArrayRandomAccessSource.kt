package org.shad.adman.vectora.engine.skainet

import sk.ainet.io.RandomAccessSource

internal class ByteArrayRandomAccessSource(private val data: ByteArray) : RandomAccessSource {

    override val size: Long get() = data.size.toLong()

    override fun readAt(position: Long, length: Int): ByteArray {
        require(position >= 0 && length >= 0 && position + length <= size) {
            "read [$position, ${position + length}) out of bounds for size $size"
        }
        return data.copyOfRange(position.toInt(), position.toInt() + length)
    }

    override fun readAt(position: Long, buffer: ByteArray, offset: Int, length: Int): Int {
        require(position in 0..size) { "position $position out of bounds for size $size" }
        val available = minOf(length.toLong(), size - position).toInt()
        data.copyInto(buffer, offset, position.toInt(), position.toInt() + available)
        return available
    }

    override fun close() {}
}
