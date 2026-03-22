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

/** Draw bitmap font text on a path */
class DrawBitmapFontTextOnPath(
    private val mTextID: Int,
    private val mBitmapFontID: Int,
    private val mPathID: Int,
    private val mStart: Int,
    private val mEnd: Int,
    private val mYAdj: Float
) : PaintOperation(), VariableSupport {

    private var mOutYAdj: Float = 0f

    override fun updateVariables(context: RemoteContext) {
        mOutYAdj = if (mYAdj.isNaN()) context.getFloat(Utils.idFromNan(mYAdj)) else mYAdj
    }

    override fun registerListening(context: RemoteContext) {
        context.listensTo(mTextID, this)
        if (mYAdj.isNaN()) context.listensTo(Utils.idFromNan(mYAdj), this)
    }

    override fun write(buffer: WireBuffer) {
        apply(buffer, mTextID, mBitmapFontID, mPathID, mStart, mEnd, mYAdj)
    }

    override fun toString(): String =
        "DrawBitmapFontTextOnPath [$mTextID] $mBitmapFontID, $mPathID, $mStart, $mEnd, $mYAdj"

    private fun measureWidth(text: String, bitmapFont: BitmapFontData): Int {
        var pos = 0
        var width = 0
        var prevGlyph = ""
        while (pos < text.length) {
            val glyph = bitmapFont.lookupGlyph(text, pos)
            if (glyph == null) {
                pos++
                prevGlyph = ""
                continue
            }
            pos += glyph.mChars!!.length
            if (glyph.mBitmapId == -1) {
                width += (glyph.mMarginLeft + glyph.mMarginRight).toInt()
                prevGlyph = ""
                continue
            }
            width += glyph.mMarginLeft.toInt()
            val kerningAdjustment = bitmapFont.mKerningTable[prevGlyph + glyph.mChars]
            if (kerningAdjustment != null) {
                width += kerningAdjustment
            }
            width += (glyph.mBitmapWidth + glyph.mMarginRight).toInt()
            prevGlyph = glyph.mChars ?: ""
        }
        return width
    }

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

        val width = measureWidth(textToPaint, bitmapFont).toFloat()
        var progress = 0f
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
                progress += glyph.mMarginLeft + glyph.mMarginRight
                prevGlyph = ""
                continue
            }
            progress += glyph.mMarginLeft
            val kerningAdjustment = bitmapFont.mKerningTable[prevGlyph + glyph.mChars]
            if (kerningAdjustment != null) {
                progress += kerningAdjustment
            }
            val halfGlyphWidth = 0.5f * glyph.mBitmapWidth
            val fractionAtMiddleOfGlyph = (progress + halfGlyphWidth) / width
            context.save()
            context.matrixFromPath(mPathID, fractionAtMiddleOfGlyph, 0f, 3)
            context.drawBitmap(
                glyph.mBitmapId,
                -halfGlyphWidth,
                mOutYAdj + glyph.mMarginTop,
                halfGlyphWidth,
                mOutYAdj + glyph.mBitmapHeight + glyph.mMarginTop
            )
            progress += glyph.mBitmapWidth + glyph.mMarginRight
            prevGlyph = glyph.mChars ?: ""
            context.restore()
        }
    }

    override fun serialize(serializer: MapSerializer) {
        serializer
            .addType(CLASS_NAME)
            .add("textId", mTextID)
            .add("bitmapFontId", mBitmapFontID)
            .add("path", mPathID)
            .add("start", mStart)
            .add("end", mEnd)
    }

    companion object {
        private val OP_CODE = Operations.DRAW_BITMAP_FONT_TEXT_RUN_ON_PATH
        private const val CLASS_NAME = "DrawBitmapFontTextOnPath"

        fun name(): String = CLASS_NAME
        fun id(): Int = OP_CODE

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val text = buffer.readInt()
            val bitmapFont = buffer.readInt()
            val path = buffer.readInt()
            val start = buffer.readInt()
            val end = buffer.readInt()
            val yAdj = buffer.readFloat()
            operations.add(DrawBitmapFontTextOnPath(text, bitmapFont, path, start, end, yAdj))
        }

        fun apply(
            buffer: WireBuffer,
            textId: Int, bitmapFontID: Int, pathID: Int, start: Int, end: Int, yAdj: Float
        ) {
            buffer.start(OP_CODE)
            buffer.writeInt(textId)
            buffer.writeInt(bitmapFontID)
            buffer.writeInt(pathID)
            buffer.writeInt(start)
            buffer.writeInt(end)
            buffer.writeFloat(yAdj)
        }
    }
}
