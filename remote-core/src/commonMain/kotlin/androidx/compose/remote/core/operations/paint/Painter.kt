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
package androidx.compose.remote.core.operations.paint

/** Provides a Builder pattern for a PaintBundle */
class Painter {
    var mPaint: PaintBundle? = null

    /** Write the paint to the buffer */
    fun commit(): PaintBundle? = mPaint

    fun setAntiAlias(aa: Boolean): Painter {
        mPaint!!.setAntiAlias(aa)
        return this
    }

    fun setColor(color: Int): Painter {
        mPaint!!.setColor(color)
        return this
    }

    fun setColorId(colorId: Int): Painter {
        mPaint!!.setColorId(colorId)
        return this
    }

    fun setStrokeJoin(join: Int): Painter {
        mPaint!!.setStrokeJoin(join)
        return this
    }

    fun setStrokeWidth(width: Float): Painter {
        mPaint!!.setStrokeWidth(width)
        return this
    }

    fun setStyle(style: Int): Painter {
        mPaint!!.setStyle(style)
        return this
    }

    fun setStrokeCap(cap: Int): Painter {
        mPaint!!.setStrokeCap(cap)
        return this
    }

    fun setStrokeMiter(miter: Float): Painter {
        mPaint!!.setStrokeMiter(miter)
        return this
    }

    fun setAlpha(alpha: Float): Painter {
        mPaint!!.setAlpha(if (alpha > 2) alpha / 255f else alpha)
        return this
    }

    fun setPorterDuffColorFilter(color: Int, mode: Int): Painter {
        mPaint!!.setColorFilter(color, mode)
        return this
    }

    fun setPorterDuffColorIdFilter(colorId: Int, mode: Int): Painter {
        mPaint!!.setColorFilterId(colorId, mode)
        return this
    }

    fun setLinearGradient(
        startX: Float, startY: Float, endX: Float, endY: Float,
        colors: IntArray, positions: FloatArray?, tileMode: Int
    ): Painter {
        mPaint!!.setLinearGradient(colors, 0, positions, startX, startY, endX, endY, tileMode)
        return this
    }

    fun setRadialGradient(
        centerX: Float, centerY: Float, radius: Float,
        colors: IntArray, positions: FloatArray?, tileMode: Int
    ): Painter {
        mPaint!!.setRadialGradient(colors, 0, positions, centerX, centerY, radius, tileMode)
        return this
    }

    fun setSweepGradient(
        centerX: Float, centerY: Float,
        colors: IntArray, positions: FloatArray?
    ): Painter {
        mPaint!!.setSweepGradient(colors, 0, positions, centerX, centerY)
        return this
    }

    fun setTextSize(size: Float): Painter {
        mPaint!!.setTextSize(size)
        return this
    }

    fun setTypeface(fontType: Int, weight: Int, italic: Boolean): Painter {
        mPaint!!.setTextStyle(fontType, weight, italic)
        return this
    }

    fun setFilterBitmap(filter: Boolean): Painter {
        mPaint!!.setFilterBitmap(filter)
        return this
    }

    fun setShader(id: Int): Painter {
        mPaint!!.setShader(id)
        return this
    }
}
