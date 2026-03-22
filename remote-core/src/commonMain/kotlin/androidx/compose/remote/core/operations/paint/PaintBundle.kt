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

import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.VariableSupport
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.Utils
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.MapSerializer.Companion.orderedOf
import androidx.compose.remote.core.serialize.Serializable

/** Paint Bundle represents a delta of changes to a paint object */
class PaintBundle : Serializable {

    var mArray: IntArray = IntArray(200)
    var mOutArray: IntArray? = null
    var mPos: Int = 0

    /** Apply changes to a PaintChanges interface */
    fun applyPaintChange(paintContext: PaintContext, p: PaintChanges) {
        var i = 0
        var mask = 0
        if (mOutArray == null) {
            mOutArray = mArray
        }
        val outArray = mOutArray!!
        while (i < mPos) {
            val cmd = outArray[i++]
            mask = mask or (1 shl (cmd - 1))
            when (cmd and 0xFFFF) {
                TEXT_SIZE -> p.setTextSize(Float.fromBits(outArray[i++]))
                TYPEFACE -> {
                    val style = cmd shr 16
                    val weight = style and 0x3ff
                    val italic = (style shr 10) > 0
                    val fontData = (style and 1024) > 0
                    val fontType = outArray[i++]
                    if (fontType > 10 && !fontData) {
                        val fontString = paintContext.getText(fontType) ?: ""
                        p.setTypeFace(fontString, weight, italic)
                    } else {
                        p.setTypeFace(fontType, weight, italic)
                    }
                }
                COLOR_ID, COLOR -> p.setColor(outArray[i++])
                STROKE_WIDTH -> p.setStrokeWidth(Float.fromBits(outArray[i++]))
                STROKE_MITER -> p.setStrokeMiter(Float.fromBits(outArray[i++]))
                STROKE_CAP -> p.setStrokeCap(cmd shr 16)
                STYLE -> p.setStyle(cmd shr 16)
                SHADER -> p.setShader(outArray[i++])
                STROKE_JOIN -> p.setStrokeJoin(cmd shr 16)
                IMAGE_FILTER_QUALITY -> p.setImageFilterQuality(cmd shr 16)
                BLEND_MODE -> p.setBlendMode(cmd shr 16)
                FILTER_BITMAP -> p.setFilterBitmap((cmd shr 16) != 0)
                GRADIENT -> i = callSetGradient(cmd, outArray, i, p)
                COLOR_FILTER_ID, COLOR_FILTER -> p.setColorFilter(outArray[i++], cmd shr 16)
                ALPHA -> p.setAlpha(Float.fromBits(outArray[i++]))
                CLEAR_COLOR_FILTER -> p.clear(0x1L shl COLOR_FILTER)
                SHADER_MATRIX -> p.setShaderMatrix(Float.fromBits(outArray[i++]))
                FONT_AXIS -> {
                    val count = cmd shr 16
                    val tags = Array(count) { "" }
                    val values = FloatArray(count)
                    for (j in 0 until count) {
                        tags[j] = paintContext.getText(outArray[i++]) ?: ""
                        values[j] = Float.fromBits(outArray[i++])
                    }
                    p.setFontVariationAxes(tags, values)
                }
                TEXTURE -> {
                    val bitmapId = mArray[i++]
                    val tileModes = mArray[i++]
                    val tileX = (tileModes and 0xF).toShort()
                    val tileY = (tileModes shr 16).toShort()
                    val filter = mArray[i++]
                    val filterMode = (filter and 0xF).toShort()
                    val maxAnisotropy = (filter shr 16).toShort()
                    p.setTextureShader(bitmapId, tileX, tileY, filterMode, maxAnisotropy)
                }
                PATH_EFFECT -> {
                    val pathEffectCount = cmd shr 16
                    var pathEffect: FloatArray? = null
                    if (pathEffectCount > 0) {
                        pathEffect = FloatArray(pathEffectCount)
                        for (j in 0 until pathEffectCount) {
                            pathEffect[j] = Float.fromBits(outArray[i++])
                        }
                    }
                    p.setPathEffect(pathEffect)
                }
                else -> println("error unknown Paint Type ${cmd and 0xFFFF}")
            }
        }
    }

