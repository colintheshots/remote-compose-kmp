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
package androidx.compose.remote.player.core.state

/** Represents a value used in a remote compose player. */
sealed interface RcValue {
    companion object
}

/** Represents an Int used in a remote compose player. */
data class RcInt(val value: Int?) : RcValue {
    companion object {
        val Null: RcInt = RcInt(null)
    }
}

/** Represents a Long used in a remote compose player. */
data class RcLong(val value: Long?) : RcValue {
    companion object {
        val Null: RcLong = RcLong(null)
    }
}

/** Represents a Float used in a remote compose player. */
data class RcFloat(val value: Float?) : RcValue {
    companion object {
        val Null: RcFloat = RcFloat(null)
    }
}

/** Represents a String used in a remote compose player. */
data class RcString(val value: String?) : RcValue {
    companion object {
        val Null: RcString = RcString(null)
    }
}

/**
 * Represents a Bitmap used in a remote compose player.
 *
 * The bitmap is stored as a platform-specific [Any] reference.
 */
data class RcBitmap(val value: Any?) : RcValue {
    companion object {
        val Null: RcBitmap = RcBitmap(null)
    }
}

/** Represents a color (as an Int) used in a remote compose player. */
data class RcColor(val value: Int?) : RcValue {
    companion object {
        val Null: RcColor = RcColor(null)
    }
}
