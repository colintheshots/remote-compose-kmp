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
package androidx.compose.remote.core

/**
 * Override of [RemoteComposeBuffer] that supports global optimizations. As the document is
 * written, RecordingRemoteComposeBuffer keeps track of which conditional container each operation
 * is written in (if any) and the operations it depends on. After all operations have been
 * written, [writeToBuffer] must be called and operations are reordered such that they're
 * in the tightest scope that allows their side effects to be visible everywhere they're referenced.
 */
class RecordingRemoteComposeBuffer(
    apiLevel: Int = CoreDocument.DOCUMENT_API_LEVEL
) : RemoteComposeBuffer(apiLevel) {

    // TODO: Full recording buffer implementation to be ported when needed.
    // This is a stub that delegates to the parent for now. The full Java implementation
    // uses a Span tree, SpanOp tracking, and DependencyExtractingRemoteContext for optimal
    // operation ordering within conditional scopes.

    fun writeToBuffer() {
        // In the full implementation, this writes operations from the span tree to the buffer
        // in optimal order. For now, operations are written directly.
    }
}
