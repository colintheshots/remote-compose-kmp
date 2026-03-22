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

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Tests for WireBuffer read/write round-trips and buffer management */
class WireBufferTest {

    @Test
    fun writeIntReadInt() {
        val buffer = WireBuffer(64)
        val values = intArrayOf(0, 1, -1, Int.MAX_VALUE, Int.MIN_VALUE, 42, 0x7FFFFFFF)
        for (v in values) {
            buffer.writeInt(v)
        }
        buffer.index = 0
        for (v in values) {
            assertEquals(v, buffer.readInt(), "Int round-trip failed for $v")
        }
    }

    @Test
    fun writeLongReadLong() {
        val buffer = WireBuffer(128)
        val values = longArrayOf(0L, 1L, -1L, Long.MAX_VALUE, Long.MIN_VALUE, 1234567886541234L)
        for (v in values) {
            buffer.writeLong(v)
        }
        buffer.index = 0
        for (v in values) {
            assertEquals(v, buffer.readLong(), "Long round-trip failed for $v")
        }
    }

    @Test
    fun writeFloatReadFloat() {
        val buffer = WireBuffer(64)
        val values = floatArrayOf(0f, 1.0f, -1.0f, 1.2345678f, Float.MAX_VALUE, Float.MIN_VALUE)
        for (v in values) {
            buffer.writeFloat(v)
        }
        buffer.index = 0
        for (v in values) {
            assertEquals(v, buffer.readFloat(), "Float round-trip failed for $v")
        }
    }

    @Test
    fun writeDoubleReadDouble() {
        val buffer = WireBuffer(128)
        val values = doubleArrayOf(0.0, 1.0, -1.0, 1.2345678901234, Double.MAX_VALUE, Double.MIN_VALUE)
        for (v in values) {
            buffer.writeDouble(v)
        }
        buffer.index = 0
        for (v in values) {
            assertEquals(v, buffer.readDouble(), "Double round-trip failed for $v")
        }
    }

    @Test
    fun writeBooleanReadBoolean() {
        val buffer = WireBuffer(16)
        buffer.writeBoolean(true)
        buffer.writeBoolean(false)
        buffer.writeBoolean(true)
        buffer.index = 0
        assertTrue(buffer.readBoolean())
        assertFalse(buffer.readBoolean())
        assertTrue(buffer.readBoolean())
    }

    @Test
    fun writeByteReadByte() {
        val buffer = WireBuffer(16)
        val values = intArrayOf(0, 1, 122, 255, 128)
        for (v in values) {
            buffer.writeByte(v)
        }
        buffer.index = 0
        for (v in values) {
            assertEquals(v, buffer.readByte(), "Byte round-trip failed for $v")
        }
    }

    @Test
    fun writeShortReadShort() {
        val buffer = WireBuffer(32)
        val values = intArrayOf(0, 1, 2234, 0xFFFF, 0x7FFF)
        for (v in values) {
            buffer.writeShort(v)
        }
        buffer.index = 0
        for (v in values) {
            assertEquals(v, buffer.readShort(), "Short round-trip failed for $v")
        }
    }

    @Test
    fun writeUTF8ReadUTF8() {
        val buffer = WireBuffer(256)
        val strings = arrayOf("hello", "", "world", "test with spaces", "unicode: \u00E9\u00E0\u00FC")
        for (s in strings) {
            buffer.writeUTF8(s)
        }
        buffer.index = 0
        for (s in strings) {
            assertEquals(s, buffer.readUTF8(), "UTF8 round-trip failed for \"$s\"")
        }
    }

    @Test
    fun writeBufferReadBuffer() {
        val buffer = WireBuffer(256)
        val random = Random(42)
        val data = random.nextBytes(12)
        buffer.writeBuffer(data)
        buffer.index = 0
        val result = buffer.readBuffer()
        assertContentEquals(data, result)
    }

