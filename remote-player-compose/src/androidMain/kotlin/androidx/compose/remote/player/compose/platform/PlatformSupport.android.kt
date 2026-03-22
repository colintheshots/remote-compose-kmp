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
package androidx.compose.remote.player.compose.platform

import android.graphics.Bitmap
import android.graphics.Outline
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.util.Log
import androidx.compose.remote.core.RcPlatformServices
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.operations.layout.managers.CoreText
import androidx.compose.remote.core.operations.layout.modifiers.GraphicsLayerModifierOperation
import androidx.compose.remote.core.operations.paint.PaintChanges
import androidx.compose.remote.player.compose.context.AndroidComposePaintChanges
import androidx.compose.remote.player.core.platform.AndroidComputedTextLayout
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.nativeCanvas
import kotlin.math.min

actual class PlatformTextSupport actual constructor() {
    private val tmpRect: Rect = Rect()
    private var cachedFontMetrics: android.graphics.Paint.FontMetrics? = null

    actual fun getTextBounds(
        text: String,
        start: Int,
        end: Int,
        flags: Int,
        bounds: FloatArray,
        paint: Paint,
    ) {
        val nativePaint = paint.asFrameworkPaint()
        if (cachedFontMetrics == null) {
            cachedFontMetrics = nativePaint.fontMetrics
        }
        nativePaint.getFontMetrics(cachedFontMetrics)
        nativePaint.getTextBounds(text, 0, text.length, tmpRect)

        if ((flags and PaintContext.TEXT_MEASURE_SPACES) != 0) {
            bounds[0] = 0f
            bounds[2] = nativePaint.measureText(text)
        } else {
            bounds[0] = tmpRect.left.toFloat()
            if ((flags and PaintContext.TEXT_MEASURE_MONOSPACE_WIDTH) != 0) {
                bounds[2] = nativePaint.measureText(text) - tmpRect.left
            } else {
                bounds[2] = tmpRect.right.toFloat()
            }
        }

        if ((flags and PaintContext.TEXT_MEASURE_FONT_HEIGHT) != 0) {
            bounds[1] = cachedFontMetrics!!.ascent.toInt().toFloat()
            bounds[3] = cachedFontMetrics!!.descent.toInt().toFloat()
        } else {
            bounds[1] = tmpRect.top.toFloat()
            bounds[3] = tmpRect.bottom.toFloat()
        }
    }

    actual fun drawTextRun(
        text: String,
        x: Float,
        y: Float,
        canvas: Canvas,
        paint: Paint,
    ) {
        canvas.nativeCanvas.drawText(text, x, y, paint.asFrameworkPaint())
    }

    actual fun drawTextOnPath(
        text: String,
        nativePath: Any,
        hOffset: Float,
        vOffset: Float,
        canvas: Canvas,
        paint: Paint,
    ) {
        val path = nativePath as? android.graphics.Path
        if (path != null) {
            canvas.nativeCanvas.drawTextOnPath(text, path, hOffset, vOffset, paint.asFrameworkPaint())
        }
    }

    actual fun layoutComplexText(
        text: String,
        start: Int,
        end: Int,
        alignment: Int,
        overflow: Int,
        maxLines: Int,
        maxWidth: Float,
        paint: Paint,
    ): RcPlatformServices.ComputedTextLayout? {
        val textPaint = TextPaint()
        textPaint.set(paint.asFrameworkPaint())
        val staticLayoutBuilder =
            StaticLayout.Builder.obtain(text, start, end, textPaint, maxWidth.toInt())
        when (alignment) {
            CoreText.TEXT_ALIGN_RIGHT,
            CoreText.TEXT_ALIGN_END ->
                staticLayoutBuilder.setAlignment(Layout.Alignment.ALIGN_OPPOSITE)
            CoreText.TEXT_ALIGN_CENTER ->
                staticLayoutBuilder.setAlignment(Layout.Alignment.ALIGN_CENTER)
            else -> staticLayoutBuilder.setAlignment(Layout.Alignment.ALIGN_NORMAL)
        }
        when (overflow) {
            CoreText.OVERFLOW_ELLIPSIS ->
                staticLayoutBuilder.setEllipsize(TextUtils.TruncateAt.END)
            CoreText.OVERFLOW_MIDDLE_ELLIPSIS ->
                staticLayoutBuilder.setEllipsize(TextUtils.TruncateAt.MIDDLE)
            CoreText.OVERFLOW_START_ELLIPSIS ->
                staticLayoutBuilder.setEllipsize(TextUtils.TruncateAt.START)
            else -> {}
        }
        staticLayoutBuilder.setMaxLines(maxLines)
        staticLayoutBuilder.setIncludePad(false)

        val staticLayout = staticLayoutBuilder.build()
        return AndroidComputedTextLayout(
            staticLayout,
            staticLayout.width.toFloat(),
            staticLayout.height.toFloat(),
            false,
        )
    }

    actual fun drawComplexText(
        computedTextLayout: RcPlatformServices.ComputedTextLayout,
        canvas: Canvas,
    ) {
        val staticLayout = (computedTextLayout as AndroidComputedTextLayout).get()
        staticLayout.draw(canvas.nativeCanvas)
    }
}