    override fun toString(): String {
        val ret = StringBuilder("\n")
        var i = 0
        while (i < mPos) {
            val cmd = mArray[i++]
            val type = cmd and 0xFFFF
            when (type) {
                TEXT_SIZE -> ret.append("    TextSize(${asFloatStr(mArray[i++])}")
                TYPEFACE -> {
                    val style = cmd shr 16
                    val weight = style and 0x3ff
                    val italic = (style shr 10) > 0
                    val fontType = mArray[i++]
                    ret.append("    TypeFace($fontType, $weight, $italic")
                }
                COLOR -> ret.append("    Color(${colorInt(mArray[i++])}")
                COLOR_ID -> ret.append("    ColorId([${mArray[i++]}]")
                STROKE_WIDTH -> ret.append("    StrokeWidth(${asFloatStr(mArray[i++])}")
                STROKE_MITER -> ret.append("    StrokeMiter(${asFloatStr(mArray[i++])}")
                STROKE_CAP -> ret.append("    StrokeCap(${cmd shr 16}")
                STYLE -> ret.append("    Style(${cmd shr 16}")
                COLOR_FILTER -> {
                    ret.append(
                        "    ColorFilter(color=${colorInt(mArray[i++])}, mode=${blendModeString(cmd shr 16)}"
                    )
                }
                COLOR_FILTER_ID -> {
                    ret.append(
                        "    ColorFilterID(color=[${mArray[i++]}], mode=${blendModeString(cmd shr 16)}"
                    )
                }
                CLEAR_COLOR_FILTER -> ret.append("    clearColorFilter")
                SHADER -> ret.append("    Shader(${mArray[i++]}")
                ALPHA -> ret.append("    Alpha(${asFloatStr(mArray[i++])}")
                IMAGE_FILTER_QUALITY -> ret.append("    ImageFilterQuality(${cmd shr 16}")
                BLEND_MODE -> ret.append("    BlendMode(${blendModeString(cmd shr 16)}")
                FILTER_BITMAP -> ret.append("    FilterBitmap(${(cmd shr 16) != 0}")
                STROKE_JOIN -> ret.append("    StrokeJoin(${cmd shr 16}")
                ANTI_ALIAS -> ret.append("    AntiAlias(${cmd shr 16}")
                GRADIENT -> i = callPrintGradient(cmd, mArray, i, ret)
                FONT_AXIS -> {
                    ret.append("    FontAxis(")
                    val count = cmd shr 16
                    for (j in 0 until count) {
                        ret.append("[${mArray[i++]}]")
                        i++
                    }
                }
                TEXTURE -> {
                    ret.append("    texture( ")
                    val bitmapId = mArray[i++]
                    val tileModes = mArray[i++]
                    val tileX = (tileModes and 0xF).toShort()
                    val tileY = (tileModes shr 16).toShort()
                    val filter = mArray[i++]
                    val filterMode = (filter and 0xF).toShort()
                    val maxAnisotropy = (filter shr 16).toShort()
                    ret.append("[$bitmapId] $tileX, $tileY, $filterMode, $maxAnisotropy")
                }
                SHADER_MATRIX -> ret.append("    ShaderMatrix(${asFloatStr(mArray[i++])}")
            }
            ret.append("),\n")
        }
        return ret.toString()
    }

    private fun registerFloat(iv: Int, context: RemoteContext, support: VariableSupport) {
        val v = Float.fromBits(iv)
        if (v.isNaN()) {
            context.listensTo(Utils.idFromNan(v), support)
        }
    }

    internal fun callRegisterGradient(
        cmd: Int, array: IntArray, i: Int, context: RemoteContext, support: VariableSupport
    ): Int {
        var ret = i
        val type = cmd shr 16
        val control = array[ret++]
        var len = 0xFF and control
        val register = 0xFFFF and (control shr 16)
        when (type) {
            LINEAR_GRADIENT -> {
                if (len > 0) {
                    for (j in 0 until len) {
                        val color = array[ret++]
                        if ((register and (1 shl j)) != 0) {
                            context.listensTo(color, support)
                        }
                    }
                }
                len = array[ret++]
                if (len > 0) {
                    for (j in 0 until len) {
                        registerFloat(array[ret++], context, support)
                    }
                }
                registerFloat(array[ret++], context, support) // start x
                registerFloat(array[ret++], context, support) // start y
                registerFloat(array[ret++], context, support) // end x
                registerFloat(array[ret++], context, support) // end y
                ret++ // tileMode
            }
            RADIAL_GRADIENT -> {
                if (len > 0) {
                    for (j in 0 until len) {
                        val color = array[ret++]
                        if ((register and (1 shl j)) != 0) {
                            context.listensTo(color, support)
                        }
                    }
                }
                len = array[ret++] // stops
                for (j in 0 until len) {
                    registerFloat(array[ret++], context, support)
                }
                registerFloat(array[ret++], context, support) // center x
                registerFloat(array[ret++], context, support) // center y
                registerFloat(array[ret++], context, support) // radius
                ret++ // tileMode
            }
            SWEEP_GRADIENT -> {
                if (len > 0) {
                    for (j in 0 until len) {
                        val color = array[ret++]
                        if ((register and (1 shl j)) != 0) {
                            context.listensTo(color, support)
                        }
                    }
                }
                len = array[ret++] // stops
                for (j in 0 until len) {
                    registerFloat(array[ret++], context, support)
                }
                registerFloat(array[ret++], context, support) // center x
                registerFloat(array[ret++], context, support) // center y
            }
            else -> println("error $type")
        }
        return ret
    }

