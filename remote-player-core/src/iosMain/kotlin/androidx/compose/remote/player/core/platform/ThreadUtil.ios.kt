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

import platform.Foundation.NSThread
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/** Thread utility methods for iOS. */
actual object ThreadUtil {
    actual fun isMainThread(): Boolean = NSThread.isMainThread

    actual fun ensureMainThread() {
        if (!isMainThread()) {
            throw IllegalStateException("Must be called on the main thread")
        }
    }

    actual fun runOnMainThread(block: () -> Unit) {
        if (isMainThread()) {
            block()
        } else {
            dispatch_async(dispatch_get_main_queue()) { block() }
        }
    }
}
