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
package androidx.compose.remote.core.operations.paint

/** Interface to a paint object. For more details see Android Paint */
interface PaintChanges {

    /** Set the size of text */
    fun setTextSize(size: Float)

    /** Set the width of lines */
    fun setStrokeWidth(width: Float)

    /** Set the color to use */
    fun setColor(color: Int)

    /** Set the Stroke Cap */
    fun setStrokeCap(cap: Int)

    /** Set the Stroke style FILL and/or STROKE */
    fun setStyle(style: Int)

    /** Set the id of the shader to use */
    fun setShader(shader: Int)

    /** Set the way image is interpolated */
    fun setImageFilterQuality(quality: Int)

    /** Set the alpha to draw under */
    fun setAlpha(a: Float)

    /** Set the Stroke Miter */
    fun setStrokeMiter(miter: Float)

    /** Set the Stroke Join */
    fun setStrokeJoin(join: Int)

    /** Should bitmaps be interpolated */
    fun setFilterBitmap(filter: Boolean)

    /** Set the blend mode can be porterduff + others */
    fun setBlendMode(mode: Int)

    /** Set the AntiAlias. Typically true. Set to off when you need pixelated look (e.g. QR codes) */
    fun setAntiAlias(aa: Boolean)

    /** Clear some sub set of the settings */
    fun clear(mask: Long)

    /** Set a linear gradient fill */
    fun setLinearGradient(
        colorsArray: IntArray,
        stopsArray: FloatArray?,
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        tileMode: Int
    )

    /** Set a radial gradient fill */
    fun setRadialGradient(
        colorsArray: IntArray,
        stopsArray: FloatArray?,
        centerX: Float,
        centerY: Float,
        radius: Float,
        tileMode: Int
    )

    /** Set a sweep gradient fill */
    fun setSweepGradient(
        colorsArray: IntArray,
        stopsArray: FloatArray?,
        centerX: Float,
        centerY: Float
    )

    /** Set Color filter mod */
    fun setColorFilter(color: Int, mode: Int)

    /**
     * Set TypeFace 0,1,2
     *
     * @param fontType the type of font 0,1,or 2
     * @param weight the weight of the font
     * @param italic if the font is italic
     */
    fun setTypeFace(fontType: Int, weight: Int, italic: Boolean)

    /** Set the shader matrix */
    fun setShaderMatrix(matrixId: Float)

    /**
     * @param fontType String to be looked up in system
     * @param weight the weight of the font
     * @param italic if the font is italic
     */
    fun setTypeFace(fontType: String, weight: Int, italic: Boolean)

    /** Set the font variation axes */
    fun setFontVariationAxes(tags: Array<String>, values: FloatArray)

    /**
     * Set the texture shader
     *
     * @param bitmapId the id of the bitmap to use
     * @param tileX The tiling mode for x to draw the bitmap in.
     * @param tileY The tiling mode for y to draw the bitmap in.
     * @param filterMode the filter mode to be used when sampling from this shader.
     * @param maxAnisotropy The Anisotropy value to use for filtering.
     */
    fun setTextureShader(
        bitmapId: Int,
        tileX: Short,
        tileY: Short,
        filterMode: Short,
        maxAnisotropy: Short
    )

    /** Set the path effect */
    fun setPathEffect(pathEffect: FloatArray?)

    companion object {
        // MASK to be set/cleared
        val CLEAR_TEXT_SIZE = 1 shl (PaintBundle.TEXT_SIZE - 1)
        val CLEAR_TEXT_STYLE = 1 shl (PaintBundle.TYPEFACE - 1)
        val CLEAR_COLOR = 1 shl (PaintBundle.COLOR - 1)
        val CLEAR_STROKE_WIDTH = 1 shl (PaintBundle.STROKE_WIDTH - 1)
        val CLEAR_STROKE_MITER = 1 shl (PaintBundle.STROKE_MITER - 1)
        val CLEAR_CAP = 1 shl (PaintBundle.STROKE_CAP - 1)
        val CLEAR_STYLE = 1 shl (PaintBundle.STYLE - 1)
        val CLEAR_SHADER = 1 shl (PaintBundle.SHADER - 1)
        val CLEAR_IMAGE_FILTER_QUALITY = 1 shl (PaintBundle.IMAGE_FILTER_QUALITY - 1)
        val CLEAR_RADIENT = 1 shl (PaintBundle.GRADIENT - 1)
        val CLEAR_ALPHA = 1 shl (PaintBundle.ALPHA - 1)
        val CLEAR_COLOR_FILTER = 1 shl (PaintBundle.COLOR_FILTER - 1)
        const val VALID_BITS = 0x1FFF // only the first 13 bit are valid now
    }
}