    internal fun callPrintGradient(cmd: Int, array: IntArray, i: Int, p: StringBuilder): Int {
        var ret = i
        val type = cmd shr 16
        var len = array[ret++]
        var colors: IntArray? = null
        var stops: Array<String>? = null
        when (type) {
            0 -> {
                p.append("    LinearGradient(\n")
                if (len > 0) {
                    colors = IntArray(len)
                    for (j in colors.indices) {
                        colors[j] = array[ret++]
                    }
                }
                len = array[ret++]
                if (len > 0) {
                    stops = Array(len) { "" }
                    for (j in stops.indices) {
                        stops[j] = asFloatStr(array[ret++])
                    }
                }
                p.append("      colors = ${colorInt(colors!!)},\n")
                p.append("      stops = ${stops?.contentToString()},\n")
                p.append("      start = [${asFloatStr(array[ret++])}")
                p.append(", ${asFloatStr(array[ret++])}],\n")
                p.append("      end = [${asFloatStr(array[ret++])}")
                p.append(", ${asFloatStr(array[ret++])}],\n")
                val tileMode = array[ret++]
                p.append("      tileMode = $tileMode\n    ")
            }
            1 -> {
                p.append("    RadialGradient(\n")
                if (len > 0) {
                    colors = IntArray(len)
                    for (j in colors.indices) {
                        colors[j] = array[ret++]
                    }
                }
                len = array[ret++]
                if (len > 0) {
                    stops = Array(len) { "" }
                    for (j in stops.indices) {
                        stops[j] = asFloatStr(array[ret++])
                    }
                }
                p.append("      colors = ${colorInt(colors!!)},\n")
                p.append("      stops = ${stops?.contentToString()},\n")
                p.append("      center = [${asFloatStr(array[ret++])}")
                p.append(", ${asFloatStr(array[ret++])}],\n")
                p.append("      radius = ${asFloatStr(array[ret++])},\n")
                val tileMode = array[ret++]
                p.append("      tileMode = $tileMode\n    ")
            }
            2 -> {
                p.append("    SweepGradient(\n")
                if (len > 0) {
                    colors = IntArray(len)
                    for (j in colors.indices) {
                        colors[j] = array[ret++]
                    }
                }
                len = array[ret++]
                if (len > 0) {
                    stops = Array(len) { "" }
                    for (j in stops.indices) {
                        stops[j] = asFloatStr(array[ret++])
                    }
                }
                p.append("      colors = ${colorInt(colors!!)},\n")
                p.append("      stops = ${stops?.contentToString()},\n")
                p.append("      center = [${asFloatStr(array[ret++])}")
                p.append(", ${asFloatStr(array[ret++])}],\n    ")
            }
            else -> p.append("GRADIENT_??????!!!!")
        }
        return ret
    }

    internal fun callSetGradient(cmd: Int, array: IntArray, i: Int, p: PaintChanges): Int {
        var ret = i
        val gradientType = cmd shr 16

        var len = 0xFF and array[ret++] // maximum 256 colors

        var colors: IntArray? = null
        if (len > 0) {
            colors = IntArray(len)
            for (j in colors.indices) {
                colors[j] = array[ret++]
            }
        }
        len = array[ret++]
        var stops: FloatArray? = null
        if (len > 0 && colors != null) {
            stops = FloatArray(len)
            for (j in colors.indices) {
                stops[j] = Float.fromBits(array[ret++])
            }
        }

        if (colors == null) return ret

        when (gradientType) {
            LINEAR_GRADIENT -> {
                val startX = Float.fromBits(array[ret++])
                val startY = Float.fromBits(array[ret++])
                val endX = Float.fromBits(array[ret++])
                val endY = Float.fromBits(array[ret++])
                val tileMode = array[ret++]
                p.setLinearGradient(colors, stops, startX, startY, endX, endY, tileMode)
            }
            RADIAL_GRADIENT -> {
                val centerX = Float.fromBits(array[ret++])
                val centerY = Float.fromBits(array[ret++])
                val radius = Float.fromBits(array[ret++])
                val tileMode = array[ret++]
                p.setRadialGradient(colors, stops, centerX, centerY, radius, tileMode)
            }
            SWEEP_GRADIENT -> {
                val centerX = Float.fromBits(array[ret++])
                val centerY = Float.fromBits(array[ret++])
                p.setSweepGradient(colors, stops, centerX, centerY)
            }
        }
        return ret
    }

    /** Write a bundle of paint changes to the buffer */
    fun writeBundle(buffer: WireBuffer) {
        buffer.writeInt(mPos)
        for (index in 0 until mPos) {
            buffer.writeInt(mArray[index])
        }
    }

    /** This will read the paint bundle off the wire buffer */
    fun readBundle(buffer: WireBuffer) {
        val len = buffer.readInt()
        if (len <= 0 || len > 1024) {
            throw RuntimeException("buffer corrupt paint len = $len")
        }
        mArray = IntArray(len)
        for (i in mArray.indices) {
            mArray[i] = buffer.readInt()
        }
        mPos = len
    }

    /**
     * sets a shader that draws a linear gradient along a line.
     */
    fun setLinearGradient(
        colors: IntArray,
        idMask: Int,
        stops: FloatArray?,
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        tileMode: Int
    ) {
        var len: Int
        mArray[mPos++] = GRADIENT or (LINEAR_GRADIENT shl 16)
        len = colors.size
        mArray[mPos++] = (idMask shl 16) or len
        for (i in 0 until len) {
            mArray[mPos++] = colors[i]
        }
        len = stops?.size ?: 0
        mArray[mPos++] = len
        for (i in 0 until len) {
            mArray[mPos++] = stops!![i].toRawBits()
        }
        mArray[mPos++] = startX.toRawBits()
        mArray[mPos++] = startY.toRawBits()
        mArray[mPos++] = endX.toRawBits()
        mArray[mPos++] = endY.toRawBits()
        mArray[mPos++] = tileMode
    }

