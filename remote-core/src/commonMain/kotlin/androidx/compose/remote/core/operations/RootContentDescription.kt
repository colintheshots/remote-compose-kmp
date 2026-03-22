// Copyright (C) 2024 The Android Open Source Project. Apache License 2.0.
package androidx.compose.remote.core.operations
import androidx.compose.remote.core.*
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable
class RootContentDescription(var mContentDescription: Int) : Operation(), RemoteComposeOperation, Serializable {
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mContentDescription) }
    override fun toString(): String = "RootContentDescription $mContentDescription"
    override fun apply(context: RemoteContext) { context.setDocumentContentDescription(mContentDescription) }
    override fun deepToString(indent: String): String = toString()
    override fun serialize(serializer: MapSerializer) { serializer.addType("RootContentDescription").add("contentDescriptionId", mContentDescription) }
    companion object {
        private val OP_CODE = Operations.ROOT_CONTENT_DESCRIPTION; fun name(): String = "RootContentDescription"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, contentDescription: Int) { buffer.start(OP_CODE); buffer.writeInt(contentDescription) }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { operations.add(RootContentDescription(buffer.readInt())) }
    }
}
