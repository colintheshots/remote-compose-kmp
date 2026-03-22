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
package androidx.compose.remote.core

import androidx.compose.remote.core.operations.BitmapData
import androidx.compose.remote.core.operations.BitmapFontData
import androidx.compose.remote.core.operations.BitmapTextMeasure
import androidx.compose.remote.core.operations.ClickArea
import androidx.compose.remote.core.operations.ClipPath
import androidx.compose.remote.core.operations.ClipRect
import androidx.compose.remote.core.operations.ColorAttribute
import androidx.compose.remote.core.operations.ColorConstant
import androidx.compose.remote.core.operations.ColorExpression
import androidx.compose.remote.core.operations.ComponentValue
import androidx.compose.remote.core.operations.ConditionalOperations
import androidx.compose.remote.core.operations.DataDynamicListFloat
import androidx.compose.remote.core.operations.DataListFloat
import androidx.compose.remote.core.operations.DataListIds
import androidx.compose.remote.core.operations.DataMapIds
import androidx.compose.remote.core.operations.DataMapLookup
import androidx.compose.remote.core.operations.DebugMessage
import androidx.compose.remote.core.operations.DrawArc
import androidx.compose.remote.core.operations.DrawBitmap
import androidx.compose.remote.core.operations.DrawBitmapFontText
import androidx.compose.remote.core.operations.DrawBitmapFontTextOnPath
import androidx.compose.remote.core.operations.DrawBitmapInt
import androidx.compose.remote.core.operations.DrawBitmapScaled
import androidx.compose.remote.core.operations.DrawBitmapTextAnchored
import androidx.compose.remote.core.operations.DrawCircle
import androidx.compose.remote.core.operations.DrawContent
import androidx.compose.remote.core.operations.DrawLine
import androidx.compose.remote.core.operations.DrawOval
import androidx.compose.remote.core.operations.DrawPath
import androidx.compose.remote.core.operations.DrawRect
import androidx.compose.remote.core.operations.DrawRoundRect
import androidx.compose.remote.core.operations.DrawSector
import androidx.compose.remote.core.operations.DrawText
import androidx.compose.remote.core.operations.DrawTextAnchored
import androidx.compose.remote.core.operations.DrawTextOnCircle
import androidx.compose.remote.core.operations.DrawTextOnPath
import androidx.compose.remote.core.operations.DrawToBitmap
import androidx.compose.remote.core.operations.DrawTweenPath
import androidx.compose.remote.core.operations.FloatConstant
import androidx.compose.remote.core.operations.FloatExpression
import androidx.compose.remote.core.operations.FloatFunctionCall
import androidx.compose.remote.core.operations.FloatFunctionDefine
import androidx.compose.remote.core.operations.FontData
import androidx.compose.remote.core.operations.HapticFeedback
import androidx.compose.remote.core.operations.Header
import androidx.compose.remote.core.operations.IdLookup
import androidx.compose.remote.core.operations.ImageAttribute
import androidx.compose.remote.core.operations.IntegerExpression
import androidx.compose.remote.core.operations.MatrixFromPath
import androidx.compose.remote.core.operations.MatrixRestore
import androidx.compose.remote.core.operations.MatrixRotate
import androidx.compose.remote.core.operations.MatrixSave
import androidx.compose.remote.core.operations.MatrixScale
import androidx.compose.remote.core.operations.MatrixSkew
import androidx.compose.remote.core.operations.MatrixTranslate
import androidx.compose.remote.core.operations.NamedVariable
import androidx.compose.remote.core.operations.PaintData
import androidx.compose.remote.core.operations.ParticlesCompare
import androidx.compose.remote.core.operations.ParticlesCreate
import androidx.compose.remote.core.operations.ParticlesLoop
import androidx.compose.remote.core.operations.PathAppend
import androidx.compose.remote.core.operations.PathCombine
import androidx.compose.remote.core.operations.PathCreate
import androidx.compose.remote.core.operations.PathData
import androidx.compose.remote.core.operations.PathExpression
import androidx.compose.remote.core.operations.PathTween
import androidx.compose.remote.core.operations.Rem
import androidx.compose.remote.core.operations.RootContentBehavior
import androidx.compose.remote.core.operations.RootContentDescription
import androidx.compose.remote.core.operations.TextAttribute
import androidx.compose.remote.core.operations.TextData
import androidx.compose.remote.core.operations.TextFromFloat
import androidx.compose.remote.core.operations.TextLength
import androidx.compose.remote.core.operations.TextLookup
import androidx.compose.remote.core.operations.TextLookupInt
import androidx.compose.remote.core.operations.TextMeasure
import androidx.compose.remote.core.operations.TextMerge
import androidx.compose.remote.core.operations.TextSubtext
import androidx.compose.remote.core.operations.Theme
import androidx.compose.remote.core.operations.TimeAttribute
import androidx.compose.remote.core.operations.TouchExpression
import androidx.compose.remote.core.operations.UpdateDynamicFloatList
import androidx.compose.remote.core.operations.Utils
import androidx.compose.remote.core.operations.WakeIn
import androidx.compose.remote.core.operations.layout.CanvasContent
import androidx.compose.remote.core.operations.layout.CanvasOperations
import androidx.compose.remote.core.operations.layout.ClickModifierOperation
import androidx.compose.remote.core.operations.layout.ComponentStart
import androidx.compose.remote.core.operations.layout.ContainerEnd
import androidx.compose.remote.core.operations.layout.ImpulseOperation
import androidx.compose.remote.core.operations.layout.ImpulseProcess
import androidx.compose.remote.core.operations.layout.LayoutComponentContent
import androidx.compose.remote.core.operations.layout.LoopOperation
import androidx.compose.remote.core.operations.layout.RootLayoutComponent
import androidx.compose.remote.core.operations.layout.TouchCancelModifierOperation
import androidx.compose.remote.core.operations.layout.TouchDownModifierOperation
import androidx.compose.remote.core.operations.layout.TouchUpModifierOperation
import androidx.compose.remote.core.operations.layout.animation.AnimationSpec
import androidx.compose.remote.core.operations.layout.managers.BoxLayout
import androidx.compose.remote.core.operations.layout.managers.CanvasLayout
import androidx.compose.remote.core.operations.layout.managers.CollapsibleColumnLayout
import androidx.compose.remote.core.operations.layout.managers.CollapsibleRowLayout
import androidx.compose.remote.core.operations.layout.managers.ColumnLayout
import androidx.compose.remote.core.operations.layout.managers.CoreText
import androidx.compose.remote.core.operations.layout.managers.FitBoxLayout
import androidx.compose.remote.core.operations.layout.managers.ImageLayout
import androidx.compose.remote.core.operations.layout.managers.RowLayout
import androidx.compose.remote.core.operations.layout.managers.StateLayout
import androidx.compose.remote.core.operations.layout.managers.TextLayout
import androidx.compose.remote.core.operations.layout.modifiers.AlignByModifierOperation
import androidx.compose.remote.core.operations.layout.modifiers.BackgroundModifierOperation
import androidx.compose.remote.core.operations.layout.modifiers.BorderModifierOperation
import androidx.compose.remote.core.operations.layout.modifiers.ClipRectModifierOperation
import androidx.compose.remote.core.operations.layout.modifiers.CollapsiblePriorityModifierOperation
import androidx.compose.remote.core.operations.layout.modifiers.ComponentVisibilityOperation
import androidx.compose.remote.core.operations.layout.modifiers.DimensionModifierOperation
import androidx.compose.remote.core.operations.layout.modifiers.DrawContentOperation
import androidx.compose.remote.core.operations.layout.modifiers.GraphicsLayerModifierOperation
import androidx.compose.remote.core.operations.layout.modifiers.HeightInModifierOperation
import androidx.compose.remote.core.operations.layout.modifiers.HeightModifierOperation
import androidx.compose.remote.core.operations.layout.modifiers.LayoutComputeOperation
import androidx.compose.remote.core.operations.layout.modifiers.MarqueeModifierOperation
import androidx.compose.remote.core.operations.layout.modifiers.OffsetModifierOperation
import androidx.compose.remote.core.operations.layout.modifiers.PaddingModifierOperation
import androidx.compose.remote.core.operations.layout.modifiers.RippleModifierOperation
import androidx.compose.remote.core.operations.layout.modifiers.RoundedClipRectModifierOperation
import androidx.compose.remote.core.operations.layout.modifiers.RunActionOperation
import androidx.compose.remote.core.operations.layout.modifiers.ScrollModifierOperation
import androidx.compose.remote.core.operations.layout.modifiers.ValueFloatChangeActionOperation
import androidx.compose.remote.core.operations.layout.modifiers.ValueFloatExpressionChangeActionOperation
import androidx.compose.remote.core.operations.layout.modifiers.ValueIntegerChangeActionOperation
import androidx.compose.remote.core.operations.layout.modifiers.ValueIntegerExpressionChangeActionOperation
import androidx.compose.remote.core.operations.layout.modifiers.ValueStringChangeActionOperation
import androidx.compose.remote.core.operations.layout.modifiers.WidthInModifierOperation
import androidx.compose.remote.core.operations.layout.modifiers.WidthModifierOperation
import androidx.compose.remote.core.operations.layout.modifiers.ZIndexModifierOperation
import androidx.compose.remote.core.operations.matrix.MatrixConstant
import androidx.compose.remote.core.operations.matrix.MatrixExpression
import androidx.compose.remote.core.operations.matrix.MatrixVectorMath
import androidx.compose.remote.core.operations.paint.PaintBundle
import androidx.compose.remote.core.operations.utilities.easing.Easing
import androidx.compose.remote.core.operations.utilities.easing.FloatAnimation
import androidx.compose.remote.core.semantics.CoreSemantics
import androidx.compose.remote.core.types.BooleanConstant
import androidx.compose.remote.core.types.IntegerConstant
import androidx.compose.remote.core.types.LongConstant

