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
package androidx.compose.remote.core.operations.utilities

import androidx.compose.remote.core.operations.Utils

/** Implement the scaling logic for Compose Image or ImageView */
class ImageScaling {

    private var mSrcLeft = 0f
    private var mSrcTop = 0f
    private var mSrcRight = 0f
    private var mSrcBottom = 0f
    private var mDstLeft = 0f
    private var mDstTop = 0f
    private var mDstRight = 0f
    private var mDstBottom = 0f
    private var mScaleFactor = 0f
    private var mScaleType = 0

    var finalDstLeft = 0f
    var finalDstTop = 0f
    var finalDstRight = 0f
    var finalDstBottom = 0f

    constructor()

    constructor(
        srcLeft: Float, srcTop: Float, srcRight: Float, srcBottom: Float,
        dstLeft: Float, dstTop: Float, dstRight: Float, dstBottom: Float,
        type: Int, scale: Float
    ) {
        mSrcLeft = srcLeft
        mSrcTop = srcTop
        mSrcRight = srcRight
        mSrcBottom = srcBottom
        mDstLeft = dstLeft
        mDstTop = dstTop
        mDstRight = dstRight
        mDstBottom = dstBottom
        mScaleType = type
        mScaleFactor = scale
        adjustDrawToType()
    }

    /**
     * Setup the ImageScaling
     */
    fun setup(
        srcLeft: Float, srcTop: Float, srcRight: Float, srcBottom: Float,
        dstLeft: Float, dstTop: Float, dstRight: Float, dstBottom: Float,
        type: Int, scale: Float
    ) {
        mSrcLeft = srcLeft
        mSrcTop = srcTop
        mSrcRight = srcRight
        mSrcBottom = srcBottom
        mDstLeft = dstLeft
        mDstTop = dstTop
        mDstRight = dstRight
        mDstBottom = dstBottom
        mScaleType = type
        mScaleFactor = scale
        adjustDrawToType()
    }

    private fun adjustDrawToType() {
        val sw = (mSrcRight - mSrcLeft).toInt()
        val sh = (mSrcBottom - mSrcTop).toInt()
        val width = mDstRight - mDstLeft
        val height = mDstBottom - mDstTop
        var dw = width.toInt()
        var dh = height.toInt()
        var dLeft = 0
        var dRight = dw
        var dTop = 0
        var dBottom = dh
        if (sh == 0 || sw == 0) return
        when (mScaleType) {
            SCALE_NONE -> {
                dh = sh
                dw = sw
                dTop = (height.toInt() - dh) / 2
                dBottom = dh + dTop
                dLeft = (width.toInt() - dw) / 2
                dRight = dw + dLeft
            }
            SCALE_INSIDE -> {
                if (dh > sh && dw > sw) {
                    dh = sh
                    dw = sw
                } else if (sw * height > width * sh) {
                    dh = (dw * sh) / sw
                } else {
                    dw = (dh * sw) / sh
                }
                dTop = (height.toInt() - dh) / 2
                dBottom = dh + dTop
                dLeft = (width.toInt() - dw) / 2
                dRight = dw + dLeft
            }
            SCALE_FILL_WIDTH -> {
                dh = (dw * sh) / sw
                dTop = (height.toInt() - dh) / 2
                dBottom = dh + dTop
                dLeft = (width.toInt() - dw) / 2
                dRight = dw + dLeft
            }
            SCALE_FILL_HEIGHT -> {
                dw = (dh * sw) / sh
                dTop = (height.toInt() - dh) / 2
                dBottom = dh + dTop
                dLeft = (width.toInt() - dw) / 2
                dRight = dw + dLeft
            }
            SCALE_FIT -> {
                if (sw * height > width * sh) {
                    dh = (dw * sh) / sw
                    dTop = (height.toInt() - dh) / 2
                    dBottom = dh + dTop
                } else {
                    dw = (dh * sw) / sh
                    dLeft = (width.toInt() - dw) / 2
                    dRight = dw + dLeft
                }
            }
            SCALE_CROP -> {
                if (sw * height < width * sh) {
                    dh = (dw * sh) / sw
                    dTop = (height.toInt() - dh) / 2
                    dBottom = dh + dTop
                } else {
                    dw = (dh * sw) / sh
                    dLeft = (width.toInt() - dw) / 2
                    dRight = dw + dLeft
                }
            }
            SCALE_FILL_BOUNDS -> { /* do nothing */ }
            SCALE_FIXED_SCALE -> {
                dh = (sh * mScaleFactor).toInt()
                dw = (sw * mScaleFactor).toInt()
                dTop = (height.toInt() - dh) / 2
                dBottom = dh + dTop
                dLeft = (width.toInt() - dw) / 2
                dRight = dw + dLeft
            }
        }

        finalDstRight = dRight + mDstLeft
        finalDstLeft = dLeft + mDstLeft
        finalDstBottom = dBottom + mDstTop
        finalDstTop = dTop + mDstTop
    }

    companion object {
        const val SCALE_NONE = 0
        const val SCALE_INSIDE = 1
        const val SCALE_FILL_WIDTH = 2
        const val SCALE_FILL_HEIGHT = 3
        const val SCALE_FIT = 4
        const val SCALE_CROP = 5
        const val SCALE_FILL_BOUNDS = 6
        const val SCALE_FIXED_SCALE = 7

        /**
         * Utility to map a type int to the given type string
         *
         * @param type the scale type
         * @return string name
         */
        fun typeToString(type: Int): String {
            val typeString = arrayOf(
                "none", "inside", "fill_width", "fill_height",
                "fit", "crop", "fill_bounds", "fixed_scale"
            )
            return typeString[type]
        }
    }
}
