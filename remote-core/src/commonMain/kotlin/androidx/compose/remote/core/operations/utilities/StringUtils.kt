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
package androidx.compose.remote.core.operations.utilities

import kotlin.math.min
import kotlin.math.roundToInt

/** Utilities for string manipulation */
object StringUtils {
    const val GROUPING_NONE: Byte = 0    // e.g. 1234567890.12
    const val GROUPING_BY3: Byte = 1     // e.g. 1,234,567,890.12
    const val GROUPING_BY4: Byte = 2     // e.g. 12,3456,7890.12
    const val GROUPING_BY32: Byte = 3    // e.g. 1,23,45,67,890.12

    const val SEPARATOR_COMMA_PERIOD: Byte = 0  // e.g. 123,456.12
    const val SEPARATOR_PERIOD_COMMA: Byte = 1  // e.g. 123.456,12
    const val SEPARATOR_SPACE_COMMA: Byte = 2   // e.g. 123 456,12
    const val SEPARATOR_UNDER_PERIOD: Byte = 3  // e.g. 123_456.12

    const val NO_OPTIONS = 0              // e.g. -890.12
    const val NEGATIVE_PARENTHESES = 1    // e.g. (890.12)
    const val ROUNDING = 2                // Default is simple clipping
    const val POINT_ZERO = 4              // Default is simple clipping

    const val PAD_NONE: Char = 0.toChar()
    const val PAD_ZERO: Char = '0'
    const val PAD_SPACE: Char = ' '

    /**
     * Converts a float into a string. Providing a defined number of characters before and after the
     * decimal point.
     *
     * @param value The value to convert to string
     * @param beforeDecimalPoint digits before the decimal point
     * @param afterDecimalPoint digits after the decimal point
     * @param pre character to pad with; 0 = no pad, typically ' ' or '0'
     * @param post character to pad with; 0 = no pad, typically ' ' or '0'
     * @return The formatted string representation of the float.
     */
    fun floatToString(
        value: Float,
        beforeDecimalPoint: Int,
        afterDecimalPoint: Int,
        pre: Char,
        post: Char
    ): String {
        var v = value
        val isNeg = v < 0
        if (isNeg) v = -v

        val integerPart = v.toInt()
        var fractionalPart = v % 1

        var integerPartString = integerPart.toString()
        val iLen = integerPartString.length
        if (iLen < beforeDecimalPoint) {
            val spacesToPad = beforeDecimalPoint - iLen
            if (pre != 0.toChar()) {
                integerPartString = pre.toString().repeat(spacesToPad) + integerPartString
            }
        } else if (iLen > beforeDecimalPoint) {
            integerPartString = integerPartString.substring(iLen - beforeDecimalPoint)
        }
        if (afterDecimalPoint == 0) {
            return (if (isNeg) "-" else "") + integerPartString
        }

        for (i in 0 until afterDecimalPoint) {
            fractionalPart *= 10
        }
        fractionalPart = fractionalPart.roundToInt().toFloat()
        for (i in 0 until afterDecimalPoint) {
            fractionalPart *= 0.1f
        }

        var fact = fractionalPart.toString()
        fact = fact.substring(2, min(fact.length, afterDecimalPoint + 2))
        var trim = fact.length
        for (i in fact.length - 1 downTo 0) {
            if (fact[i] != '0') break
            trim--
        }
        if (trim != fact.length) {
            fact = fact.substring(0, trim)
        }
        val len = fact.length
        if (post != 0.toChar() && len < afterDecimalPoint) {
            fact += post.toString().repeat(afterDecimalPoint - len)
        }

        return (if (isNeg) "-" else "") + integerPartString + "." + fact
    }

