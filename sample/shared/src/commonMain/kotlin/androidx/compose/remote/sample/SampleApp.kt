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
package androidx.compose.remote.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.remote.core.CoreDocument
import androidx.compose.remote.core.RemoteComposeBuffer
import androidx.compose.remote.core.SystemClock
import androidx.compose.remote.player.compose.RemoteDocumentPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A sample Compose Multiplatform app that demonstrates RemoteCompose document
 * creation and rendering.
 *
 * It creates three sample documents using [RemoteComposeWriter] and renders
 * each one with [RemoteDocumentPlayer].
 */
@Composable
fun SampleApp() {
    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("RemoteCompose Samples", style = MaterialTheme.typography.headlineSmall)

            SampleDocumentView(
                label = "Simple Rectangle",
                documentBytes = remember { createSimpleRectDocument() },
                documentWidth = 200,
                documentHeight = 200,
            )

            SampleDocumentView(
                label = "Text Rendering",
                documentBytes = remember { createTextDocument() },
                documentWidth = 300,
                documentHeight = 200,
            )

            SampleDocumentView(
                label = "Animated Rotation",
                documentBytes = remember { createAnimatedDocument() },
                documentWidth = 200,
                documentHeight = 200,
            )
        }
    }
}

/**
 * Displays a labeled RemoteCompose document.
 *
 * Loads the given [documentBytes] into a [CoreDocument] and renders it
 * using [RemoteDocumentPlayer].
 */
@Composable
private fun SampleDocumentView(
    label: String,
    documentBytes: ByteArray,
    documentWidth: Int,
    documentHeight: Int,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium)

        val coreDocument = remember(documentBytes) {
            val doc = CoreDocument(SystemClock())
            val buffer = RemoteComposeBuffer()
            buffer.getBuffer().loadFromBytes(documentBytes)
            doc.initFromBuffer(buffer)
            doc
        }

        RemoteDocumentPlayer(
            document = coreDocument,
            documentWidth = documentWidth,
            documentHeight = documentHeight,
        )
    }
}
