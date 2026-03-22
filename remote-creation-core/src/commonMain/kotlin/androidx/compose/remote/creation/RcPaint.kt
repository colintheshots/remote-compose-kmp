/*
 * Copyright 2025 The Android Open Source Project
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
package androidx.compose.remote.creation

import androidx.compose.remote.core.operations.paint.PaintBundle

open class RcPaint(internal val mBuilder: RemoteComposeWriter) {
    protected var mPaint: PaintBundle = PaintBundle()

    companion object {
        var FONT_TYPE_DEFAULT: Int = PaintBundle.FONT_TYPE_DEFAULT
        var FONT_TYPE_SANS_SERIF: Int = PaintBundle.FONT_TYPE_SANS_SERIF
        var FONT_TYPE_SERIF: Int = PaintBundle.FONT_TYPE_SERIF
        var FONT_TYPE_MONOSPACE: Int = PaintBundle.FONT_TYPE_MONOSPACE
        private const val NORMAL_WEIGHT = 400
    }

    /** Write the paint to the buffer */
    fun commit() {
        mBuilder.mBuffer.addPaint(mPaint)
        mPaint.reset()
    }

    fun setAntiAlias(aa: Boolean): RcPaint {
        mPaint.setAntiAlias(aa)
        return this
    }

    fun setColor(color: Int): RcPaint {
        mPaint.setColor(color)
        return this
    }

    fun setColorId(colorId: Int): RcPaint {
        mPaint.setColorId(colorId)
        return this
    }

    fun setStrokeJoin(join: Int): RcPaint {
        mPaint.setStrokeJoin(join)
        return this
    }

    fun setStrokeWidth(width: Float): RcPaint {
        mPaint.setStrokeWidth(width)
        return this
    }

    fun setStyle(style: Int): RcPaint {
        mPaint.setStyle(style)
        return this
    }

    fun setStrokeCap(cap: Int): RcPaint {
        mPaint.setStrokeCap(cap)
        return this
    }

    fun setStrokeMiter(miter: Float): RcPaint {
        mPaint.setStrokeMiter(miter)
        return this
    }

    fun setAlpha(alpha: Float): RcPaint {
        mPaint.setAlpha(if (alpha > 2) alpha / 255f else alpha)
        return this
    }

    fun setPorterDuffColorFilter(color: Int, mode: Int): RcPaint {
        mPaint.setColorFilter(color, mode)
        return this
    }

    fun clearColorFilter(): RcPaint {
        mPaint.clearColorFilter()
        return this
    }

    fun setLinearGradient(
        startX: Float, startY: Float, endX: Float, endY: Float,
        colors: IntArray, positions: FloatArray?, tileMode: Int,
    ): RcPaint {
        mPaint.setLinearGradient(colors, 0, positions, startX, startY, endX, endY, tileMode)
        return this
    }

    fun setLinearGradient(
        startX: Float, startY: Float, endX: Float, endY: Float,
        colors: IntArray, mask: Int, positions: FloatArray, tileMode: Int,
    ): RcPaint {
        mPaint.setLinearGradient(colors, mask, positions, startX, startY, endX, endY, tileMode)
        return this
    }

    fun setRadialGradient(
        centerX: Float, centerY: Float, radius: Float,
        colors: IntArray, positions: FloatArray?, tileMode: Int,
    ): RcPaint {
        mPaint.setRadialGradient(colors, 0, positions, centerX, centerY, radius, tileMode)
        return this
    }

    fun setRadialGradient(
        centerX: Float, centerY: Float, radius: Float,
        colors: IntArray, mask: Int, positions: FloatArray?, tileMode: Int,
    ): RcPaint {
        mPaint.setRadialGradient(colors, mask, positions, centerX, centerY, radius, tileMode)
        return this
    }

    fun setSweepGradient(
        centerX: Float, centerY: Float, colors: IntArray, positions: FloatArray?,
    ): RcPaint {
        mPaint.setSweepGradient(colors, 0, positions, centerX, centerY)
        return this
    }

    fun setSweepGradient(
        centerX: Float, centerY: Float, colors: IntArray, mask: Int, positions: FloatArray?,
    ): RcPaint {
        mPaint.setSweepGradient(colors, mask, positions, centerX, centerY)
        return this
    }

    fun setShaderMatrix(matrixId: Float): RcPaint {
        mPaint.setShaderMatrix(matrixId)
        return this
    }

    fun setTextSize(size: Float): RcPaint {
        mPaint.setTextSize(size)
        return this
    }

    fun setTypeface(fontType: Int, weight: Int, italic: Boolean): RcPaint {
        mPaint.setTextStyle(fontType, weight, italic)
        return this
    }

    fun setTypeface(typeface: String): RcPaint {
        val fontType = mBuilder.textCreateId(typeface)
        mPaint.setTextStyle(fontType, NORMAL_WEIGHT, false)
        return this
    }

    fun setTypeface(fontDataId: Int): RcPaint {
        mPaint.setTextStyle(fontDataId, NORMAL_WEIGHT, false, true)
        return this
    }

    fun setFilterBitmap(filter: Boolean): RcPaint {
        mPaint.setFilterBitmap(filter)
        return this
    }

    fun setBlendMode(blendMode: Int): RcPaint {
        mPaint.setBlendMode(blendMode)
        return this
    }

    fun setShader(id: Int): RcPaint {
        mPaint.setShader(id)
        return this
    }

    fun setAxis(tags: Array<String>, values: FloatArray): RcPaint {
        val tagIds = IntArray(tags.size) { mBuilder.textCreateId(tags[it]) }
        mPaint.setTextAxis(tagIds, values)
        return this
    }

    fun setAxis(tagIds: IntArray, values: FloatArray): RcPaint {
        mPaint.setTextAxis(tagIds, values)
        return this
    }

    fun setTextureShader(
        texture: Int, tileModeX: Short, tileModeY: Short,
        filterMode: Short, maxAnisotropy: Short,
    ): RcPaint {
        mPaint.setTextureShader(texture, tileModeX, tileModeY, filterMode, maxAnisotropy)
        return this
    }

    fun setPathEffect(pathEffectData: FloatArray?): RcPaint {
        mPaint.setPathEffect(pathEffectData)
        return this
    }
}