/** Provides an abstract buffer to encode/decode RemoteCompose operations */
open class RemoteComposeBuffer(apiLevel: Int = CoreDocument.DOCUMENT_API_LEVEL) {

    companion object {
        const val EASING_CUBIC_STANDARD = Easing.CUBIC_STANDARD
        const val EASING_CUBIC_ACCELERATE = Easing.CUBIC_ACCELERATE
        const val EASING_CUBIC_DECELERATE = Easing.CUBIC_DECELERATE
        const val EASING_CUBIC_LINEAR = Easing.CUBIC_LINEAR
        const val EASING_CUBIC_ANTICIPATE = Easing.CUBIC_ANTICIPATE
        const val EASING_CUBIC_OVERSHOOT = Easing.CUBIC_OVERSHOOT
        const val EASING_CUBIC_CUSTOM = Easing.CUBIC_CUSTOM
        const val EASING_SPLINE_CUSTOM = Easing.SPLINE_CUSTOM
        const val EASING_EASE_OUT_BOUNCE = Easing.EASE_OUT_BOUNCE
        const val EASING_EASE_OUT_ELASTIC = Easing.EASE_OUT_ELASTIC

        const val PAD_AFTER_SPACE = TextFromFloat.PAD_AFTER_SPACE
        const val PAD_AFTER_NONE = TextFromFloat.PAD_AFTER_NONE
        const val PAD_AFTER_ZERO = TextFromFloat.PAD_AFTER_ZERO
        const val PAD_PRE_SPACE = TextFromFloat.PAD_PRE_SPACE
        const val PAD_PRE_NONE = TextFromFloat.PAD_PRE_NONE
        const val PAD_PRE_ZERO = TextFromFloat.PAD_PRE_ZERO

        private const val DEBUG = false

        fun version(): String = "v1.0"

        fun packAnimation(
            duration: Float, type: Int, spec: FloatArray?, initialValue: Float, wrap: Float
        ): FloatArray = FloatAnimation.packToFloatArray(duration, type, spec, initialValue, wrap)
    }