    /** Combined round-trip test matching the original Java WireTest */
    @Test
    fun combinedRoundTrip() {
        val initSize = 23
        val w = WireBuffer(initSize)

        assertFalse(w.available())
        assertEquals(0, w.size)
        assertEquals(initSize, w.maxSize)

        val a = 122
        val b = 2234
        val c = 3123456
        val d = 1234567886541234L
        val e = "hello"
        val f = true
        val g = 1.2345678901234
        val h = 1.2345678f

        w.writeByte(a)
        w.writeShort(b)
        w.writeInt(c)
        w.writeLong(d)
        w.writeUTF8(e)
        w.writeBoolean(f)
        w.writeDouble(g)
        w.writeFloat(h)

        val random = Random(42)
        val buf = random.nextBytes(12)
        w.writeBuffer(buf)
        w.writeFloat(h)

        w.index = 0
        assertEquals(a, w.readByte())
        assertEquals(b, w.readShort())
        assertEquals(c, w.readInt())
        assertEquals(d, w.readLong())
        assertEquals(e, w.readUTF8())
        assertEquals(f, w.readBoolean())
        assertEquals(g, w.readDouble())
        assertEquals(h, w.readFloat())
        assertContentEquals(buf, w.readBuffer())
        assertEquals(h, w.readFloat())
    }

    /** Test that buffer automatically resizes when capacity is exceeded */
    @Test
    fun bufferResize() {
        val initSize = 32
        val w = WireBuffer(initSize)
        val random = Random(42)
        val data = random.nextBytes(3200)

        for (byte in data) {
            w.writeByte(byte.toInt())
            w.writeInt(byte.toInt())
        }

        // Buffer should have grown beyond initial size
        assertTrue(w.maxSize > initSize, "Buffer should have resized")

        w.index = 0
        for (byte in data) {
            assertEquals(byte.toInt() and 0xFF, w.readByte())
            assertEquals(byte.toInt(), w.readInt())
        }
    }

    @Test
    fun cloneBytes() {
        val buffer = WireBuffer(64)
        buffer.writeInt(42)
        buffer.writeInt(99)
        val cloned = buffer.cloneBytes()
        assertEquals(8, cloned.size) // 2 ints = 8 bytes
        // Verify cloned bytes decode to same values
        val w2 = WireBuffer(64)
        w2.loadFromBytes(cloned)
        assertEquals(42, w2.readInt())
        assertEquals(99, w2.readInt())
    }

    @Test
    fun moveBlock() {
        val buffer = WireBuffer(64)
        // Write "AABB" pattern: A block then B block
        buffer.writeByte(0xAA)
        buffer.writeByte(0xAA)
        val beyond = buffer.index
        buffer.writeByte(0xBB)
        buffer.writeByte(0xBB)

        // Move B block before A block
        buffer.moveBlock(beyond, 0)

        buffer.index = 0
        // After move, B should come first
        assertEquals(0xBB, buffer.readByte())
        assertEquals(0xBB, buffer.readByte())
        assertEquals(0xAA, buffer.readByte())
        assertEquals(0xAA, buffer.readByte())
    }

    @Test
    fun moveBlockNoOpWhenInsertionBeyondOrEqual() {
        val buffer = WireBuffer(64)
        buffer.writeByte(0x01)
        buffer.writeByte(0x02)
        buffer.writeByte(0x03)

        // insertLocation >= beyond should be a no-op
        buffer.moveBlock(2, 2)
        buffer.index = 0
        assertEquals(0x01, buffer.readByte())
        assertEquals(0x02, buffer.readByte())
        assertEquals(0x03, buffer.readByte())
    }

    @Test
    fun loadFromBytes() {
        val buffer = WireBuffer(64)
        buffer.writeInt(123)
        buffer.writeFloat(3.14f)
        val bytes = buffer.cloneBytes()

        val buffer2 = WireBuffer(64)
        buffer2.loadFromBytes(bytes)
        assertEquals(123, buffer2.readInt())
        assertEquals(3.14f, buffer2.readFloat())
    }

    @Test
    fun peekIntDoesNotAdvanceIndex() {
        val buffer = WireBuffer(64)
        buffer.writeInt(42)
        buffer.index = 0
        val peeked = buffer.peekInt()
        assertEquals(42, peeked)
        assertEquals(0, buffer.index, "peekInt should not advance index")
    }

    @Test
    fun resetClearsState() {
        val buffer = WireBuffer(64)
        buffer.writeInt(42)
        buffer.reset()
        assertEquals(0, buffer.index)
        assertEquals(0, buffer.size)
    }

    @Test
    fun availableReflectsData() {
        val buffer = WireBuffer(64)
        assertFalse(buffer.available())
        buffer.writeInt(42)
        buffer.index = 0
        assertTrue(buffer.available())
    }
}
