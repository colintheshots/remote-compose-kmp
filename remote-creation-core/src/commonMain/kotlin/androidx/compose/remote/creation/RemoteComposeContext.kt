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
package androidx.compose.remote.creation

import androidx.compose.remote.core.RcPlatformServices
import androidx.compose.remote.core.RemoteComposeBuffer
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.operations.BitmapFontData
import androidx.compose.remote.core.operations.DrawTextOnCircle
import androidx.compose.remote.core.operations.TouchExpression
import androidx.compose.remote.core.operations.layout.managers.BoxLayout
import androidx.compose.remote.core.operations.layout.managers.ColumnLayout
import androidx.compose.remote.core.operations.layout.managers.RowLayout
import androidx.compose.remote.core.operations.layout.managers.CoreText
import androidx.compose.remote.core.operations.paint.PaintBundle
import androidx.compose.remote.creation.actions.Action
import androidx.compose.remote.creation.modifiers.RecordingModifier
import androidx.compose.remote.creation.profile.Profile

/** Kotlin API to create a new RemoteCompose byte array */
open class RemoteComposeContext {

    lateinit var mRemoteWriter: RemoteComposeWriter

    constructor(writer: RemoteComposeWriter) { mRemoteWriter = writer }

    constructor(
        creationDisplayInfo: CreationDisplayInfo, contentDescription: String,
        profile: Profile, content: RemoteComposeContext.() -> Unit,
    ) {
        mRemoteWriter = profile.create(creationDisplayInfo, null)
        content()
    }

    constructor(
        vararg tags: RemoteComposeWriter.HTag, profile: Profile,
        content: RemoteComposeContext.() -> Unit,
    ) {
        mRemoteWriter = RemoteComposeWriter(profile, *tags)
        content()
    }

    constructor(width: Int, height: Int, contentDescription: String, profile: Profile) {
        mRemoteWriter = RemoteComposeWriter.obtain(width, height, contentDescription, profile)
    }

    constructor(width: Int, height: Int, contentDescription: String, platform: RcPlatformServices) {
        mRemoteWriter = RemoteComposeWriter(width, height, contentDescription, platform)
    }

    constructor(
        width: Int, height: Int, contentDescription: String, platform: RcPlatformServices,
        content: RemoteComposeContext.() -> Unit,
    ) {
        mRemoteWriter = RemoteComposeWriter(width, height, contentDescription, platform)
        content()
    }

    constructor(
        width: Int, height: Int, contentDescription: String,
        apiLevel: Int, profiles: Int, platform: RcPlatformServices,
        content: RemoteComposeContext.() -> Unit,
    ) {
        mRemoteWriter = RemoteComposeWriter(width, height, contentDescription, apiLevel, profiles, platform)
        content()
    }

    constructor(
        width: Int, height: Int, contentDescription: String,
        apiLevel: Int, profiles: Int, platform: RcPlatformServices,
    ) {
        mRemoteWriter = RemoteComposeWriter(width, height, contentDescription, apiLevel, profiles, platform)
    }

    constructor(
        vararg tags: RemoteComposeWriter.HTag, platform: RcPlatformServices,
        content: RemoteComposeContext.() -> Unit,
    ) {
        mRemoteWriter = RemoteComposeWriter(platform, *tags)
    }

    fun matrix(vararg exp: Float): Matrix = Matrix(this, *exp)

    val buffer: RemoteComposeBuffer get() = mRemoteWriter.getBuffer()
    val writer: RemoteComposeWriter get() = mRemoteWriter

    val TIME_IN_SEC: Float = RemoteContext.FLOAT_CONTINUOUS_SEC
    val FONT_TYPE_DEFAULT: Int = PaintBundle.FONT_TYPE_DEFAULT
    val FONT_TYPE_SANS_SERIF: Int = PaintBundle.FONT_TYPE_SANS_SERIF
    val FONT_TYPE_SERIF: Int = PaintBundle.FONT_TYPE_SERIF
    val FONT_TYPE_MONOSPACE: Int = PaintBundle.FONT_TYPE_MONOSPACE

    val Modifier: RecordingModifier get() = RecordingModifier()

    fun column(
        modifier: RecordingModifier = Modifier, horizontal: Int = ColumnLayout.START,
        vertical: Int = ColumnLayout.TOP, content: RemoteComposeContext.() -> Unit,
    ) { mRemoteWriter.column(modifier, horizontal, vertical) { content() } }

    fun row(
        modifier: RecordingModifier = Modifier, horizontal: Int = RowLayout.START,
        vertical: Int = RowLayout.TOP, content: RemoteComposeContext.() -> Unit,
    ) { mRemoteWriter.row(modifier, horizontal, vertical) { content() } }

    fun box(
        modifier: RecordingModifier = Modifier, horizontal: Int = BoxLayout.START,
        vertical: Int = BoxLayout.TOP, content: RemoteComposeContext.() -> Unit,
    ) { mRemoteWriter.box(modifier, horizontal, vertical) { content() } }

