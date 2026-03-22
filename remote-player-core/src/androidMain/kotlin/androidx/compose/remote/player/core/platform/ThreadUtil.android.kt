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

import android.os.Handler
import android.os.Looper

/** Thread utility methods for Android. */
actual object ThreadUtil {
    private val mainThread: Thread = Looper.getMainLooper().thread
    private val mainThreadHandler: Handler = Handler(Looper.getMainLooper())

    /** Returns true if the current thread is the main thread. */
    actual fun isMainThread(): Boolean = Thread.currentThread() == mainThread

    /** Checks that the current thread is the main thread. Otherwise throws an exception. */
    actual fun ensureMainThread() {
        if (!isMainThread()) {
            throw IllegalStateException("Must be called on the main thread")
        }
    }

    /**
     * Executes the block directly if already on main thread, otherwise, post it on the main
     * thread.
     */
    actual fun runOnMainThread(block: () -> Unit) {
        if (isMainThread()) {
            block()
        } else {
            mainThreadHandler.post(block)
        }
    }
}
