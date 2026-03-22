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

/** Draw a bitmap */
class DrawBitmap(
    private val mId: Int,
    private val mLeft: Float,
    private val mTop: Float,
    private val mRight: Float,
    private val mBottom: Float,
    private val mDescriptionId: Int = 0
) : PaintOperation(), VariableSupport {

    private var mOutputLeft: Float = 0f
    private var mOutputTop: Float = 0f
    private var mOutputRight: Float = 0f
    private var mOutputBottom: Float = 0f

    override fun updateVariables(context: RemoteContext) {
        mOutputLeft = if (mLeft.isNaN()) context.getFloat(Utils.idFromNan(mLeft)) else mLeft
        mOutputTop = if (mTop.isNaN()) context.getFloat(Utils.idFromNan(mTop)) else mTop
        mOutputRight = if (mRight.isNaN()) context.getFloat(Utils.idFromNan(mRight)) else mRight
        mOutputBottom = if (mBottom.isNaN()) context.getFloat(Utils.idFromNan(mBottom)) else mBottom
    }

    override fun registerListening(context: RemoteContext) {
        if (mLeft.isNaN()) context.listensTo(Utils.idFromNan(mLeft), this)
        if (mTop.isNaN()) context.listensTo(Utils.idFromNan(mTop), this)
        if (mRight.isNaN()) context.listensTo(Utils.idFromNan(mRight), this)
        if (mBottom.isNaN()) context.listensTo(Utils.idFromNan(mBottom), this)
    }

    override fun write(buffer: WireBuffer) {
        apply(buffer, mId, mLeft, mTop, mRight, mBottom, mDescriptionId)
    }

    override fun toString(): String =
        "DrawBitmap (desc=$mDescriptionId)$mLeft $mTop $mRight $mBottom;"

    override fun paint(context: PaintContext) {
        context.drawBitmap(getId(mId, context), mOutputLeft, mOutputTop, mOutputRight, mOutputBottom)
    }

    override fun serialize(serializer: MapSerializer) {
        serializer
            .addType(CLASS_NAME)
            .add("imageId", mId)
            .add("contentDescriptionId", mDescriptionId)
            .add("left", mLeft, mOutputLeft)
            .add("top", mTop, mOutputTop)
            .add("right", mRight, mOutputRight)
            .add("bottom", mBottom, mOutputBottom)
    }

    companion object {
        private val OP_CODE = Operations.DRAW_BITMAP
        private const val CLASS_NAME = "DrawBitmap"

        fun name(): String = CLASS_NAME
        fun id(): Int = OP_CODE

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val id = buffer.readInt()
            val sLeft = buffer.readFloat()
            val srcTop = buffer.readFloat()
            val srcRight = buffer.readFloat()
            val srcBottom = buffer.readFloat()
            val descriptionId = buffer.readInt()
            operations.add(DrawBitmap(id, sLeft, srcTop, srcRight, srcBottom, descriptionId))
        }

        fun apply(
            buffer: WireBuffer,
            id: Int, left: Float, top: Float, right: Float, bottom: Float, descriptionId: Int
        ) {
            buffer.start(OP_CODE)
            buffer.writeInt(id)
            buffer.writeFloat(left)
            buffer.writeFloat(top)
            buffer.writeFloat(right)
            buffer.writeFloat(bottom)
            buffer.writeInt(descriptionId)
        }
    }
}
