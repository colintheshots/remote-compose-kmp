/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.compose.remote.core

/** The base communication buffer capable of encoding and decoding various types */
class WireBuffer(size: Int = BUFFER_SIZE) {

    companion object {
        private const val BUFFER_SIZE = 1024 * 1024
    }

    var maxSize: Int = size
        private set
    var buffer: ByteArray = ByteArray(maxSize)
        private set
    var index: Int = 0
    var startingIndex: Int = 0
        private set
    var size: Int = 0
        private set
    var validOperations: BooleanArray = BooleanArray(256) { true }

    private fun resize(need: Int) {
        if (size + need >= maxSize) {
            maxSize = maxOf(maxSize * 2, size + need)
            buffer = buffer.copyOf(maxSize)
        }
    }

    /** Write a byte representing the command into the buffer */
    fun start(type: Int) {
        if (!validOperations[type]) {
            throw RuntimeException("Operation $type is not supported for this version")
        }
        startingIndex = index
        writeByte(type)
    }

    fun startWithSize(type: Int) {
        startingIndex = index
        writeByte(type)
        index += 4 // skip ahead for the future size
    }

    fun endWithSize() {
        val sz = index - startingIndex
        val currentIndex = index
        index = startingIndex + 1 // (type)
        writeInt(sz)
        index = currentIndex
    }

    /** Reset the internal buffer */
    fun reset(expectedSize: Int = 0) {
        index = 0
        startingIndex = 0
        size = 0
        if (expectedSize >= maxSize) {
            resize(expectedSize)
        }
    }

    /** Load the buffer from a byte array */
    fun loadFromBytes(bytes: ByteArray) {
        reset(bytes.size)
        bytes.copyInto(buffer)
        size = bytes.size
    }

    /** Bytes available */
    fun available(): Boolean = size - index > 0

    // ///////////////////////////////////////////////////////////////////////////
    // Read values
    // ///////////////////////////////////////////////////////////////////////////

    /** Read the operation type (reads a single byte) */
    fun readOperationType(): Int = readByte()

    /** Read a boolean (stored as a byte 1 = true) */
    fun readBoolean(): Boolean {
        val value = buffer[index]
        index++
        return value.toInt() == 1
    }

    /** Read a single byte */
    fun readByte(): Int {
        val value = 0xFF and buffer[index].toInt()
        index++
        return value
    }

    /** Read a short (2 bytes, big-endian) */
    fun readShort(): Int {
        val v1 = (buffer[index++].toInt() and 0xFF) shl 8
        val v2 = (buffer[index++].toInt() and 0xFF)
        return v1 + v2
    }

    /** Read an integer without incrementing the index */
    fun peekInt(): Int {
        var tmp = index
        val v1 = (buffer[tmp++].toInt() and 0xFF) shl 24
        val v2 = (buffer[tmp++].toInt() and 0xFF) shl 16
        val v3 = (buffer[tmp++].toInt() and 0xFF) shl 8
        val v4 = (buffer[tmp].toInt() and 0xFF)
        return v1 + v2 + v3 + v4
    }

    /** Read an integer (4 bytes, big-endian) */
    fun readInt(): Int {
        val v1 = (buffer[index++].toInt() and 0xFF) shl 24
        val v2 = (buffer[index++].toInt() and 0xFF) shl 16
        val v3 = (buffer[index++].toInt() and 0xFF) shl 8
        val v4 = (buffer[index++].toInt() and 0xFF)
        return v1 + v2 + v3 + v4
    }

    /** Read a long (8 bytes, big-endian) */
    fun readLong(): Long {
        val v1 = (buffer[index++].toLong() and 0xFFL) shl 56
        val v2 = (buffer[index++].toLong() and 0xFFL) shl 48
        val v3 = (buffer[index++].toLong() and 0xFFL) shl 40
        val v4 = (buffer[index++].toLong() and 0xFFL) shl 32
        val v5 = (buffer[index++].toLong() and 0xFFL) shl 24
        val v6 = (buffer[index++].toLong() and 0xFFL) shl 16
        val v7 = (buffer[index++].toLong() and 0xFFL) shl 8
        val v8 = (buffer[index++].toLong() and 0xFFL)
        return v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8
    }

    /** Read a 32 bit float IEEE standard */
    fun readFloat(): Float = Float.fromBits(readInt())

    /** Read a 64 bit double */
    fun readDouble(): Double = Double.fromBits(readLong())

    /** Read a byte buffer (4 byte length followed by bytes) */
    fun readBuffer(): ByteArray {
        val count = readInt()
        val b = buffer.copyOfRange(index, index + count)
        index += count
        return b
    }

