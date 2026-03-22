// Copyright (C) 2024 The Android Open Source Project. Apache License 2.0.
package androidx.compose.remote.core.operations
import androidx.compose.remote.core.*
import androidx.compose.remote.core.serialize.MapSerializer
import kotlin.math.max
import kotlin.math.min
class BitmapTextMeasure(var mId: Int, var mTextId: Int, private val mBitmapFontId: Int, var mType: Int) : PaintOperation() {
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mId, mTextId, mBitmapFontId, mType) }
    override fun toString(): String = "BitmapTextMeasure[$mId] = $mTextId $mBitmapFontId $mType"
    override fun deepToString(indent: String): String = indent + toString()
    private val mBounds = FloatArray(4)
    override fun paint(context: PaintContext) {
        val rc = context.getContext(); val bf = rc.getObject(mBitmapFontId) as BitmapFontData; val text = rc.getText(mTextId) ?: return
        var xMax = 0f; var yMax = -Float.MAX_VALUE; var yMin = 1000f; var xPos = 0f; var pos = 0; var prev = ""
        while (pos < text.length) {
            val g = bf.lookupGlyph(text, pos); if (g == null) { pos++; prev = ""; continue }
            pos += g.mChars!!.length; xPos += g.mMarginLeft + g.mMarginRight
            if (g.mBitmapId != -1) xPos += g.mBitmapWidth
            bf.mKerningTable[prev + g.mChars]?.let { xPos += it }
            xMax = xPos; yMax = max(yMax, (g.mBitmapHeight + g.mMarginTop + g.mMarginBottom).toFloat()); yMin = min(yMin, g.mMarginTop.toFloat()); prev = g.mChars!!
        }
        mBounds[0] = 0f; mBounds[1] = yMin; mBounds[2] = xMax; mBounds[3] = yMax
        val v = mType and 255; val ctx = context.getContext()
        when (v) { 0 -> ctx.loadFloat(mId, mBounds[2] - mBounds[0]); 1 -> ctx.loadFloat(mId, mBounds[3] - mBounds[1]); 2 -> ctx.loadFloat(mId, mBounds[0]); 4 -> ctx.loadFloat(mId, mBounds[1]); 3 -> ctx.loadFloat(mId, mBounds[2]); 5 -> ctx.loadFloat(mId, mBounds[3]) }
    }
    override fun serialize(serializer: MapSerializer) { serializer.addType("BitmapTextMeasure").add("id", mId).add("textId", mTextId).add("bitmapFontId", mBitmapFontId) }
    companion object {
        private val OP_CODE = Operations.BITMAP_TEXT_MEASURE; fun name(): String = "BitmapTextMeasure"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, id: Int, textId: Int, bitmapFontId: Int, type: Int) { buffer.start(OP_CODE); buffer.writeInt(id); buffer.writeInt(textId); buffer.writeInt(bitmapFontId); buffer.writeInt(type) }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { operations.add(BitmapTextMeasure(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt())) }
    }
}
