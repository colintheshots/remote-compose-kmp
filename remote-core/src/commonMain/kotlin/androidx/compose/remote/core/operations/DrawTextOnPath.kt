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

/** Draw text along a path */
class DrawTextOnPath(
    val mTextId: Int,
    private val mPathId: Int,
    private val mHOffset: Float,
    private val mVOffset: Float
) : PaintOperation(), VariableSupport, Serializable {

    private var mOutHOffset: Float = mHOffset
    private var mOutVOffset: Float = mVOffset

    override fun updateVariables(context: RemoteContext) {
        mOutHOffset = if (mHOffset.isNaN()) context.getFloat(Utils.idFromNan(mHOffset)) else mHOffset
        mOutVOffset = if (mVOffset.isNaN()) context.getFloat(Utils.idFromNan(mVOffset)) else mVOffset
    }

    override fun registerListening(context: RemoteContext) {
        context.listensTo(mTextId, this)
        if (mHOffset.isNaN()) context.listensTo(Utils.idFromNan(mHOffset), this)
        if (mVOffset.isNaN()) context.listensTo(Utils.idFromNan(mVOffset), this)
    }

    override fun write(buffer: WireBuffer) {
        apply(buffer, mTextId, mPathId, mHOffset, mVOffset)
    }

    override fun toString(): String =
        "DrawTextOnPath [$mTextId] [$mPathId] " +
            "${Utils.floatToString(mHOffset, mOutHOffset)}, " +
            Utils.floatToString(mVOffset, mOutVOffset)

    override fun paint(context: PaintContext) {
        context.drawTextOnPath(mTextId, getId(mPathId, context), mOutHOffset, mOutVOffset)
    }

    override fun serialize(serializer: MapSerializer) {
        serializer
            .addType(CLASS_NAME)
            .add("pathId", mPathId)
            .add("textId", mTextId)
            .add("vOffset", mVOffset, mOutVOffset)
            .add("hOffset", mHOffset, mOutHOffset)
    }

    companion object {
        private val OP_CODE = Operations.DRAW_TEXT_ON_PATH
        private const val CLASS_NAME = "DrawTextOnPath"

        fun name(): String = CLASS_NAME
        fun id(): Int = OP_CODE

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val textId = buffer.readInt()
            val pathId = buffer.readInt()
            val vOffset = buffer.readFloat()
            val hOffset = buffer.readFloat()
            operations.add(DrawTextOnPath(textId, pathId, hOffset, vOffset))
        }

        fun apply(buffer: WireBuffer, textId: Int, pathId: Int, hOffset: Float, vOffset: Float) {
            buffer.start(OP_CODE)
            buffer.writeInt(textId)
            buffer.writeInt(pathId)
            buffer.writeFloat(vOffset)
            buffer.writeFloat(hOffset)
        }
    }
}
