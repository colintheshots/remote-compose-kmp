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

/** Base class for RemoteCompose operations */
abstract class Operation {

    companion object {
        private const val ENABLE_DIRTY_FLAG_OPTIMIZATION = true
    }

    private var dirty = true

    abstract fun write(buffer: WireBuffer)
    abstract fun apply(context: RemoteContext)
    abstract fun deepToString(indent: String): String

    open fun markDirty() {
        dirty = true
    }

    fun markNotDirty() {
        if (ENABLE_DIRTY_FLAG_OPTIMIZATION) {
            dirty = false
        }
    }

    fun isDirty(): Boolean {
        return if (ENABLE_DIRTY_FLAG_OPTIMIZATION) dirty else true
    }
}
