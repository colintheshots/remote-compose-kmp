// Copyright (C) 2024 The Android Open Source Project. Apache License 2.0.
package androidx.compose.remote.core.operations
import androidx.compose.remote.core.*
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable
class TextAttribute(var mId: Int, var mTextId: Int, var mType: Short) : PaintOperation(), Serializable {
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mId, mTextId, mType) }
    override fun toString(): String = "TextAttribute[$mId] = $mTextId $mType"
    override fun deepToString(indent: String): String = indent + toString()
    private val mBounds = FloatArray(4)
    override fun paint(context: PaintContext) {
        val v = mType.toInt() and 255; val flags = mType.toInt() shr 8
        if (v <= MEASURE_BOTTOM) context.getTextBounds(mTextId, 0, -1, flags, mBounds)
        val ctx = context.getContext()
        when (v) { MEASURE_WIDTH.toInt() -> ctx.loadFloat(mId, mBounds[2] - mBounds[0]); MEASURE_HEIGHT.toInt() -> ctx.loadFloat(mId, mBounds[3] - mBounds[1])
            MEASURE_LEFT.toInt() -> ctx.loadFloat(mId, mBounds[0]); MEASURE_TOP.toInt() -> ctx.loadFloat(mId, mBounds[1])
            MEASURE_RIGHT.toInt() -> ctx.loadFloat(mId, mBounds[2]); MEASURE_BOTTOM.toInt() -> ctx.loadFloat(mId, mBounds[3])
            TEXT_LENGTH.toInt() -> ctx.loadFloat(mId, context.getText(mTextId)!!.length.toFloat()) }
    }
    override fun serialize(serializer: MapSerializer) { serializer.addType("TextMeasure").add("id", mId).add("textId", mTextId) }
    companion object {
        private val OP_CODE = Operations.ATTRIBUTE_TEXT
        const val MEASURE_WIDTH: Short = 0; const val MEASURE_HEIGHT: Short = 1; const val MEASURE_LEFT: Short = 2
        const val MEASURE_RIGHT: Short = 3; const val MEASURE_TOP: Short = 4; const val MEASURE_BOTTOM: Short = 5; const val TEXT_LENGTH: Short = 6
        fun name(): String = "TextMeasure"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, id: Int, textId: Int, type: Short) { buffer.start(OP_CODE); buffer.writeInt(id); buffer.writeInt(textId); buffer.writeShort(type.toInt()); buffer.writeShort(0) }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { val id = buffer.readInt(); val textId = buffer.readInt(); val type = buffer.readShort().toShort(); buffer.readShort(); operations.add(TextAttribute(id, textId, type)) }
    }
}
