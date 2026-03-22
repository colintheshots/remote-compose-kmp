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

package androidx.compose.remote.creation.platform

import androidx.compose.remote.core.RcPlatformServices
import androidx.compose.remote.creation.RemotePath

/**
 * Common (multiplatform) implementation of [RcPlatformServices].
 * Provides path-related services that work across platforms.
 * Image services are left as stubs -- platform-specific subclasses
 * should override them.
 */
open class CommonRcPlatformServices : RcPlatformServices {
    override fun imageToByteArray(image: Any): ByteArray? {
        // Override in platform-specific subclass
        return null
    }

    override fun getImageWidth(image: Any): Int = 0

    override fun getImageHeight(image: Any): Int = 0

    override fun isAlpha8Image(image: Any): Boolean = false

    override fun pathToFloatArray(path: Any): FloatArray? {
        if (path is RemotePath) {
            return path.createFloatArray()
        }
        return null
    }

    override fun parsePath(pathData: String): Any {
        return RemotePath(pathData)
    }

    override fun log(category: RcPlatformServices.LogCategory, message: String) {
        // Default: no-op. Override in platform-specific subclass.
    }
}