    private var mBuffer: WireBuffer = WireBuffer()
    protected var mLastComponentId: Int = 0
    private var mGeneratedComponentId: Int = -1
    protected var mApiLevel: Int = apiLevel
    protected var mProfileMask: Int = 0
    var mMap: Operations.UniqueIntMap<CompanionOperation> = Operations.UniqueIntMap()

    open fun reset(expectedSize: Int) {
        mBuffer.reset(expectedSize)
        mLastComponentId = 0
        mGeneratedComponentId = -1
    }

    fun getLastComponentId(): Int = mLastComponentId
    fun getBuffer(): WireBuffer = mBuffer
    fun setBuffer(buffer: WireBuffer) { mBuffer = buffer }

    // -- Operations --

    open fun addHeader(tags: ShortArray, values: Array<Any>) { Header.apply(mBuffer, mApiLevel, tags, values) }
    open fun header(width: Int, height: Int, density: Float, capabilities: Long) {
        Header.apply(mBuffer, width, height, density, capabilities)
    }
    open fun addRootContentDescription(contentDescriptionId: Int) {
        if (contentDescriptionId != 0) RootContentDescription.apply(mBuffer, contentDescriptionId)
    }
    open fun drawBitmap(imageId: Int, imageWidth: Int, imageHeight: Int, srcLeft: Int, srcTop: Int,
        srcRight: Int, srcBottom: Int, dstLeft: Int, dstTop: Int, dstRight: Int, dstBottom: Int,
        contentDescriptionId: Int) {
        DrawBitmapInt.apply(mBuffer, imageId, srcLeft, srcTop, srcRight, srcBottom, dstLeft, dstTop, dstRight, dstBottom, contentDescriptionId)
    }
    open fun mapLookup(id: Int, mapId: Int, strId: Int) { DataMapLookup.apply(mBuffer, id, mapId, strId) }
    open fun addText(id: Int, text: String) { TextData.apply(mBuffer, id, text) }
    open fun addClickArea(id: Int, contentDescriptionId: Int, left: Float, top: Float, right: Float, bottom: Float, metadataId: Int) {
        ClickArea.apply(mBuffer, id, contentDescriptionId, left, top, right, bottom, metadataId)
    }
    open fun setRootContentBehavior(scroll: Int, alignment: Int, sizing: Int, mode: Int) {
        RootContentBehavior.apply(mBuffer, scroll, alignment, sizing, mode)
    }
    open fun addDrawArc(left: Float, top: Float, right: Float, bottom: Float, startAngle: Float, sweepAngle: Float) {
        DrawArc.apply(mBuffer, left, top, right, bottom, startAngle, sweepAngle)
    }
    open fun addDrawSector(left: Float, top: Float, right: Float, bottom: Float, startAngle: Float, sweepAngle: Float) {
        DrawSector.apply(mBuffer, left, top, right, bottom, startAngle, sweepAngle)
    }
    open fun addDrawBitmap(imageId: Int, left: Float, top: Float, right: Float, bottom: Float, contentDescriptionId: Int) {
        DrawBitmap.apply(mBuffer, imageId, left, top, right, bottom, contentDescriptionId)
    }
    open fun drawScaledBitmap(imageId: Int, srcLeft: Float, srcTop: Float, srcRight: Float, srcBottom: Float,
        dstLeft: Float, dstTop: Float, dstRight: Float, dstBottom: Float, scaleType: Int, scaleFactor: Float, contentDescriptionId: Int) {
        DrawBitmapScaled.apply(mBuffer, imageId, srcLeft, srcTop, srcRight, srcBottom, dstLeft, dstTop, dstRight, dstBottom, scaleType, scaleFactor, contentDescriptionId)
    }
    open fun addBitmapFont(id: Int, glyphs: Array<BitmapFontData.Glyph>): Int { BitmapFontData.apply(mBuffer, id, glyphs, null); return id }
    open fun addBitmapFont(id: Int, glyphs: Array<BitmapFontData.Glyph>, kerningTable: Map<String, Short>): Int { BitmapFontData.apply(mBuffer, id, glyphs, kerningTable); return id }
    open fun setBitmapName(id: Int, name: String) { NamedVariable.apply(mBuffer, id, NamedVariable.IMAGE_TYPE, name) }
    open fun addDrawCircle(centerX: Float, centerY: Float, radius: Float) { DrawCircle.apply(mBuffer, centerX, centerY, radius) }
    open fun addDrawLine(x1: Float, y1: Float, x2: Float, y2: Float) { DrawLine.apply(mBuffer, x1, y1, x2, y2) }
    open fun addDrawOval(left: Float, top: Float, right: Float, bottom: Float) { DrawOval.apply(mBuffer, left, top, right, bottom) }
    open fun pathTween(out: Int, pid1: Int, pid2: Int, tween: Float): Int { PathTween.apply(mBuffer, out, pid1, pid2, tween); return out }
    open fun pathCreate(out: Int, x: Float, y: Float): Int { PathCreate.apply(mBuffer, out, x, y); return out }
    open fun pathAppend(id: Int, vararg path: Float) { PathAppend.apply(mBuffer, id, path) }
    open fun addDrawPath(pathId: Int) { DrawPath.apply(mBuffer, pathId) }
    open fun addDrawRect(left: Float, top: Float, right: Float, bottom: Float) { DrawRect.apply(mBuffer, left, top, right, bottom) }
    open fun addDrawRoundRect(left: Float, top: Float, right: Float, bottom: Float, radiusX: Float, radiusY: Float) {
        DrawRoundRect.apply(mBuffer, left, top, right, bottom, radiusX, radiusY)
    }
    open fun addDrawTextOnPath(textId: Int, pathId: Int, hOffset: Float, vOffset: Float) { DrawTextOnPath.apply(mBuffer, textId, pathId, hOffset, vOffset) }
    open fun addDrawTextOnCircle(textId: Int, centerX: Float, centerY: Float, radius: Float, startAngle: Float,
        warpRadiusOffset: Float, alignment: DrawTextOnCircle.Alignment, placement: DrawTextOnCircle.Placement) {
        DrawTextOnCircle.apply(mBuffer, textId, centerX, centerY, radius, startAngle, warpRadiusOffset, alignment, placement)
    }
    open fun addDrawTextRun(textId: Int, start: Int, end: Int, contextStart: Int, contextEnd: Int, x: Float, y: Float, rtl: Boolean) {
        DrawText.apply(mBuffer, textId, start, end, contextStart, contextEnd, x, y, rtl)
    }
    open fun addDrawBitmapFontTextRun(textId: Int, bitmapFontId: Int, start: Int, end: Int, x: Float, y: Float) {
        DrawBitmapFontText.apply(mBuffer, textId, bitmapFontId, start, end, x, y)
    }
    open fun addDrawBitmapFontTextRunOnPath(textId: Int, bitmapFontId: Int, pathId: Int, start: Int, end: Int, yAdj: Float) {
        DrawBitmapFontTextOnPath.apply(mBuffer, textId, bitmapFontId, pathId, start, end, yAdj)
    }
    open fun drawBitmapTextAnchored(textId: Int, bitmapFontId: Int, start: Float, end: Float, x: Float, y: Float, panX: Float, panY: Float) {
        DrawBitmapTextAnchored.apply(mBuffer, textId, bitmapFontId, start, end, x, y, panX, panY)
    }
    open fun textMerge(textId: Int, id1: Int, id2: Int): Int { TextMerge.apply(mBuffer, textId, id1, id2); return textId }
    open fun createTextFromFloat(id: Int, value: Float, digitsBefore: Short, digitsAfter: Short, flags: Int): Int {
        TextFromFloat.apply(mBuffer, id, value, digitsBefore, digitsAfter, flags); return id
    }
    open fun drawTextAnchored(textId: Int, x: Float, y: Float, panX: Float, panY: Float, flags: Int) {
        DrawTextAnchored.apply(mBuffer, textId, x, y, panX, panY, flags)
    }
    open fun addDrawTweenPath(path1Id: Int, path2Id: Int, tween: Float, start: Float, stop: Float) {
        DrawTweenPath.apply(mBuffer, path1Id, path2Id, tween, start, stop)
    }
    open fun addPathData(id: Int, pathData: FloatArray): Int { PathData.apply(mBuffer, id, pathData); return id }
    open fun addPathData(id: Int, pathData: FloatArray, winding: Int): Int {
        if (mApiLevel < 7 && winding != 0) throw RuntimeException("winding not supported in API level < 7")
        PathData.apply(mBuffer, id or (winding shl 24), pathData); return id
    }
    open fun addPaint(paint: PaintBundle) { PaintData.apply(mBuffer, paint) }

