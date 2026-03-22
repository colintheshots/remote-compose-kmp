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

// TODO: this interface is unused. Delete it.
interface TextPaint {
    fun setARGB(a: Int, r: Int, g: Int, b: Int)
    fun setDither(dither: Boolean)
    fun setElegantTextHeight(elegant: Boolean)
    fun setEndHyphenEdit(endHyphen: Int)
    fun setFakeBoldText(fakeBoldText: Boolean)
    fun setFlags(flags: Int)
    fun setFontFeatureSettings(settings: String)
    fun setHinting(mode: Int)
    fun setLetterSpacing(letterSpacing: Float)
    fun setLinearText(linearText: Boolean)
    fun setShadowLayer(radius: Float, dx: Float, dy: Float, shadowColor: Int)
    fun setStartHyphenEdit(startHyphen: Int)
    fun setStrikeThruText(strikeThruText: Boolean)
    fun setStrokeCap(cap: Int)
    fun setSubpixelText(subpixelText: Boolean)
    fun setTextAlign(align: Int)
    fun setTextLocale(locale: Int)
    fun setTextLocales(localesArray: Int)
    fun setTextScaleX(scaleX: Float)
    fun setTextSize(textSize: Float)
    fun setTextSkewX(skewX: Float)
    fun setUnderlineText(underlineText: Boolean)
    fun setWordSpacing(wordSpacing: Float)
}
