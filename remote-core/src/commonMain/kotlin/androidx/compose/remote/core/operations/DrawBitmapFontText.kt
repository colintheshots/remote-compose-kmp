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

/** Draw bitmap font text */
class DrawBitmapFontText(
    private val mTextID: Int,
    private val mBitmapFontID: Int,
    private val mStart: Int,
    private val mEnd: Int,
    private val mX: Float,
    private val mY: Float
) : PaintOperation(), VariableSupport {

    private var mOutX: Float = mX
    private var mOutY: Float = mY

    override fun updateVariables(context: RemoteContext) {
        mOutX = if (mX.isNaN()) context.getFloat(Utils.idFromNan(mX)) else mX
        mOutY = if (mY.isNaN()) context.getFloat(Utils.idFromNan(mY)) else mY
    }

    override fun registerListening(context: RemoteContext) {
        context.listensTo(mTextID, this)
        if (mX.isNaN()) context.listensTo(Utils.idFromNan(mX), this)
        if (mY.isNaN()) context.listensTo(Utils.idFromNan(mY), this)
    }

    override fun write(buffer: WireBuffer) {
        apply(buffer, mTextID, mBitmapFontID, mStart, mEnd, mX, mY)
    }

    override fun toString(): String =
        "DrawBitmapFontText [$mTextID] $mBitmapFontID, $mStart, $mEnd, " +
            "${Utils.floatToString(mX, mOutX)}, ${Utils.floatToString(mY, mOutY)}"

    override fun paint(context: PaintContext) {
        val remoteContext = context.getContext()
        var textToPaint = remoteContext.getText(mTextID) ?: return
        if (mEnd == -1) {
            if (mStart != 0) {
                textToPaint = textToPaint.substring(mStart)
            }
        } else if (mEnd > textToPaint.length) {
            textToPaint = textToPaint.substring(mStart)
        } else {
            textToPaint = textToPaint.substring(mStart, mEnd)
        }

        val bitmapFont = remoteContext.getObject(mBitmapFontID) as? BitmapFontData ?: return

        var xPos = mOutX
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
                xPos, mOutY + glyph.mMarginTop,
                xPos2, mOutY + glyph.mBitmapHeight + glyph.mMarginTop
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
        private val OP_CODE = Operations.DRAW_BITMAP_FONT_TEXT_RUN
        private const val CLASS_NAME = "DrawBitmapFontText"

        fun name(): String = CLASS_NAME
        fun id(): Int = OP_CODE

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val text = buffer.readInt()
            val bitmapFont = buffer.readInt()
            val start = buffer.readInt()
            val end = buffer.readInt()
            val x = buffer.readFloat()
            val y = buffer.readFloat()
            operations.add(DrawBitmapFontText(text, bitmapFont, start, end, x, y))
        }

        fun apply(
            buffer: WireBuffer,
            textId: Int, bitmapFontID: Int, start: Int, end: Int, x: Float, y: Float
        ) {
            buffer.start(OP_CODE)
            buffer.writeInt(textId)
            buffer.writeInt(bitmapFontID)
            buffer.writeInt(start)
            buffer.writeInt(end)
            buffer.writeFloat(x)
            buffer.writeFloat(y)
        }
    }
}