    @Suppress("UNCHECKED_CAST")
    fun inflateFromBuffer(operations: ArrayList<Operation>) {
        mApiLevel = Header.readApiLevel(mBuffer)
        var profiles = 0
        if (mApiLevel >= 7) {
            try {
                val header = Header.readDirect(mBuffer)
                profiles = header.getProfiles()
            } catch (e: Exception) { throw RuntimeException(e) }
        }
        mBuffer.index = 0
        if (mApiLevel == -1) return
        val map = Operations.getOperations(mApiLevel, profiles) ?: return
        mMap = map
        while (mBuffer.available()) {
            val opId = mBuffer.readByte()
            if (DEBUG) Utils.log(">> $opId")
            val operation = mMap.get(opId) ?: throw RuntimeException("Unknown operation encountered $opId")
            operation(mBuffer, operations)
        }
    }

    fun copy(): RemoteComposeBuffer {
        val operations = ArrayList<Operation>()
        inflateFromBuffer(operations)
        val buffer = RemoteComposeBuffer()
        return copyFromOperations(operations, buffer)
    }

    open fun setTheme(theme: Int) { Theme.apply(mBuffer, theme) }

    fun copyFromOperations(operations: ArrayList<Operation>, buffer: RemoteComposeBuffer): RemoteComposeBuffer {
        for (operation in operations) operation.write(buffer.mBuffer)
        return buffer
    }

