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

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import androidx.compose.remote.core.operations.BitmapData
import androidx.compose.remote.player.core.platform.BitmapLoader

internal actual fun decodeBitmapPlatform(
    encoding: Short,
    type: Short,
    width: Int,
    height: Int,
    data: ByteArray,
    bitmapLoader: BitmapLoader,
): Any? {
    var image: Bitmap? = null
    when (encoding) {
        BitmapData.ENCODING_INLINE -> {
            when (type) {
                BitmapData.TYPE_PNG_8888 -> {
                    val opts = BitmapFactory.Options()
                    opts.inJustDecodeBounds = true
                    BitmapFactory.decodeByteArray(data, 0, data.size, opts)
                    if (opts.outWidth > width || opts.outHeight > height) {
                        throw RuntimeException(
                            "dimension don't match ${opts.outWidth}x${opts.outHeight} vs ${width}x${height}"
                        )
                    }
                    image = BitmapFactory.decodeByteArray(data, 0, data.size)
                }
                BitmapData.TYPE_PNG_ALPHA_8 -> {
                    val options = BitmapFactory.Options()
                    options.inPreferredConfig = Bitmap.Config.ALPHA_8
                    image = BitmapFactory.decodeByteArray(data, 0, data.size, options)

                    if (image!!.config != Bitmap.Config.ALPHA_8) {
                        val alpha8Bitmap = Bitmap.createBitmap(
                            image.width, image.height, Bitmap.Config.ALPHA_8
                        )
                        val canvas = Canvas(alpha8Bitmap)
                        val paint = Paint()
                        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
                        canvas.drawBitmap(image, 0f, 0f, paint)
                        image.recycle()
                        image = alpha8Bitmap
                    }
                }
                BitmapData.TYPE_RAW8888 -> {
                    image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val intData = IntArray(data.size / 4)
                    for (i in intData.indices) {
                        val p = i * 4
                        intData[i] = ((data[p].toInt() shl 24) or
                            ((data[p + 1].toInt() and 0xFF) shl 16) or
                            ((data[p + 2].toInt() and 0xFF) shl 8) or
                            (data[p + 3].toInt() and 0xFF))
                    }
                    image.setPixels(intData, 0, width, 0, 0, width, height)
                }
                BitmapData.TYPE_RAW8 -> {
                    image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val bitmapData = IntArray(data.size / 4)
                    for (i in bitmapData.indices) {
                        bitmapData[i] = 0x1010101 * data[i].toInt()
                    }
                    image.setPixels(bitmapData, 0, width, 0, 0, width, height)
                }
            }
        }
        BitmapData.ENCODING_FILE -> {
            image = BitmapFactory.decodeFile(String(data))
        }
        BitmapData.ENCODING_URL -> {
            val bytes = bitmapLoader.loadBitmap(String(data))
            image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
        BitmapData.ENCODING_EMPTY -> {
            image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        }
    }
    return image
}
