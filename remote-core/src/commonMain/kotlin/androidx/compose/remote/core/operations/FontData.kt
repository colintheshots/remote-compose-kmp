// Copyright (C) 2024 The Android Open Source Project. Apache License 2.0.
package androidx.compose.remote.core.operations
import androidx.compose.remote.core.*
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable
class FontData(val mFontId: Int, @Suppress("UNUSED_PARAMETER") type: Int, var mFontData: ByteArray) : Operation(), Serializable {
    fun update(from: FontData) { mFontData = from.mFontData }
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mFontId, 0, mFontData) }
    override fun toString(): String = "FONT DATA $mFontId"
    override fun apply(context: RemoteContext) { context.loadFont(mFontId, mFontData) }
    override fun deepToString(indent: String): String = indent + toString()
    override fun serialize(serializer: MapSerializer) { serializer.addType("FontData").add("imageId", mFontId) }
    companion object {
        private val OP_CODE = Operations.DATA_FONT; fun name(): String = "FontData"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, fontId: Int, type: Int, fontData: ByteArray) { buffer.start(OP_CODE); buffer.writeInt(fontId); buffer.writeInt(type); buffer.writeBuffer(fontData) }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { val id = buffer.readInt(); val type = buffer.readInt(); val data = buffer.readBuffer(); operations.add(FontData(id, type, data)) }
    }
}