    open fun addMatrixSkew(skewX: Float, skewY: Float) { MatrixSkew.apply(mBuffer, skewX, skewY) }
    open fun addMatrixRestore() { MatrixRestore.apply(mBuffer) }
    open fun addMatrixSave() { MatrixSave.apply(mBuffer) }
    open fun addMatrixRotate(angle: Float, centerX: Float, centerY: Float) { MatrixRotate.apply(mBuffer, angle, centerX, centerY) }
    open fun addMatrixTranslate(dx: Float, dy: Float) { MatrixTranslate.apply(mBuffer, dx, dy) }
    open fun addMatrixScale(scaleX: Float, scaleY: Float) { MatrixScale.apply(mBuffer, scaleX, scaleY, Float.NaN, Float.NaN) }
    open fun addMatrixScale(scaleX: Float, scaleY: Float, centerX: Float, centerY: Float) { MatrixScale.apply(mBuffer, scaleX, scaleY, centerX, centerY) }
    open fun addClipPath(pathId: Int) { ClipPath.apply(mBuffer, pathId) }
    open fun addClipRect(left: Float, top: Float, right: Float, bottom: Float) { ClipRect.apply(mBuffer, left, top, right, bottom) }
    open fun addFloat(id: Int, value: Float): Float { FloatConstant.apply(mBuffer, id, value); return Utils.asNan(id) }
    open fun addInteger(id: Int, value: Int) { IntegerConstant.apply(mBuffer, id, value) }
    open fun addLong(id: Int, value: Long) { LongConstant.apply(mBuffer, id, value) }
    open fun addBoolean(id: Int, value: Boolean) { BooleanConstant.apply(mBuffer, id, value) }
    fun asFloatId(id: Int): Float = Utils.asNan(id)
    open fun addAnimatedFloat(id: Int, vararg value: Float) { FloatExpression.apply(mBuffer, id, value, null) }
    open fun addAnimatedFloat(id: Int, value: FloatArray, animation: FloatArray?) { FloatExpression.apply(mBuffer, id, value, animation) }
    open fun addTouchExpression(id: Int, value: Float, min: Float, max: Float, velocityId: Float,
        touchEffects: Int, exp: FloatArray, touchMode: Int, touchSpec: FloatArray?, easingSpec: FloatArray?) {
        TouchExpression.apply(mBuffer, id, value, min, max, velocityId, touchEffects, exp, touchMode, touchSpec, easingSpec)
    }
    open fun textMeasure(id: Int, textId: Int, mode: Int) { TextMeasure.apply(mBuffer, id, textId, mode) }
    open fun textLength(id: Int, textId: Int) { TextLength.apply(mBuffer, id, textId) }
    open fun addFloatArray(id: Int, values: FloatArray) { DataListFloat.apply(mBuffer, id, values) }
    open fun addDynamicFloatArray(id: Int, size: Float) { DataDynamicListFloat.apply(mBuffer, id, size) }
    open fun setArrayValue(id: Int, index: Float, value: Float) { UpdateDynamicFloatList.apply(mBuffer, id, index, value) }
    open fun addList(id: Int, listId: IntArray) { DataListIds.apply(mBuffer, id, listId) }
    open fun addMap(id: Int, keys: Array<String>, types: ByteArray?, listId: IntArray) { DataMapIds.apply(mBuffer, id, keys, types, listId) }
    open fun textLookup(id: Int, dataSet: Float, index: Float) { TextLookup.apply(mBuffer, id, Utils.idFromNan(dataSet), index) }
    open fun idLookup(id: Int, dataSet: Float, index: Float) { IdLookup.apply(mBuffer, id, Utils.idFromNan(dataSet), index) }
    open fun textLookup(id: Int, dataSet: Float, index: Int) { TextLookupInt.apply(mBuffer, id, Utils.idFromNan(dataSet), index) }
    open fun addIntegerExpression(id: Int, mask: Int, value: IntArray) { IntegerExpression.apply(mBuffer, id, mask, value) }
    open fun addColor(id: Int, color: Int) { ColorConstant(id, color).write(mBuffer) }
    open fun addColorExpression(id: Int, color1: Int, color2: Int, tween: Float) { ColorExpression(id, 0, color1, color2, tween).write(mBuffer) }
    open fun addColorExpression(id: Int, color1: Short, color2: Int, tween: Float) { ColorExpression(id, 1, color1.toInt(), color2, tween).write(mBuffer) }
    open fun addColorExpression(id: Int, color1: Int, color2: Short, tween: Float) { ColorExpression(id, 2, color1, color2.toInt(), tween).write(mBuffer) }
    open fun addColorExpression(id: Int, color1: Short, color2: Short, tween: Float) { ColorExpression(id, 3, color1.toInt(), color2.toInt(), tween).write(mBuffer) }
    open fun addColorExpression(id: Int, hue: Float, sat: Float, value: Float) { ColorExpression(id, hue, sat, value).write(mBuffer) }
    open fun addColorExpression(id: Int, alpha: Int, hue: Float, sat: Float, value: Float) {
        ColorExpression(id, ColorExpression.HSV_MODE, alpha, hue, sat, value).write(mBuffer)
    }
    open fun addColorExpression(id: Int, alpha: Float, red: Float, green: Float, blue: Float) {
        ColorExpression(id, ColorExpression.ARGB_MODE, alpha, red, green, blue).write(mBuffer)
    }
    open fun setNamedVariable(id: Int, name: String, type: Int) { NamedVariable.apply(mBuffer, id, type, name) }

    protected fun getComponentId(id: Int): Int {
        return if (id != -1) id else { mGeneratedComponentId--; mGeneratedComponentId }
    }

