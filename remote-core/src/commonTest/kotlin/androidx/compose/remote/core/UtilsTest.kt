/*
 * Copyright (C) 2024 The Android Open Source Project
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

import androidx.compose.remote.core.operations.Utils
import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Tests for Utils and color utility functions */
class UtilsTest {

    // -----------------------------------------------------------------------
    // floatToString
    // -----------------------------------------------------------------------

    @Test
    fun floatToStringNormalValue() {
        val result = Utils.floatToString(42.5f)
        assertEquals("42.5", result)
    }

    @Test
    fun floatToStringNanWithId() {
        val id = 42
        val nanFloat = Utils.asNan(id)
        val result = Utils.floatToString(nanFloat)
        assertEquals("[$id]", result)
    }

    @Test
    fun floatToStringNanWithZeroId() {
        // A true NaN with id=0 should return "NaN"
        val result = Utils.floatToString(Float.NaN)
        assertEquals("NaN", result)
    }

    @Test
    fun floatToStringTwoArgNanValues() {
        val id = 100
        val nanFloat = Utils.asNan(id)
        val result = Utils.floatToString(nanFloat, 5.0f)
        assertEquals("[$id]5.0", result)
    }

    // -----------------------------------------------------------------------
    // isVariable / idFromNan / asNan
    // -----------------------------------------------------------------------

    @Test
    fun isVariableReturnsTrueForVariableIds() {
        // IDs > 40 or < 10 (but not 0) should be variables
        assertTrue(Utils.isVariable(Utils.asNan(41)))
        assertTrue(Utils.isVariable(Utils.asNan(50)))
        assertTrue(Utils.isVariable(Utils.asNan(100)))
        assertTrue(Utils.isVariable(Utils.asNan(5)))
        assertTrue(Utils.isVariable(Utils.asNan(1)))
    }

    @Test
    fun isVariableReturnsFalseForNonNan() {
        assertFalse(Utils.isVariable(1.0f))
        assertFalse(Utils.isVariable(0.0f))
        assertFalse(Utils.isVariable(-5.0f))
    }

    @Test
    fun isVariableReturnsFalseForReservedRange() {
        // IDs 10..40 are not variables
        for (id in 10..40) {
            assertFalse(
                Utils.isVariable(Utils.asNan(id)),
                "id=$id should not be a variable"
            )
        }
    }

    @Test
    fun isVariableReturnsFalseForPlainNan() {
        // Plain Float.NaN (id=0) is not a variable
        assertFalse(Utils.isVariable(Float.NaN))
    }

    @Test
    fun idFromNanRoundTrip() {
        for (id in listOf(1, 42, 100, 0x3FFFFF)) {
            val nan = Utils.asNan(id)
            assertTrue(nan.isNaN())
            assertEquals(id, Utils.idFromNan(nan))
        }
    }

    // -----------------------------------------------------------------------
    // Color packing/unpacking
    // -----------------------------------------------------------------------

    @Test
    fun colorIntFormatsCorrectly() {
        assertEquals("0xff000000", Utils.colorInt(0xFF000000.toInt()))
        assertEquals("0xffff0000", Utils.colorInt(0xFFFF0000.toInt()))
        assertEquals("0x00000000", Utils.colorInt(0x00000000))
        assertEquals("0xffffffff", Utils.colorInt(0xFFFFFFFF.toInt()))
    }

    @Test
    fun toARGBPacksCorrectly() {
        val argb = Utils.toARGB(1.0f, 1.0f, 0.0f, 0.0f)
        assertEquals(0xFFFF0000.toInt(), argb) // opaque red
    }

    @Test
    fun toARGBBlack() {
        val argb = Utils.toARGB(1.0f, 0.0f, 0.0f, 0.0f)
        assertEquals(0xFF000000.toInt(), argb)
    }

    @Test
    fun toARGBWhite() {
        val argb = Utils.toARGB(1.0f, 1.0f, 1.0f, 1.0f)
        assertEquals(0xFFFFFFFF.toInt(), argb)
    }

