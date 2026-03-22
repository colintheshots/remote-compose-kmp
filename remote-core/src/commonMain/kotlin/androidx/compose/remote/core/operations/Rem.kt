// Copyright (C) 2024 The Android Open Source Project. Apache License 2.0.
package androidx.compose.remote.core.operations
import androidx.compose.remote.core.*
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable
class Rem(var mText: String) : Operation(), Serializable {
    fun update(from: Rem) { mText = from.mText }; override fun write(buffer: WireBuffer) { Companion.apply(buffer, mText) }
    override fun toString(): String = "Rem \"${Utils.trimString(mText, 10)}\""; override fun apply(context: RemoteContext) {}
    override fun deepToString(indent: String): String = indent + toString()
    override fun serialize(serializer: MapSerializer) { serializer.addType("Rem").add("text", mText) }
    companion object { private val OP_CODE = Operations.REM; const val MAX_STRING_SIZE = 4000; fun name(): String = "Rem"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, text: String) { buffer.start(OP_CODE); buffer.writeUTF8(text) }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { operations.add(Rem(buffer.readUTF8(MAX_STRING_SIZE))) } }
}