    open fun addComponentStart(type: Int, id: Int) {
        mLastComponentId = getComponentId(id)
        ComponentStart.apply(mBuffer, type, mLastComponentId, 0f, 0f)
    }
    open fun addComponentStart(type: Int) { addComponentStart(type, -1) }
    open fun addContainerEnd() { ContainerEnd.apply(mBuffer) }
    open fun addModifierScroll(direction: Int, max: Float) { ScrollModifierOperation.apply(mBuffer, direction, 0f, max, 0f); ContainerEnd.apply(mBuffer) }
    open fun addModifierBackground(color: Int, shape: Int) {
        val r = (color shr 16 and 0xff) / 255f; val g = (color shr 8 and 0xff) / 255f
        val b = (color and 0xff) / 255f; val a = (color shr 24 and 0xff) / 255f
        BackgroundModifierOperation.apply(mBuffer, 0, 0, 0, 0, r, g, b, a, shape)
    }
    open fun addDynamicModifierBackground(colorId: Int, shape: Int) {
        BackgroundModifierOperation.apply(mBuffer, BackgroundModifierOperation.COLOR_REF, colorId, 0, 0, 0f, 0f, 0f, 0f, shape)
    }
    open fun addModifierBackground(r: Float, g: Float, b: Float, a: Float, shape: Int) {
        BackgroundModifierOperation.apply(mBuffer, 0, 0, 0, 0, r, g, b, a, shape)
    }
    open fun addModifierAlignBy(line: Float) { AlignByModifierOperation.apply(mBuffer, line, 0) }
    open fun addModifierBorder(borderWidth: Float, borderRoundedCorner: Float, color: Int, shape: Int) {
        val r = (color shr 16 and 0xff) / 255f; val g = (color shr 8 and 0xff) / 255f
        val b = (color and 0xff) / 255f; val a = (color shr 24 and 0xff) / 255f
        BorderModifierOperation.apply(mBuffer, 0, 0, 0, 0, borderWidth, borderRoundedCorner, r, g, b, a, shape)
    }
    open fun addModifierDynamicBorder(borderWidth: Float, borderRoundedCorner: Float, colorId: Int, shape: Int) {
        BorderModifierOperation.apply(mBuffer, BorderModifierOperation.COLOR_REF, colorId, 0, 0, borderWidth, borderRoundedCorner, 0f, 0f, 0f, 0f, shape)
    }
    open fun addModifierPadding(left: Float, top: Float, right: Float, bottom: Float) { PaddingModifierOperation.apply(mBuffer, left, top, right, bottom) }
    open fun addModifierOffset(x: Float, y: Float) { OffsetModifierOperation.apply(mBuffer, x, y) }
    open fun addModifierZIndex(value: Float) { ZIndexModifierOperation.apply(mBuffer, value) }
    open fun addModifierRipple() { RippleModifierOperation.apply(mBuffer) }
    open fun addModifierMarquee(iterations: Int, animationMode: Int, repeatDelayMillis: Float, initialDelayMillis: Float, spacing: Float, velocity: Float) {
        MarqueeModifierOperation.apply(mBuffer, iterations, animationMode, repeatDelayMillis, initialDelayMillis, spacing, velocity)
    }
    open fun addModifierGraphicsLayer(attributes: HashMap<Int, Any>) { GraphicsLayerModifierOperation.apply(mBuffer, attributes) }
    open fun addRoundClipRectModifier(topStart: Float, topEnd: Float, bottomStart: Float, bottomEnd: Float) {
        RoundedClipRectModifierOperation.apply(mBuffer, topStart, topEnd, bottomStart, bottomEnd)
    }
    open fun addClipRectModifier() { ClipRectModifierOperation.apply(mBuffer) }
    open fun addLoopStart(indexId: Int, from: Float, step: Float, until: Float) { LoopOperation.apply(mBuffer, indexId, from, step, until) }
    open fun addLoopEnd() { ContainerEnd.apply(mBuffer) }
    open fun addStateLayout(componentId: Int, animationId: Int, horizontal: Int, vertical: Int, indexId: Int) {
        mLastComponentId = getComponentId(componentId); StateLayout.apply(mBuffer, mLastComponentId, animationId, horizontal, vertical, indexId)
    }
    open fun addBoxStart(componentId: Int, animationId: Int, horizontal: Int, vertical: Int) {
        mLastComponentId = getComponentId(componentId); BoxLayout.apply(mBuffer, mLastComponentId, animationId, horizontal, vertical)
    }
    open fun addFitBoxStart(componentId: Int, animationId: Int, horizontal: Int, vertical: Int) {
        mLastComponentId = getComponentId(componentId); FitBoxLayout.apply(mBuffer, mLastComponentId, animationId, horizontal, vertical, 0f)
    }
    open fun addImage(componentId: Int, animationId: Int, bitmapId: Int, scaleType: Int, alpha: Float) {
        mLastComponentId = getComponentId(componentId); ImageLayout.apply(mBuffer, componentId, animationId, bitmapId, scaleType, alpha)
    }
    open fun addRowStart(componentId: Int, animationId: Int, horizontal: Int, vertical: Int, spacedBy: Float) {
        mLastComponentId = getComponentId(componentId); RowLayout.apply(mBuffer, mLastComponentId, animationId, horizontal, vertical, spacedBy)
    }
    open fun addCollapsibleRowStart(componentId: Int, animationId: Int, horizontal: Int, vertical: Int, spacedBy: Float) {
        mLastComponentId = getComponentId(componentId); CollapsibleRowLayout.apply(mBuffer, mLastComponentId, animationId, horizontal, vertical, spacedBy)
    }
    open fun addColumnStart(componentId: Int, animationId: Int, horizontal: Int, vertical: Int, spacedBy: Float) {
        mLastComponentId = getComponentId(componentId); ColumnLayout.apply(mBuffer, mLastComponentId, animationId, horizontal, vertical, spacedBy)
    }
    open fun addCollapsibleColumnStart(componentId: Int, animationId: Int, horizontal: Int, vertical: Int, spacedBy: Float) {
        mLastComponentId = getComponentId(componentId); CollapsibleColumnLayout.apply(mBuffer, mLastComponentId, animationId, horizontal, vertical, spacedBy)
    }
    open fun addCanvasStart(componentId: Int, animationId: Int) {
        mLastComponentId = getComponentId(componentId); CanvasLayout.apply(mBuffer, mLastComponentId, animationId)
    }
    open fun addCanvasContentStart(componentId: Int) {
        mLastComponentId = getComponentId(componentId); CanvasContent.apply(mBuffer, mLastComponentId)
    }
    open fun addRootStart() { mLastComponentId = getComponentId(-1); RootLayoutComponent.apply(mBuffer, mLastComponentId) }
    open fun addContentStart() { mLastComponentId = getComponentId(-1); LayoutComponentContent.apply(mBuffer, mLastComponentId) }
    open fun addCanvasOperationsStart() { CanvasOperations.apply(mBuffer) }
    open fun addRunActionsStart() { RunActionOperation.apply(mBuffer) }
    open fun addComponentWidthValue(id: Int) { ComponentValue.apply(mBuffer, ComponentValue.WIDTH, mLastComponentId, id) }
    open fun addComponentHeightValue(id: Int) { ComponentValue.apply(mBuffer, ComponentValue.HEIGHT, mLastComponentId, id) }

