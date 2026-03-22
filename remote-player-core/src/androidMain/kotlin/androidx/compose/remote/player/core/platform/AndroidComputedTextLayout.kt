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

import android.text.StaticLayout
import androidx.compose.remote.core.RcPlatformServices

class AndroidComputedTextLayout(
    private var staticLayout: StaticLayout,
    override val width: Float,
    override val height: Float,
    private val mIsHyphenatedText: Boolean
) : RcPlatformServices.ComputedTextLayout {

    /** Set a StaticLayout on this container. */
    fun set(layout: StaticLayout) {
        staticLayout = layout
    }

    /** Retrieve the stored StaticLayout. */
    fun get(): StaticLayout = staticLayout

    override fun isHyphenatedText(): Boolean = mIsHyphenatedText
}
