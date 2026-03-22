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
package androidx.compose.remote.core.operations

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

/** Utilities to be used across all core operations */
object Utils {

    /**
     * Convert an integer id into a float
     *
     * @param v the integer id to convert
     * @return the id as a float
     */
    fun asNan(v: Int): Float {
        return Float.fromBits(v or -0x800000)
    }

    /**
     * Convert a float into an integer id
     *
     * @param value the float id to convert
     * @return the id as an integer
     */
    fun idFromNan(value: Float): Int {
        val b = value.toRawBits()
        return b and 0x3FFFFF
    }

    /**
     * Converts an id encoded in a float to the corresponding long id.
     *
     * @param value the float id to convert
     * @return the float id converted to a long id
     */
    fun longIdFromNan(value: Float): Long {
        return idFromNan(value).toLong() + 0x100000000L
    }

    /**
     * Convert a long into an ID
     *
     * @param v the long to convert
     * @return the id still as a long
     */
    fun idFromLong(v: Long): Long {
        return v - 0x100000000L
    }

    /**
     * Convert a float id and turn it into a string
     *
     * @param value float to convert
     * @return string form of an id
     */
    fun idStringFromNan(value: Float): String {
        val b = value.toRawBits() and 0x3FFFFF
        return idString(b)
    }

    /**
     * Print an id as a string
     *
     * @param b the id
     * @return the id as a string
     */
    fun idString(b: Int): String {
        return if (b > 0xFFFFF) "A_${b and 0xFFFFF}" else "$b"
    }

    /**
     * Trim a string to n characters; if trimmed, end with "..."
     *
     * @param str the string to trim
     * @param n max length
     * @return trimmed string
     */
    fun trimString(str: String, n: Int): String {
        return if (str.length > n) {
            str.substring(0, n - 3) + "..."
        } else {
            str
        }
    }

    /**
     * Print the id and the value of a float
     *
     * @param idValue the id value
     * @param value the value
     * @return string representation
     */
    fun floatToString(idValue: Float, value: Float): String {
        if (idValue.isNaN()) {
            if (idFromNan(value) == 0) {
                return "NaN"
            }
            return "[${idFromNan(idValue)}]${floatToString(value)}"
        }
        return floatToString(value)
    }

    /**
     * Convert float to string but render NaN id in brackets [n]
     *
     * @param value the float value
     * @return string representation
     */
    fun floatToString(value: Float): String {
        if (value.isNaN()) {
            if (idFromNan(value) == 0) {
                return "NaN"
            }
            return "[${idFromNan(value)}]"
        }
        return value.toString()
    }

    /**
     * Debugging util to print a message and include the file/line it came from
     *
     * @param str the message
     */
    fun log(str: String) {
        // In KMP we don't have StackTraceElement; use println directly
        println(str)
    }

    /**
     * Utility to produce a log string
     *
     * @param str string to append
     * @return the log string
     */
    fun logString(str: String): String {
        return str
    }

    /**
     * Debugging util to print the stack
     *
     * @param str the message
     * @param n depth
     */
    fun logStack(str: String, n: Int) {
        for (i in 1..n) {
            val space = " ".repeat(i)
            println("$space$str")
        }
    }

    /**
     * Is a variable allowed in calculation and references.
     *
     * @param v the value to check
     * @return true if it is a variable
     */
    fun isVariable(v: Float): Boolean {
        if (v.isNaN()) {
            val id = idFromNan(v)
            if (id == 0) return false
            return id > 40 || id < 10
        }
        return false
    }

    /**
     * Print a color in the familiar 0xAARRGGBB pattern
     *
     * @param color the color int
     * @return hex string representation
     */
    fun colorInt(color: Int): String {
        val str = "000000000000" + color.toUInt().toString(16)
        return "0x" + str.substring(str.length - 8)
    }

    /**
     * Interpolate two colors. Gamma corrected colors are interpolated in the form
     * c1 * (1-t) + c2 * t
     *
     * @param c1 first color
     * @param c2 second color
     * @param t interpolation factor
     * @return interpolated color
     */
    fun interpolateColor(c1: Int, c2: Int, t: Float): Int {
        if (t.isNaN() || t == 0.0f) {
            return c1
        } else if (t == 1.0f) {
            return c2
        }
        var a = 0xFF and (c1 shr 24)
        var r = 0xFF and (c1 shr 16)
        var g = 0xFF and (c1 shr 8)
        var b = 0xFF and c1
        var fR = (r / 255.0f).pow(2.2f)
        var fG = (g / 255.0f).pow(2.2f)
        var fB = (b / 255.0f).pow(2.2f)
        val c1fr = fR
        val c1fg = fG
        val c1fb = fB
        val c1fa = a / 255f

        a = 0xFF and (c2 shr 24)
        r = 0xFF and (c2 shr 16)
        g = 0xFF and (c2 shr 8)
        b = 0xFF and c2
        fR = (r / 255.0f).pow(2.2f)
        fG = (g / 255.0f).pow(2.2f)
        fB = (b / 255.0f).pow(2.2f)
        val c2fr = fR
        val c2fg = fG
        val c2fb = fB
        val c2fa = a / 255f
        fR = c1fr + t * (c2fr - c1fr)
        fG = c1fg + t * (c2fg - c1fg)
        fB = c1fb + t * (c2fb - c1fb)
        val fA = c1fa + t * (c2fa - c1fa)

        val outr = clamp((fR.pow(1.0f / 2.2f) * 255.0f).toInt())
        val outg = clamp((fG.pow(1.0f / 2.2f) * 255.0f).toInt())
        val outb = clamp((fB.pow(1.0f / 2.2f) * 255.0f).toInt())
        val outa = clamp((fA * 255.0f).toInt())

        return (outa shl 24) or (outr shl 16) or (outg shl 8) or outb
    }