    /** Set a shader that draws a sweep gradient around a center point. */
    fun setSweepGradient(
        colors: IntArray,
        idMask: Int,
        stops: FloatArray?,
        centerX: Float,
        centerY: Float
    ) {
        var len: Int
        mArray[mPos++] = GRADIENT or (SWEEP_GRADIENT shl 16)
        len = colors.size
        mArray[mPos++] = (idMask shl 16) or len
        for (i in 0 until len) {
            mArray[mPos++] = colors[i]
        }
        len = stops?.size ?: 0
        mArray[mPos++] = len
        for (i in 0 until len) {
            mArray[mPos++] = stops!![i].toRawBits()
        }
        mArray[mPos++] = centerX.toRawBits()
        mArray[mPos++] = centerY.toRawBits()
    }

    /** Sets a shader that draws a radial gradient given the center and radius. */
    fun setRadialGradient(
        colors: IntArray,
        idMask: Int,
        stops: FloatArray?,
        centerX: Float,
        centerY: Float,
        radius: Float,
        tileMode: Int
    ) {
        var len: Int
        mArray[mPos++] = GRADIENT or (RADIAL_GRADIENT shl 16)
        len = colors.size
        mArray[mPos++] = (idMask shl 16) or len
        for (i in 0 until len) {
            mArray[mPos++] = colors[i]
        }
        len = stops?.size ?: 0
        mArray[mPos++] = len
        for (i in 0 until len) {
            mArray[mPos++] = stops!![i].toRawBits()
        }
        mArray[mPos++] = centerX.toRawBits()
        mArray[mPos++] = centerY.toRawBits()
        mArray[mPos++] = radius.toRawBits()
        mArray[mPos++] = tileMode
    }

    /** Create a color filter that uses the specified color and Porter-Duff mode. */
    fun setColorFilter(color: Int, mode: Int) {
        mArray[mPos++] = COLOR_FILTER or (mode shl 16)
        mArray[mPos++] = color
    }

    /** Create a color filter that uses the specified color id and Porter-Duff mode. */
    fun setColorFilterId(color: Int, mode: Int) {
        mArray[mPos++] = COLOR_FILTER_ID or (mode shl 16)
        mArray[mPos++] = color
        mColorFilterSet = true
    }

    /** This sets the color filter to null */
    fun clearColorFilter() {
        mArray[mPos++] = CLEAR_COLOR_FILTER
        mColorFilterSet = false
    }

    /** Set the paint's text size. This value must be > 0 */
    fun setTextSize(size: Float) {
        mArray[mPos++] = TEXT_SIZE
        mArray[mPos++] = size.toRawBits()
    }

    /**
     * Set the paint's font 0,1,2 are built else ttf or string based system fonts
     */
    fun setTextStyle(fontType: Int, weight: Int, italic: Boolean, ttf: Boolean) {
        val style = (weight and 0x3FF) or (if (italic) 2048 else 0) or (if (ttf) 1024 else 0)
        mArray[mPos++] = TYPEFACE or (style shl 16)
        mArray[mPos++] = fontType
    }

    fun setTextStyle(fontType: Int, weight: Int, italic: Boolean) {
        val style = (weight and 0x3FF) or (if (italic) 2048 else 0)
        mArray[mPos++] = TYPEFACE or (style shl 16)
        mArray[mPos++] = fontType
    }

    /** Set the TextAxis for the text */
    fun setTextAxis(tags: IntArray, values: FloatArray) {
        if (tags.size != values.size) {
            throw RuntimeException(" tags.length ${tags.size} not value.length${values.size}")
        }
        if (values.size > 8) {
            throw RuntimeException(" too many values ${values.size}")
        }
        mArray[mPos++] = FONT_AXIS or (values.size shl 16)
        for (i in tags.indices) {
            mArray[mPos++] = tags[i]
            mArray[mPos++] = values[i].toRawBits()
        }
    }

    /** Set the width for stroking. */
    fun setStrokeWidth(width: Float) {
        mArray[mPos++] = STROKE_WIDTH
        mArray[mPos++] = width.toRawBits()
    }

    /** Set the Color based on Color */
    fun setColor(color: Int) {
        mArray[mPos++] = COLOR
        mArray[mPos++] = color
    }

    /** Set the color based on the R,G,B,A values (0-255) */
    fun setColor(r: Int, g: Int, b: Int, a: Int) {
        setColor((a shl 24) or (r shl 16) or (g shl 8) or b)
    }

    /** Set the color based the R,G,B,A values (0.0 to 1.0) */
    fun setColor(r: Float, g: Float, b: Float, a: Float) {
        setColor(Utils.toARGB(a, r, g, b))
    }

    /** Set the Color based on ID */
    fun setColorId(color: Int) {
        mArray[mPos++] = COLOR_ID
        mArray[mPos++] = color
    }

    /** Set the paint's Cap. */
    fun setStrokeCap(cap: Int) {
        mArray[mPos++] = STROKE_CAP or (cap shl 16)
    }

    /** Set the style STROKE and/or FILL */
    fun setStyle(style: Int) {
        mArray[mPos++] = STYLE or (style shl 16)
    }

    /** Set the shader id to use */
    fun setShader(shaderId: Int) {
        mLastShaderSet = shaderId
        mArray[mPos++] = SHADER
        mArray[mPos++] = shaderId
    }

