/*
 * Copyright (C) 2025 The Android Open Source Project
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
package androidx.compose.remote.core.operations

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.PaintOperation
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.VariableSupport
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.serialize.MapSerializer
import kotlin.math.max
import kotlin.math.min

/** Draw bitmap text anchored to a point */
class DrawBitmapTextAnchored(
    private val mTextID: Int,
    private val mBitmapFontID: Int,
    private val mStart: Float,
    private val mEnd: Float,
    private val mX: Float,
    private val mY: Float,
    private val mPanX: Float,
    private val mPanY: Float
) : PaintOperation(), VariableSupport {

    private var mOutStart: Float = mStart
    private var mOutEnd: Float = mEnd
    private var mOutX: Float = mX
    private var mOutY: Float = mY
    private var mOutPanX: Float = mPanX
    private var mOutPanY: Float = mPanY
    private val mBounds = FloatArray(4)

    override fun updateVariables(context: RemoteContext) {
        context.listensTo(mTextID, this)
        mOutX = if (mX.isNaN()) context.getFloat(Utils.idFromNan(mX)) else mX
        mOutY = if (mY.isNaN()) context.getFloat(Utils.idFromNan(mY)) else mY
        mOutPanX = if (mPanX.isNaN()) context.getFloat(Utils.idFromNan(mPanX)) else mPanX
        mOutPanY = if (mPanY.isNaN()) context.getFloat(Utils.idFromNan(mPanY)) else mPanY
        mOutStart = if (mStart.isNaN()) context.getFloat(Utils.idFromNan(mStart)) else mStart
        mOutEnd = if (mEnd.isNaN()) context.getFloat(Utils.idFromNan(mEnd)) else mEnd
    }

    override fun registerListening(context: RemoteContext) {
        if (mX.isNaN()) context.listensTo(Utils.idFromNan(mX), this)
        if (mY.isNaN()) context.listensTo(Utils.idFromNan(mY), this)
        if (mPanX.isNaN()) context.listensTo(Utils.idFromNan(mPanX), this)
        if (mPanY.isNaN()) context.listensTo(Utils.idFromNan(mPanY), this)
        if (mStart.isNaN()) context.listensTo(Utils.idFromNan(mStart), this)
        if (mEnd.isNaN()) context.listensTo(Utils.idFromNan(mEnd), this)
    }

    override fun write(buffer: WireBuffer) {
        apply(buffer, mTextID, mBitmapFontID, mStart, mEnd, mX, mY, mPanX, mPanY)
    }

    override fun toString(): String =
        "DrawBitmapFontText [$mTextID] $mBitmapFontID, $mStart, $mEnd, " +
            "${Utils.floatToString(mX, mOutX)}, ${Utils.floatToString(mY, mOutY)}"

    private fun measure(bitmapFont: BitmapFontData, textToMeasure: String) {
        val xMin = 0f
        var yMin = 1000f
        var xMax = 0f
        var yMax = -Float.MAX_VALUE
        var xPos = 0f
        var pos = 0
        while (pos < textToMeasure.length) {
            val glyph = bitmapFont.lookupGlyph(textToMeasure, pos)
            if (glyph == null) {
                pos++
                continue
            }
            pos += glyph.mChars!!.length
            xPos += glyph.mMarginLeft + glyph.mMarginRight
            if (glyph.mBitmapId != -1) {
                xPos += glyph.mBitmapWidth
            }
            xMax = xPos
            yMax = max(yMax, (glyph.mBitmapHeight + glyph.mMarginTop + glyph.mMarginBottom).toFloat())
            yMin = min(yMin, glyph.mMarginTop.toFloat())
        }
        mBounds[0] = xMin
        mBounds[1] = yMin
        mBounds[2] = xMax
        mBounds[3] = yMax
    }

    private fun getHorizontalOffset(): Float {
        val scale = 1.0f
        val textWidth = scale * (mBounds[2] - mBounds[0])
        val boxWidth = 0f
        return (boxWidth - textWidth) * (1 + mOutPanX) / 2f - (scale * mBounds[0])
    }

    private fun getVerticalOffset(): Float {
        val scale = 1.0f
        val boxHeight = 0f
        val textHeight = scale * (mBounds[3] - mBounds[1])
        return (boxHeight - textHeight) * (1 - mOutPanY) / 2 - (scale * mBounds[1])
    }

    override fun paint(context: PaintContext) {
        val remoteContext = context.getContext()
        var textToPaint = remoteContext.getText(mTextID) ?: return
        val end = mOutEnd.toInt()
        val start = mOutStart.toInt()

        textToPaint = textToPaint.substring(
            maxOf(start, 0),
            if (end < 0 || end > textToPaint.length) textToPaint.length else end
        )
        val bitmapFont = remoteContext.getObject(mBitmapFontID) as? BitmapFontData ?: return
        measure(bitmapFont, textToPaint)

        var xPos = mOutX + getHorizontalOffset()
        val yPos = mOutY + getVerticalOffset()

        var pos = 0
        var prevGlyph = ""
        while (pos < textToPaint.length) {
            val glyph = bitmapFont.lookupGlyph(textToPaint, pos)
            if (glyph == null) {
                pos++
                prevGlyph = ""
                continue
            }
            pos += glyph.mChars!!.length
            if (glyph.mBitmapId == -1) {
                xPos += glyph.mMarginLeft + glyph.mMarginRight
                prevGlyph = glyph.mChars ?: ""
                continue
            }
            xPos += glyph.mMarginLeft
            val kerningAdjustment = bitmapFont.mKerningTable[prevGlyph + glyph.mChars]
            if (kerningAdjustment != null) {
                xPos += kerningAdjustment
            }
            val xPos2 = xPos + glyph.mBitmapWidth
            context.drawBitmap(
                glyph.mBitmapId,
                xPos, yPos + glyph.mMarginTop,
                xPos2, yPos + glyph.mBitmapHeight + glyph.mMarginTop
            )
            xPos = xPos2 + glyph.mMarginRight
            prevGlyph = glyph.mChars ?: ""
        }
    }

    override fun serialize(serializer: MapSerializer) {
        serializer
            .addType(CLASS_NAME)
            .add("textId", mTextID)
            .add("bitmapFontId", mBitmapFontID)
            .add("start", mStart)
            .add("end", mEnd)
            .add("x", mX, mOutX)
            .add("y", mY, mOutY)
    }

    companion object {
        private val OP_CODE = Operations.DRAW_BITMAP_TEXT_ANCHORED
        private const val CLASS_NAME = "DrawBitmapTextAnchored"

        fun name(): String = CLASS_NAME
        fun id(): Int = OP_CODE

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val text = buffer.readInt()
            val bitmapFont = buffer.readInt()
            val start = buffer.readFloat()
            val end = buffer.readFloat()
            val x = buffer.readFloat()
            val y = buffer.readFloat()
            val panX = buffer.readFloat()
            val panY = buffer.readFloat()
            operations.add(DrawBitmapTextAnchored(text, bitmapFont, start, end, x, y, panX, panY))
        }

        fun apply(
            buffer: WireBuffer,
            textId: Int, bitmapFontID: Int,
            start: Float, end: Float, x: Float, y: Float, panX: Float, panY: Float
        ) {
            buffer.start(OP_CODE)
            buffer.writeInt(textId)
            buffer.writeInt(bitmapFontID)
            buffer.writeFloat(start)
            buffer.writeFloat(end)
            buffer.writeFloat(x)
            buffer.writeFloat(y)
            buffer.writeFloat(panX)
            buffer.writeFloat(panY)
        }
    }
}
