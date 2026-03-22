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

import androidx.compose.remote.core.MatrixAccess
import androidx.compose.remote.core.RcPlatformServices
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.operations.ShaderData
import androidx.compose.remote.core.operations.Utils
import androidx.compose.remote.core.operations.layout.managers.CoreText
import androidx.compose.remote.core.operations.paint.PaintBundle
import androidx.compose.remote.core.operations.paint.PaintChanges
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Data
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.Font
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontSlant
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.FontVariation
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.Matrix33
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.PaintStrokeCap
import org.jetbrains.skia.PaintStrokeJoin
import org.jetbrains.skia.Rect
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder
import org.jetbrains.skia.Surface
import org.jetbrains.skia.TextLine
import org.jetbrains.skia.Typeface
import org.jetbrains.skia.paragraph.Alignment
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skia.paragraph.ParagraphBuilder
import org.jetbrains.skia.paragraph.ParagraphStyle
import org.jetbrains.skia.paragraph.TextStyle
import platform.Foundation.NSLog
import kotlin.math.PI
import kotlin.math.atan2

// Shared paint state between IosPaintChanges and PlatformTextSupport
internal var iosTextSize: Float = 14f
internal var iosTypeface: Typeface? = null

/**
 * Wrapper around a Skia Paragraph for complex text layout.
 */
private class SkiaParagraphLayout(
    val paragraph: org.jetbrains.skia.paragraph.Paragraph,
    override val width: Float,
    override val height: Float,
) : RcPlatformServices.ComputedTextLayout {
    override fun isHyphenatedText(): Boolean = false
}

