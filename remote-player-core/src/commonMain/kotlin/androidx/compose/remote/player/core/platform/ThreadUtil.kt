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

/** Thread utility methods. Platform-specific implementations should override. */
expect object ThreadUtil {
    /** Returns true if the current thread is the main thread. */
    fun isMainThread(): Boolean

    /** Checks that the current thread is the main thread. Otherwise throws an exception. */
    fun ensureMainThread()

    /** Executes the block directly if already on main thread, otherwise posts it. */
    fun runOnMainThread(block: () -> Unit)
}