    open fun addTextComponentStart(componentId: Int, animationId: Int, textId: Int, color: Int,
        fontSize: Float, fontStyle: Int, fontWeight: Float, fontFamilyId: Int,
        flags: Short, textAlign: Short, overflow: Int, maxLines: Int) {
        mLastComponentId = getComponentId(componentId)
        val flagsAndTextAlign = (flags.toInt() shl 16) or (textAlign.toInt() and 0xFFFF)
        TextLayout.apply(mBuffer, mLastComponentId, animationId, textId, color, fontSize, fontStyle, fontWeight, fontFamilyId, flagsAndTextAlign, overflow, maxLines)
    }

    open fun addTextComponentStart(componentId: Int, animationId: Int, textId: Int, color: Int,
        colorId: Int, fontSize: Float, fontStyle: Int, fontWeight: Float, fontFamilyId: Int,
        textAlign: Int, overflow: Int, maxLines: Int, letterSpacing: Float, lineHeightAdd: Float,
        lineHeightMultiplier: Float, lineBreakStrategy: Int, hyphenationFrequency: Int,
        justificationMode: Int, underline: Boolean, strikethrough: Boolean,
        fontAxis: IntArray?, fontAxisValues: FloatArray?, autosize: Boolean, flags: Int) {
        mLastComponentId = getComponentId(componentId)
        if (mApiLevel < 7) {
            // Check unsupported params and fallback to TextLayout
            if (letterSpacing != 0f || lineHeightAdd != 0f || lineHeightMultiplier != 1f ||
                lineBreakStrategy != 0 || hyphenationFrequency != 0 || justificationMode != 0 ||
                underline || strikethrough || (fontAxis != null && fontAxis.isNotEmpty()) ||
                (fontAxisValues != null && fontAxisValues.isNotEmpty()) || autosize) {
                throw RuntimeException("Text parameters not supported on API level < 7")
            }
            TextLayout.apply(mBuffer, mLastComponentId, animationId, textId, color, fontSize, fontStyle, fontWeight, fontFamilyId, textAlign, overflow, maxLines)
        } else {
            CoreText.apply(mBuffer, mLastComponentId, animationId, textId, color, colorId, fontSize,
                fontStyle, fontWeight, fontFamilyId, textAlign, overflow, maxLines, letterSpacing,
                lineHeightAdd, lineHeightMultiplier, lineBreakStrategy, hyphenationFrequency,
                justificationMode, underline, strikethrough, fontAxis, fontAxisValues, autosize, flags)
        }
    }

