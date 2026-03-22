// Copyright (C) 2024 The Android Open Source Project. Apache License 2.0.
package androidx.compose.remote.core.operations
import androidx.compose.remote.core.*
import androidx.compose.remote.core.serialize.MapSerializer
class TextMeasure(var mId: Int, var mTextId: Int, var mType: Int) : PaintOperation(), VariableSupport {
    override fun registerListening(context: RemoteContext) { context.listensTo(mTextId, this) }
    override fun updateVariables(context: RemoteContext) {}
    override fun markDirty() { super<PaintOperation>.markDirty() }
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mId, mTextId, mType) }
    override fun toString(): String = "TextMeasure[$mId] = $mTextId $mType"
    override fun deepToString(indent: String): String = indent + toString()
    private val mBounds = FloatArray(4)
    override fun paint(context: PaintContext) {
        val v = mType and 255; val flags = mType shr 8
        context.getTextBounds(mTextId, 0, -1, flags, mBounds)
        val ctx = context.getContext()
        when (v) { MEASURE_WIDTH -> ctx.loadFloat(mId, mBounds[2] - mBounds[0]); MEASURE_HEIGHT -> ctx.loadFloat(mId, mBounds[3] - mBounds[1])
            MEASURE_LEFT -> ctx.loadFloat(mId, mBounds[0]); MEASURE_TOP -> ctx.loadFloat(mId, mBounds[1])
            MEASURE_RIGHT -> ctx.loadFloat(mId, mBounds[2]); MEASURE_BOTTOM -> ctx.loadFloat(mId, mBounds[3]) }
    }
    override fun serialize(serializer: MapSerializer) { serializer.addType("TextMeasure").add("id", mId).add("textId", mTextId) }
    companion object {
        private val OP_CODE = Operations.TEXT_MEASURE; const val MEASURE_WIDTH = 0; const val MEASURE_HEIGHT = 1
        const val MEASURE_LEFT = 2; const val MEASURE_RIGHT = 3; const val MEASURE_TOP = 4; const val MEASURE_BOTTOM = 5
        const val MEASURE_MONOSPACE_FLAG = PaintContext.TEXT_MEASURE_MONOSPACE_WIDTH shl 8
        const val MEASURE_MAX_HEIGHT_FLAG = PaintContext.TEXT_MEASURE_FONT_HEIGHT shl 8
        fun name(): String = "TextMeasure"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, id: Int, textId: Int, type: Int) { buffer.start(OP_CODE); buffer.writeInt(id); buffer.writeInt(textId); buffer.writeInt(type) }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { operations.add(TextMeasure(buffer.readInt(), buffer.readInt(), buffer.readInt())) }
    }
}