    // Delegate methods
    fun areFloatExpressionOperationsValid(f: Float) = mRemoteWriter.areFloatExpressionOperationsValid(f)
    fun validateOps(ops: FloatArray) = mRemoteWriter.validateOps(ops)
    fun reset() = mRemoteWriter.reset()
    fun header(width: Int, height: Int, contentDescription: String?, density: Float, capabilities: Long) =
        mRemoteWriter.header(width, height, contentDescription, density, capabilities)
    fun pathCombine(path1: Int, path2: Int, op: Byte) = mRemoteWriter.pathCombine(path1, path2, op)
    fun performHaptic(feedbackConstant: Int) = mRemoteWriter.performHaptic(feedbackConstant)
    fun getColorAttribute(baseColor: Int, type: Short) = mRemoteWriter.getColorAttribute(baseColor, type)
    fun addAction(vararg actions: Action?) = mRemoteWriter.addAction(*actions)
    fun textSubtext(txtId: Int, start: Float, len: Float) = mRemoteWriter.textSubtext(txtId, start, len)
    fun bitmapTextMeasure(textId: Int, bmFontId: Int, measureWidth: Int) = mRemoteWriter.bitmapTextMeasure(textId, bmFontId, measureWidth)
    fun MatrixMultiply(matrixId: Float, from: FloatArray?, out: FloatArray) = mRemoteWriter.addMatrixMultiply(matrixId, from, out)
    fun MatrixMultiply(matrixId: Float, type: Short, from: FloatArray?, out: FloatArray) = mRemoteWriter.addMatrixMultiply(matrixId, type, from, out)
    fun checkAndClearForceSendingNewPaint() = mRemoteWriter.checkAndClearForceSendingNewPaint()
    fun buffer() = mRemoteWriter.buffer()
    fun bufferSize() = mRemoteWriter.bufferSize()
    fun createShader(shaderString: String) = mRemoteWriter.createShader(shaderString)
    fun setTheme(theme: Int) = mRemoteWriter.setTheme(theme)
    fun drawBitmap(image: Any, width: Int, height: Int, contentDescription: String) = mRemoteWriter.drawBitmap(image, width, height, contentDescription)
    fun setRootContentBehavior(scroll: Int, alignment: Int, sizing: Int, mode: Int) = mRemoteWriter.setRootContentBehavior(scroll, alignment, sizing, mode)
    fun addClickArea(id: Int, contentDescription: String?, left: Float, top: Float, right: Float, bottom: Float, metadata: String?) =
        mRemoteWriter.addClickArea(id, contentDescription, left, top, right, bottom, metadata)
    fun drawArc(left: Float, top: Float, right: Float, bottom: Float, startAngle: Float, sweepAngle: Float) =
        mRemoteWriter.drawArc(left, top, right, bottom, startAngle, sweepAngle)
    fun drawSector(left: Float, top: Float, right: Float, bottom: Float, startAngle: Float, sweepAngle: Float) =
        mRemoteWriter.drawSector(left, top, right, bottom, startAngle, sweepAngle)
    fun drawBitmap(image: Any, left: Float, top: Float, right: Float, bottom: Float, contentDescription: String?) =
        mRemoteWriter.drawBitmap(image, left, top, right, bottom, contentDescription)
    fun drawBitmap(imageId: Int, left: Float, top: Float, right: Float, bottom: Float, contentDescription: String?) =
        mRemoteWriter.drawBitmap(imageId, left, top, right, bottom, contentDescription)
    fun drawBitmap(imageId: Int, left: Float, top: Float, contentDescription: String?) =
        mRemoteWriter.drawBitmap(imageId, left, top, contentDescription)
    fun drawScaledBitmap(image: Any, srcLeft: Float, srcTop: Float, srcRight: Float, srcBottom: Float, dstLeft: Float, dstTop: Float, dstRight: Float, dstBottom: Float, scaleType: Int, scaleFactor: Float, contentDescription: String?) =
        mRemoteWriter.drawScaledBitmap(image, srcLeft, srcTop, srcRight, srcBottom, dstLeft, dstTop, dstRight, dstBottom, scaleType, scaleFactor, contentDescription)
    fun drawScaledBitmap(imageId: Int, srcLeft: Float, srcTop: Float, srcRight: Float, srcBottom: Float, dstLeft: Float, dstTop: Float, dstRight: Float, dstBottom: Float, scaleType: Int, scaleFactor: Float, contentDescription: String?) =
        mRemoteWriter.drawScaledBitmap(imageId, srcLeft, srcTop, srcRight, srcBottom, dstLeft, dstTop, dstRight, dstBottom, scaleType, scaleFactor, contentDescription)
    fun drawCircle(centerX: Float, centerY: Float, radius: Float) = mRemoteWriter.drawCircle(centerX, centerY, radius)
    fun drawLine(x1: Float, y1: Float, x2: Float, y2: Float) = mRemoteWriter.drawLine(x1, y1, x2, y2)
    fun drawOval(left: Float, top: Float, right: Float, bottom: Float) = mRemoteWriter.drawOval(left, top, right, bottom)
    fun drawPath(path: Any) = mRemoteWriter.drawPath(path)
    fun drawPath(pathId: Int) = mRemoteWriter.drawPath(pathId)
    fun drawRect(left: Float, top: Float, right: Float, bottom: Float) = mRemoteWriter.drawRect(left, top, right, bottom)
    fun drawRoundRect(left: Float, top: Float, right: Float, bottom: Float, radiusX: Float, radiusY: Float) =
        mRemoteWriter.drawRoundRect(left, top, right, bottom, radiusX, radiusY)
    fun textCreateId(text: String) = mRemoteWriter.textCreateId(text)
    fun textMerge(id1: Int, id2: Int) = mRemoteWriter.textMerge(id1, id2)
    fun textMerge(vararg id: Int): Int { var ret = id[0]; for (i in id.drop(1)) ret = mRemoteWriter.textMerge(ret, i); return ret }
    fun drawTextOnPath(text: String, path: Any, hOffset: Float, vOffset: Float) = mRemoteWriter.drawTextOnPath(text, path, hOffset, vOffset)
    fun drawTextOnPath(textId: Int, path: Any, hOffset: Float, vOffset: Float) = mRemoteWriter.drawTextOnPath(textId, path, hOffset, vOffset)
    fun drawTextOnCircle(textId: Int, centerX: Float, centerY: Float, radius: Float, startAngle: Float, warpRadiusOffset: Float, alignment: DrawTextOnCircle.Alignment, placement: DrawTextOnCircle.Placement) =
        mRemoteWriter.drawTextOnCircle(textId, centerX, centerY, radius, startAngle, warpRadiusOffset, alignment, placement)
    fun drawTextRun(text: String, start: Int, end: Int, contextStart: Int, contextEnd: Int, x: Float, y: Float, rtl: Boolean) =
        mRemoteWriter.drawTextRun(text, start, end, contextStart, contextEnd, x, y, rtl)
    fun drawTextRun(textId: Int, start: Int, end: Int, contextStart: Int, contextEnd: Int, x: Float, y: Float, rtl: Boolean) =
        mRemoteWriter.drawTextRun(textId, start, end, contextStart, contextEnd, x, y, rtl)
    fun drawBitmapFontTextRun(textId: Int, bitmapFontId: Int, start: Int, end: Int, x: Float, y: Float) =
        mRemoteWriter.drawBitmapFontTextRun(textId, bitmapFontId, start, end, x, y)
    fun drawTextAnchored(str: String, x: Float, y: Float, panX: Float, panY: Float, flags: Int) = mRemoteWriter.drawTextAnchored(str, x, y, panX, panY, flags)
    fun drawTextAnchored(strId: Int, x: Float, y: Float, panX: Float, panY: Float, flags: Int) = mRemoteWriter.drawTextAnchored(strId, x, y, panX, panY, flags)
    fun drawTweenPath(path1: Any, path2: Any, tween: Float, start: Float, stop: Float) = mRemoteWriter.drawTweenPath(path1, path2, tween, start, stop)
    fun drawBitmapTextAnchored(text: String, bitmapFontId: Int, start: Float, end: Float, x: Float, y: Float, panX: Float, panY: Float) =
        mRemoteWriter.drawBitmapTextAnchored(text, bitmapFontId, start, end, x, y, panX, panY)
    fun drawBitmapTextAnchored(textId: Int, bitmapFontId: Int, start: Float, end: Float, x: Float, y: Float, panX: Float, panY: Float) =
        mRemoteWriter.drawBitmapTextAnchored(textId, bitmapFontId, start, end, x, y, panX, panY)
    fun drawTweenPath(path1Id: Int, path2Id: Int, tween: Float, start: Float, stop: Float) = mRemoteWriter.drawTweenPath(path1Id, path2Id, tween, start, stop)
    fun addPathData(path: Any) = mRemoteWriter.addPathData(path)
    fun pathTween(pid1: Int, pid2: Int, tween: Float) = mRemoteWriter.pathTween(pid1, pid2, tween)
    fun pathCreate(x: Float, y: Float) = mRemoteWriter.pathCreate(x, y)
    fun pathAppend(pathId: Int, vararg path: Float) = mRemoteWriter.pathAppend(pathId, *path)
    fun pathAppendLineTo(pathId: Int, x: Float, y: Float) = mRemoteWriter.pathAppendLineTo(pathId, x, y)
    fun pathAppendQuadTo(pathId: Int, x1: Float, y1: Float, x2: Float, y2: Float) = mRemoteWriter.pathAppendQuadTo(pathId, x1, y1, x2, y2)
    fun pathAppendMoveTo(pathId: Int, x: Float, y: Float) = mRemoteWriter.pathAppendMoveTo(pathId, x, y)
    fun pathAppendClose(pathId: Int) = mRemoteWriter.pathAppendClose(pathId)
    fun pathAppendReset(pathId: Int) = mRemoteWriter.pathAppendReset(pathId)
    fun addPathString(path: String) = mRemoteWriter.addPathString(path)
    fun rFun(f: (RFloat) -> RFloat): RFloat = f.invoke(rf(Rc.FloatExpression.VAR1))
    fun addPathExpression(expressionX: RFloat, expressionY: RFloat, start: Number, end: Number, count: Number, flags: Int = 0): Int =
        mRemoteWriter.addPathExpression(expressionX.array, expressionY.array, start.toFloat(), end.toFloat(), count.toFloat(), flags)
    fun addPolarPathExpression(expressionR: RFloat, start: Number, end: Number, count: Number, centerX: Number, centerY: Number, flags: Int = 0): Int =
        mRemoteWriter.addPolarPathExpression(expressionR.array, start.toFloat(), end.toFloat(), count.toFloat(), centerX.toFloat(), centerY.toFloat(), flags)
    fun skew(skewX: Float, skewY: Float) = mRemoteWriter.skew(skewX, skewY)
    fun rotate(angle: Float, centerX: Float, centerY: Float) = mRemoteWriter.rotate(angle, centerX, centerY)
    fun rotate(angle: Float) = mRemoteWriter.rotate(angle)
    fun matrixFromPath(pathId: Int, fraction: Float, vOffset: Float, flags: Int) = mRemoteWriter.matrixFromPath(pathId, fraction, vOffset, flags)
    fun save() = mRemoteWriter.save()
    fun restore() = mRemoteWriter.restore()
    fun translate(dx: Float, dy: Float) = mRemoteWriter.translate(dx, dy)
    fun scale(scaleX: Float, scaleY: Float, centerX: Float, centerY: Float) = mRemoteWriter.scale(scaleX, scaleY, centerX, centerY)
    fun scale(scaleX: Float, scaleY: Float) = mRemoteWriter.scale(scaleX, scaleY)
    fun addClipPath(pathId: Int) = mRemoteWriter.addClipPath(pathId)
    fun clipRect(left: Float, top: Float, right: Float, bottom: Float) = mRemoteWriter.clipRect(left, top, right, bottom)
    fun addFloatConstant(value: Float) = mRemoteWriter.addFloatConstant(value)
    fun reserveFloatVariable() = mRemoteWriter.reserveFloatVariable()
    fun addComponentWidthValue() = mRemoteWriter.addComponentWidthValue()
    fun addComponentHeightValue() = mRemoteWriter.addComponentHeightValue()
    fun addColor(color: Int) = mRemoteWriter.addColor(color)
    fun addNamedColor(name: String, color: Int) = mRemoteWriter.addNamedColor(name, color)
    fun setColorName(id: Int, name: String) = mRemoteWriter.setColorName(id, name)
    fun setStringName(id: Int, name: String) = mRemoteWriter.setStringName(id, name)
    fun addNamedString(name: String, initialValue: String) = mRemoteWriter.addNamedString(name, initialValue)
    fun addNamedInt(name: String, initialValue: Int) = mRemoteWriter.addNamedInt(name, initialValue)
    fun addNamedFloat(name: String, initialValue: Float) = mRemoteWriter.addNamedFloat(name, initialValue)
    fun addNamedBitmap(name: String, initialValue: Any) = mRemoteWriter.addNamedBitmap(name, initialValue)
    fun addNamedLong(name: String, initialValue: Long) = mRemoteWriter.addNamedLong(name, initialValue)
    fun addColorExpression(color1: Int, color2: Int, tween: Float) = mRemoteWriter.addColorExpression(color1, color2, tween)
    fun addColorExpression(colorId1: Short, color2: Int, tween: Float) = mRemoteWriter.addColorExpression(colorId1, color2, tween)
    fun addColorExpression(color1: Int, colorId2: Short, tween: Float) = mRemoteWriter.addColorExpression(color1, colorId2, tween)
    fun addColorExpression(colorId1: Short, colorId2: Short, tween: Float) = mRemoteWriter.addColorExpression(colorId1, colorId2, tween)
    fun addColorExpression(hue: Float, sat: Float, value: Float) = mRemoteWriter.addColorExpression(hue, sat, value)
    fun addColorExpression(alpha: Int, hue: Float, sat: Float, value: Float) = mRemoteWriter.addColorExpression(alpha, hue, sat, value)
    fun addColorExpression(alpha: Float, red: Float, green: Float, blue: Float) = mRemoteWriter.addColorExpression(alpha, red, green, blue)
    fun floatExpression(vararg value: Float) = mRemoteWriter.floatExpression(*value)
    fun floatExpression(value: FloatArray, animation: FloatArray) = mRemoteWriter.floatExpression(value, animation)
    fun addInteger(value: Int) = mRemoteWriter.addInteger(value)
    fun addLong(value: Long) = mRemoteWriter.addLong(value)
    fun addBoolean(value: Boolean) = mRemoteWriter.addBoolean(value)
    fun mapLookup(mapId: Int, str: String) = mRemoteWriter.mapLookup(mapId, str)
    fun mapLookup(mapId: Int, strId: Int) = mRemoteWriter.mapLookup(mapId, strId)
    fun addText(text: String) = mRemoteWriter.addText(text)
    fun textMeasure(textId: Int, mode: Int) = mRemoteWriter.textMeasure(textId, mode)
    fun textLength(textId: Int) = mRemoteWriter.textLength(textId)
    fun easing(maxTime: Float, maxAcceleration: Float, maxVelocity: Float) = mRemoteWriter.easing(maxTime, maxAcceleration, maxVelocity)
    fun touchExpression(vararg exp: Float, defValue: Float = 0f, min: Float = 0f, max: Float = 10f, touchMode: Int = TouchExpression.STOP_GENTLY, velocityId: Float = 0f, touchEffects: Int = 0, touchSpec: FloatArray? = null, easingSpec: FloatArray? = null) =
        mRemoteWriter.addTouch(defValue, min, max, touchMode, velocityId, touchEffects, touchSpec, easingSpec, *exp)
    fun addTouch(defValue: Float, min: Float, max: Float, touchMode: Int, velocityId: Float, touchEffects: Int, touchSpec: FloatArray?, easingSpec: FloatArray?, vararg exp: Float) =
        mRemoteWriter.addTouch(defValue, min, max, touchMode, velocityId, touchEffects, touchSpec, easingSpec, *exp)
    fun spring(stiffness: Float, damping: Float, stopThreshold: Float, boundaryMode: Int) = mRemoteWriter.spring(stiffness, damping, stopThreshold, boundaryMode)
    fun bitmapAttribute(bitmapId: Int, attribute: Short) = mRemoteWriter.bitmapAttribute(bitmapId, attribute)
    fun textAttribute(textId: Int, attribute: Short) = mRemoteWriter.textAttribute(textId, attribute)
    fun addDataMap(vararg data: RemoteComposeWriter.DataMap?) = mRemoteWriter.addDataMap(*data)
    fun addDataMap(keys: Array<String>, ids: IntArray) = mRemoteWriter.addDataMap(keys, ids)
    fun integerExpression(mask: Int, vararg value: Int) = mRemoteWriter.integerExpression(mask, *value)
    fun integerExpression(vararg v: Long) = mRemoteWriter.integerExpression(*v)
    fun asFloatId(id: Long) = mRemoteWriter.asFloatId(id)
    fun timeAttribute(longID: Int, type: Short, vararg args: Int) = mRemoteWriter.timeAttribute(longID, type, *args)
    fun exp(vararg value: Float) = mRemoteWriter.exp(*value)
    fun anim(duration: Float, type: Int, spec: FloatArray?, initialValue: Float, wrap: Float) = mRemoteWriter.anim(duration, type, spec, initialValue, wrap)
    fun anim(duration: Float, type: Int, spec: FloatArray?, initialValue: Float) = mRemoteWriter.anim(duration, type, spec, initialValue)
    fun anim(duration: Float, type: Int, spec: FloatArray?) = mRemoteWriter.anim(duration, type, spec)
    fun anim(duration: Float, type: Int) = mRemoteWriter.anim(duration, type)
    fun anim(duration: Float) = mRemoteWriter.anim(duration)
    fun textLookup(arrayId: Float, index: Float) = mRemoteWriter.textLookup(arrayId, index)
    fun textLookup(arrayId: Float, indexId: Int) = mRemoteWriter.textLookup(arrayId, indexId)
    fun createTextFromFloat(value: Float, before: Int, after: Int, flags: Int) = mRemoteWriter.createTextFromFloat(value, before, after, flags)
    fun createID(type: Int) = mRemoteWriter.createID(type)
    fun nextId() = mRemoteWriter.nextId()
    fun root(content: RemoteComposeWriterInterface) = mRemoteWriter.root(content)
    fun startLoop(indexId: Int, from: Float, step: Float, until: Float) = mRemoteWriter.startLoop(indexId, from, step, until)
    fun startLoopVar(from: Float, step: Float, until: Float) = mRemoteWriter.startLoopVar(from, step, until)
    fun startLoop(count: Float) = mRemoteWriter.startLoop(count)
    fun endLoop() = mRemoteWriter.endLoop()
    fun loop(indexId: Int, from: Float, step: Float, until: Float, content: RemoteComposeWriterInterface) = mRemoteWriter.loop(indexId, from, step, until, content)
    fun loop(indexId: Int, from: Int, step: Int, until: Int, content: RemoteComposeWriterInterface) = mRemoteWriter.loop(indexId, from, step, until, content)
    fun conditionalOperations(type: Byte, a: Float, b: Float, content: RemoteComposeWriterInterface) = mRemoteWriter.conditionalOperations(type, a, b, content)
    fun conditionalOperations(type: Byte, a: Float, b: Float) = mRemoteWriter.conditionalOperations(type, a, b)
    fun endConditionalOperations() = mRemoteWriter.endConditionalOperations()
    fun column(modifier: RecordingModifier, horizontal: Int, vertical: Int, content: RemoteComposeWriterInterface) = mRemoteWriter.column(modifier, horizontal, vertical, content)
    fun startColumn(modifier: RecordingModifier, horizontal: Int, vertical: Int) = mRemoteWriter.startColumn(modifier, horizontal, vertical)
    fun endColumn() = mRemoteWriter.endColumn()
    fun collapsibleColumn(modifier: RecordingModifier, horizontal: Int, vertical: Int, content: RemoteComposeWriterInterface) = mRemoteWriter.collapsibleColumn(modifier, horizontal, vertical, content)
    fun startCollapsibleColumn(modifier: RecordingModifier, horizontal: Int, vertical: Int) = mRemoteWriter.startCollapsibleColumn(modifier, horizontal, vertical)
    fun endCollapsibleColumn() = mRemoteWriter.endCollapsibleColumn()
    fun row(modifier: RecordingModifier, horizontal: Int, vertical: Int, content: RemoteComposeWriterInterface) = mRemoteWriter.row(modifier, horizontal, vertical, content)
    fun startRow(modifier: RecordingModifier, horizontal: Int, vertical: Int) = mRemoteWriter.startRow(modifier, horizontal, vertical)
    fun endRow() = mRemoteWriter.endRow()
    fun collapsibleRow(modifier: RecordingModifier, horizontal: Int, vertical: Int, content: RemoteComposeWriterInterface) = mRemoteWriter.collapsibleRow(modifier, horizontal, vertical, content)
    fun startCollapsibleRow(modifier: RecordingModifier, horizontal: Int, vertical: Int) = mRemoteWriter.startCollapsibleRow(modifier, horizontal, vertical)
    fun endCollapsibleRow() = mRemoteWriter.endCollapsibleRow()
    fun canvas(modifier: RecordingModifier, content: RemoteComposeWriterInterface) = mRemoteWriter.canvas(modifier, content)
    fun drawComponentContent() = mRemoteWriter.drawComponentContent()
    fun startCanvas(modifier: RecordingModifier) = mRemoteWriter.startCanvas(modifier)
    fun endCanvas() = mRemoteWriter.endCanvas()
    fun startCanvasOperations() = mRemoteWriter.startCanvasOperations()
    fun endCanvasOperations() = mRemoteWriter.endCanvasOperations()
    fun startRunActions() = mRemoteWriter.startRunActions()
    fun endRunActions() = mRemoteWriter.endRunActions()
    fun box(modifier: RecordingModifier, horizontal: Int, vertical: Int, content: RemoteComposeWriterInterface) = mRemoteWriter.box(modifier, horizontal, vertical, content)
    fun startBox(modifier: RecordingModifier, horizontal: Int, vertical: Int) = mRemoteWriter.startBox(modifier, horizontal, vertical)
    fun startBox(modifier: RecordingModifier) = mRemoteWriter.startBox(modifier)
    fun endBox() = mRemoteWriter.endBox()
    fun startFitBox(modifier: RecordingModifier, horizontal: Int, vertical: Int) = mRemoteWriter.startFitBox(modifier, horizontal, vertical)
    fun endFitBox() = mRemoteWriter.endFitBox()
    fun image(modifier: RecordingModifier, imageId: Int, scaleType: Int, alpha: Float) = mRemoteWriter.image(modifier, imageId, scaleType, alpha)
    fun stateLayout(modifier: RecordingModifier, indexId: Int, content: RemoteComposeWriterInterface) = mRemoteWriter.stateLayout(modifier, indexId, content)
    fun startStateLayout(modifier: RecordingModifier, indexId: Int) = mRemoteWriter.startStateLayout(modifier, indexId)
    fun endStateLayout() = mRemoteWriter.endStateLayout()
    fun addModifierScroll(direction: Int, positionId: Float) = mRemoteWriter.addModifierScroll(direction, positionId)
    fun addModifierScroll(direction: Int, positionId: Float, notches: Int) = mRemoteWriter.addModifierScroll(direction, positionId, notches)
    fun addModifierScroll(direction: Int) = mRemoteWriter.addModifierScroll(direction)
    fun textComponent(modifier: RecordingModifier, textId: Int, color: Int, fontSize: Float, fontStyle: Int, fontWeight: Float, fontFamily: String?, textAlign: Int, overflow: Int, maxLines: Int, content: RemoteComposeWriterInterface) =
        mRemoteWriter.textComponent(modifier, textId, color, fontSize, fontStyle, fontWeight, fontFamily, textAlign, overflow, maxLines, content)
    fun startTextComponent(modifier: RecordingModifier, textId: Int, color: Int, colorId: Int, fontSize: Float, fontStyle: Int, fontWeight: Float, fontFamily: String?, textAlign: Int, overflow: Int, maxLines: Int, letterSpacing: Float, lineHeightAdd: Float, lineHeightMultiplier: Float, lineBreakStrategy: Int, hyphenationFrequency: Int, justificationMode: Int, underline: Boolean, strikethrough: Boolean, fontAxis: Array<String>?, fontAxisValues: FloatArray?, autosize: Boolean, flags: Int) =
        mRemoteWriter.startTextComponent(modifier, textId, color, colorId, fontSize, fontStyle, fontWeight, fontFamily, textAlign, overflow, maxLines, letterSpacing, lineHeightAdd, lineHeightMultiplier, lineBreakStrategy, hyphenationFrequency, justificationMode, underline, strikethrough, fontAxis, fontAxisValues, autosize, flags)
    fun endTextComponent() = mRemoteWriter.endTextComponent()
    fun box(modifier: RecordingModifier, horizontal: Int, vertical: Int) = mRemoteWriter.box(modifier, horizontal, vertical)
    fun box(modifier: RecordingModifier) = mRemoteWriter.box(modifier)
    fun addStringList(vararg strs: String?) = mRemoteWriter.addStringList(*strs)
    fun addStringList(vararg strIds: Int) = mRemoteWriter.addStringList(*strIds)
    fun addList(listId: IntArray) = mRemoteWriter.addList(listId)
    fun addFloatArray(values: FloatArray) = mRemoteWriter.addFloatArray(values)
    fun addFloatList(values: FloatArray) = mRemoteWriter.addFloatList(values)
    fun addFloatMap(keys: Array<String>, values: FloatArray) = mRemoteWriter.addFloatMap(keys, values)
    fun storeBitmap(image: Any) = mRemoteWriter.storeBitmap(image)
    fun addBitmap(image: Any) = mRemoteWriter.addBitmap(image)
    fun addBitmap(image: Any, name: String) = mRemoteWriter.addBitmap(image, name)
    fun addBitmapFont(glyphs: Array<BitmapFontData.Glyph>) = mRemoteWriter.addBitmapFont(glyphs)
    fun nameBitmapId(id: Int, omicron: String) = mRemoteWriter.nameBitmapId(id, omicron)
    fun createFloatId() = mRemoteWriter.createFloatId()
    fun impulse(duration: Float, start: Float) = mRemoteWriter.impulse(duration, start)
    fun impulse(duration: Float, start: Float, run: () -> Unit) = mRemoteWriter.impulse(duration, start, run)
    fun impulseProcess(run: () -> Unit) = mRemoteWriter.impulseProcess(run)
    fun impulseProcess() = mRemoteWriter.impulseProcess()
    fun impulseEnd() = mRemoteWriter.impulseEnd()
    fun createParticles(variables: FloatArray, initialExpressions: Array<FloatArray>, particleCount: Int) =
        mRemoteWriter.createParticles(variables, initialExpressions, particleCount)
    fun particlesLoop(id: Float, restart: FloatArray?, expressions: Array<FloatArray>, r: () -> Unit) =
        mRemoteWriter.particlesLoop(id, restart, expressions, r)
    fun createFloatFunction(args: FloatArray) = mRemoteWriter.createFloatFunction(args)
    fun endFloatFunction() = mRemoteWriter.endFloatFunction()
    fun callFloatFunction(id: Int, vararg args: Float) = mRemoteWriter.callFloatFunction(id, *args)
    fun addTimeLong(time: Long) = mRemoteWriter.addTimeLong(time)
    fun addDebugMessage(message: String) = mRemoteWriter.addDebugMessage(message)
    fun addDebugMessage(message: String, value: RFloat) = mRemoteWriter.addDebugMessage(message, value.toFloat())
    fun addDebugMessage(message: String, value: Float) = mRemoteWriter.addDebugMessage(message, value)
    fun addDebugMessage(message: String, value: Float, flag: Int) = mRemoteWriter.addDebugMessage(message, value, flag)
    fun addDebugMessage(textId: Int, value: Float, flag: Int) = mRemoteWriter.addDebugMessage(textId, value, flag)
    fun matrixExpression(vararg exp: Float) = mRemoteWriter.matrixExpression(*exp)
    fun addFont(data: ByteArray) = mRemoteWriter.addFont(data)