    /** Set the Alpha value */
    fun setAlpha(alpha: Float) {
        mArray[mPos++] = ALPHA
        mArray[mPos++] = alpha.toRawBits()
    }

    /** Set the shader matrix */
    fun setShaderMatrix(matrixId: Float) {
        mArray[mPos++] = SHADER_MATRIX
        mArray[mPos++] = matrixId.toRawBits()
    }

    /** Set the paint's stroke miter value. */
    fun setStrokeMiter(miter: Float) {
        mArray[mPos++] = STROKE_MITER
        mArray[mPos++] = miter.toRawBits()
    }

    /** Set the paint's Join. */
    fun setStrokeJoin(join: Int) {
        mArray[mPos++] = STROKE_JOIN or (join shl 16)
    }

    /** set Filter Bitmap */
    fun setFilterBitmap(filter: Boolean) {
        mArray[mPos++] = FILTER_BITMAP or (if (filter) (1 shl 16) else 0)
    }

    /** Set or clear the blend mode. */
    fun setBlendMode(blendmode: Int) {
        mArray[mPos++] = BLEND_MODE or (blendmode shl 16)
    }

    /** Set AntiAlias. */
    fun setAntiAlias(aa: Boolean) {
        mArray[mPos++] = ANTI_ALIAS or ((if (aa) 1 else 0) shl 16)
    }

    /** Set the texture shader */
    fun setTextureShader(
        texture: Int, tileModeX: Short, tileModeY: Short, filterMode: Short, maxAnisotropy: Short
    ) {
        mArray[mPos++] = TEXTURE
        mArray[mPos++] = texture
        mArray[mPos++] = tileModeX.toInt() or (tileModeY.toInt() shl 16)
        mArray[mPos++] = filterMode.toInt() or (maxAnisotropy.toInt() shl 16)
    }

    /** Set the path effect */
    fun setPathEffect(pathEffect: FloatArray?) {
        if (pathEffect == null) {
            mArray[mPos++] = PATH_EFFECT
            return
        }
        if (pathEffect.size > MAX_PATH_EFFECT_LENGTH) {
            throw RuntimeException("pathEffect to long ${pathEffect.size}")
        }
        mArray[mPos++] = PATH_EFFECT or (pathEffect.size shl 16)
        for (i in pathEffect.indices) {
            mArray[mPos++] = pathEffect[i].toRawBits()
        }
    }

    /** clear a series of paint parameters. Currently not used */
    @Suppress("UNUSED_PARAMETER")
    fun clear(mask: Long) { /* unused for now */ }

    /** Reset the content of the paint bundle so that it can be reused */
    fun reset() {
        mPos = 0
        if (mColorFilterSet) {
            clearColorFilter()
        }
        if (mLastShaderSet != -1 && mLastShaderSet != 0) {
            setShader(0)
        }
    }