    /**
     * Efficient clamping function
     *
     * @param c value to clamp
     * @return number between 0 and 255
     */
    fun clamp(c: Int): Int {
        val n = 255
        var v = c
        v = v and (v shr 31).inv()
        v -= n
        v = v and (v shr 31)
        v += n
        return v
    }

    /**
     * Convert hue saturation and value to RGB
     *
     * @param hue 0..1
     * @param saturation 0..1 0=on the gray scale
     * @param value 0..1 0=black
     * @return packed ARGB int
     */
    fun hsvToRgb(hue: Float, saturation: Float, value: Float): Int {
        val h = (hue * 6).toInt()
        val f = hue * 6 - h
        val p = (0.5f + 255 * value * (1 - saturation)).toInt()
        val q = (0.5f + 255 * value * (1 - f * saturation)).toInt()
        val t = (0.5f + 255 * value * (1 - (1 - f) * saturation)).toInt()
        val v = (0.5f + 255 * value).toInt()
        return when (h) {
            0 -> 0xFF000000.toInt() or (v shl 16) + (t shl 8) + p
            1 -> 0xFF000000.toInt() or (q shl 16) + (v shl 8) + p
            2 -> 0xFF000000.toInt() or (p shl 16) + (v shl 8) + t
            3 -> 0xFF000000.toInt() or (p shl 16) + (q shl 8) + v
            4 -> 0xFF000000.toInt() or (t shl 16) + (p shl 8) + v
            5 -> 0xFF000000.toInt() or (v shl 16) + (p shl 8) + q
            else -> 0
        }
    }

    /**
     * Convert float alpha, red, green, blue to ARGB int
     *
     * @param alpha alpha value
     * @param red red value
     * @param green green value
     * @param blue blue value
     * @return ARGB int
     */
    fun toARGB(alpha: Float, red: Float, green: Float, blue: Float): Int {
        val a = (alpha * 255.0f + 0.5f).toInt()
        val r = (red * 255.0f + 0.5f).toInt()
        val g = (green * 255.0f + 0.5f).toInt()
        val b = (blue * 255.0f + 0.5f).toInt()
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }

    /**
     * Returns the hue of an ARGB int
     *
     * @param argb the color int
     * @return hue in range [0, 1]
     */
    fun getHue(argb: Int): Float {
        val r = (argb shr 16) and 0xFF
        val g = (argb shr 8) and 0xFF
        val b = argb and 0xFF
        val rf = r / 255f
        val gf = g / 255f
        val bf = b / 255f
        val maxVal = max(rf, max(gf, bf))
        val minVal = min(rf, min(gf, bf))
        val deltaMaxMin = maxVal - minVal
        var h: Float
        if (maxVal == minVal) {
            h = 0f
        } else {
            h = when (maxVal) {
                rf -> ((gf - bf) / deltaMaxMin) % 6f
                gf -> ((bf - rf) / deltaMaxMin) + 2f
                else -> ((rf - gf) / deltaMaxMin) + 4f
            }
        }
        h = (h * 60f) % 360f
        if (h < 0) {
            h += 360f
        }
        return h / 360f
    }

    /**
     * Get the saturation of an ARGB int
     *
     * @param argb the color int
     * @return saturation in range [0, 1]
     */
    fun getSaturation(argb: Int): Float {
        val r = (argb shr 16) and 0xFF
        val g = (argb shr 8) and 0xFF
        val b = argb and 0xFF
        val rf = r / 255f
        val gf = g / 255f
        val bf = b / 255f
        val maxVal = max(rf, max(gf, bf))
        val minVal = min(rf, min(gf, bf))
        val deltaMaxMin = maxVal - minVal
        return if (maxVal == minVal) 0f else deltaMaxMin / maxVal
    }

    /**
     * Get the brightness of an ARGB int
     *
     * @param argb the color int
     * @return brightness in range [0, 1]
     */
    fun getBrightness(argb: Int): Float {
        val r = (argb shr 16) and 0xFF
        val g = (argb shr 8) and 0xFF
        val b = argb and 0xFF
        val rf = r / 255f
        val gf = g / 255f
        val bf = b / 255f
        return max(rf, max(gf, bf))
    }
}