    fun Hour(): RFloat = mRemoteWriter.Hour()
    fun Minutes(): RFloat = mRemoteWriter.Minutes()
    fun Seconds(): RFloat = mRemoteWriter.Seconds()
    fun ContinuousSec(): RFloat = mRemoteWriter.ContinuousSec()
    fun UtcOffset(): RFloat = mRemoteWriter.UtcOffset()
    fun DayOfWeek(): RFloat = mRemoteWriter.DayOfWeek()
    fun Month(): RFloat = mRemoteWriter.Month()
    fun DayOfMonth(): RFloat = mRemoteWriter.DayOfMonth()
    fun ComponentWidth(): RFloat = mRemoteWriter.ComponentWidth()
    fun ComponentHeight(): RFloat = mRemoteWriter.ComponentHeight()
    fun rand(): RFloat = mRemoteWriter.rand()
    fun index(): RFloat = mRemoteWriter.index()
    fun animationTime(): RFloat = mRemoteWriter.animationTime()
    fun deltaTime(): RFloat = mRemoteWriter.deltaTime()
    fun rf(vararg elements: Float): RFloat = mRemoteWriter.rf(*elements)
    fun rf(v: Number): RFloat = mRemoteWriter.rf(v)
    fun windowWidth(): RFloat = mRemoteWriter.windowWidth()
    fun windowHeight(): RFloat = mRemoteWriter.windowHeight()

