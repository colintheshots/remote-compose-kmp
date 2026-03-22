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
package androidx.compose.remote.player.core.platform

/**
 * BitmapLoader able to take URLs and return a ByteArray. This interface provides an abstraction
 * for loading bitmaps from URLs, allowing different environments or platforms to provide their
 * own implementation based on available network and image loading stacks.
 */
interface BitmapLoader {
    /**
     * Load a bitmap from a URL.
     *
     * @param url the url to fetch
     * @return a ByteArray with data.
     * @throws Exception on failure to load
     */
    fun loadBitmap(url: String): ByteArray

    companion object {
        /** A default loader that does not support loading bitmaps. */
        val UNSUPPORTED: BitmapLoader = object : BitmapLoader {
            override fun loadBitmap(url: String): ByteArray {
                throw UnsupportedOperationException("BitmapLoader not supported")
            }
        }
    }
}