actual class PlatformBitmapSupport actual constructor() {
    actual fun drawBitmap(
        imageData: Any,
        srcLeft: Int,
        srcTop: Int,
        srcRight: Int,
        srcBottom: Int,
        dstLeft: Int,
        dstTop: Int,
        dstRight: Int,
        dstBottom: Int,
        canvas: Canvas,
        paint: Paint,
    ) {
        val bitmap = imageData as? Bitmap ?: return
        canvas.nativeCanvas.drawBitmap(
            bitmap,
            Rect(srcLeft, srcTop, srcRight, srcBottom),
            Rect(dstLeft, dstTop, dstRight, dstBottom),
            paint.asFrameworkPaint(),
        )
    }

    actual fun drawBitmap(
        imageData: Any,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        canvas: Canvas,
        paint: Paint,
    ) {
        val bitmap = imageData as? Bitmap ?: return
        val src = Rect(0, 0, bitmap.width, bitmap.height)
        val dst = RectF(left, top, right, bottom)
        canvas.nativeCanvas.drawBitmap(bitmap, src, dst, paint.asFrameworkPaint())
    }

    actual fun getCanvasForBitmap(bitmap: Any): Canvas {
        return Canvas((bitmap as Bitmap).asImageBitmap())
    }

    actual fun eraseBitmap(bitmap: Any, color: Int) {
        (bitmap as? Bitmap)?.eraseColor(color)
    }
}

