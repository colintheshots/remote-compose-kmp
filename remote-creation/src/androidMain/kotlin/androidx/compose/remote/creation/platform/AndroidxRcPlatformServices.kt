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

package androidx.compose.remote.creation.platform

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.remote.core.RcPlatformServices
import androidx.compose.remote.creation.RemotePath
import java.io.ByteArrayOutputStream

/**
 * Platform services needed by RemoteCompose core on Android.
 * Provides PNG compression, path conversion, and logging.
 */
open class AndroidxRcPlatformServices(private val logger: RCLogger = RCLogger.AndroidLog) :
    RcPlatformServices {

    private fun convertAlpha8ToARGB8888(alphaBitmap: Bitmap): Bitmap {
        if (alphaBitmap.config != Bitmap.Config.ALPHA_8) {
            return alphaBitmap
        }
        val argbBitmap =
            Bitmap.createBitmap(alphaBitmap.width, alphaBitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(argbBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        paint.color = 0xFF000000.toInt()
        canvas.drawBitmap(alphaBitmap, 0f, 0f, paint)
        return argbBitmap
    }

    override fun imageToByteArray(image: Any): ByteArray? {
        if (image is Bitmap) {
            val treatedImage = convertAlpha8ToARGB8888(image)
            val byteArrayBitmapStream = ByteArrayOutputStream()
            val successful =
                treatedImage.compress(Bitmap.CompressFormat.PNG, 90, byteArrayBitmapStream)
            assert(successful) { "Image could not be compressed" }
            return byteArrayBitmapStream.toByteArray()
        }
        return null
    }

    override fun getImageWidth(image: Any): Int {
        if (image is Bitmap) return image.width
        return 0
    }

    override fun getImageHeight(image: Any): Int {
        if (image is Bitmap) return image.height
        return 0
    }

    override fun isAlpha8Image(image: Any): Boolean {
        if (image is Bitmap) return image.config == Bitmap.Config.ALPHA_8
        return false
    }

    override fun pathToFloatArray(path: Any): FloatArray? {
        if (path is RemotePath) {
            return path.createFloatArray()
        }
        if (path is Path) {
            return androidPathToFloatArray(path)
        }
        return null
    }

    override fun parsePath(pathData: String): Any {
        val path = Path()
        val cords = FloatArray(6)

        val commands =
            pathData
                .split("(?=[MmZzLlHhVvCcSsQqTtAa])".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()
        for (command in commands) {
            val cmd = command[0]
            val values =
                command
                    .substring(1)
                    .trim { it <= ' ' }
                    .split("[,\\s]+".toRegex())
                    .dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            when (cmd) {
                'M' -> path.moveTo(values[0].toFloat(), values[1].toFloat())
                'L' -> {
                    var i = 0
                    while (i < values.size) {
                        path.lineTo(values[i].toFloat(), values[i + 1].toFloat())
                        i += 2
                    }
                }
                'H' ->
                    for (value in values) {
                        path.lineTo(value.toFloat(), cords[1])
                    }
                'C' -> {
                    var i = 0
                    while (i < values.size) {
                        path.cubicTo(
                            values[i].toFloat(),
                            values[i + 1].toFloat(),
                            values[i + 2].toFloat(),
                            values[i + 3].toFloat(),
                            values[i + 4].toFloat(),
                            values[i + 5].toFloat(),
                        )
                        i += 6
                    }
                }
                'S' -> {
                    var i = 0
                    while (i < values.size) {
                        path.cubicTo(
                            2 * cords[0] - cords[2],
                            2 * cords[1] - cords[3],
                            values[i].toFloat(),
                            values[i + 1].toFloat(),
                            values[i + 2].toFloat(),
                            values[i + 3].toFloat(),
                        )
                        i += 4
                    }
                }
                'Z' -> path.close()
                else -> throw IllegalArgumentException("Unsupported command: $cmd")
            }
            if (cmd != 'Z' && cmd != 'H') {
                cords[0] = values[values.size - 2].toFloat()
                cords[1] = values[values.size - 1].toFloat()
                if (cmd == 'C' || cmd == 'S') {
                    cords[2] = values[values.size - 4].toFloat()
                    cords[3] = values[values.size - 3].toFloat()
                }
            }
        }

        return path
    }

    private fun androidPathToFloatArray(path: Path): FloatArray? {
        // Android Path objects cannot be iterated without the androidx.graphics.path library.
        // Use RemotePath instead for path serialization in KMP.
        return null
    }

    override fun log(category: RcPlatformServices.LogCategory, message: String) {
        logger.log(category = category, message = message)
    }
}
