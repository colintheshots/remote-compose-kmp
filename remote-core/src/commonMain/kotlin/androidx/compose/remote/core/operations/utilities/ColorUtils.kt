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

/**
 * These are tools to use long Color as variables. Long colors are stored as 0xXXXXXXXX XXXXXX??
 * In SRGB the colors are stored 0xAARRGGBB,00000000. SRGB color space is color space 0.
 * Our Color will use color float with a current android supports SRGB, LINEAR_SRGB, etc.
 *
 * Our color space will be 62 (MAX_ID-1). (0x3E) Storing the default value in SRGB format and
 * having the id of the color between the ARGB values and the 62
 * i.e. 0xAARRGGBB 00 00 00 3E
 */
class ColorUtils {

    fun packRCColor(defaultARGB: Int, id: Int): Long {
        val l = defaultARGB.toLong()
        return (l shl 32) or (id.toLong() shl 8) or sRC_COLOR.toLong()
    }

    fun isRCColor(color: Long): Boolean {
        return (color and 0x3F) == 62L
    }

    fun getID(color: Long): Int {
        return if (isRCColor(color)) {
            ((color and 0xFFFFFF00) shr 8).toInt()
        } else {
            -1
        }
    }

    /**
     * Get default color from long color
     *
     * @param color the long color
     * @return default color as int
     */
    fun getDefaultColor(color: Long): Int {
        if (isRCColor(color)) {
            return (color shr 32).toInt()
        }
        if ((color and 0xFF) == 0L) {
            return (color shr 32).toInt()
        }
        return 0
    }

    companion object {
        var sRC_COLOR = 62

        /**
         * Utility function to create a color as an int
         *
         * @param r red
         * @param g green
         * @param b blue
         * @param a alpha
         * @return int packed color
         */
        fun createColor(r: Int, g: Int, b: Int, a: Int): Int {
            return (a shl 24) or (r shl 16) or (g shl 8) or b
        }
    }
}
