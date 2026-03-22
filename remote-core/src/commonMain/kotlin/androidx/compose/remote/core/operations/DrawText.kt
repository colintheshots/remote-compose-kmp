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

/** Draw Text */
class DrawText(
    private val mTextID: Int,
    private val mStart: Int,
    private val mEnd: Int,
    private val mContextStart: Int,
    private val mContextEnd: Int,
    private val mX: Float,
    private val mY: Float,
    private val mRtl: Boolean
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
        apply(buffer, mTextID, mStart, mEnd, mContextStart, mContextEnd, mX, mY, mRtl)
    }

    override fun toString(): String =
        "DrawTextRun [$mTextID] $mStart, $mEnd, " +
            "${Utils.floatToString(mX, mOutX)}, ${Utils.floatToString(mY, mOutY)}"

    override fun paint(context: PaintContext) {
        context.drawTextRun(mTextID, mStart, mEnd, mContextStart, mContextEnd, mOutX, mOutY, mRtl)
    }

    override fun serialize(serializer: MapSerializer) {
        serializer
            .addType(CLASS_NAME)
            .add("textId", mTextID)
            .add("start", mStart)
            .add("end", mEnd)
            .add("contextStart", mContextStart)
            .add("contextEnd", mContextEnd)
            .add("x", mX, mOutX)
            .add("y", mY, mOutY)
            .add("rtl", mRtl)
    }

    companion object {
        private val OP_CODE = Operations.DRAW_TEXT_RUN
        private const val CLASS_NAME = "DrawText"

        fun name(): String = CLASS_NAME
        fun id(): Int = OP_CODE

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val text = buffer.readInt()
            val start = buffer.readInt()
            val end = buffer.readInt()
            val contextStart = buffer.readInt()
            val contextEnd = buffer.readInt()
            val x = buffer.readFloat()
            val y = buffer.readFloat()
            val rtl = buffer.readBoolean()
            operations.add(DrawText(text, start, end, contextStart, contextEnd, x, y, rtl))
        }

        fun apply(
            buffer: WireBuffer,
            textId: Int, start: Int, end: Int,
            contextStart: Int, contextEnd: Int,
            x: Float, y: Float, rtl: Boolean
        ) {
            buffer.start(OP_CODE)
            buffer.writeInt(textId)
            buffer.writeInt(start)
            buffer.writeInt(end)
            buffer.writeInt(contextStart)
            buffer.writeInt(contextEnd)
            buffer.writeFloat(x)
            buffer.writeFloat(y)
            buffer.writeBoolean(rtl)
        }
    }
}