    /**
     * Converts a float into a string with grouping and separator options.
     */
    fun floatToString(
        value: Float,
        beforeDecimalPoint: Int,
        afterDecimalPoint: Int,
        pre: Char,
        post: Char,
        separator: Byte,
        grouping: Byte,
        options: Int
    ): String {
        var groupSep = ','
        var decSep = '.'
        when (separator) {
            SEPARATOR_PERIOD_COMMA -> { groupSep = '.'; decSep = ',' }
            SEPARATOR_SPACE_COMMA -> { groupSep = ' '; decSep = ',' }
            SEPARATOR_UNDER_PERIOD -> { groupSep = '_'; decSep = '.' }
        }
        val useParenthesesForNeg = (options and NEGATIVE_PARENTHESES) != 0
        val rounding = (options and ROUNDING) != 0
        var v = value
        val isNeg = v < 0
        if (isNeg) v = -v

        val chars = toChars(v, beforeDecimalPoint, afterDecimalPoint, rounding)
        val str = chars.concatToString()
        var fractionalPart = v % 1

        var integerPartString = str.substring(0, str.indexOf('.'))
        if (grouping != GROUPING_NONE) {
            val gLen = integerPartString.length
            when (grouping) {
                GROUPING_BY3 -> {
                    var i = gLen - 3
                    while (i > 0) {
                        integerPartString = integerPartString.substring(0, i) +
                            groupSep + integerPartString.substring(i)
                        i -= 3
                    }
                }
                GROUPING_BY4 -> {
                    var i = gLen - 4
                    while (i > 0) {
                        integerPartString = integerPartString.substring(0, i) +
                            groupSep + integerPartString.substring(i)
                        i -= 4
                    }
                }
                GROUPING_BY32 -> {
                    var i = gLen - 3
                    while (i > 0) {
                        integerPartString = integerPartString.substring(0, i) +
                            groupSep + integerPartString.substring(i)
                        i -= 2
                    }
                }
            }
        }
        val iLen = integerPartString.length
        if (iLen < beforeDecimalPoint) {
            val spacesToPad = beforeDecimalPoint - iLen
            if (pre != 0.toChar()) {
                integerPartString = pre.toString().repeat(spacesToPad) + integerPartString
            }
        } else if (iLen > beforeDecimalPoint) {
            integerPartString = integerPartString.substring(iLen - beforeDecimalPoint)
        }

        if (afterDecimalPoint == 0) {
            if (!isNeg) return integerPartString
            return if (useParenthesesForNeg) "($integerPartString)" else "-$integerPartString"
        }

        for (i in 0 until afterDecimalPoint) {
            fractionalPart *= 10
        }
        fractionalPart = fractionalPart.roundToInt().toFloat()
        for (i in 0 until afterDecimalPoint) {
            fractionalPart *= 0.1f
        }

        var fact = fractionalPart.toString()
        fact = fact.substring(2, min(fact.length, afterDecimalPoint + 2))
        var trim = fact.length
        for (i in fact.length - 1 downTo 1) {
            if (fact[i] != '0') break
            trim--
        }
        if (trim != fact.length) {
            fact = fact.substring(0, trim)
        }
        val len = fact.length
        if (post != 0.toChar() && len < afterDecimalPoint) {
            fact += post.toString().repeat(afterDecimalPoint - len)
        }
        if (!isNeg) return integerPartString + decSep + fact
        return if (useParenthesesForNeg) {
            "($integerPartString$decSep$fact)"
        } else {
            "-$integerPartString$decSep$fact"
        }
    }

    private fun toChars(
        value: Float,
        beforeDecimalPoint: Int,
        afterDecimalPoint: Int,
        rounding: Boolean
    ): CharArray {
        var v = value
        var isNegative = false
        if (v < 0) {
            isNegative = true
            v = -v
        }

        var powerOf10 = 1L
        for (i in 0 until afterDecimalPoint) {
            powerOf10 *= 10
        }

        if (rounding) {
            var roundingFactor = 0.5f
            for (i in 0 until afterDecimalPoint) {
                roundingFactor /= 10.0f
            }
            v += roundingFactor
        }

        val integerPart = v.toLong()
        val fractionalPart = v - integerPart

        var intLength = 1
        if (integerPart > 0) {
            var tempInt = integerPart
            intLength = 0
            while (tempInt > 0) {
                tempInt /= 10
                intLength++
            }
        }
        val actualBefore = min(beforeDecimalPoint, intLength)

        val integerChars = CharArray(actualBefore)
        var tempInt = integerPart
        for (i in actualBefore - 1 downTo 0) {
            integerChars[i] = ('0' + (tempInt % 10).toInt())
            tempInt /= 10
        }

        var tempFrac = (fractionalPart * powerOf10).toLong()

        var fracLength = 0
        if (afterDecimalPoint > 0) {
            var temp = tempFrac
            if (temp == 0L) {
                fracLength = 1
            } else {
                while (temp > 0 && temp % 10 == 0L) {
                    temp /= 10
                }
                if (temp > 0) {
                    var t = temp
                    while (t > 0) {
                        t /= 10
                        fracLength++
                    }
                }
            }
        }

        val actualAfter = min(afterDecimalPoint, fracLength)

        val fractionalChars = CharArray(actualAfter)
        tempFrac = (fractionalPart * powerOf10).toLong()
        for (i in actualAfter - 1 downTo 0) {
            fractionalChars[i] = ('0' + (tempFrac % 10).toInt())
            tempFrac /= 10
        }

        val totalLength = (if (isNegative) 1 else 0) + actualBefore + 1 + actualAfter
        val result = CharArray(totalLength)
        var currentIndex = 0

        if (isNegative) {
            result[currentIndex++] = '-'
        }
        for (i in 0 until actualBefore) {
            result[currentIndex++] = integerChars[i]
        }
        result[currentIndex++] = '.'
        for (i in 0 until actualAfter) {
            result[currentIndex++] = fractionalChars[i]
        }

        return result
    }
}