actual class PlatformTextSupport actual constructor() {

    actual fun getTextBounds(
        text: String,
        start: Int,
        end: Int,
        flags: Int,
        bounds: FloatArray,
        paint: Paint,
    ) {
        val font = Font(iosTypeface, iosTextSize)
        val line = TextLine.make(text, font)
        bounds[0] = 0f
        bounds[1] = -line.ascent
        bounds[2] = line.width
        bounds[3] = line.descent
    }

    actual fun drawTextRun(
        text: String,
        x: Float,
        y: Float,
        canvas: Canvas,
        paint: Paint,
    ) {
        val font = Font(iosTypeface, iosTextSize)
        val line = TextLine.make(text, font)
        canvas.nativeCanvas.drawTextLine(line, x, y, paint.toSkiaPaint())
    }

    actual fun drawTextOnPath(
        text: String,
        nativePath: Any,
        hOffset: Float,
        vOffset: Float,
        canvas: Canvas,
        paint: Paint,
    ) {
        val composePath = nativePath as? Path
        if (composePath == null || composePath.isEmpty) {
            drawTextRun(text, hOffset, vOffset, canvas, paint)
            return
        }

        val font = Font(iosTypeface, iosTextSize)
        val measure = PathMeasure()
        measure.setPath(composePath, false)
        val pathLength = measure.length
        if (pathLength == 0f) {
            drawTextRun(text, hOffset, vOffset, canvas, paint)
            return
        }

        val skiaPaint = paint.toSkiaPaint()
        var currentOffset = hOffset

        for (char in text) {
            if (currentOffset >= pathLength) break

            val line = TextLine.make(char.toString(), font)
            val charWidth = line.width

            val position = measure.getPosition(currentOffset)
            val tangent = measure.getTangent(currentOffset)
            val angle = atan2(tangent.y, tangent.x) * 180f / PI.toFloat()

            canvas.nativeCanvas.save()
            canvas.nativeCanvas.translate(position.x, position.y + vOffset)
            canvas.nativeCanvas.rotate(angle)
            canvas.nativeCanvas.drawTextLine(line, 0f, 0f, skiaPaint)
            canvas.nativeCanvas.restore()

            currentOffset += charWidth
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
        val fontCollection = FontCollection()
        fontCollection.setDefaultFontManager(FontMgr.default)

        val paraStyle = ParagraphStyle()
        paraStyle.alignment = when (alignment) {
            CoreText.TEXT_ALIGN_RIGHT, CoreText.TEXT_ALIGN_END -> Alignment.END
            CoreText.TEXT_ALIGN_CENTER -> Alignment.CENTER
            CoreText.TEXT_ALIGN_JUSTIFY -> Alignment.JUSTIFY
            else -> Alignment.START
        }
        paraStyle.maxLinesCount = if (maxLines > 0) maxLines else Int.MAX_VALUE
        if (overflow == CoreText.OVERFLOW_ELLIPSIS) {
            paraStyle.ellipsis = "..."
        }

        val textStyle = TextStyle()
        textStyle.fontSize = iosTextSize
        textStyle.color = paint.color.toArgb()
        if (iosTypeface != null) {
            textStyle.fontFamilies = arrayOf(iosTypeface!!.familyName)
        }

        val actualEnd = if (end == -1 || end > text.length) text.length else end
        val subText = text.substring(start, actualEnd)

        val builder = ParagraphBuilder(paraStyle, fontCollection)
        builder.pushStyle(textStyle)
        builder.addText(subText)
        builder.popStyle()
        val paragraph = builder.build()
        paragraph.layout(maxWidth)

        return SkiaParagraphLayout(
            paragraph,
            paragraph.maxIntrinsicWidth.coerceAtMost(maxWidth),
            paragraph.height,
        )
    }

    actual fun drawComplexText(
        computedTextLayout: RcPlatformServices.ComputedTextLayout,
        canvas: Canvas,
    ) {
        val layout = computedTextLayout as? SkiaParagraphLayout ?: return
        layout.paragraph.paint(canvas.nativeCanvas, 0f, 0f)
    }
}

// Extension to convert CMP Paint to Skia Paint
internal fun Paint.toSkiaPaint(): org.jetbrains.skia.Paint {
    val skiaPaint = org.jetbrains.skia.Paint()
    skiaPaint.color = this.color.toArgb()
    skiaPaint.isAntiAlias = this.isAntiAlias
    when (this.style) {
        PaintingStyle.Fill -> skiaPaint.mode = PaintMode.FILL
        PaintingStyle.Stroke -> {
            skiaPaint.mode = PaintMode.STROKE
            skiaPaint.strokeWidth = this.strokeWidth
            skiaPaint.strokeMiter = this.strokeMiterLimit
            when (this.strokeCap) {
                StrokeCap.Butt -> skiaPaint.strokeCap = PaintStrokeCap.BUTT
                StrokeCap.Round -> skiaPaint.strokeCap = PaintStrokeCap.ROUND
                StrokeCap.Square -> skiaPaint.strokeCap = PaintStrokeCap.SQUARE
                else -> {}
            }
            when (this.strokeJoin) {
                StrokeJoin.Miter -> skiaPaint.strokeJoin = PaintStrokeJoin.MITER
                StrokeJoin.Round -> skiaPaint.strokeJoin = PaintStrokeJoin.ROUND
                StrokeJoin.Bevel -> skiaPaint.strokeJoin = PaintStrokeJoin.BEVEL
                else -> {}
            }
        }
        else -> skiaPaint.mode = PaintMode.FILL
    }
    skiaPaint.alpha = (this.alpha * 255).toInt()
    return skiaPaint
}

actual class PlatformBitmapSupport actual constructor() {

    actual fun drawBitmap(
        imageData: Any,
        srcLeft: Int, srcTop: Int, srcRight: Int, srcBottom: Int,
        dstLeft: Int, dstTop: Int, dstRight: Int, dstBottom: Int,
        canvas: Canvas,
        paint: Paint,
    ) {
        val image = imageData as? Image ?: return
        canvas.nativeCanvas.drawImageRect(
            image,
            Rect.makeLTRB(srcLeft.toFloat(), srcTop.toFloat(), srcRight.toFloat(), srcBottom.toFloat()),
            Rect.makeLTRB(dstLeft.toFloat(), dstTop.toFloat(), dstRight.toFloat(), dstBottom.toFloat()),
            paint.toSkiaPaint(),
        )
    }

    actual fun drawBitmap(
        imageData: Any,
        left: Float, top: Float, right: Float, bottom: Float,
        canvas: Canvas,
        paint: Paint,
    ) {
        val image = imageData as? Image ?: return
        canvas.nativeCanvas.drawImageRect(
            image,
            Rect.makeWH(image.width.toFloat(), image.height.toFloat()),
            Rect.makeLTRB(left, top, right, bottom),
            paint.toSkiaPaint(),
        )
    }

    actual fun getCanvasForBitmap(bitmap: Any): Canvas {
        val image = bitmap as Image
        val skiaBitmap = Bitmap()
        skiaBitmap.allocPixels(ImageInfo.makeN32Premul(image.width, image.height))
        val skiaCanvas = org.jetbrains.skia.Canvas(skiaBitmap)
        skiaCanvas.drawImage(image, 0f, 0f)
        return Canvas(skiaBitmap.asComposeImageBitmap())
    }

    actual fun eraseBitmap(bitmap: Any, color: Int) {
        // For Skia Image-based bitmaps, erasing is a no-op since Image is immutable.
        // The draw-to-bitmap flow uses Surface which handles this via clear().
    }
}

actual class PlatformGraphicsLayerSupport actual constructor() {
    private var offscreenBitmap: Bitmap? = null
    private var layerAttributes: HashMap<Int, Any> = hashMapOf()

    actual fun startGraphicsLayer(w: Int, h: Int): Canvas? {
        val bmp = Bitmap()
        bmp.allocPixels(ImageInfo.makeN32Premul(w.coerceAtLeast(1), h.coerceAtLeast(1)))
        offscreenBitmap = bmp
        return Canvas(bmp.asComposeImageBitmap())
    }

    actual fun setGraphicsLayer(attributes: HashMap<Int, Any>) {
        layerAttributes = attributes
    }

    actual fun endGraphicsLayer(canvas: Canvas) {
        val bmp = offscreenBitmap ?: return
        offscreenBitmap = null
        val image = Image.makeFromBitmap(bmp)
        val attrs = layerAttributes
        val nativeCanvas = canvas.nativeCanvas

        // Extract attributes with defaults matching Android RenderNode behavior
        val scaleX = (attrs[G.SCALE_X] as? Float) ?: 1f
        val scaleY = (attrs[G.SCALE_Y] as? Float) ?: 1f
        val rotationZ = (attrs[G.ROTATION_Z] as? Float) ?: 0f
        val translationX = (attrs[G.TRANSLATION_X] as? Float) ?: 0f
        val translationY = (attrs[G.TRANSLATION_Y] as? Float) ?: 0f
        val alpha = (attrs[G.ALPHA] as? Float) ?: 1f
        val elevation = (attrs[G.SHADOW_ELEVATION] as? Float) ?: 0f
        val originX = (attrs[G.TRANSFORM_ORIGIN_X] as? Float) ?: 0.5f
        val originY = (attrs[G.TRANSFORM_ORIGIN_Y] as? Float) ?: 0.5f
        val hasBlur = (attrs[G.HAS_BLUR] as? Float) ?: 0f
        val blurRadiusX = (attrs[G.BLUR_RADIUS_X] as? Float) ?: 0f
        val blurRadiusY = (attrs[G.BLUR_RADIUS_Y] as? Float) ?: 0f
        val shape = (attrs[G.SHAPE] as? Float)?.toInt() ?: G.SHAPE_RECT
        val shapeRadius = (attrs[G.SHAPE_RADIUS] as? Float) ?: 0f
        val spotShadowColor = (attrs[G.SPOT_SHADOW_COLOR] as? Float)?.toInt() ?: 0x66000000.toInt()
        val ambientShadowColor = (attrs[G.AMBIENT_SHADOW_COLOR] as? Float)?.toInt() ?: 0x44000000.toInt()

        val w = image.width.toFloat()
        val h = image.height.toFloat()
        val pivotX = w * originX
        val pivotY = h * originY

        nativeCanvas.save()

        // 1. Apply transforms (same order as Android RenderNode)
        nativeCanvas.translate(translationX + pivotX, translationY + pivotY)
        if (rotationZ != 0f) nativeCanvas.rotate(rotationZ)
        if (scaleX != 1f || scaleY != 1f) nativeCanvas.scale(scaleX, scaleY)
        nativeCanvas.translate(-pivotX, -pivotY)

        // 2. Draw shadow if elevation > 0
        if (elevation > 0f) {
            val shadowPath = org.jetbrains.skia.Path()
            when (shape) {
                G.SHAPE_ROUND_RECT -> shadowPath.addRRect(
                    org.jetbrains.skia.RRect.makeLTRB(0f, 0f, w, h, shapeRadius)
                )
                G.SHAPE_CIRCLE -> shadowPath.addOval(Rect.makeWH(w, h))
                else -> shadowPath.addRect(Rect.makeWH(w, h))
            }
            org.jetbrains.skia.ShadowUtils.drawShadow(
                nativeCanvas, shadowPath,
                org.jetbrains.skia.Point3(0f, 0f, elevation),
                org.jetbrains.skia.Point3(0f, -300f, 600f),
                600f,
                ambientShadowColor,
                spotShadowColor,
                alpha < 1f,
                false,
            )
        }

        // 3. Apply clipping based on shape
        if (shape == G.SHAPE_ROUND_RECT && shapeRadius > 0f) {
            nativeCanvas.clipRRect(org.jetbrains.skia.RRect.makeLTRB(0f, 0f, w, h, shapeRadius))
        } else if (shape == G.SHAPE_CIRCLE) {
            val clipPath = org.jetbrains.skia.Path().addOval(Rect.makeWH(w, h))
            nativeCanvas.clipPath(clipPath)
        }

        // 4. Draw the layer image with blur and/or alpha
        val paint = org.jetbrains.skia.Paint()
        if (alpha < 1f) paint.alpha = (alpha * 255).toInt()
        if (hasBlur > 0f && (blurRadiusX > 0f || blurRadiusY > 0f)) {
            paint.imageFilter = org.jetbrains.skia.ImageFilter.makeBlur(
                blurRadiusX, blurRadiusY, org.jetbrains.skia.FilterTileMode.CLAMP
            )
        }
        nativeCanvas.drawImage(image, 0f, 0f, paint)

        nativeCanvas.restore()
    }
}

// Alias for GraphicsLayerModifierOperation constants
private typealias G = androidx.compose.remote.core.operations.layout.modifiers.GraphicsLayerModifierOperation

actual class PlatformPaintSupport actual constructor() {

    actual fun createPaintChanges(
        remoteContext: RemoteContext,
        getPaint: () -> Paint,
    ): PaintChanges {
        return IosPaintChanges(remoteContext, getPaint)
    }

    actual fun resetPaint(paint: Paint) {
        val defaultPaint = Paint()
        paint.alpha = defaultPaint.alpha
        paint.isAntiAlias = defaultPaint.isAntiAlias
        paint.color = defaultPaint.color
        paint.blendMode = defaultPaint.blendMode
        paint.style = defaultPaint.style
        paint.strokeWidth = defaultPaint.strokeWidth
        paint.strokeCap = defaultPaint.strokeCap
        paint.strokeJoin = defaultPaint.strokeJoin
        paint.strokeMiterLimit = defaultPaint.strokeMiterLimit
        paint.filterQuality = defaultPaint.filterQuality
        paint.shader = null
        paint.colorFilter = null
        paint.pathEffect = null
        iosTextSize = 14f
        iosTypeface = null
    }
}

actual fun isPlatformAnimationEnabled(): Boolean = true

@OptIn(kotlin.experimental.ExperimentalNativeApi::class)
actual fun nanoTime(): Long {
    val uptime: Double = platform.Foundation.NSProcessInfo.processInfo.systemUptime
    return (uptime * 1_000_000_000.0).toLong()
}

actual fun logWarning(tag: String, message: String) {
    NSLog("[$tag] $message")
}

/**
 * iOS PaintChanges implementation using Skia APIs.
 */
private class IosPaintChanges(
    private val remoteContext: RemoteContext,
    private val getPaint: () -> Paint,
) : PaintChanges {

    // Track current Skia shader for matrix application
    private var currentSkiaShader: org.jetbrains.skia.Shader? = null
    // Cache for texture shader recreation with matrix
    private var cachedTextureImage: Image? = null
    private var cachedTextureTileModeX: FilterTileMode? = null
    private var cachedTextureTileModeY: FilterTileMode? = null
    private var cachedShaderMatrix: Matrix33? = null

    override fun setTextSize(size: Float) {
        iosTextSize = size
    }

    override fun setTypeFace(fontType: Int, weight: Int, italic: Boolean) {
        val slant = if (italic) FontSlant.ITALIC else FontSlant.UPRIGHT
        val fontStyle = FontStyle(weight, 5, slant)
        val fontMgr = FontMgr.default

        when (fontType) {
            PaintBundle.FONT_TYPE_DEFAULT, PaintBundle.FONT_TYPE_SANS_SERIF -> {
                val styleSet = fontMgr.matchFamily("Helvetica")
                iosTypeface = if (styleSet.count() > 0) {
                    styleSet.matchStyle(fontStyle)
                } else {
                    fontMgr.matchFamilyStyle(null, fontStyle)
                }
            }
            PaintBundle.FONT_TYPE_SERIF -> {
                val styleSet = fontMgr.matchFamily("Georgia")
                iosTypeface = if (styleSet.count() > 0) {
                    styleSet.matchStyle(fontStyle)
                } else {
                    fontMgr.matchFamilyStyle(null, fontStyle)
                }
            }
            PaintBundle.FONT_TYPE_MONOSPACE -> {
                val styleSet = fontMgr.matchFamily("Courier")
                iosTypeface = if (styleSet.count() > 0) {
                    styleSet.matchStyle(fontStyle)
                } else {
                    fontMgr.matchFamilyStyle(null, fontStyle)
                }
            }
            else -> {
                // Custom font from FontInfo data
                val fi = remoteContext.getObject(fontType) as? RemoteContext.FontInfo
                if (fi != null) {
                    iosTypeface = fontMgr.makeFromData(Data.makeFromBytes(fi.mFontData))
                } else {
                    iosTypeface = fontMgr.matchFamilyStyle(null, fontStyle)
                }
            }
        }
    }

    override fun setTypeFace(fontType: String, weight: Int, italic: Boolean) {
        val slant = if (italic) FontSlant.ITALIC else FontSlant.UPRIGHT
        val fontStyle = FontStyle(weight, 5, slant)
        val fontMgr = FontMgr.default
        val styleSet = fontMgr.matchFamily(fontType)
        iosTypeface = if (styleSet.count() > 0) {
            styleSet.matchStyle(fontStyle)
        } else {
            fontMgr.matchFamilyStyle(null, fontStyle)
        }
    }

    override fun setShaderMatrix(matrixId: Float) {
        val id = Utils.idFromNan(matrixId)
        if (id == 0) {
            return
        }
        val matAccess = remoteContext.getObject(id) as? MatrixAccess ?: return
        val m3x3 = MatrixAccess.to3x3(matAccess.get()) ?: return
        // Store the matrix for use when recreating texture shaders
        cachedShaderMatrix = Matrix33(
            m3x3[0], m3x3[1], m3x3[2],
            m3x3[3], m3x3[4], m3x3[5],
            m3x3[6], m3x3[7], m3x3[8],
        )
        // If we have a cached texture image, recreate the shader with the new matrix
        val image = cachedTextureImage
        if (image != null) {
            val skiaShader = image.makeShader(
                cachedTextureTileModeX ?: FilterTileMode.CLAMP,
                cachedTextureTileModeY ?: FilterTileMode.CLAMP,
                localMatrix = cachedShaderMatrix,
            )
            currentSkiaShader = skiaShader
        }
    }

    override fun setFontVariationAxes(tags: Array<String>, values: FloatArray) {
        val currentTypeface = iosTypeface ?: return
        val variations = Array(tags.size) { i ->
            FontVariation(tags[i], values[i])
        }
        iosTypeface = currentTypeface.makeClone(variations, 0)
    }

    override fun setTextureShader(
        bitmapId: Int, tileX: Short, tileY: Short, filterMode: Short, maxAnisotropy: Short,
    ) {
        val imageData = remoteContext.mRemoteComposeState.getFromId(bitmapId) ?: return
        val image = imageData as? Image ?: return
        val tmX = toSkiaTileMode(tileX.toInt())
        val tmY = toSkiaTileMode(tileY.toInt())
        cachedTextureImage = image
        cachedTextureTileModeX = tmX
        cachedTextureTileModeY = tmY
        val skiaShader = image.makeShader(tmX, tmY, localMatrix = cachedShaderMatrix)
        currentSkiaShader = skiaShader

        val composeTileModeX = toComposeTileMode(tileX.toInt())
        val composeTileModeY = toComposeTileMode(tileY.toInt())
        val skiaBitmap = bitmapFromImage(image)
        getPaint().shader = androidx.compose.ui.graphics.ImageShader(
            skiaBitmap.asComposeImageBitmap(),
            composeTileModeX,
            composeTileModeY,
        )
    }

    override fun setPathEffect(pathEffect: FloatArray?) {
        if (pathEffect == null || pathEffect.isEmpty()) {
            getPaint().pathEffect = null
            return
        }
        getPaint().pathEffect = androidx.compose.ui.graphics.PathEffect.Companion.dashPathEffect(pathEffect, 0f)
    }

    override fun setStrokeWidth(width: Float) {
        getPaint().strokeWidth = width
    }

    override fun setColor(color: Int) {
        getPaint().color = Color(color)
    }

    override fun setStrokeCap(cap: Int) {
        val caps = arrayOf(StrokeCap.Butt, StrokeCap.Round, StrokeCap.Square)
        if (cap in caps.indices) getPaint().strokeCap = caps[cap]
    }

    override fun setStyle(style: Int) {
        val styles = arrayOf(PaintingStyle.Fill, PaintingStyle.Stroke, PaintingStyle.Fill)
        if (style in styles.indices) getPaint().style = styles[style]
    }

    override fun setShader(shaderId: Int) {
        if (shaderId == 0) {
            getPaint().shader = null
            currentSkiaShader = null
            cachedTextureImage = null
            cachedTextureTileModeX = null
            cachedTextureTileModeY = null
            cachedShaderMatrix = null
            return
        }
        val data: ShaderData? = remoteContext.mRemoteComposeState.getFromId(shaderId) as? ShaderData
        if (data == null) return

        val sksl = remoteContext.getText(data.getShaderTextId()) ?: return
        val effect = RuntimeEffect.makeForShader(sksl)

        val builder = RuntimeShaderBuilder(effect)

        // Set float uniforms
        val floatNames = data.getUniformFloatNames()
        for (name in floatNames) {
            val values = data.getUniformFloats(name)
            builder.uniform(name, values)
        }

        // Set int uniforms - expand to individual calls since RuntimeShaderBuilder
        // doesn't have a uniform(String, IntArray) overload
        val intNames = data.getUniformIntegerNames()
        for (name in intNames) {
            val values = data.getUniformInts(name)
            when (values.size) {
                1 -> builder.uniform(name, values[0])
                2 -> builder.uniform(name, values[0], values[1])
                3 -> builder.uniform(name, values[0], values[1], values[2])
                4 -> builder.uniform(name, values[0], values[1], values[2], values[3])
            }
        }

        // Set bitmap uniforms as child shaders
        val bitmapNames = data.getUniformBitmapNames()
        for (name in bitmapNames) {
            val bitmapId = data.getUniformBitmapId(name)
            val bitmapImage = remoteContext.mRemoteComposeState.getFromId(bitmapId) as? Image
            if (bitmapImage != null) {
                val bitmapShader = bitmapImage.makeShader()
                builder.child(name, bitmapShader)
            }
        }

        val skiaShader = builder.makeShader()
        currentSkiaShader = skiaShader
        // Runtime shaders are not directly settable via CMP Paint.shader.
        // They remain tracked for shader matrix operations.
        // On Skiko/iOS, runtime shader support requires platform-level integration.
        getPaint().shader = null
    }

    override fun setImageFilterQuality(quality: Int) {
        // Filter quality is handled by Skia automatically
    }

    override fun setBlendMode(mode: Int) {
        val blendMode = androidx.compose.remote.player.compose.utils.remoteToBlendMode(mode)
        if (blendMode != null) getPaint().blendMode = blendMode
    }

    override fun setAlpha(a: Float) {
        getPaint().alpha = a
    }

    override fun setStrokeMiter(miter: Float) {
        getPaint().strokeMiterLimit = miter
    }

    override fun setStrokeJoin(join: Int) {
        val joins = arrayOf(StrokeJoin.Miter, StrokeJoin.Round, StrokeJoin.Bevel)
        if (join in joins.indices) getPaint().strokeJoin = joins[join]
    }

    override fun setFilterBitmap(filter: Boolean) {
        // Handled by Skia
    }

    override fun setAntiAlias(aa: Boolean) {
        getPaint().isAntiAlias = aa
    }

    override fun clear(mask: Long) {
        if ((mask and (1L shl PaintBundle.COLOR_FILTER)) != 0L) {
            getPaint().colorFilter = null
        }
    }

    override fun setLinearGradient(
        colors: IntArray, stops: FloatArray?,
        startX: Float, startY: Float, endX: Float, endY: Float,
        tileMode: Int,
    ) {
        val composeColors = colors.map { Color(it) }
        val offset = stops?.toList() ?: List(colors.size) { it.toFloat() / (colors.size - 1).coerceAtLeast(1) }
        getPaint().shader = androidx.compose.ui.graphics.LinearGradientShader(
            from = Offset(startX, startY),
            to = Offset(endX, endY),
            colors = composeColors,
            colorStops = offset,
            tileMode = toComposeTileMode(tileMode),
        )
    }

    override fun setRadialGradient(
        colors: IntArray, stops: FloatArray?,
        centerX: Float, centerY: Float, radius: Float,
        tileMode: Int,
    ) {
        val composeColors = colors.map { Color(it) }
        val offset = stops?.toList() ?: List(colors.size) { it.toFloat() / (colors.size - 1).coerceAtLeast(1) }
        getPaint().shader = androidx.compose.ui.graphics.RadialGradientShader(
            center = Offset(centerX, centerY),
            radius = radius,
            colors = composeColors,
            colorStops = offset,
            tileMode = toComposeTileMode(tileMode),
        )
    }

    override fun setSweepGradient(
        colors: IntArray, stops: FloatArray?,
        centerX: Float, centerY: Float,
    ) {
        val composeColors = colors.map { Color(it) }
        val offset = stops?.toList() ?: List(colors.size) { it.toFloat() / (colors.size - 1).coerceAtLeast(1) }
        getPaint().shader = androidx.compose.ui.graphics.SweepGradientShader(
            center = Offset(centerX, centerY),
            colors = composeColors,
            colorStops = offset,
        )
    }

    override fun setColorFilter(color: Int, mode: Int) {
        getPaint().colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
            Color(color),
            androidx.compose.remote.player.compose.utils.remoteToBlendMode(mode) ?: androidx.compose.ui.graphics.BlendMode.SrcIn
        )
    }

    private fun toSkiaTileMode(mode: Int): FilterTileMode {
        return when (mode) {
            0 -> FilterTileMode.CLAMP
            1 -> FilterTileMode.REPEAT
            2 -> FilterTileMode.MIRROR
            else -> FilterTileMode.CLAMP
        }
    }

    private fun toComposeTileMode(mode: Int): androidx.compose.ui.graphics.TileMode {
        return when (mode) {
            0 -> androidx.compose.ui.graphics.TileMode.Clamp
            1 -> androidx.compose.ui.graphics.TileMode.Repeated
            2 -> androidx.compose.ui.graphics.TileMode.Mirror
            else -> androidx.compose.ui.graphics.TileMode.Clamp
        }
    }

    private fun bitmapFromImage(image: Image): Bitmap {
        val bitmap = Bitmap()
        bitmap.allocPixels(ImageInfo.makeN32Premul(image.width, image.height))
        val canvas = org.jetbrains.skia.Canvas(bitmap)
        canvas.drawImage(image, 0f, 0f)
        return bitmap
    }
}
