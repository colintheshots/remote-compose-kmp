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

/** Services that are needed to be provided by the platform during encoding. */
interface RcPlatformServices {
    fun imageToByteArray(image: Any): ByteArray?
    fun getImageWidth(image: Any): Int
    fun getImageHeight(image: Any): Int
    fun isAlpha8Image(image: Any): Boolean
    fun pathToFloatArray(path: Any): FloatArray?
    fun parsePath(pathData: String): Any
    fun log(category: LogCategory, message: String)

    enum class LogCategory {
        DEBUG, INFO, WARN, ERROR, TODO
    }

    interface ComputedTextLayout {
        val width: Float
        val height: Float
        fun isHyphenatedText(): Boolean
    }

    companion object {
        val None: RcPlatformServices = object : RcPlatformServices {
            override fun imageToByteArray(image: Any): ByteArray? =
                throw UnsupportedOperationException()

            override fun getImageWidth(image: Any): Int =
                throw UnsupportedOperationException()

            override fun getImageHeight(image: Any): Int =
                throw UnsupportedOperationException()

            override fun isAlpha8Image(image: Any): Boolean =
                throw UnsupportedOperationException()

            override fun pathToFloatArray(path: Any): FloatArray? =
                throw UnsupportedOperationException()

            override fun parsePath(pathData: String): Any =
                throw UnsupportedOperationException()

            override fun log(category: LogCategory, message: String) {}
        }
    }
}
