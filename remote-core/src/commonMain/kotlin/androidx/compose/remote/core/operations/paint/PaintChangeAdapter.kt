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

open class PaintChangeAdapter : PaintChanges {
    override fun setTextSize(size: Float) {}
    override fun setTypeFace(fontType: Int, weight: Int, italic: Boolean) {}
    override fun setShaderMatrix(matrixId: Float) {}
    override fun setTypeFace(fontType: String, weight: Int, italic: Boolean) {}
    override fun setFontVariationAxes(tags: Array<String>, values: FloatArray) {}
    override fun setTextureShader(
        bitmapId: Int, tileX: Short, tileY: Short, filterMode: Short, maxAnisotropy: Short
    ) {}
    override fun setPathEffect(pathEffect: FloatArray?) {}
    override fun setStrokeWidth(width: Float) {}
    override fun setColor(color: Int) {}
    override fun setStrokeCap(cap: Int) {}
    override fun setStyle(style: Int) {}
    override fun setShader(shader: Int) {}
    override fun setImageFilterQuality(quality: Int) {}
    override fun setAlpha(a: Float) {}
    override fun setStrokeMiter(miter: Float) {}
    override fun setStrokeJoin(join: Int) {}
    override fun setFilterBitmap(filter: Boolean) {}
    override fun setBlendMode(mode: Int) {}
    override fun setAntiAlias(aa: Boolean) {}
    override fun clear(mask: Long) {}
    override fun setLinearGradient(
        colorsArray: IntArray, stopsArray: FloatArray?,
        startX: Float, startY: Float, endX: Float, endY: Float, tileMode: Int
    ) {}
    override fun setRadialGradient(
        colorsArray: IntArray, stopsArray: FloatArray?,
        centerX: Float, centerY: Float, radius: Float, tileMode: Int
    ) {}
    override fun setSweepGradient(
        colorsArray: IntArray, stopsArray: FloatArray?,
        centerX: Float, centerY: Float
    ) {}
    override fun setColorFilter(color: Int, mode: Int) {}
}