actual class PlatformGraphicsLayerSupport actual constructor() {
    private var node: RenderNode? = null

    actual fun startGraphicsLayer(w: Int, h: Int): Canvas? {
        val newNode = RenderNode("layer")
        newNode.setPosition(0, 0, w, h)
        node = newNode
        return Canvas(newNode.beginRecording())
    }

    actual fun setGraphicsLayer(attributes: HashMap<Int, Any>) {
        val node = this.node ?: return
        var hasBlurEffect = false
        var hasOutline = false
        for (key in attributes.keys) {
            val value = attributes[key]
            when (key) {
                GraphicsLayerModifierOperation.SCALE_X -> node.scaleX = value as Float
                GraphicsLayerModifierOperation.SCALE_Y -> node.scaleY = value as Float
                GraphicsLayerModifierOperation.ROTATION_X -> node.rotationX = value as Float
                GraphicsLayerModifierOperation.ROTATION_Y -> node.rotationY = value as Float
                GraphicsLayerModifierOperation.ROTATION_Z -> node.rotationZ = value as Float
                GraphicsLayerModifierOperation.TRANSFORM_ORIGIN_X ->
                    node.pivotX = value as Float * node.width
                GraphicsLayerModifierOperation.TRANSFORM_ORIGIN_Y ->
                    node.pivotY = value as Float * node.width
                GraphicsLayerModifierOperation.TRANSLATION_X ->
                    node.translationX = value as Float
                GraphicsLayerModifierOperation.TRANSLATION_Y ->
                    node.translationY = value as Float
                GraphicsLayerModifierOperation.TRANSLATION_Z ->
                    node.translationZ = value as Float
                GraphicsLayerModifierOperation.SHAPE -> hasOutline = true
                GraphicsLayerModifierOperation.SHADOW_ELEVATION ->
                    node.elevation = value as Float
                GraphicsLayerModifierOperation.ALPHA -> node.alpha = value as Float
                GraphicsLayerModifierOperation.CAMERA_DISTANCE ->
                    node.setCameraDistance(value as Float)
                GraphicsLayerModifierOperation.SPOT_SHADOW_COLOR ->
                    node.spotShadowColor = value as Int
                GraphicsLayerModifierOperation.AMBIENT_SHADOW_COLOR ->
                    node.ambientShadowColor = value as Int
                GraphicsLayerModifierOperation.HAS_BLUR ->
                    hasBlurEffect = (value as Int?) != 0
            }
        }
        if (hasOutline) {
            val outline = Outline()
            outline.alpha = 1f
            val oShape = attributes[GraphicsLayerModifierOperation.SHAPE]
            if (oShape != null) {
                val oShapeRadius = attributes[GraphicsLayerModifierOperation.SHAPE_RADIUS]
                val type = oShape as Int
                if (type == GraphicsLayerModifierOperation.SHAPE_RECT) {
                    outline.setRect(0, 0, node.width, node.height)
                } else if (type == GraphicsLayerModifierOperation.SHAPE_ROUND_RECT) {
                    if (oShapeRadius != null) {
                        val radius = oShapeRadius as Float
                        outline.setRoundRect(Rect(0, 0, node.width, node.height), radius)
                    } else {
                        outline.setRect(0, 0, node.width, node.height)
                    }
                } else if (type == GraphicsLayerModifierOperation.SHAPE_CIRCLE) {
                    val radius: Float = min(node.width, node.height) / 2f
                    outline.setRoundRect(Rect(0, 0, node.width, node.height), radius)
                }
            }
            node.setOutline(outline)
        }
        if (hasBlurEffect) {
            val oBlurRadiusX = attributes[GraphicsLayerModifierOperation.BLUR_RADIUS_X]
            var blurRadiusX = 0f
            if (oBlurRadiusX != null) {
                blurRadiusX = oBlurRadiusX as Float
            }
            val oBlurRadiusY = attributes[GraphicsLayerModifierOperation.BLUR_RADIUS_Y]
            var blurRadiusY = 0f
            if (oBlurRadiusY != null) {
                blurRadiusY = oBlurRadiusY as Float
            }
            var blurTileMode = 0
            val oBlurTileMode = attributes[GraphicsLayerModifierOperation.BLUR_TILE_MODE]
            if (oBlurTileMode != null) {
                blurTileMode = oBlurTileMode as Int
            }
            var tileMode = Shader.TileMode.CLAMP
            when (blurTileMode) {
                GraphicsLayerModifierOperation.TILE_MODE_CLAMP ->
                    tileMode = Shader.TileMode.CLAMP
                GraphicsLayerModifierOperation.TILE_MODE_DECAL ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        tileMode = Shader.TileMode.DECAL
                    }
                GraphicsLayerModifierOperation.TILE_MODE_MIRROR ->
                    tileMode = Shader.TileMode.MIRROR
                GraphicsLayerModifierOperation.TILE_MODE_REPEATED ->
                    tileMode = Shader.TileMode.REPEAT
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val effect = RenderEffect.createBlurEffect(blurRadiusX, blurRadiusY, tileMode)
                node.setRenderEffect(effect)
            }
        }
    }

    actual fun endGraphicsLayer(canvas: Canvas) {
        val n = node ?: return
        n.endRecording()
        if (canvas.nativeCanvas.isHardwareAccelerated) {
            canvas.enableZ()
            canvas.nativeCanvas.drawRenderNode(n)
            canvas.disableZ()
        }
        node = null
    }
}

actual class PlatformPaintSupport actual constructor() {
    actual fun createPaintChanges(
        remoteContext: RemoteContext,
        getPaint: () -> Paint,
    ): PaintChanges {
        return AndroidComposePaintChanges(remoteContext, getPaint)
    }

    actual fun resetPaint(paint: Paint) {
        with(paint.asFrameworkPaint()) {
            setTypeface(Typeface.DEFAULT)
            reset()
        }
    }
}

actual fun isPlatformAnimationEnabled(): Boolean {
    // On Android, we return true by default. The actual check against Settings
    // requires a Context, which is passed separately via a11yAnimationEnabled.
    return true
}

actual fun nanoTime(): Long = System.nanoTime()

actual fun logWarning(tag: String, message: String) {
    Log.w(tag, message)
}

// Reference to PaintContext companion constants
private object PaintContext {
    const val TEXT_MEASURE_MONOSPACE_WIDTH: Int = 0x01
    const val TEXT_MEASURE_FONT_HEIGHT: Int = 0x02
    const val TEXT_MEASURE_SPACES: Int = 0x04
}