    /** Read a byte buffer limited to max size */
    fun readBuffer(maxSz: Int): ByteArray {
        val count = readInt()
        if (count < 0 || count > maxSz) {
            throw RuntimeException("attempt read a buff of invalid size 0 <= $count > $maxSz")
        }
        val b = buffer.copyOfRange(index, index + count)
        index += count
        return b
    }

    /** Read a string encoded in UTF8 */
    fun readUTF8(): String {
        val stringBuffer = readBuffer()
        return stringBuffer.decodeToString()
    }

    /** Read a string encoded in UTF8 with max size */
    fun readUTF8(maxSz: Int): String {
        val stringBuffer = readBuffer(maxSz)
        return stringBuffer.decodeToString()
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Write values
    // ///////////////////////////////////////////////////////////////////////////

    /** Write a boolean value (as byte 1=true) */
    fun writeBoolean(value: Boolean) {
        resize(1)
        buffer[index++] = if (value) 1 else 0
        size++
    }

    /** Write a byte value */
    fun writeByte(value: Int) {
        resize(1)
        buffer[index++] = value.toByte()
        size++
    }

    /** Write a short value (2 bytes, big-endian) */
    fun writeShort(value: Int) {
        val need = 2
        resize(need)
        buffer[index++] = (value ushr 8 and 0xFF).toByte()
        buffer[index++] = (value and 0xFF).toByte()
        size += need
    }

    /** Write an int (4 bytes, big-endian) */
    fun writeInt(value: Int) {
        val need = 4
        resize(need)
        buffer[index++] = (value ushr 24 and 0xFF).toByte()
        buffer[index++] = (value ushr 16 and 0xFF).toByte()
        buffer[index++] = (value ushr 8 and 0xFF).toByte()
        buffer[index++] = (value and 0xFF).toByte()
        size += need
    }

    /** Write a long (8 bytes, big-endian) */
    fun writeLong(value: Long) {
        val need = 8
        resize(need)
        buffer[index++] = (value ushr 56 and 0xFF).toByte()
        buffer[index++] = (value ushr 48 and 0xFF).toByte()
        buffer[index++] = (value ushr 40 and 0xFF).toByte()
        buffer[index++] = (value ushr 32 and 0xFF).toByte()
        buffer[index++] = (value ushr 24 and 0xFF).toByte()
        buffer[index++] = (value ushr 16 and 0xFF).toByte()
        buffer[index++] = (value ushr 8 and 0xFF).toByte()
        buffer[index++] = (value and 0xFF).toByte()
        size += need
    }

    /** Write a 32 bit IEEE float value */
    fun writeFloat(value: Float) {
        writeInt(value.toRawBits())
    }

    /** Write a 64 bit IEEE double value */
    fun writeDouble(value: Double) {
        writeLong(value.toRawBits())
    }

    /** Write a buffer (length prefix followed by bytes) */
    fun writeBuffer(b: ByteArray) {
        resize(b.size + 4)
        writeInt(b.size)
        b.copyInto(buffer, index)
        index += b.size
        size += b.size
    }

    /** Write a string encoded as UTF8 */
    fun writeUTF8(content: String) {
        val buf = content.encodeToByteArray()
        writeBuffer(buf)
    }

    /** Copy the buffer to a new byte array */
    fun cloneBytes(): ByteArray = buffer.copyOfRange(0, size)

    /** Set the version used to write operations */
    fun setVersion(documentApiLevel: Int, profiles: Int) {
        for (i in validOperations.indices) {
            validOperations[i] = Operations.valid(i, documentApiLevel, profiles)
        }
    }

    /** Sets the operations that are considered valid for this buffer */
    fun setValidOperations(supportedOperations: Set<Int>) {
        for (o in supportedOperations) {
            validOperations[o] = true
        }
    }

    /** Move commands from beyond to insertLocation */
    fun moveBlock(beyond: Int, insertLocation: Int) {
        if (insertLocation < 0 || beyond > size || insertLocation >= beyond) {
            return
        }

        val lengthOfBlockA = beyond - insertLocation
        val lengthOfBlockB = size - beyond

        if (lengthOfBlockB < lengthOfBlockA) {
            val temp = ByteArray(lengthOfBlockB)
            buffer.copyInto(temp, 0, beyond, beyond + lengthOfBlockB)
            buffer.copyInto(buffer, insertLocation + lengthOfBlockB, insertLocation, insertLocation + lengthOfBlockA)
            temp.copyInto(buffer, insertLocation)
        } else {
            val temp = ByteArray(lengthOfBlockA)
            buffer.copyInto(temp, 0, insertLocation, insertLocation + lengthOfBlockA)
            buffer.copyInto(buffer, insertLocation, beyond, beyond + lengthOfBlockB)
            temp.copyInto(buffer, insertLocation + lengthOfBlockB)
        }
    }
}
