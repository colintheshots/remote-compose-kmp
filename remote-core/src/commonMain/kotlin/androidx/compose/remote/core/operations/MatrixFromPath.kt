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

/** Set matrix from a path position */
class MatrixFromPath(
    private val mPathId: Int,
    private val mFraction: Float,
    private val mVOffset: Float,
    private val mFlags: Int
) : PaintOperation(), VariableSupport, Serializable {

    private var mOutFraction: Float = mFraction
    private var mOutVOffset: Float = mVOffset

    override fun updateVariables(context: RemoteContext) {
        mOutFraction = if (mFraction.isNaN()) context.getFloat(Utils.idFromNan(mFraction)) else mFraction
        mOutVOffset = if (mVOffset.isNaN()) context.getFloat(Utils.idFromNan(mVOffset)) else mVOffset
    }

    override fun registerListening(context: RemoteContext) {
        if (mFraction.isNaN()) context.listensTo(Utils.idFromNan(mFraction), this)
        if (mVOffset.isNaN()) context.listensTo(Utils.idFromNan(mVOffset), this)
    }

    override fun write(buffer: WireBuffer) {
        apply(buffer, mPathId, mFraction, mVOffset, mFlags)
    }

    override fun toString(): String =
        "DrawTextOnPath [$mPathId] " +
            "${Utils.floatToString(mFraction, mOutFraction)}, " +
            "${Utils.floatToString(mVOffset, mOutVOffset)}, $mFlags"

    override fun paint(context: PaintContext) {
        context.matrixFromPath(mPathId, mOutFraction, mOutVOffset, mFlags)
    }

    override fun serialize(serializer: MapSerializer) {
        serializer
            .addType(CLASS_NAME)
            .add("pathId", mPathId)
            .add("vOffset", mVOffset, mOutVOffset)
            .add("hOffset", mFraction, mOutFraction)
    }

    companion object {
        private val OP_CODE = Operations.MATRIX_FROM_PATH
        private const val CLASS_NAME = "MatrixFromPath"

        const val POSITION_MATRIX_FLAG = 0x01
        const val TANGENT_MATRIX_FLAG = 0x02

        fun name(): String = CLASS_NAME
        fun id(): Int = OP_CODE

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val pathId = buffer.readInt()
            val percent = buffer.readFloat()
            val vOffset = buffer.readFloat()
            val flags = buffer.readInt()
            operations.add(MatrixFromPath(pathId, percent, vOffset, flags))
        }

        fun apply(buffer: WireBuffer, pathId: Int, percent: Float, vOffset: Float, flags: Int) {
            buffer.start(OP_CODE)
            buffer.writeInt(pathId)
            buffer.writeFloat(percent)
            buffer.writeFloat(vOffset)
            buffer.writeInt(flags)
        }
    }
}