    fun text(
        stringId: Int, modifier: RecordingModifier = RecordingModifier(),
        color: Int = 0xFF000000.toInt(), fontSize: Float = 36f, fontStyle: Int = 0,
        fontWeight: Float = 400f, fontFamily: String? = null,
        textAlign: Int = CoreText.TEXT_ALIGN_LEFT, overflow: Int = CoreText.OVERFLOW_CLIP,
        maxLines: Int = Int.MAX_VALUE,
    ) {
        mRemoteWriter.textComponent(modifier, stringId, color, fontSize, fontStyle, fontWeight, fontFamily, textAlign, overflow, maxLines) {}
    }

    fun text(
        string: String, modifier: RecordingModifier = RecordingModifier(),
        color: Int = 0xFF000000.toInt(), colorId: Int = -1, fontSize: Float = 36f,
        fontStyle: Int = 0, fontWeight: Float = 400f, fontFamily: String? = null,
        textAlign: Int = CoreText.TEXT_ALIGN_LEFT, overflow: Int = CoreText.OVERFLOW_CLIP,
        maxLines: Int = Int.MAX_VALUE, letterSpacing: Float = 0.0f,
        lineHeightAdd: Float = 0.0f, lineHeightMultiplier: Float = 1.0f,
        lineBreakStrategy: Int = 0, hyphenationFrequency: Int = 0, justificationMode: Int = 0,
        underline: Boolean = false, strikethrough: Boolean = false, autosize: Boolean = false,
        fontAxis: List<Pair<String, Float>>? = null,
    ) {
        val textId = mRemoteWriter.textCreateId(string)
        text(textId, modifier, color, colorId, fontSize, fontStyle, fontWeight, fontFamily,
            textAlign, overflow, maxLines, letterSpacing, lineHeightAdd, lineHeightMultiplier,
            lineBreakStrategy, hyphenationFrequency, justificationMode, underline, strikethrough,
            autosize, fontAxis)
    }

