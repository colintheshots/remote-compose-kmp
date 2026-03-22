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

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.utilities.StringSerializer
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.SerializeTags

/** Implementation of the most common semantics used in typical Android apps. */
class CoreSemantics : Operation, AccessibilityModifier {

    // Re-export nested types for backward compatibility
    companion object {
        fun apply(
            buffer: WireBuffer, contentDescriptionId: Int, role: Byte, textId: Int,
            stateDescriptionId: Int, mode: Int, enabled: Boolean, clickable: Boolean
        ) {
            buffer.start(Operations.ACCESSIBILITY_SEMANTICS)
            buffer.writeInt(contentDescriptionId)
            buffer.writeByte(role.toInt())
            buffer.writeInt(textId)
            buffer.writeInt(stateDescriptionId)
            buffer.writeByte(mode)
            buffer.writeBoolean(enabled)
            buffer.writeBoolean(clickable)
        }

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val semantics = CoreSemantics()
            semantics.readFromBuffer(buffer)
            operations.add(semantics)
        }
    }

    enum class Mode { SET, CLEAR_AND_SET, MERGE }

    var mContentDescriptionId: Int = 0
    var mRole: AccessibleComponent.Role? = null
    var mTextId: Int = 0
    var mStateDescriptionId: Int = 0
    var mEnabled: Boolean = true
    var mMode: Mode = Mode.SET
    var mClickable: Boolean = false

    constructor()

    constructor(
        contentDescriptionId: Int, role: Byte, textId: Int,
        stateDescriptionId: Int, mode: Int, enabled: Boolean, clickable: Boolean
    ) {
        mContentDescriptionId = contentDescriptionId
        mRole = AccessibleComponent.Role.fromInt(role.toInt())
        mTextId = textId
        mStateDescriptionId = stateDescriptionId
        mEnabled = enabled
        mMode = Mode.values()[mode]
        mClickable = clickable
    }

    override fun getOpCode(): Int = Operations.ACCESSIBILITY_SEMANTICS

    override fun getRole(): AccessibleComponent.Role? = mRole

    override fun getMode(): CoreSemantics.Mode = mMode

    override fun write(buffer: WireBuffer) {
        buffer.writeInt(mContentDescriptionId)
        buffer.writeByte(if (mRole != null) mRole!!.ordinal else -1)
        buffer.writeInt(mTextId)
        buffer.writeInt(mStateDescriptionId)
        buffer.writeByte(mMode.ordinal)
        buffer.writeBoolean(mEnabled)
        buffer.writeBoolean(mClickable)
    }

    private fun readFromBuffer(buffer: WireBuffer) {
        mContentDescriptionId = buffer.readInt()
        mRole = AccessibleComponent.Role.fromInt(buffer.readByte())
        mTextId = buffer.readInt()
        mStateDescriptionId = buffer.readInt()
        mMode = Mode.values()[buffer.readByte()]
        mEnabled = buffer.readBoolean()
        mClickable = buffer.readBoolean()
    }

    override fun apply(context: RemoteContext) {
        // Handled via touch helper
    }

    override fun toString(): String = buildString {
        append("SEMANTICS")
        if (mMode != Mode.SET) { append(" "); append(mMode) }
        if (mRole != null) { append(" "); append(mRole) }
        if (mContentDescriptionId > 0) append(" contentDescription=$mContentDescriptionId")
        if (mTextId > 0) append(" text=$mTextId")
        if (mStateDescriptionId > 0) append(" stateDescription=$mStateDescriptionId")
        if (!mEnabled) append(" disabled")
        if (mClickable) append(" clickable")
    }

    override fun deepToString(indent: String): String = "$indent$this"

    override fun serializeToString(indent: Int, serializer: StringSerializer) {
        serializer.append(indent, "SEMANTICS = $this")
    }

    override fun getContentDescriptionId(): Int? =
        if (mContentDescriptionId != 0) mContentDescriptionId else null

    fun getStateDescriptionId(): Int? =
        if (mStateDescriptionId != 0) mStateDescriptionId else null

    override fun getTextId(): Int? =
        if (mTextId != 0) mTextId else null

    fun serialize(serializer: MapSerializer) {
        serializer
            .addTags(SerializeTags.MODIFIER, SerializeTags.A11Y)
            .addType("CoreSemantics")
            .add("contentDescriptionId", mContentDescriptionId)
            .add("role", mRole)
            .add("textId", mTextId)
            .add("stateDescriptionId", mStateDescriptionId)
            .add("enabled", mEnabled)
            .add("mode", mMode)
            .add("clickable", mClickable)
    }
}