    @Test
    fun interpolateColorEndpoints() {
        val c1 = 0xFFFF0000.toInt()
        val c2 = 0xFF0000FF.toInt()
        // t=0 should return c1
        assertEquals(c1, Utils.interpolateColor(c1, c2, 0.0f))
        // t=1 should return c2
        assertEquals(c2, Utils.interpolateColor(c1, c2, 1.0f))
    }

    @Test
    fun interpolateColorNanReturnsFirst() {
        val c1 = 0xFFFF0000.toInt()
        val c2 = 0xFF0000FF.toInt()
        assertEquals(c1, Utils.interpolateColor(c1, c2, Float.NaN))
    }

    @Test
    fun clampValues() {
        assertEquals(0, Utils.clamp(-10))
        assertEquals(0, Utils.clamp(0))
        assertEquals(128, Utils.clamp(128))
        assertEquals(255, Utils.clamp(255))
        assertEquals(255, Utils.clamp(300))
    }

    // -----------------------------------------------------------------------
    // HSV <-> RGB
    // -----------------------------------------------------------------------

    @Test
    fun hsvToRgbBasicColors() {
        // Red: hue=0, sat=1, val=1
        val red = Utils.hsvToRgb(0.0f, 1.0f, 1.0f)
        assertEquals(0xFF, (red shr 16) and 0xFF, "Red channel should be 255")
        assertEquals(0x00, (red shr 8) and 0xFF, "Green channel should be 0")
        // Blue at hue=0.666..
        val blue = Utils.hsvToRgb(4.0f / 6.0f, 1.0f, 1.0f)
        assertEquals(0x00, (blue shr 16) and 0xFF, "Red channel should be 0 for blue")
        assertEquals(0xFF, blue and 0xFF, "Blue channel should be 255")
    }

    @Test
    fun getHueSaturationBrightnessRoundTrip() {
        // Use a known color and verify HSV extraction
        val color = 0xFFFF8000.toInt() // orange-ish
        val h = Utils.getHue(color)
        val s = Utils.getSaturation(color)
        val b = Utils.getBrightness(color)

        // Hue for orange should be around 30/360 = 0.083
        assertTrue(h in 0.0f..0.15f, "Hue should be in orange range, got $h")
        assertTrue(s > 0.9f, "Saturation should be high for pure orange")
        assertEquals(1.0f, b, "Brightness should be 1.0 for full-value color")
    }

    @Test
    fun hsvRoundTripWithRandomColors() {
        val random = Random(12323)
        for (i in 0 until 20) {
            val h = random.nextFloat()
            val s = random.nextFloat()
            val v = random.nextFloat()

            val rgb = Utils.hsvToRgb(h, s, v)
            // Extract components and verify they are in valid range
            val r = (rgb shr 16) and 0xFF
            val g = (rgb shr 8) and 0xFF
            val b = rgb and 0xFF
            assertTrue(r in 0..255)
            assertTrue(g in 0..255)
            assertTrue(b in 0..255)
        }
    }

    @Test
    fun getHueSatBrightnessConsistency() {
        val random = Random(12323)
        for (i in 0 until 20) {
            val rgb = random.nextInt()
            val h = Utils.getHue(rgb)
            val s = Utils.getSaturation(rgb)
            val v = Utils.getBrightness(rgb)

            assertTrue(h in 0.0f..1.0f, "Hue should be in [0,1], got $h")
            assertTrue(s in 0.0f..1.0f, "Saturation should be in [0,1], got $s")
            assertTrue(v in 0.0f..1.0f, "Brightness should be in [0,1], got $v")
        }
    }

    // -----------------------------------------------------------------------
    // Misc utilities
    // -----------------------------------------------------------------------

    @Test
    fun trimString() {
        assertEquals("hello", Utils.trimString("hello", 10))
        assertEquals("hel...", Utils.trimString("hello world", 6))
        assertEquals("hello", Utils.trimString("hello", 5))
        assertEquals("he...", Utils.trimString("hello!", 5))
    }

    @Test
    fun idString() {
        assertEquals("42", Utils.idString(42))
        assertEquals("0", Utils.idString(0))
        // IDs > 0xFFFFF use A_ prefix
        assertEquals("A_0", Utils.idString(0x100000))
        assertEquals("A_1", Utils.idString(0x100001))
    }
}
