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
package androidx.compose.remote.player.core

import androidx.compose.remote.core.CoreDocument
import androidx.compose.remote.core.RemoteComposeBuffer
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.SystemClock
import androidx.compose.remote.core.operations.layout.Component
import androidx.compose.remote.core.serialize.MapSerializer

/**
 * Public API to create a new RemoteComposeDocument coming from a byte array.
 */
class RemoteDocument {

    var document: CoreDocument

    constructor(inputBytes: ByteArray) {
        document = CoreDocument(SystemClock())
        val buffer = RemoteComposeBuffer()
        buffer.getBuffer().loadFromBytes(inputBytes)
        document.initFromBuffer(buffer)
    }

    constructor(document: CoreDocument) {
        this.document = document
    }

    /** Called when an initialization is needed. */
    fun initializeContext(context: RemoteContext) {
        document.initializeContext(context, null)
    }

    /** Called when an initialization is needed with a data map. */
    fun initializeContext(context: RemoteContext, map: Map<Int, Any>?) {
        document.initializeContext(context, map)
    }

    /** Returns the width of the document in pixels. */
    val width: Int get() = document.mWidth

    /** Returns the height of the document in pixels. */
    val height: Int get() = document.mHeight

    /** Paint the document. */
    fun paint(context: RemoteContext, theme: Int) {
        document.paint(context, theme)
    }

    /** The delay in milliseconds to next repaint; -1 = not needed, 0 = asap. */
    fun needsRepaint(): Int = document.needsRepaint()

    /** Returns true if the document can be displayed given this version of the player. */
    fun canBeDisplayed(majorVersion: Int, minorVersion: Int, capabilities: Long): Boolean =
        document.canBeDisplayed(majorVersion, minorVersion, capabilities)

    override fun toString(): String = "Document{\n$document}"

    /** Gets an array of names of the named colors defined in the loaded doc. */
    fun getNamedColors(): Array<String>? = document.getNamedColors()

    /** Gets an array of names of the named variables of a specific type defined in the doc. */
    fun getNamedVariables(type: Int): Array<String> = document.getNamedVariables(type)

    /** Return a component associated with id. */
    fun getComponent(id: Int): Component? = document.getComponent(id)

    /** Invalidate the document for layout measures. */
    fun invalidate() {
        document.invalidateMeasure()
    }

    /** Returns a list of useful statistics for the runtime document. */
    fun getStats(): Array<String> = document.getStats()

    /** Returns the number of sensor listeners. */
    fun hasSensorListeners(ids: IntArray): Int = 0

    /** Returns the current clock. */
    fun getClock() = document.getClock()

    /** Returns true if the current document is an update-only document. */
    fun isUpdateDoc(): Boolean = document.isUpdateDoc()

    /** Serialize the document. */
    fun serialize(serializer: MapSerializer) {
        document.serialize(serializer)
    }
}