    /** Check all the floats for Nan(id) floats and call listenTo */
    fun registerVars(context: RemoteContext, support: VariableSupport) {
        var i = 0
        while (i < mPos) {
            val cmd = mArray[i++]
            val type = cmd and 0xFFFF
            when (type) {
                STROKE_MITER, STROKE_WIDTH, ALPHA, TEXT_SIZE -> {
                    val v = Float.fromBits(mArray[i++])
                    if (v.isNaN()) {
                        context.listensTo(Utils.idFromNan(v), support)
                    }
                }
                COLOR_FILTER_ID, COLOR_ID -> context.listensTo(mArray[i++], support)
                COLOR, TYPEFACE, SHADER, COLOR_FILTER -> i++
                STROKE_JOIN, FILTER_BITMAP, STROKE_CAP, STYLE,
                IMAGE_FILTER_QUALITY, BLEND_MODE, ANTI_ALIAS -> { /* no data */ }
                FONT_AXIS -> {
                    val count = cmd shr 16
                    for (j in 0 until count) {
                        i++ // skip tag
                        val v = Float.fromBits(mArray[i++])
                        if (v.isNaN()) {
                            context.listensTo(Utils.idFromNan(v), support)
                        }
                    }
                }
                TEXTURE -> i += 3
                GRADIENT -> i = callRegisterGradient(cmd, mArray, i, context, support)
                PATH_EFFECT -> {
                    val count = cmd shr 16
                    if (count > 0) {
                        i = PaintPathEffects.getIds(mArray, i) { off ->
                            if (Float.fromBits(mArray[off]).isNaN()) {
                                context.listensTo(
                                    Utils.idFromNan(Float.fromBits(mArray[off])),
                                    support
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    /** Update variables if any are float ids */
    fun updateVariables(context: RemoteContext) {
        if (mOutArray == null) {
            mOutArray = mArray.copyOf()
        } else {
            mArray.copyInto(mOutArray!!, 0, 0, mArray.size)
        }
        val outArray = mOutArray!!
        var i = 0
        while (i < mPos) {
            val cmd = mArray[i++]
            val type = cmd and 0xFFFF
            when (type) {
                STROKE_MITER, STROKE_WIDTH, ALPHA, TEXT_SIZE -> {
                    outArray[i] = fixFloatVar(mArray[i], context)
                    i++
                }
                COLOR_FILTER_ID, COLOR_ID -> {
                    outArray[i] = fixColor(mArray[i], context)
                    i++
                }
                COLOR, TYPEFACE, SHADER, COLOR_FILTER -> i++
                STROKE_JOIN, FILTER_BITMAP, STROKE_CAP, STYLE,
                IMAGE_FILTER_QUALITY, BLEND_MODE, ANTI_ALIAS, CLEAR_COLOR_FILTER -> { /* no data */ }
                FONT_AXIS -> {
                    val count = cmd shr 16
                    for (j in 0 until count) {
                        i++ // skip tag
                        outArray[i] = fixFloatVar(mArray[i], context)
                        i++
                    }
                }
                TEXTURE -> i += 3
                GRADIENT -> i = updateFloatsInGradient(cmd, outArray, mArray, i, context)
                PATH_EFFECT -> {
                    val count = cmd shr 16
                    if (count > 0) {
                        i = PaintPathEffects.getIds(mArray, i) { off ->
                            outArray[off] = fixFloatVar(mArray[off], context)
                        }
                    }
                }
            }
        }
    }

    private fun fixFloatVar(value: Int, context: RemoteContext): Int {
        val v = Float.fromBits(value)
        if (v.isNaN()) {
            val id = Utils.idFromNan(v)
            return context.getFloat(id).toRawBits()
        }
        return value
    }

    private fun fixColor(colorId: Int, context: RemoteContext): Int {
        return context.getColor(colorId)
    }

    internal fun updateFloatsInGradient(
        cmd: Int, out: IntArray, array: IntArray, i: Int, context: RemoteContext
    ): Int {
        var ret = i
        val type = cmd shr 16
        val control = array[ret++]
        var len = 0xFF and control
        val register = 0xFFFF and (control shr 16)
        when (type) {
            LINEAR_GRADIENT -> {
                if (len > 0) {
                    for (j in 0 until len) {
                        val color = array[ret]
                        if ((register and (1 shl j)) != 0) {
                            out[ret] = fixColor(color, context)
                        }
                        ret++
                    }
                }
                len = array[ret++]
                if (len > 0) {
                    for (j in 0 until len) {
                        out[ret] = fixFloatVar(array[ret], context)
                        ret++
                    }
                }
                out[ret] = fixFloatVar(array[ret], context); ret++
                out[ret] = fixFloatVar(array[ret], context); ret++
                out[ret] = fixFloatVar(array[ret], context); ret++
                out[ret] = fixFloatVar(array[ret], context); ret++
                ret++ // tileMode
            }
            RADIAL_GRADIENT -> {
                if (len > 0) {
                    for (j in 0 until len) {
                        val color = array[ret]
                        if ((register and (1 shl j)) != 0) {
                            out[ret] = fixColor(color, context)
                        }
                        ret++
                    }
                }
                len = array[ret++]
                if (len > 0) {
                    for (j in 0 until len) {
                        out[ret] = fixFloatVar(array[ret], context)
                        ret++
                    }
                }
                out[ret] = fixFloatVar(array[ret], context); ret++ // center x
                out[ret] = fixFloatVar(array[ret], context); ret++ // center y
                out[ret] = fixFloatVar(array[ret], context); ret++ // radius
                ret++ // tileMode
            }
            SWEEP_GRADIENT -> {
                if (len > 0) {
                    for (j in 0 until len) {
                        val color = array[ret]
                        if ((register and (1 shl j)) != 0) {
                            out[ret] = fixColor(color, context)
                        }
                        ret++
                    }
                }
                len = array[ret++]
                if (len > 0) {
                    for (j in 0 until len) {
                        out[ret] = fixFloatVar(array[ret], context)
                        ret++
                    }
                }
                out[ret] = fixFloatVar(array[ret], context); ret++ // center x
                out[ret] = fixFloatVar(array[ret], context); ret++ // center y
            }
            else -> println("gradient type unknown")
        }
        return ret
    }

    override fun serialize(serializer: MapSerializer) {
        serializer.addType("PaintBundle")
        val list = mutableListOf<Map<String, Any>>()
        var i = 0
        while (i < mPos) {
            val cmd = mArray[i++]
            val type = cmd and 0xFFFF
            when (type) {
                TEXT_SIZE ->
                    list.add(orderedOf("type", "TextSize", "size", getVariable(mArray[i++])))
                TYPEFACE -> {
                    val style = cmd shr 16
                    val weight = (style and 0x3ff).toFloat()
                    val italic = (style shr 10) > 0
                    val fontFamily = mArray[i++]
                    list.add(orderedOf("type", "FontFamily", "fontFamily", fontFamily))
                    list.add(orderedOf("type", "FontWeight", "weight", weight))
                    list.add(orderedOf("type", "TypeFace", "italic", italic))
                }
                COLOR ->
                    list.add(orderedOf("type", "Color", "color", colorInt(mArray[i++])))
                COLOR_ID ->
                    list.add(orderedOf("type", "ColorId", "id", mArray[i++]))
                STROKE_WIDTH ->
                    list.add(orderedOf("type", "StrokeWidth", "width", getVariable(mArray[i++])))
                STROKE_MITER ->
                    list.add(orderedOf("type", "StrokeMiter", "miter", getVariable(mArray[i++])))
                STROKE_CAP ->
                    list.add(orderedOf("type", "StrokeCap", "cap", cmd shr 16))
                STYLE ->
                    list.add(orderedOf("type", "Style", "style", cmd shr 16))
                COLOR_FILTER ->
                    list.add(
                        orderedOf(
                            "type", "ColorFilter",
                            "color", colorInt(mArray[i++]),
                            "mode", blendModeString(cmd shr 16)
                        )
                    )
                COLOR_FILTER_ID ->
                    list.add(
                        orderedOf(
                            "type", "ColorFilterID",
                            "id", mArray[i++],
                            "mode", blendModeString(cmd shr 16)
                        )
                    )
                CLEAR_COLOR_FILTER ->
                    list.add(orderedOf("type", "ClearColorFilter"))
                SHADER ->
                    list.add(orderedOf("type", "Shader", "id", mArray[i++]))
                ALPHA ->
                    list.add(orderedOf("type", "Alpha", "alpha", getVariable(mArray[i++])))
                IMAGE_FILTER_QUALITY ->
                    list.add(orderedOf("type", "ImageFilterQuality", "quality", cmd shr 16))
                BLEND_MODE ->
                    list.add(orderedOf("type", "BlendMode", "mode", blendModeString(cmd shr 16)))
                FILTER_BITMAP ->
                    list.add(orderedOf("type", "FilterBitmap", "enabled", (cmd shr 16) != 0))
                STROKE_JOIN ->
                    list.add(orderedOf("type", "StrokeJoin", "strokeJoin", cmd shr 16))
                ANTI_ALIAS ->
                    list.add(orderedOf("type", "AntiAlias", "enabled", (cmd shr 16) != 0))
                GRADIENT ->
                    i = serializeGradient(cmd, mArray, i, list)
            }
        }
        serializer.add("operations", list)
    }

    companion object {
        private const val MAX_PATH_EFFECT_LENGTH = 2028

        const val TEXT_SIZE = 1 // float
        const val COLOR = 4 // int
        const val STROKE_WIDTH = 5 // float
        const val STROKE_MITER = 6
        const val STROKE_CAP = 7 // int
        const val STYLE = 8 // int
        const val SHADER = 9 // int
        const val IMAGE_FILTER_QUALITY = 10 // int
        const val GRADIENT = 11
        const val ALPHA = 12
        const val COLOR_FILTER = 13
        const val ANTI_ALIAS = 14
        const val STROKE_JOIN = 15
        const val TYPEFACE = 16
        const val FILTER_BITMAP = 17
        const val BLEND_MODE = 18
        const val COLOR_ID = 19
        const val COLOR_FILTER_ID = 20
        const val CLEAR_COLOR_FILTER = 21
        const val SHADER_MATRIX = 22
        const val FONT_AXIS = 23
        const val TEXTURE = 24
        const val PATH_EFFECT = 25

        const val BLEND_MODE_CLEAR = 0
        const val BLEND_MODE_SRC = 1
        const val BLEND_MODE_DST = 2
        const val BLEND_MODE_SRC_OVER = 3
        const val BLEND_MODE_DST_OVER = 4
        const val BLEND_MODE_SRC_IN = 5
        const val BLEND_MODE_DST_IN = 6
        const val BLEND_MODE_SRC_OUT = 7
        const val BLEND_MODE_DST_OUT = 8
        const val BLEND_MODE_SRC_ATOP = 9
        const val BLEND_MODE_DST_ATOP = 10
        const val BLEND_MODE_XOR = 11
        const val BLEND_MODE_PLUS = 12
        const val BLEND_MODE_MODULATE = 13
        const val BLEND_MODE_SCREEN = 14
        const val BLEND_MODE_OVERLAY = 15
        const val BLEND_MODE_DARKEN = 16
        const val BLEND_MODE_LIGHTEN = 17
        const val BLEND_MODE_COLOR_DODGE = 18
        const val BLEND_MODE_COLOR_BURN = 19
        const val BLEND_MODE_HARD_LIGHT = 20
        const val BLEND_MODE_SOFT_LIGHT = 21
        const val BLEND_MODE_DIFFERENCE = 22
        const val BLEND_MODE_EXCLUSION = 23
        const val BLEND_MODE_MULTIPLY = 24
        const val BLEND_MODE_HUE = 25
        const val BLEND_MODE_SATURATION = 26
        const val BLEND_MODE_COLOR = 27
        const val BLEND_MODE_LUMINOSITY = 28
        const val BLEND_MODE_NULL = 29
        const val PORTER_MODE_ADD = 30

        const val FONT_NORMAL = 0
        const val FONT_BOLD = 1
        const val FONT_ITALIC = 2
        const val FONT_BOLD_ITALIC = 3

        const val FONT_TYPE_DEFAULT = 0
        const val FONT_TYPE_SANS_SERIF = 1
        const val FONT_TYPE_SERIF = 2
        const val FONT_TYPE_MONOSPACE = 3

        const val STYLE_FILL = 0
        const val STYLE_STROKE = 1
        const val STYLE_FILL_AND_STROKE = 2
        const val LINEAR_GRADIENT = 0
        const val RADIAL_GRADIENT = 1
        const val SWEEP_GRADIENT = 2

        private fun colorInt(color: Int): String {
            val str = "000000000000" + color.toUInt().toString(16)
            return "0x" + str.substring(str.length - 8)
        }

        private fun colorInt(color: IntArray): String {
            return color.joinToString(", ", "[", "]") { colorInt(it) }
        }

        private fun asFloatStr(value: Int): String {
            val fValue = Float.fromBits(value)
            if (fValue.isNaN()) {
                return "[${Utils.idFromNan(fValue)}]"
            }
            return fValue.toString()
        }

        /** Convert a blend mode integer as a string */
        fun blendModeString(mode: Int): String = when (mode) {
            BLEND_MODE_CLEAR -> "CLEAR"
            BLEND_MODE_SRC -> "SRC"
            BLEND_MODE_DST -> "DST"
            BLEND_MODE_SRC_OVER -> "SRC_OVER"
            BLEND_MODE_DST_OVER -> "DST_OVER"
            BLEND_MODE_SRC_IN -> "SRC_IN"
            BLEND_MODE_DST_IN -> "DST_IN"
            BLEND_MODE_SRC_OUT -> "SRC_OUT"
            BLEND_MODE_DST_OUT -> "DST_OUT"
            BLEND_MODE_SRC_ATOP -> "SRC_ATOP"
            BLEND_MODE_DST_ATOP -> "DST_ATOP"
            BLEND_MODE_XOR -> "XOR"
            BLEND_MODE_PLUS -> "PLUS"
            BLEND_MODE_MODULATE -> "MODULATE"
            BLEND_MODE_SCREEN -> "SCREEN"
            BLEND_MODE_OVERLAY -> "OVERLAY"
            BLEND_MODE_DARKEN -> "DARKEN"
            BLEND_MODE_LIGHTEN -> "LIGHTEN"
            BLEND_MODE_COLOR_DODGE -> "COLOR_DODGE"
            BLEND_MODE_COLOR_BURN -> "COLOR_BURN"
            BLEND_MODE_HARD_LIGHT -> "HARD_LIGHT"
            BLEND_MODE_SOFT_LIGHT -> "SOFT_LIGHT"
            BLEND_MODE_DIFFERENCE -> "DIFFERENCE"
            BLEND_MODE_EXCLUSION -> "EXCLUSION"
            BLEND_MODE_MULTIPLY -> "MULTIPLY"
            BLEND_MODE_HUE -> "HUE"
            BLEND_MODE_SATURATION -> "SATURATION"
            BLEND_MODE_COLOR -> "COLOR"
            BLEND_MODE_LUMINOSITY -> "LUMINOSITY"
            BLEND_MODE_NULL -> "null"
            PORTER_MODE_ADD -> "ADD"
            else -> "null"
        }

        private fun getVariable(value: Int): Map<String, Any> {
            val fValue = Float.fromBits(value)
            return if (fValue.isNaN()) {
                orderedOf("type", "Variable", "id", Utils.idFromNan(fValue))
            } else {
                orderedOf("type", "Value", "value", fValue)
            }
        }

        private fun serializeGradient(
            cmd: Int, array: IntArray, i: Int, list: MutableList<Map<String, Any>>
        ): Int {
            var ret = i
            val gradientType = cmd shr 16

            var len = 0xFF and array[ret++]

            var colors: Array<String>? = null
            if (len > 0) {
                colors = Array(len) { "" }
                for (j in colors.indices) {
                    colors[j] = colorInt(array[ret++])
                }
            }
            len = array[ret++]
            var stops: FloatArray? = null
            if (len > 0 && colors != null) {
                stops = FloatArray(len)
                for (j in colors.indices) {
                    stops[j] = Float.fromBits(array[ret++])
                }
            }

            if (colors == null) return ret

            when (gradientType) {
                LINEAR_GRADIENT -> {
                    val startX = array[ret++]
                    val startY = array[ret++]
                    val endX = array[ret++]
                    val endY = array[ret++]
                    val tileMode = array[ret++]
                    list.add(
                        orderedOf(
                            "type", "LinearGradient",
                            "colors", colors,
                            "stops", stops ?: emptyList<Any>(),
                            "startX", getVariable(startX),
                            "startY", getVariable(startY),
                            "endX", getVariable(endX),
                            "endY", getVariable(endY),
                            "tileMode", tileMode
                        )
                    )
                }
                RADIAL_GRADIENT -> {
                    val centerX = array[ret++]
                    val centerY = array[ret++]
                    val radius = array[ret++]
                    val tileMode = array[ret++]
                    list.add(
                        orderedOf(
                            "type", "RadialGradient",
                            "colors", colors,
                            "stops", stops ?: emptyList<Any>(),
                            "centerX", getVariable(centerX),
                            "centerY", getVariable(centerY),
                            "radius", getVariable(radius),
                            "tileMode", tileMode
                        )
                    )
                }
                SWEEP_GRADIENT -> {
                    val centerX = array[ret++]
                    val centerY = array[ret++]
                    list.add(
                        orderedOf(
                            "type", "SweepGradient",
                            "colors", colors,
                            "stops", stops ?: emptyList<Any>(),
                            "centerX", getVariable(centerX),
                            "centerY", getVariable(centerY)
                        )
                    )
                }
            }
            return ret
        }
    }

    private var mLastShaderSet = -1
    private var mColorFilterSet = false
}
