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
package androidx.compose.remote.player.core.platform

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.BlendMode
import android.graphics.Canvas
import android.graphics.ComposePathEffect
import android.graphics.DashPathEffect
import android.graphics.DiscretePathEffect
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathDashPathEffect
import android.graphics.PathEffect
import android.graphics.PathMeasure
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.graphics.SumPathEffect
import android.graphics.SweepGradient
import android.graphics.Typeface
import android.graphics.fonts.Font
import android.graphics.fonts.FontFamily
import android.graphics.fonts.FontStyle
import android.graphics.fonts.FontVariationAxis
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import androidx.compose.remote.core.MatrixAccess
import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.RcPlatformServices
import androidx.compose.remote.core.RemoteComposeState
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.operations.ClipPath
import androidx.compose.remote.core.operations.ShaderData
import androidx.compose.remote.core.operations.Utils
import androidx.compose.remote.core.operations.layout.managers.CoreText
import androidx.compose.remote.core.operations.layout.modifiers.GraphicsLayerModifierOperation
import androidx.compose.remote.core.operations.paint.PaintBundle
import androidx.compose.remote.core.operations.paint.PaintChanges
import androidx.compose.remote.core.operations.paint.PaintPathEffects
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.util.Locale
import java.util.Objects

/**
 * An implementation of PaintContext for the Android Canvas. This is used to play the RemoteCompose
 * operations on Android.
 */
