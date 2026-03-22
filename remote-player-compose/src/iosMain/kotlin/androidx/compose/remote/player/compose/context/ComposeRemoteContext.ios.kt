/*
 * Copyright 2025 The Android Open Source Project
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
package androidx.compose.remote.player.compose.context

import androidx.compose.remote.core.operations.BitmapData
import androidx.compose.remote.player.core.platform.BitmapLoader
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo

internal actual fun decodeBitmapPlatform(
    encoding: Short,
    type: Short,
    width: Int,
    height: Int,
    data: ByteArray,
    bitmapLoader: BitmapLoader,
): Any? {
    return when (encoding) {
        BitmapData.ENCODING_INLINE -> {
            when (type) {
                BitmapData.TYPE_PNG_8888, BitmapData.TYPE_PNG -> {
                    Image.makeFromEncoded(data)
                }
                BitmapData.TYPE_PNG_ALPHA_8 -> {
                    Image.makeFromEncoded(data)
                }
                BitmapData.TYPE_RAW8888 -> {
                    val imageInfo = ImageInfo(width, height, ColorType.RGBA_8888, ColorAlphaType.PREMUL)
                    val bitmap = Bitmap()
                    bitmap.allocPixels(imageInfo)
                    // Convert ARGB byte data to RGBA for Skia
                    val pixelData = ByteArray(width * height * 4)
                    for (i in 0 until width * height) {
                        val p = i * 4
                        // Input is ARGB, Skia RGBA_8888 expects RGBA
                        pixelData[p] = data[p + 1]     // R
                        pixelData[p + 1] = data[p + 2] // G
                        pixelData[p + 2] = data[p + 3] // B
                        pixelData[p + 3] = data[p]     // A
                    }
                    bitmap.installPixels(imageInfo, pixelData, width * 4)
                    Image.makeFromBitmap(bitmap)
                }
                BitmapData.TYPE_RAW8 -> {
                    // Raw 8-bit grayscale - convert to RGBA
                    val imageInfo = ImageInfo(width, height, ColorType.RGBA_8888, ColorAlphaType.PREMUL)
                    val bitmap = Bitmap()
                    bitmap.allocPixels(imageInfo)
                    val pixelData = ByteArray(width * height * 4)
                    val pixelCount = (data.size / 4).coerceAtMost(width * height)
                    for (i in 0 until pixelCount) {
                        val gray = data[i].toInt() and 0xFF
                        val p = i * 4
                        pixelData[p] = gray.toByte()     // R
                        pixelData[p + 1] = gray.toByte() // G
                        pixelData[p + 2] = gray.toByte() // B
                        pixelData[p + 3] = gray.toByte() // A (use gray as alpha like Android)
                    }
                    bitmap.installPixels(imageInfo, pixelData, width * 4)
                    Image.makeFromBitmap(bitmap)
                }
                else -> Image.makeFromEncoded(data)
            }
        }
        BitmapData.ENCODING_URL -> {
            val bytes = bitmapLoader.loadBitmap(data.decodeToString())
            Image.makeFromEncoded(bytes)
        }
        BitmapData.ENCODING_EMPTY -> {
            val imageInfo = ImageInfo.makeN32Premul(width, height)
            val bitmap = Bitmap()
            bitmap.allocPixels(imageInfo)
            Image.makeFromBitmap(bitmap)
        }
        else -> null
    }
}