    fun text(
        textId: Int, modifier: RecordingModifier = RecordingModifier(),
        color: Int = 0xFF000000.toInt(), colorId: Int = 0, fontSize: Float = 36f,
        fontStyle: Int = 0, fontWeight: Float = 400f, fontFamily: String? = null,
        textAlign: Int = CoreText.TEXT_ALIGN_LEFT, overflow: Int = CoreText.OVERFLOW_CLIP,
        maxLines: Int = Int.MAX_VALUE, letterSpacing: Float = 0.0f,
        lineHeightAdd: Float = 0.0f, lineHeightMultiplier: Float = 1.0f,
        lineBreakStrategy: Int = 0, hyphenationFrequency: Int = 0, justificationMode: Int = 0,
        underline: Boolean = false, strikethrough: Boolean = false, autosize: Boolean = false,
        fontAxis: List<Pair<String, Float>>? = null, flags: Int = 0,
    ) {
        var explicitStringArray: Array<String>? = null
        var explicitFloatArray: FloatArray? = null
        if (fontAxis != null) {
            explicitStringArray = fontAxis.map { it.first }.toTypedArray()
            explicitFloatArray = fontAxis.map { it.second }.toFloatArray()
        }
        mRemoteWriter.textComponent(modifier, textId, color, colorId, fontSize, fontStyle,
            fontWeight, fontFamily, textAlign, overflow, maxLines, letterSpacing, lineHeightAdd,
            lineHeightMultiplier, lineBreakStrategy, hyphenationFrequency, justificationMode,
            underline, strikethrough, explicitStringArray, explicitFloatArray, autosize, flags) {}
    }
}

fun RemoteComposeWriter.particlesLoops(
    id: Float, restart: RFloat?, rexpressions: Array<Number>, r: () -> Unit,
) {
    val expressions = Array(rexpressions.size) {
        val v = rexpressions[it]
        if (v is RFloat) v.array else floatArrayOf(v.toFloat())
    }
    this.particlesLoop(id, restart?.array, expressions, r)
}

operator fun <RFloat> Array<RFloat>.component6(): RFloat = this[5]
operator fun <RFloat> Array<RFloat>.component7(): RFloat = this[6]
