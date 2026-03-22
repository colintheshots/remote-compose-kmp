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
import androidx.compose.remote.core.serialize.Serializable

/** Draw curved text on a circle */
class DrawTextOnCircle(
    val mTextId: Int,
    private val mCenterX: Float,
    private val mCenterY: Float,
    private val mRadius: Float,
    private val mStartAngle: Float,
    private val mWarpRadiusOffset: Float,
    private val mAlignment: Alignment,
    private val mPlacement: Placement
) : PaintOperation(), VariableSupport, Serializable {

    enum class Alignment { START, CENTER, END }
    enum class Placement { OUTSIDE, INSIDE }

    override fun updateVariables(context: RemoteContext) {
        // no variable floats
    }

    override fun registerListening(context: RemoteContext) {
        context.listensTo(mTextId, this)
    }

    override fun write(buffer: WireBuffer) {
        apply(
            buffer, mTextId, mCenterX, mCenterY, mRadius,
            mStartAngle, mWarpRadiusOffset, mAlignment, mPlacement
        )
    }

    override fun toString(): String = "DrawTextOnCircle [$mTextId]"

    override fun paint(context: PaintContext) {
        throw UnsupportedOperationException("DrawTextOnCircle is not supported")
    }

    override fun serialize(serializer: MapSerializer) {
        serializer
            .addType(CLASS_NAME)
            .add("textId", mTextId)
            .add("centerX", mCenterX)
            .add("centerY", mCenterY)
            .add("radius", mRadius)
            .add("startAngle", mStartAngle)
            .add("warpRadiusOffset", mWarpRadiusOffset)
            .add("alignment", mAlignment)
            .add("placement", mPlacement)
    }

    companion object {
        private val OP_CODE = Operations.DRAW_TEXT_ON_CIRCLE
        private const val CLASS_NAME = "DrawTextOnCircle"

        fun name(): String = CLASS_NAME
        fun id(): Int = OP_CODE

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val textId = buffer.readInt()
            val centerX = buffer.readFloat()
            val centerY = buffer.readFloat()
            val radius = buffer.readFloat()
            val startAngle = buffer.readFloat()
            val warpRadiusOffset = buffer.readFloat()
            val alignment = Alignment.entries[buffer.readByte()]
            val placement = Placement.entries[buffer.readByte()]
            operations.add(
                DrawTextOnCircle(
                    textId, centerX, centerY, radius,
                    startAngle, warpRadiusOffset, alignment, placement
                )
            )
        }

        fun apply(
            buffer: WireBuffer,
            textId: Int, centerX: Float, centerY: Float, radius: Float,
            startAngle: Float, warpRadiusOffset: Float,
            alignment: Alignment, placement: Placement
        ) {
            buffer.start(OP_CODE)
            buffer.writeInt(textId)
            buffer.writeFloat(centerX)
            buffer.writeFloat(centerY)
            buffer.writeFloat(radius)
            buffer.writeFloat(startAngle)
            buffer.writeFloat(warpRadiusOffset)
            buffer.writeByte(alignment.ordinal)
            buffer.writeByte(placement.ordinal)
        }
    }
}