class AndroidPaintContext(
    context: RemoteContext,
    canvas: Canvas
) : PaintContext(context) {

    companion object {
        private const val SYSTEM_FONTS_PATH = "/system/fonts/"
    }

    var mPaint = Paint()
    val mPaintList = mutableListOf<Paint>()
    var mCanvas: Canvas = canvas
    var mMainCanvas: Canvas? = null
    val mTmpRect = Rect()
    var mNode: RenderNode? = null
    var mPreviousCanvas: Canvas? = null

    fun getCanvas(): Canvas = mCanvas

    fun setCanvas(canvas: Canvas) {
        mCanvas = canvas
        mMainCanvas = canvas
    }

    override fun save() { mCanvas.save() }

    override fun saveLayer(x: Float, y: Float, width: Float, height: Float) {
        mCanvas.saveLayer(x, y, x + width, y + height, mPaint)
    }

    override fun restore() { mCanvas.restore() }

    override fun drawBitmap(
        imageId: Int,
        srcLeft: Int, srcTop: Int, srcRight: Int, srcBottom: Int,
        dstLeft: Int, dstTop: Int, dstRight: Int, dstBottom: Int,
        cdId: Int
    ) {
        val androidContext = mContext as AndroidRemoteContext
        if (androidContext.mRemoteComposeState.containsId(imageId)) {
            val bitmap = androidContext.mRemoteComposeState.getFromId(imageId) as Bitmap
            mCanvas.drawBitmap(
                bitmap,
                Rect(srcLeft, srcTop, srcRight, srcBottom),
                Rect(dstLeft, dstTop, dstRight, dstBottom),
                mPaint
            )
        }
    }

    override fun scale(scaleX: Float, scaleY: Float) {
        mCanvas.scale(scaleX, scaleY)
    }

    override fun startGraphicsLayer(w: Int, h: Int) {
        val node = RenderNode("layer")
        node.setPosition(0, 0, w, h)
        mPreviousCanvas = mCanvas
        mCanvas = node.beginRecording()
        mNode = node
    }

    override fun setGraphicsLayer(attributes: HashMap<Int, Any>) {
        val node = mNode ?: return
        var hasBlurEffect = false
        var hasOutline = false
        for ((key, value) in attributes) {
            when (key) {
                GraphicsLayerModifierOperation.SCALE_X -> node.scaleX = value as Float
                GraphicsLayerModifierOperation.SCALE_Y -> node.scaleY = value as Float
                GraphicsLayerModifierOperation.ROTATION_X -> node.rotationX = value as Float
                GraphicsLayerModifierOperation.ROTATION_Y -> node.rotationY = value as Float
                GraphicsLayerModifierOperation.ROTATION_Z -> node.rotationZ = value as Float
                GraphicsLayerModifierOperation.TRANSFORM_ORIGIN_X ->
                    node.pivotX = (value as Float) * node.width
                GraphicsLayerModifierOperation.TRANSFORM_ORIGIN_Y ->
                    node.pivotY = (value as Float) * node.width
                GraphicsLayerModifierOperation.TRANSLATION_X -> node.translationX = value as Float
                GraphicsLayerModifierOperation.TRANSLATION_Y -> node.translationY = value as Float
                GraphicsLayerModifierOperation.TRANSLATION_Z -> node.translationZ = value as Float
                GraphicsLayerModifierOperation.SHAPE -> hasOutline = true
                GraphicsLayerModifierOperation.SHADOW_ELEVATION -> node.elevation = value as Float
                GraphicsLayerModifierOperation.ALPHA -> node.alpha = value as Float
                GraphicsLayerModifierOperation.CAMERA_DISTANCE -> node.cameraDistance = value as Float
                GraphicsLayerModifierOperation.SPOT_SHADOW_COLOR ->
                    node.setSpotShadowColor(value as Int)
                GraphicsLayerModifierOperation.AMBIENT_SHADOW_COLOR ->
                    node.setAmbientShadowColor(value as Int)
                GraphicsLayerModifierOperation.HAS_BLUR ->
                    hasBlurEffect = (value as Int) != 0
            }
        }
        if (hasOutline) {
            val outline = Outline()
            outline.alpha = 1f
            val oShape = attributes[GraphicsLayerModifierOperation.SHAPE]
            if (oShape != null) {
                val oShapeRadius = attributes[GraphicsLayerModifierOperation.SHAPE_RADIUS]
                val type = oShape as Int
                when (type) {
                    GraphicsLayerModifierOperation.SHAPE_RECT ->
                        outline.setRect(0, 0, node.width, node.height)
                    GraphicsLayerModifierOperation.SHAPE_ROUND_RECT -> {
                        if (oShapeRadius != null) {
                            outline.setRoundRect(
                                Rect(0, 0, node.width, node.height),
                                oShapeRadius as Float
                            )
                        } else {
                            outline.setRect(0, 0, node.width, node.height)
                        }
                    }
                    GraphicsLayerModifierOperation.SHAPE_CIRCLE -> {
                        val radius = minOf(node.width, node.height) / 2f
                        outline.setRoundRect(Rect(0, 0, node.width, node.height), radius)
                    }
                }
            }
            node.setOutline(outline)
        }
        if (hasBlurEffect) {
            val blurRadiusX = (attributes[GraphicsLayerModifierOperation.BLUR_RADIUS_X] as? Float) ?: 0f
            val blurRadiusY = (attributes[GraphicsLayerModifierOperation.BLUR_RADIUS_Y] as? Float) ?: 0f
            val blurTileMode = (attributes[GraphicsLayerModifierOperation.BLUR_TILE_MODE] as? Int) ?: 0
            val tileMode = when (blurTileMode) {
                GraphicsLayerModifierOperation.TILE_MODE_CLAMP -> Shader.TileMode.CLAMP
                GraphicsLayerModifierOperation.TILE_MODE_DECAL ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Shader.TileMode.DECAL
                    else Shader.TileMode.CLAMP
                GraphicsLayerModifierOperation.TILE_MODE_MIRROR -> Shader.TileMode.MIRROR
                GraphicsLayerModifierOperation.TILE_MODE_REPEATED -> Shader.TileMode.REPEAT
                else -> Shader.TileMode.CLAMP
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val effect = RenderEffect.createBlurEffect(blurRadiusX, blurRadiusY, tileMode)
                node.setRenderEffect(effect)
            }
        }
    }

    override fun endGraphicsLayer() {
        val node = mNode ?: return
        node.endRecording()
        mCanvas = mPreviousCanvas!!
        if (mCanvas.isHardwareAccelerated) {
            mCanvas.enableZ()
            mCanvas.drawRenderNode(node)
            mCanvas.disableZ()
        }
        mNode = null
    }

    override fun translate(translateX: Float, translateY: Float) {
        mCanvas.translate(translateX, translateY)
    }

    override fun drawArc(
        left: Float, top: Float, right: Float, bottom: Float,
        startAngle: Float, sweepAngle: Float
    ) {
        mCanvas.drawArc(left, top, right, bottom, startAngle, sweepAngle, false, mPaint)
    }

    override fun drawSector(
        left: Float, top: Float, right: Float, bottom: Float,
        startAngle: Float, sweepAngle: Float
    ) {
        mCanvas.drawArc(left, top, right, bottom, startAngle, sweepAngle, true, mPaint)
    }

    override fun drawBitmap(id: Int, left: Float, top: Float, right: Float, bottom: Float) {
        val androidContext = mContext as AndroidRemoteContext
        if (androidContext.mRemoteComposeState.containsId(id)) {
            val bitmap = androidContext.mRemoteComposeState.getFromId(id) as Bitmap
            val src = Rect(0, 0, bitmap.width, bitmap.height)
            val dst = RectF(left, top, right, bottom)
            mCanvas.drawBitmap(bitmap, src, dst, mPaint)
        }
    }

    override fun drawCircle(centerX: Float, centerY: Float, radius: Float) {
        mCanvas.drawCircle(centerX, centerY, radius, mPaint)
    }

    override fun drawLine(x1: Float, y1: Float, x2: Float, y2: Float) {
        mCanvas.drawLine(x1, y1, x2, y2, mPaint)
    }

    override fun drawOval(left: Float, top: Float, right: Float, bottom: Float) {
        mCanvas.drawOval(left, top, right, bottom, mPaint)
    }

    override fun drawPath(id: Int, start: Float, end: Float) {
        mCanvas.drawPath(getPath(id, start, end), mPaint)
    }

    override fun drawRect(left: Float, top: Float, right: Float, bottom: Float) {
        mCanvas.drawRect(left, top, right, bottom, mPaint)
    }

    override fun savePaint() {
        mPaintList.add(Paint(mPaint))
    }

    override fun restorePaint() {
        mPaint = mPaintList.removeAt(mPaintList.size - 1)
    }

    override fun replacePaint(paintBundle: PaintBundle) {
        mPaint.reset()
        applyPaint(paintBundle)
    }

    override fun drawRoundRect(
        left: Float, top: Float, right: Float, bottom: Float,
        radiusX: Float, radiusY: Float
    ) {
        mCanvas.drawRoundRect(left, top, right, bottom, radiusX, radiusY, mPaint)
    }

    override fun drawTextOnPath(textId: Int, pathId: Int, hOffset: Float, vOffset: Float) {
        mCanvas.drawTextOnPath(getText(textId)!!, getPath(pathId, 0f, 1f), hOffset, vOffset, mPaint)
    }

    private var mCachedFontMetrics: Paint.FontMetrics? = null

    override fun getTextBounds(textId: Int, start: Int, end: Int, flags: Int, bounds: FloatArray) {
        val str = getText(textId) ?: run {
            bounds[0] = 0f; bounds[1] = 0f; bounds[2] = 0f; bounds[3] = 0f
            return
        }
        val effectiveEnd = if (end == -1 || end > str.length) str.length else end
        if (mCachedFontMetrics == null) mCachedFontMetrics = mPaint.fontMetrics
        mPaint.getFontMetrics(mCachedFontMetrics)
        mPaint.getTextBounds(str, start, effectiveEnd, mTmpRect)
        if ((flags and TEXT_MEASURE_SPACES) != 0) {
            bounds[0] = 0f
            bounds[2] = mPaint.measureText(str, start, effectiveEnd)
        } else {
            bounds[0] = mTmpRect.left.toFloat()
            bounds[2] = if ((flags and TEXT_MEASURE_MONOSPACE_WIDTH) != 0) {
                mPaint.measureText(str, start, effectiveEnd) - mTmpRect.left
            } else {
                mTmpRect.right.toFloat()
            }
        }
        if ((flags and TEXT_MEASURE_FONT_HEIGHT) != 0) {
            bounds[1] = Math.round(mCachedFontMetrics!!.ascent).toFloat()
            bounds[3] = Math.round(mCachedFontMetrics!!.descent).toFloat()
        } else {
            bounds[1] = mTmpRect.top.toFloat()
            bounds[3] = mTmpRect.bottom.toFloat()
        }
    }

    override fun layoutComplexText(
        textId: Int, start: Int, end: Int,
        alignment: Int, overflow: Int, maxLines: Int, maxWidth: Float,
        letterSpacing: Float, lineHeightAdd: Float, lineHeightMultiplier: Float,
        lineBreakStrategy: Int, hyphenationFrequency: Int, justificationMode: Int,
        underline: Boolean, strikethrough: Boolean, flags: Int
    ): RcPlatformServices.ComputedTextLayout? {
        val str = getText(textId) ?: return null
        val effectiveEnd = if (end == -1 || end > str.length) str.length else end

        val textPaint = TextPaint()
        var useAdvancedFeatures = (flags and TEXT_MEASURE_AUTOSIZE) != 0

        if (letterSpacing > 0f) {
            textPaint.letterSpacing = letterSpacing
            useAdvancedFeatures = true
        }
        if (underline) {
            mPaint.isUnderlineText = true
            useAdvancedFeatures = true
        }
        if (strikethrough) {
            mPaint.isStrikeThruText = true
            useAdvancedFeatures = true
        }

        textPaint.set(mPaint)
        val builder = StaticLayout.Builder.obtain(str, start, effectiveEnd, textPaint, maxWidth.toInt())
        when (alignment) {
            CoreText.TEXT_ALIGN_RIGHT, CoreText.TEXT_ALIGN_END ->
                builder.setAlignment(Layout.Alignment.ALIGN_OPPOSITE)
            CoreText.TEXT_ALIGN_CENTER ->
                builder.setAlignment(Layout.Alignment.ALIGN_CENTER)
            else -> builder.setAlignment(Layout.Alignment.ALIGN_NORMAL)
        }
        when (overflow) {
            CoreText.OVERFLOW_ELLIPSIS -> builder.setEllipsize(TextUtils.TruncateAt.END)
            CoreText.OVERFLOW_MIDDLE_ELLIPSIS -> builder.setEllipsize(TextUtils.TruncateAt.MIDDLE)
            CoreText.OVERFLOW_START_ELLIPSIS -> builder.setEllipsize(TextUtils.TruncateAt.START)
        }
        builder.setMaxLines(maxLines)
        builder.setIncludePad(false)

        if (lineBreakStrategy > 0) {
            builder.setBreakStrategy(lineBreakStrategy)
            useAdvancedFeatures = true
        }
        if (hyphenationFrequency > 0) {
            builder.setHyphenationFrequency(hyphenationFrequency)
            useAdvancedFeatures = true
        }
        if (justificationMode > 0) {
            builder.setJustificationMode(justificationMode)
            useAdvancedFeatures = true
        }
        if (lineHeightAdd > 0f || lineHeightMultiplier != 1f) {
            builder.setLineSpacing(lineHeightAdd, lineHeightMultiplier)
            useAdvancedFeatures = true
        }

        val staticLayout = builder.build()
        return if (useAdvancedFeatures) {
            val bounds = Rect(0, 0, 0, 0)
            val isHyphenated = getTightBoundingBox(staticLayout, bounds)
            AndroidComputedTextLayout(staticLayout, bounds.width().toFloat(), bounds.height().toFloat(), isHyphenated)
        } else {
            AndroidComputedTextLayout(staticLayout, staticLayout.width.toFloat(), staticLayout.height.toFloat(), false)
        }
    }

    fun isLineHyphenated(layout: StaticLayout, originalText: CharSequence, lineIndex: Int): Boolean {
        if (lineIndex >= layout.lineCount - 1) return false
        val lastCharIndexOnLine = layout.getLineEnd(lineIndex) - 1
        val charBeforeBreak = originalText[lastCharIndexOnLine]
        val charAfterBreak = originalText[lastCharIndexOnLine + 1]
        return !charBeforeBreak.isWhitespace() && !charAfterBreak.isWhitespace()
    }

    fun getTightBoundingBox(layout: StaticLayout, bounds: Rect): Boolean {
        val lineCount = layout.lineCount
        if (lineCount == 0) return false
        var isHyphenated = false
        val top = layout.getLineTop(0)
        val bottom = layout.getLineBottom(lineCount - 1)
        var maxContentWidth = 0f
        for (i in 0 until lineCount) {
            val lineWidth = layout.getLineMax(i)
            if (lineWidth > maxContentWidth) maxContentWidth = lineWidth
        }
        var minLeft = 0f
        for (i in 0 until lineCount) {
            val lineLeft = layout.getLineLeft(i)
            if (lineLeft < minLeft) minLeft = lineLeft
            if (!isHyphenated) {
                isHyphenated = isHyphenated or isLineHyphenated(layout, layout.text, i)
            }
        }
        bounds.left = minLeft.toInt()
        bounds.top = top
        bounds.right = maxContentWidth.toInt()
        bounds.bottom = bottom
        return isHyphenated
    }

    override fun drawTextRun(
        textId: Int, start: Int, end: Int,
        contextStart: Int, contextEnd: Int,
        x: Float, y: Float, rtl: Boolean
    ) {
        var textToPaint = getText(textId) ?: return
        val effectiveEnd = if (end == -1) -1 else end
        textToPaint = if (effectiveEnd == -1) {
            if (start != 0) textToPaint.substring(start) else textToPaint
        } else if (effectiveEnd > textToPaint.length) {
            textToPaint.substring(start)
        } else {
            textToPaint.substring(start, effectiveEnd)
        }
        mCanvas.drawText(textToPaint, x, y, mPaint)
    }

    override fun drawComplexText(computedTextLayout: RcPlatformServices.ComputedTextLayout?) {
        if (computedTextLayout == null) return
        val staticLayout = (computedTextLayout as AndroidComputedTextLayout).get()
        staticLayout.draw(mCanvas)
    }

    override fun drawTweenPath(path1Id: Int, path2Id: Int, tween: Float, start: Float, end: Float) {
        mCanvas.drawPath(getPath(path1Id, path2Id, tween, start, end), mPaint)
    }

    private fun remoteToAndroidPorterDuffMode(mode: Int): PorterDuff.Mode {
        return when (mode) {
            PaintBundle.BLEND_MODE_CLEAR -> PorterDuff.Mode.CLEAR
            PaintBundle.BLEND_MODE_SRC -> PorterDuff.Mode.SRC
            PaintBundle.BLEND_MODE_DST -> PorterDuff.Mode.DST
            PaintBundle.BLEND_MODE_SRC_OVER -> PorterDuff.Mode.SRC_OVER
            PaintBundle.BLEND_MODE_DST_OVER -> PorterDuff.Mode.DST_OVER
            PaintBundle.BLEND_MODE_SRC_IN -> PorterDuff.Mode.SRC_IN
            PaintBundle.BLEND_MODE_DST_IN -> PorterDuff.Mode.DST_IN
            PaintBundle.BLEND_MODE_SRC_OUT -> PorterDuff.Mode.SRC_OUT
            PaintBundle.BLEND_MODE_DST_OUT -> PorterDuff.Mode.DST_OUT
            PaintBundle.BLEND_MODE_SRC_ATOP -> PorterDuff.Mode.SRC_ATOP
            PaintBundle.BLEND_MODE_DST_ATOP -> PorterDuff.Mode.DST_ATOP
            PaintBundle.BLEND_MODE_XOR -> PorterDuff.Mode.XOR
            PaintBundle.BLEND_MODE_SCREEN -> PorterDuff.Mode.SCREEN
            PaintBundle.BLEND_MODE_OVERLAY -> PorterDuff.Mode.OVERLAY
            PaintBundle.BLEND_MODE_DARKEN -> PorterDuff.Mode.DARKEN
            PaintBundle.BLEND_MODE_LIGHTEN -> PorterDuff.Mode.LIGHTEN
            PaintBundle.BLEND_MODE_MULTIPLY -> PorterDuff.Mode.MULTIPLY
            PaintBundle.PORTER_MODE_ADD -> PorterDuff.Mode.ADD
            else -> PorterDuff.Mode.SRC_OVER
        }
    }

    private fun remoteToAndroidBlendMode(mode: Int): BlendMode? {
        return when (mode) {
            PaintBundle.BLEND_MODE_CLEAR -> BlendMode.CLEAR
            PaintBundle.BLEND_MODE_SRC -> BlendMode.SRC
            PaintBundle.BLEND_MODE_DST -> BlendMode.DST
            PaintBundle.BLEND_MODE_SRC_OVER -> BlendMode.SRC_OVER
            PaintBundle.BLEND_MODE_DST_OVER -> BlendMode.DST_OVER
            PaintBundle.BLEND_MODE_SRC_IN -> BlendMode.SRC_IN
            PaintBundle.BLEND_MODE_DST_IN -> BlendMode.DST_IN
            PaintBundle.BLEND_MODE_SRC_OUT -> BlendMode.SRC_OUT
            PaintBundle.BLEND_MODE_DST_OUT -> BlendMode.DST_OUT
            PaintBundle.BLEND_MODE_SRC_ATOP -> BlendMode.SRC_ATOP
            PaintBundle.BLEND_MODE_DST_ATOP -> BlendMode.DST_ATOP
            PaintBundle.BLEND_MODE_XOR -> BlendMode.XOR
            PaintBundle.BLEND_MODE_PLUS -> BlendMode.PLUS
            PaintBundle.BLEND_MODE_MODULATE -> BlendMode.MODULATE
            PaintBundle.BLEND_MODE_SCREEN -> BlendMode.SCREEN
            PaintBundle.BLEND_MODE_OVERLAY -> BlendMode.OVERLAY
            PaintBundle.BLEND_MODE_DARKEN -> BlendMode.DARKEN
            PaintBundle.BLEND_MODE_LIGHTEN -> BlendMode.LIGHTEN
            PaintBundle.BLEND_MODE_COLOR_DODGE -> BlendMode.COLOR_DODGE
            PaintBundle.BLEND_MODE_COLOR_BURN -> BlendMode.COLOR_BURN
            PaintBundle.BLEND_MODE_HARD_LIGHT -> BlendMode.HARD_LIGHT
            PaintBundle.BLEND_MODE_SOFT_LIGHT -> BlendMode.SOFT_LIGHT
            PaintBundle.BLEND_MODE_DIFFERENCE -> BlendMode.DIFFERENCE
            PaintBundle.BLEND_MODE_EXCLUSION -> BlendMode.EXCLUSION
            PaintBundle.BLEND_MODE_MULTIPLY -> BlendMode.MULTIPLY
            PaintBundle.BLEND_MODE_HUE -> BlendMode.HUE
            PaintBundle.BLEND_MODE_SATURATION -> BlendMode.SATURATION
            PaintBundle.BLEND_MODE_COLOR -> BlendMode.COLOR
            PaintBundle.BLEND_MODE_LUMINOSITY -> BlendMode.LUMINOSITY
            PaintBundle.BLEND_MODE_NULL -> null
            else -> null
        }
    }

    val mCachedPaintChanges: PaintChanges = object : PaintChanges {
        private var mFontBuilder: Font.Builder? = null
        val mTmpMatrix = Matrix()

        override fun setTextSize(size: Float) { mPaint.textSize = size }

        override fun setTypeFace(fontType: Int, weight: Int, italic: Boolean) {
            when (fontType) {
                PaintBundle.FONT_TYPE_DEFAULT -> {
                    mPaint.typeface = if (weight == 400 && !italic) Typeface.DEFAULT
                    else Typeface.create(Typeface.DEFAULT, weight, italic)
                }
                PaintBundle.FONT_TYPE_SERIF -> {
                    mPaint.typeface = if (weight == 400 && !italic) Typeface.SERIF
                    else Typeface.create(Typeface.SERIF, weight, italic)
                }
                PaintBundle.FONT_TYPE_SANS_SERIF -> {
                    mPaint.typeface = if (weight == 400 && !italic) Typeface.SANS_SERIF
                    else Typeface.create(Typeface.SANS_SERIF, weight, italic)
                }
                PaintBundle.FONT_TYPE_MONOSPACE -> {
                    mPaint.typeface = if (weight == 400 && !italic) Typeface.MONOSPACE
                    else Typeface.create(Typeface.MONOSPACE, weight, italic)
                }
                else -> {
                    val fi = mContext.getObject(fontType) as RemoteContext.FontInfo
                    var builder = fi.fontBuilder as? Font.Builder
                    if (builder == null) {
                        builder = createFontBuilder(fi.mFontData, weight, italic)
                        fi.fontBuilder = builder
                    }
                    mFontBuilder = builder
                    setAxis(null)
                }
            }
        }

        override fun setShaderMatrix(matrixId: Float) {
            val id = Utils.idFromNan(matrixId)
            if (id == 0) {
                mPaint.shader.setLocalMatrix(null)
                return
            }
            val matAccess = mContext.getObject(id) as MatrixAccess
            mTmpMatrix.setValues(MatrixAccess.to3x3(matAccess.get()))
            mPaint.shader.setLocalMatrix(mTmpMatrix)
        }

        override fun setTypeFace(fontType: String, weight: Int, italic: Boolean) {
            val path = getFontPath(fontType) ?: return
            mFontBuilder = Font.Builder(File(path)).also {
                it.setWeight(weight)
                it.setSlant(if (italic) FontStyle.FONT_SLANT_ITALIC else FontStyle.FONT_SLANT_UPRIGHT)
            }
            setAxis(null)
        }

        private fun createFontBuilder(data: ByteArray, weight: Int, italic: Boolean): Font.Builder {
            val buffer = ByteBuffer.allocateDirect(data.size)
            buffer.put(data)
            buffer.rewind()
            val builder = Font.Builder(buffer)
            builder.setWeight(weight)
            builder.setSlant(if (italic) FontStyle.FONT_SLANT_ITALIC else FontStyle.FONT_SLANT_UPRIGHT)
            mFontBuilder = builder
            setAxis(null)
            return builder
        }

        private fun setAxis(axis: Array<FontVariationAxis>?) {
            val builder = mFontBuilder ?: return
            try {
                if (axis != null) builder.setFontVariationSettings(axis)
                val font = builder.build()
                val fontFamilyBuilder = FontFamily.Builder(font)
                val fontFamily = fontFamilyBuilder.build()
                val typeface = Typeface.CustomFallbackBuilder(fontFamily)
                    .setSystemFallback("sans-serif")
                    .build()
                mPaint.typeface = typeface
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        private fun getFontPath(fontName: String): String? {
            val fontsDir = File(SYSTEM_FONTS_PATH)
            if (!fontsDir.exists() || !fontsDir.isDirectory) {
                System.err.println("System fonts directory not found")
                return null
            }
            val fontFiles = fontsDir.listFiles()
            if (fontFiles == null) {
                System.err.println("Unable to list font files")
                return null
            }
            val lowerFontName = fontName.lowercase(Locale.ROOT)
            for (fontFile in fontFiles) {
                if (fontFile.name.lowercase(Locale.ROOT).contains(lowerFontName)) {
                    return fontFile.absolutePath
                }
            }
            System.err.println("font \"$fontName\" not found")
            return null
        }

        override fun setFontVariationAxes(tags: Array<String>, values: FloatArray) {
            val axes = Array(tags.size) { FontVariationAxis(tags[it], values[it]) }
            setAxis(axes)
        }

        override fun setTextureShader(
            bitmapId: Int, tileX: Short, tileY: Short, filterMode: Short, maxAnisotropy: Short
        ) {
            var shader = mContext.mRemoteComposeState.getFromId(
                bitmapId + RemoteComposeState.BITMAP_TEXTURE_ID_OFFSET
            ) as? BitmapShader
            if (shader != null) {
                mPaint.shader = shader
                return
            }
            val bitmap = mContext.mRemoteComposeState.getFromId(bitmapId) as? Bitmap ?: return
            shader = BitmapShader(
                bitmap,
                Shader.TileMode.entries[tileX.toInt()],
                Shader.TileMode.entries[tileY.toInt()]
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (filterMode > 0) shader.setFilterMode(filterMode.toInt())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    if (maxAnisotropy > 0) shader.setMaxAnisotropy(maxAnisotropy.toInt())
                }
            }
            mPaint.shader = shader
        }

        override fun setPathEffect(pathEffect: FloatArray?) {
            if (pathEffect == null) {
                mPaint.pathEffect = null
                return
            }
            val pe = PaintPathEffects.parse(pathEffect, 0)
            mPaint.pathEffect = getPathEffect(pe)
        }

        private fun getPathEffect(pe: PaintPathEffects?): PathEffect? {
            if (pe == null) return null
            return when (pe.getType()) {
                PaintPathEffects.DASH -> {
                    val dash = pe as PaintPathEffects.Dash
                    DashPathEffect(dash.mIntervals, dash.mPhase)
                }
                PaintPathEffects.DISCRETE_PATH -> {
                    val discrete = pe as PaintPathEffects.Discrete
                    DiscretePathEffect(discrete.mSegmentLength, discrete.mDeviation)
                }
                PaintPathEffects.PATH_DASH -> {
                    val pathDash = pe as PaintPathEffects.PathDash
                    PathDashPathEffect(
                        getPath(pathDash.mShapeId, 0f, 1f),
                        pathDash.mAdvance, pathDash.mPhase,
                        PathDashPathEffect.Style.entries[pathDash.mStyle]
                    )
                }
                PaintPathEffects.SUM -> {
                    val sum = pe as PaintPathEffects.Sum
                    SumPathEffect(getPathEffect(sum.mFirst), getPathEffect(sum.mSecond))
                }
                PaintPathEffects.COMPOSE -> {
                    val compose = pe as PaintPathEffects.Compose
                    ComposePathEffect(getPathEffect(compose.mOuterPE), getPathEffect(compose.mInnerPE))
                }
                else -> null
            }
        }

        override fun setStrokeWidth(width: Float) { mPaint.strokeWidth = width }
        override fun setColor(color: Int) { mPaint.color = color }
        override fun setStrokeCap(cap: Int) { mPaint.strokeCap = Paint.Cap.entries[cap] }
        override fun setStyle(style: Int) { mPaint.style = Paint.Style.entries[style] }

        @SuppressLint("NewApi")
        override fun setShader(shaderId: Int) {
            if (shaderId == 0) {
                mPaint.shader = null
                return
            }
            val data = getShaderData(shaderId) ?: return
            val shader = RuntimeShader(getText(data.getShaderTextId())!!)
            val floatNames = data.getUniformFloatNames()
            for (name in floatNames) {
                val v = data.getUniformFloats(name)
                if (v.size == 1 && v[0].isNaN()) {
                    val values = mContext.getCollectionsAccess()?.getDynamicFloats(Utils.idFromNan(v[0]))
                    if (values != null) shader.setFloatUniform(name, values)
                } else {
                    shader.setFloatUniform(name, v)
                }
            }
            val intNames = data.getUniformIntegerNames()
            for (name in intNames) {
                shader.setIntUniform(name, data.getUniformInts(name))
            }
            val bitmapNames = data.getUniformBitmapNames()
            for (name in bitmapNames) {
                val bitmapId = data.getUniformBitmapId(name)
                val bitmap = (mContext as AndroidRemoteContext).mRemoteComposeState.getFromId(bitmapId) as Bitmap
                val bitmapShader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
                shader.setInputShader(name, bitmapShader)
            }
            mPaint.shader = shader
        }

        override fun setImageFilterQuality(quality: Int) { mPaint.isFilterBitmap = quality == 1 }
        override fun setBlendMode(mode: Int) { mPaint.blendMode = remoteToAndroidBlendMode(mode) }
        override fun setAlpha(a: Float) { mPaint.alpha = (255 * a).toInt() }
        override fun setStrokeMiter(miter: Float) { mPaint.strokeMiter = miter }
        override fun setStrokeJoin(join: Int) { mPaint.strokeJoin = Paint.Join.entries[join] }
        override fun setFilterBitmap(filter: Boolean) { mPaint.isFilterBitmap = filter }
        override fun setAntiAlias(aa: Boolean) { mPaint.isAntiAlias = aa }

        override fun clear(mask: Long) {
            if ((mask and (1L shl PaintBundle.COLOR_FILTER)) != 0L) {
                mPaint.colorFilter = null
            }
        }

        private val mTileModes = arrayOf(
            Shader.TileMode.CLAMP, Shader.TileMode.REPEAT, Shader.TileMode.MIRROR
        )

        override fun setLinearGradient(
            colorsArray: IntArray, stopsArray: FloatArray?,
            startX: Float, startY: Float, endX: Float, endY: Float, tileMode: Int
        ) {
            mPaint.shader = LinearGradient(startX, startY, endX, endY, colorsArray, stopsArray, mTileModes[tileMode])
        }

        override fun setRadialGradient(
            colorsArray: IntArray, stopsArray: FloatArray?,
            centerX: Float, centerY: Float, radius: Float, tileMode: Int
        ) {
            mPaint.shader = RadialGradient(centerX, centerY, radius, colorsArray, stopsArray, mTileModes[tileMode])
        }

        override fun setSweepGradient(
            colorsArray: IntArray, stopsArray: FloatArray?, centerX: Float, centerY: Float
        ) {
            mPaint.shader = SweepGradient(centerX, centerY, colorsArray, stopsArray)
        }

        override fun setColorFilter(color: Int, mode: Int) {
            val pmode = remoteToAndroidPorterDuffMode(mode)
            mPaint.colorFilter = PorterDuffColorFilter(color, pmode)
        }
    }

    override fun applyPaint(paintData: PaintBundle) {
        paintData.applyPaintChange(this, mCachedPaintChanges)
    }

    override fun matrixScale(scaleX: Float, scaleY: Float, centerX: Float, centerY: Float) {
        if (centerX.isNaN()) mCanvas.scale(scaleX, scaleY)
        else mCanvas.scale(scaleX, scaleY, centerX, centerY)
    }

    override fun matrixTranslate(translateX: Float, translateY: Float) {
        mCanvas.translate(translateX, translateY)
    }

    override fun matrixSkew(skewX: Float, skewY: Float) {
        mCanvas.skew(skewX, skewY)
    }

    override fun matrixRotate(rotate: Float, pivotX: Float, pivotY: Float) {
        if (pivotX.isNaN()) mCanvas.rotate(rotate)
        else mCanvas.rotate(rotate, pivotX, pivotY)
    }

    override fun matrixSave() { mCanvas.save() }
    override fun matrixRestore() { mCanvas.restore() }

    override fun clipRect(left: Float, top: Float, right: Float, bottom: Float) {
        mCanvas.clipRect(left, top, right, bottom)
    }

    override fun roundedClipRect(
        width: Float, height: Float,
        topStart: Float, topEnd: Float, bottomStart: Float, bottomEnd: Float
    ) {
        val roundedPath = Path()
        val radii = floatArrayOf(
            topStart, topStart, topEnd, topEnd,
            bottomEnd, bottomEnd, bottomStart, bottomStart
        )
        roundedPath.addRoundRect(0f, 0f, width, height, radii, Path.Direction.CW)
        mCanvas.clipPath(roundedPath)
    }

    override fun clipPath(pathId: Int, regionOp: Int) {
        val path = getPath(pathId, 0f, 1f)
        if (regionOp == ClipPath.DIFFERENCE) mCanvas.clipOutPath(path)
        else mCanvas.clipPath(path)
    }

    override fun tweenPath(out: Int, path1: Int, path2: Int, tween: Float) {
        val p = getPathArray(path1, path2, tween)
        val androidContext = mContext as AndroidRemoteContext
        androidContext.mRemoteComposeState.putPathData(out, p)
    }

    override fun combinePath(out: Int, path1: Int, path2: Int, operation: Byte) {
        val p1 = getPath(path1, 0f, 1f)
        val p2 = getPath(path2, 0f, 1f)
        val ops = arrayOf(
            Path.Op.DIFFERENCE, Path.Op.INTERSECT, Path.Op.REVERSE_DIFFERENCE,
            Path.Op.UNION, Path.Op.XOR
        )
        val p = Path(p1)
        p.op(p2, ops[operation.toInt()])
        val androidContext = mContext as AndroidRemoteContext
        androidContext.mRemoteComposeState.putPath(out, p)
    }

    override fun reset() {
        mPaint.typeface = Typeface.DEFAULT
        mPaint.reset()
    }

    private fun getPath(path1Id: Int, path2Id: Int, tween: Float, start: Float, end: Float): Path {
        return getPath(getPathArray(path1Id, path2Id, tween), start, end)
    }

    private fun getPathArray(path1Id: Int, path2Id: Int, tween: Float): FloatArray {
        val androidContext = mContext as AndroidRemoteContext
        if (tween == 0.0f) return androidContext.mRemoteComposeState.getPathData(path1Id)!!
        if (tween == 1.0f) return androidContext.mRemoteComposeState.getPathData(path2Id)!!
        val data1 = androidContext.mRemoteComposeState.getPathData(path1Id)!!
        val data2 = androidContext.mRemoteComposeState.getPathData(path2Id)!!
        return FloatArray(data2.size) { i ->
            if (data1[i].isNaN() || data2[i].isNaN()) data1[i]
            else (data2[i] - data1[i]) * tween + data1[i]
        }
    }

    private fun getPath(tmp: FloatArray, start: Float, end: Float): Path {
        val path = Path()
        FloatsToPath.genPath(path, tmp, start, end)
        return path
    }

    private fun getPath(id: Int, start: Float, end: Float): Path {
        val androidContext = mContext as AndroidRemoteContext
        val p = androidContext.mRemoteComposeState.getPath(id) as? Path
        val w = androidContext.mRemoteComposeState.getPathWinding(id)
        if (p != null) return p
        val path = Path()
        val pathData = androidContext.mRemoteComposeState.getPathData(id)
        if (pathData != null) {
            FloatsToPath.genPath(path, pathData, start, end)
            when (w) {
                1 -> path.fillType = Path.FillType.EVEN_ODD
                2 -> path.fillType = Path.FillType.INVERSE_EVEN_ODD
                3 -> path.fillType = Path.FillType.INVERSE_WINDING
            }
            androidContext.mRemoteComposeState.putPath(id, path)
        }
        return path
    }

    override fun getText(id: Int): String? =
        mContext.mRemoteComposeState.getFromId(id) as? String

    private fun getShaderData(id: Int): ShaderData? =
        mContext.mRemoteComposeState.getFromId(id) as? ShaderData

    override fun matrixFromPath(pathId: Int, fraction: Float, vOffset: Float, flags: Int) {
        val path = getPath(pathId, 0f, 1f)
        val measure = PathMeasure(path, false)
        val len = measure.length
        val matrix = Matrix()
        measure.getMatrix((len * fraction) % len, matrix, flags)
        mCanvas.concat(matrix)
    }

    private val mCCache = HashMap<Bitmap, Canvas>()

    override fun drawToBitmap(bitmapId: Int, mode: Int, color: Int) {
        if (mMainCanvas == null) mMainCanvas = mCanvas
        if (bitmapId == 0) {
            mCanvas = mMainCanvas!!
            return
        }
        val bitmap = mContext.mRemoteComposeState.getFromId(bitmapId) as Bitmap
        Objects.requireNonNull(bitmap)
        val cached = mCCache[bitmap]
        if (cached != null) {
            mCanvas = cached
            if ((mode and 1) == 0) bitmap.eraseColor(color)
            return
        }
        mCanvas = Canvas(bitmap)
        if ((mode and 1) == 0) bitmap.eraseColor(color)
        mCCache[bitmap] = mCanvas
    }
}