    open fun addImpulse(duration: Float, start: Float) { ImpulseOperation.apply(mBuffer, duration, start) }
    open fun addImpulseProcess() { ImpulseProcess.apply(mBuffer) }
    open fun addImpulseEnd() { ContainerEnd.apply(mBuffer) }
    open fun addParticles(id: Int, varIds: IntArray, initialExpressions: Array<FloatArray>, particleCount: Int) {
        ParticlesCreate.apply(mBuffer, id, varIds, initialExpressions, particleCount)
    }
    open fun addParticlesLoop(id: Int, restart: FloatArray?, expressions: Array<FloatArray>) {
        ParticlesLoop.apply(mBuffer, id, restart, expressions)
    }
    open fun addParticlesComparison(id: Int, flags: Short, min: Float, max: Float, condition: FloatArray?,
        apply1: Array<FloatArray>?, apply2: Array<FloatArray>?) {
        ParticlesCompare.apply(mBuffer, id, flags, min, max, condition, apply1, apply2)
    }
    open fun addParticleLoopEnd() { ContainerEnd.apply(mBuffer) }
    open fun defineFloatFunction(fid: Int, args: IntArray) { FloatFunctionDefine.apply(mBuffer, fid, args) }
    open fun addEndFloatFunctionDef() { ContainerEnd.apply(mBuffer) }
    open fun callFloatFunction(id: Int, args: FloatArray?) { FloatFunctionCall.apply(mBuffer, id, args) }
    open fun bitmapAttribute(id: Int, bitmapId: Int, attribute: Short) { ImageAttribute.apply(mBuffer, id, bitmapId, attribute, null) }
    open fun textAttribute(id: Int, textId: Int, attribute: Short) { TextAttribute.apply(mBuffer, id, textId, attribute) }
    open fun timeAttribute(id: Int, timeId: Int, attribute: Short, vararg args: Int) { TimeAttribute.apply(mBuffer, id, timeId, attribute, args) }
    open fun drawComponentContent() { DrawContent.apply(mBuffer) }
    open fun storeBitmap(imageId: Int, imageWidth: Int, imageHeight: Int, data: ByteArray): Int {
        BitmapData.apply(mBuffer, imageId, imageWidth, imageHeight, data); return imageId
    }
    open fun createBitmap(imageId: Int, imageWidth: Short, imageHeight: Short): Int {
        BitmapData.apply(mBuffer, imageId, BitmapData.TYPE_RAW8888, imageWidth, BitmapData.ENCODING_EMPTY, imageHeight, ByteArray(0)); return imageId
    }
    open fun drawOnBitmap(imageId: Int, mode: Int, color: Int) { DrawToBitmap.apply(mBuffer, imageId, mode, color) }
    open fun storeBitmapA8(imageId: Int, imageWidth: Int, imageHeight: Int, data: ByteArray): Int {
        BitmapData.apply(mBuffer, imageId, BitmapData.TYPE_PNG_ALPHA_8, imageWidth.toShort(), BitmapData.ENCODING_INLINE, imageHeight.toShort(), data); return imageId
    }
    open fun storeBitmapUrl(imageId: Int, url: String): Int {
        BitmapData.apply(mBuffer, imageId, BitmapData.TYPE_PNG, 1.toShort(), BitmapData.ENCODING_URL, 1.toShort(), url.encodeToByteArray()); return imageId
    }
    open fun pathCombine(id: Int, path1: Int, path2: Int, op: Byte) { PathCombine.apply(mBuffer, id, path1, path2, op) }
    open fun performHaptic(feedbackConstant: Int) { HapticFeedback.apply(mBuffer, feedbackConstant) }
    open fun addConditionalOperations(type: Byte, a: Float, b: Float) { ConditionalOperations.apply(mBuffer, type, a, b) }
    open fun endConditionalOperations() { addContainerEnd() }
    open fun addDebugMessage(textId: Int, value: Float, flags: Int) { DebugMessage.apply(mBuffer, textId, value, flags) }
    open fun getColorAttribute(id: Int, baseColor: Int, type: Short) { ColorAttribute.apply(mBuffer, id, baseColor, type) }
    open fun setMatrixFromPath(pathId: Int, fraction: Float, vOffset: Float, flags: Int) { MatrixFromPath.apply(mBuffer, pathId, fraction, vOffset, flags) }
    open fun textSubtext(id: Int, txtId: Int, start: Float, len: Float) { TextSubtext.apply(mBuffer, id, txtId, start, len) }
    open fun bitmapTextMeasure(id: Int, textId: Int, bmFontId: Int, type: Int) { BitmapTextMeasure.apply(mBuffer, id, textId, bmFontId, type) }
    open fun rem(text: String) { Rem.apply(mBuffer, text) }
    open fun setVersion(documentApiLevel: Int, profiles: Int) { mApiLevel = documentApiLevel; mProfileMask = profiles; mBuffer.setVersion(documentApiLevel, profiles) }
    open fun setVersion(documentApiLevel: Int, profileMask: Int, supportedOperations: Set<Int>) {
        mApiLevel = documentApiLevel; mProfileMask = profileMask; mBuffer.setValidOperations(supportedOperations)
    }
    open fun addMatrixConst(id: Int, values: FloatArray) { MatrixConstant.apply(mBuffer, id, 0, values) }
    open fun addMatrixExpression(id: Int, exp: FloatArray) { MatrixExpression.apply(mBuffer, id, 0, exp) }
    open fun addMatrixVectorMath(matrixId: Float, type: Short, from: FloatArray, outId: IntArray) {
        MatrixVectorMath.apply(mBuffer, type, outId, Utils.idFromNan(matrixId), from)
    }
    open fun addFont(id: Int, type: Int, data: ByteArray) { FontData.apply(mBuffer, id, type, data) }
    open fun wakeIn(seconds: Float) { WakeIn.apply(mBuffer, seconds) }
    open fun addPathExpression(id: Int, expressionX: FloatArray, expressionY: FloatArray?, start: Float, end: Float, count: Float, flags: Int) {
        PathExpression.apply(mBuffer, id, expressionX, expressionY, start, end, count, flags)
    }
    open fun addComponentVisibilityOperation(valueId: Int) { ComponentVisibilityOperation.apply(mBuffer, valueId) }
    open fun addWidthModifierOperation(type: Int, value: Float) { WidthModifierOperation.apply(mBuffer, type, value) }
    open fun addHeightModifierOperation(type: Int, value: Float) { HeightModifierOperation.apply(mBuffer, type, value) }
    open fun addHeightInModifierOperation(min: Float, max: Float) { HeightInModifierOperation.apply(mBuffer, min, max) }
    open fun addTouchDownModifierOperation() { TouchDownModifierOperation.apply(mBuffer) }
    open fun addTouchUpModifierOperation() { TouchUpModifierOperation.apply(mBuffer) }
    open fun addTouchCancelModifierOperation() { TouchCancelModifierOperation.apply(mBuffer) }
    open fun addWidthInModifierOperation(min: Float, max: Float) { WidthInModifierOperation.apply(mBuffer, min, max) }
    open fun addDrawContentOperation() { DrawContentOperation.apply(mBuffer) }
    open fun startLayoutCompute(type: Int, boundsId: Int, animateChanges: Boolean) { LayoutComputeOperation.apply(mBuffer, type, boundsId, animateChanges) }
    open fun endLayoutCompute() { ContainerEnd.apply(mBuffer) }
    open fun addSemanticsModifier(contentDescriptionId: Int, role: Byte, textId: Int, stateDescriptionId: Int, mode: Int, enabled: Boolean, clickable: Boolean) {
        CoreSemantics.apply(mBuffer, contentDescriptionId, role, textId, stateDescriptionId, mode, enabled, clickable)
    }
    open fun addClickModifierOperation() { ClickModifierOperation.apply(mBuffer) }
    open fun addCollapsiblePriorityModifier(orientation: Int, priority: Float) { CollapsiblePriorityModifierOperation.apply(mBuffer, priority, orientation) }
    open fun addAnimationSpecModifier(animationId: Int, motionDuration: Float, motionEasingType: Int,
        visibilityDuration: Float, visibilityEasingType: Int, enterAnimation: Int, exitAnimation: Int) {
        AnimationSpec.apply(mBuffer, animationId, motionDuration, motionEasingType, visibilityDuration, visibilityEasingType, enterAnimation, exitAnimation)
    }
    open fun addValueStringChangeActionOperation(destTextId: Int, srcTextId: Int) { ValueStringChangeActionOperation.apply(mBuffer, destTextId, srcTextId) }
    open fun addValueIntegerExpressionChangeActionOperation(destIntegerId: Long, srcIntegerId: Long) {
        ValueIntegerExpressionChangeActionOperation.apply(mBuffer, destIntegerId, srcIntegerId)
    }
    open fun addValueFloatChangeActionOperation(valueId: Int, value: Float) { ValueFloatChangeActionOperation.apply(mBuffer, valueId, value) }
    open fun addValueIntegerChangeActionOperation(valueId: Int, value: Int) { ValueIntegerChangeActionOperation.apply(mBuffer, valueId, value) }
    open fun addValueFloatExpressionChangeActionOperation(mValueId: Int, mValue: Int) {
        ValueFloatExpressionChangeActionOperation.apply(mBuffer, mValueId, mValue)
    }
}
