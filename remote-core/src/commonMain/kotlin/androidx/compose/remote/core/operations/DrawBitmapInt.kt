/*
 * Copyright (C) 2023 The Android Open Source Project
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
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.serialize.MapSerializer

/** Operation to draw a given cached bitmap using integer coordinates */
class DrawBitmapInt(
    private val mImageId: Int,
    private val mSrcLeft: Int,
    private val mSrcTop: Int,
    private val mSrcRight: Int,
    private val mSrcBottom: Int,
    private val mDstLeft: Int,
    private val mDstTop: Int,
    private val mDstRight: Int,
    private val mDstBottom: Int,
    private val mContentDescId: Int = 0
) : PaintOperation() {

    override fun write(buffer: WireBuffer) {
        apply(
            buffer, mImageId,
            mSrcLeft, mSrcTop, mSrcRight, mSrcBottom,
            mDstLeft, mDstTop, mDstRight, mDstBottom,
            mContentDescId
        )
    }

    override fun toString(): String =
        "DRAW_BITMAP_INT $mImageId on $mSrcLeft $mSrcTop $mSrcRight $mSrcBottom - $mDstLeft $mDstTop $mDstRight $mDstBottom;"

    override fun paint(context: PaintContext) {
        context.drawBitmap(
            getId(mImageId, context),
            mSrcLeft, mSrcTop, mSrcRight, mSrcBottom,
            mDstLeft, mDstTop, mDstRight, mDstBottom,
            mContentDescId
        )
    }

    override fun serialize(serializer: MapSerializer) {
        serializer
            .addType(CLASS_NAME)
            .add("imageId", mImageId)
            .add("contentDescriptionId", mContentDescId)
            .add("srcLeft", mSrcLeft)
            .add("srcTop", mSrcTop)
            .add("srcRight", mSrcRight)
            .add("srcBottom", mSrcBottom)
            .add("dstLeft", mDstLeft)
            .add("dstTop", mDstTop)
            .add("dstRight", mDstRight)
            .add("dstBottom", mDstBottom)
    }

    companion object {
        private val OP_CODE = Operations.DRAW_BITMAP_INT
        private const val CLASS_NAME = "DrawBitmapInt"

        fun name(): String = CLASS_NAME
        fun id(): Int = OP_CODE

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val imageId = buffer.readInt()
            val sLeft = buffer.readInt()
            val srcTop = buffer.readInt()
            val srcRight = buffer.readInt()
            val srcBottom = buffer.readInt()
            val dstLeft = buffer.readInt()
            val dstTop = buffer.readInt()
            val dstRight = buffer.readInt()
            val dstBottom = buffer.readInt()
            val cdId = buffer.readInt()
            operations.add(
                DrawBitmapInt(
                    imageId, sLeft, srcTop, srcRight, srcBottom,
                    dstLeft, dstTop, dstRight, dstBottom, cdId
                )
            )
        }

        fun apply(
            buffer: WireBuffer,
            imageId: Int,
            srcLeft: Int, srcTop: Int, srcRight: Int, srcBottom: Int,
            dstLeft: Int, dstTop: Int, dstRight: Int, dstBottom: Int,
            cdId: Int
        ) {
            buffer.start(OP_CODE)
            buffer.writeInt(imageId)
            buffer.writeInt(srcLeft)
            buffer.writeInt(srcTop)
            buffer.writeInt(srcRight)
            buffer.writeInt(srcBottom)
            buffer.writeInt(dstLeft)
            buffer.writeInt(dstTop)
            buffer.writeInt(dstRight)
            buffer.writeInt(dstBottom)
            buffer.writeInt(cdId)
        }
    }
}
