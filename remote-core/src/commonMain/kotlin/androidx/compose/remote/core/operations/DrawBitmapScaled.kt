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
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.VariableSupport
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.utilities.ImageScaling
import androidx.compose.remote.core.serialize.MapSerializer

/** Operation to draw a given cached bitmap with scaling */
class DrawBitmapScaled(
    imageId: Int,
    private val mSrcLeft: Float,
    private val mSrcTop: Float,
    private val mSrcRight: Float,
    private val mSrcBottom: Float,
    private val mDstLeft: Float,
    private val mDstTop: Float,
    private val mDstRight: Float,
    private val mDstBottom: Float,
    type: Int,
    private val mScaleFactor: Float,
    private val mContentDescId: Int
) : PaintOperation(), VariableSupport {

    private var mImageId: Int = imageId
    private var mOutSrcLeft: Float = mSrcLeft
    private var mOutSrcTop: Float = mSrcTop
    private var mOutSrcRight: Float = mSrcRight
    private var mOutSrcBottom: Float = mSrcBottom
    private var mOutDstLeft: Float = mDstLeft
    private var mOutDstTop: Float = mDstTop
    private var mOutDstRight: Float = mDstRight
    private var mOutDstBottom: Float = mDstBottom
    private var mOutScaleFactor: Float = mScaleFactor
    private val mScaleType: Int
    private val mScaling: ImageScaling = ImageScaling()

    init {
        mScaleType = type and 0xFF
        if (((type shr 8) and 0x1) != 0) {
            mImageId = mImageId or PTR_DEREFERENCE
        }
    }

    override fun updateVariables(context: RemoteContext) {
        mOutSrcLeft = if (mSrcLeft.isNaN()) context.getFloat(Utils.idFromNan(mSrcLeft)) else mSrcLeft
        mOutSrcTop = if (mSrcTop.isNaN()) context.getFloat(Utils.idFromNan(mSrcTop)) else mSrcTop
        mOutSrcRight = if (mSrcRight.isNaN()) context.getFloat(Utils.idFromNan(mSrcRight)) else mSrcRight
        mOutSrcBottom = if (mSrcBottom.isNaN()) context.getFloat(Utils.idFromNan(mSrcBottom)) else mSrcBottom
        mOutDstLeft = if (mDstLeft.isNaN()) context.getFloat(Utils.idFromNan(mDstLeft)) else mDstLeft
        mOutDstTop = if (mDstTop.isNaN()) context.getFloat(Utils.idFromNan(mDstTop)) else mDstTop
        mOutDstRight = if (mDstRight.isNaN()) context.getFloat(Utils.idFromNan(mDstRight)) else mDstRight
        mOutDstBottom = if (mDstBottom.isNaN()) context.getFloat(Utils.idFromNan(mDstBottom)) else mDstBottom
        mOutScaleFactor = if (mScaleFactor.isNaN()) context.getFloat(Utils.idFromNan(mScaleFactor)) else mScaleFactor
    }

    override fun registerListening(context: RemoteContext) {
        register(context, mSrcLeft)
        register(context, mSrcTop)
        register(context, mSrcRight)
        register(context, mSrcBottom)
        register(context, mDstLeft)
        register(context, mDstTop)
        register(context, mDstRight)
        register(context, mDstBottom)
        register(context, mScaleFactor)
    }

    private fun register(context: RemoteContext, value: Float) {
        if (value.isNaN()) {
            context.listensTo(Utils.idFromNan(value), this)
        }
    }

    override fun write(buffer: WireBuffer) {
        apply(
            buffer, mImageId,
            mSrcLeft, mSrcTop, mSrcRight, mSrcBottom,
            mDstLeft, mDstTop, mDstRight, mDstBottom,
            mScaleType, mScaleFactor, mContentDescId
        )
    }

    override fun toString(): String =
        "DrawBitmapScaled $mImageId " +
            "[${Utils.floatToString(mSrcLeft, mOutSrcLeft)} " +
            "${Utils.floatToString(mSrcTop, mOutSrcTop)} " +
            "${Utils.floatToString(mSrcRight, mOutSrcRight)} " +
            "${Utils.floatToString(mSrcBottom, mOutSrcBottom)}] " +
            "- [${Utils.floatToString(mDstLeft, mOutDstLeft)} " +
            "${Utils.floatToString(mDstTop, mOutDstTop)} " +
            "${Utils.floatToString(mDstRight, mOutDstRight)} " +
            "${Utils.floatToString(mDstBottom, mOutDstBottom)}] " +
            " $mScaleType ${Utils.floatToString(mScaleFactor, mOutScaleFactor)}"

    override fun paint(context: PaintContext) {
        mScaling.setup(
            mOutSrcLeft, mOutSrcTop, mOutSrcRight, mOutSrcBottom,
            mOutDstLeft, mOutDstTop, mOutDstRight, mOutDstBottom,
            mScaleType, mOutScaleFactor
        )
        context.save()
        context.clipRect(mOutDstLeft, mOutDstTop, mOutDstRight, mOutDstBottom)
        context.drawBitmap(
            getId(mImageId, context),
            mOutSrcLeft.toInt(), mOutSrcTop.toInt(),
            mOutSrcRight.toInt(), mOutSrcBottom.toInt(),
            mScaling.finalDstLeft.toInt(), mScaling.finalDstTop.toInt(),
            mScaling.finalDstRight.toInt(), mScaling.finalDstBottom.toInt(),
            mContentDescId
        )
        context.restore()
    }

    override fun serialize(serializer: MapSerializer) {
        serializer
            .addType(CLASS_NAME)
            .add("imageId", mImageId)
            .add("contentDescriptionId", mContentDescId)
            .add("scaleType", getScaleTypeString())
            .add("scaleFactor", mScaleFactor, mOutScaleFactor)
            .add("srcLeft", mSrcLeft, mOutSrcLeft)
            .add("srcTop", mSrcTop, mOutSrcTop)
            .add("srcRight", mSrcRight, mOutSrcRight)
            .add("srcBottom", mSrcBottom, mOutSrcBottom)
            .add("dstLeft", mDstLeft, mOutDstLeft)
            .add("dstTop", mDstTop, mOutDstTop)
            .add("dstRight", mDstRight, mOutDstRight)
            .add("dstBottom", mDstBottom, mOutDstBottom)
    }

    private fun getScaleTypeString(): String = when (mScaleType) {
        SCALE_NONE -> "SCALE_NONE"
        SCALE_INSIDE -> "SCALE_INSIDE"
        SCALE_FILL_WIDTH -> "SCALE_FILL_WIDTH"
        SCALE_FILL_HEIGHT -> "SCALE_FILL_HEIGHT"
        SCALE_FIT -> "SCALE_FIT"
        SCALE_CROP -> "SCALE_CROP"
        SCALE_FILL_BOUNDS -> "SCALE_FILL_BOUNDS"
        SCALE_FIXED_SCALE -> "SCALE_FIXED_SCALE"
        else -> "INVALID_SCALE_TYPE"
    }

    companion object {
        private val OP_CODE = Operations.DRAW_BITMAP_SCALED
        private const val CLASS_NAME = "DrawBitmapScaled"

        const val SCALE_NONE = ImageScaling.SCALE_NONE
        const val SCALE_INSIDE = ImageScaling.SCALE_INSIDE
        const val SCALE_FILL_WIDTH = ImageScaling.SCALE_FILL_WIDTH
        const val SCALE_FILL_HEIGHT = ImageScaling.SCALE_FILL_HEIGHT
        const val SCALE_FIT = ImageScaling.SCALE_FIT
        const val SCALE_CROP = ImageScaling.SCALE_CROP
        const val SCALE_FILL_BOUNDS = ImageScaling.SCALE_FILL_BOUNDS
        const val SCALE_FIXED_SCALE = ImageScaling.SCALE_FIXED_SCALE

        fun name(): String = CLASS_NAME
        fun id(): Int = OP_CODE

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val imageId = buffer.readInt()
            val sLeft = buffer.readFloat()
            val srcTop = buffer.readFloat()
            val srcRight = buffer.readFloat()
            val srcBottom = buffer.readFloat()
            val dstLeft = buffer.readFloat()
            val dstTop = buffer.readFloat()
            val dstRight = buffer.readFloat()
            val dstBottom = buffer.readFloat()
            val scaleType = buffer.readInt()
            val scaleFactor = buffer.readFloat()
            val cdId = buffer.readInt()
            operations.add(
                DrawBitmapScaled(
                    imageId, sLeft, srcTop, srcRight, srcBottom,
                    dstLeft, dstTop, dstRight, dstBottom,
                    scaleType, scaleFactor, cdId
                )
            )
        }

        fun apply(
            buffer: WireBuffer,
            imageId: Int,
            srcLeft: Float, srcTop: Float, srcRight: Float, srcBottom: Float,
            dstLeft: Float, dstTop: Float, dstRight: Float, dstBottom: Float,
            scaleType: Int, scaleFactor: Float, cdId: Int
        ) {
            buffer.start(OP_CODE)
            buffer.writeInt(imageId)
            buffer.writeFloat(srcLeft)
            buffer.writeFloat(srcTop)
            buffer.writeFloat(srcRight)
            buffer.writeFloat(srcBottom)
            buffer.writeFloat(dstLeft)
            buffer.writeFloat(dstTop)
            buffer.writeFloat(dstRight)
            buffer.writeFloat(dstBottom)
            buffer.writeInt(scaleType)
            buffer.writeFloat(scaleFactor)
            buffer.writeInt(cdId)
        }
    }
}
