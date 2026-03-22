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
package androidx.compose.remote.core.operations

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.PaintOperation
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.VariableSupport
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable

/** Draw text anchored to a point */
class DrawTextAnchored(
    private val mTextID: Int,
    private val mX: Float,
    private val mY: Float,
    private val mPanX: Float,
    private val mPanY: Float,
    private val mFlags: Int
) : PaintOperation(), VariableSupport, Serializable {

    private var mOutX: Float = mX
    private var mOutY: Float = mY
    private var mOutPanX: Float = mPanX
    private var mOutPanY: Float = mPanY
    private var mLastString: String? = null
    private val mBounds = FloatArray(4)

    override fun updateVariables(context: RemoteContext) {
        mOutX = if (mX.isNaN()) context.getFloat(Utils.idFromNan(mX)) else mX
        mOutY = if (mY.isNaN()) context.getFloat(Utils.idFromNan(mY)) else mY
        mOutPanX = if (mPanX.isNaN()) context.getFloat(Utils.idFromNan(mPanX)) else mPanX
        mOutPanY = if (mPanY.isNaN()) context.getFloat(Utils.idFromNan(mPanY)) else mPanY
    }

    override fun registerListening(context: RemoteContext) {
        context.listensTo(mTextID, this)
        if (mX.isNaN()) context.listensTo(Utils.idFromNan(mX), this)
        if (mY.isNaN()) context.listensTo(Utils.idFromNan(mY), this)
        if (mPanX.isNaN()) context.listensTo(Utils.idFromNan(mPanX), this)
        if (mPanY.isNaN() && Utils.idFromNan(mPanY) > 0) {
            context.listensTo(Utils.idFromNan(mPanY), this)
        }
    }

    override fun write(buffer: WireBuffer) {
        apply(buffer, mTextID, mX, mY, mPanX, mPanY, mFlags)
    }

    override fun toString(): String {
        fun floatToStr(v: Float): String =
            if (v.isNaN()) "[${Utils.idFromNan(v)}]" else v.toString()
        return "DrawTextAnchored [$mTextID] ${floatToStr(mX)}, ${floatToStr(mY)}, " +
            "${floatToStr(mPanX)}, ${floatToStr(mPanY)}, ${mFlags.toString(2)}"
    }

    private fun getHorizontalOffset(): Float {
        val scale = 1.0f
        val textWidth = scale * (mBounds[2] - mBounds[0])
        val boxWidth = 0f
        return (boxWidth - textWidth) * (1 + mOutPanX) / 2f - (scale * mBounds[0])
    }

    private fun getVerticalOffset(baseline: Boolean): Float {
        val scale = 1.0f
        val boxHeight = 0f
        val textHeight = scale * (mBounds[3] - mBounds[1])
        return (boxHeight - textHeight) * (1 - mOutPanY) / 2 +
            (if (baseline) textHeight / 2 else (-scale * mBounds[1]))
    }

    override fun paint(context: PaintContext) {
        val flags = if ((mFlags and ANCHOR_MONOSPACE_MEASURE) != 0) {
            PaintContext.TEXT_MEASURE_MONOSPACE_WIDTH
        } else {
            0
        }

        val str = context.getText(mTextID) ?: ""
        // Reference check: we use identity check for caching
        if (str !== mLastString || (mFlags and MEASURE_EVERY_TIME) != 0) {
            mLastString = str
            context.getTextBounds(mTextID, 0, -1, flags, mBounds)
        }
        val baseline = (mFlags and BASELINE_RELATIVE) != 0
        val x = mOutX + getHorizontalOffset()
        val y = if (mOutPanY.isNaN()) mOutY else mOutY + getVerticalOffset(baseline)

        context.drawTextRun(mTextID, 0, -1, 0, 1, x, y, (mFlags and ANCHOR_TEXT_RTL) == 1)
    }

    override fun serialize(serializer: MapSerializer) {
        serializer
            .addType(CLASS_NAME)
            .add("textId", mTextID)
            .add("x", mX, mOutX)
            .add("y", mY, mOutY)
            .add("panX", mPanX, mOutPanX)
            .add("panY", mPanY, mOutPanY)
            .add("flags", mFlags)
    }

    companion object {
        private val OP_CODE = Operations.DRAW_TEXT_ANCHOR
        private const val CLASS_NAME = "DrawTextAnchored"

        const val ANCHOR_TEXT_RTL = 1
        const val ANCHOR_MONOSPACE_MEASURE = 2
        const val MEASURE_EVERY_TIME = 4
        const val BASELINE_RELATIVE = 8

        fun name(): String = CLASS_NAME
        fun id(): Int = OP_CODE

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val textId = buffer.readInt()
            val x = buffer.readFloat()
            val y = buffer.readFloat()
            val panX = buffer.readFloat()
            val panY = buffer.readFloat()
            val flags = buffer.readInt()
            operations.add(DrawTextAnchored(textId, x, y, panX, panY, flags))
        }

        fun apply(
            buffer: WireBuffer,
            textId: Int, x: Float, y: Float, panX: Float, panY: Float, flags: Int
        ) {
            buffer.start(OP_CODE)
            buffer.writeInt(textId)
            buffer.writeFloat(x)
            buffer.writeFloat(y)
            buffer.writeFloat(panX)
            buffer.writeFloat(panY)
            buffer.writeInt(flags)
        }

    }
}
