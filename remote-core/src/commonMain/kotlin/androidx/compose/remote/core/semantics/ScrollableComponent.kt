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
package androidx.compose.remote.core.semantics

import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.operations.layout.Component

/** Interface for components that support scrolling. */
interface ScrollableComponent : AccessibilitySemantics {
    companion object {
        const val SCROLL_NONE = 0
        const val SCROLL_HORIZONTAL = 1
        const val SCROLL_VERTICAL = 2
    }

    fun supportsScrollByOffset(): Boolean = true

    fun scrollByOffset(context: RemoteContext, offset: Int): Int = offset

    fun scrollDirection(context: RemoteContext, direction: ScrollDirection): Boolean = false

    fun showOnScreen(context: RemoteContext, child: Component): Boolean = false

    fun scrollDirection(): Int

    fun getScrollAxisRange(): ScrollAxisRange? = null

    class ScrollAxisRange(
        val mValue: Float,
        val maxValue: Float,
        val canScrollForward: Boolean,
        val canScrollBackwards: Boolean
    )

    enum class ScrollDirection {
        FORWARD, BACKWARD, UP, DOWN, LEFT, RIGHT
    }
}
