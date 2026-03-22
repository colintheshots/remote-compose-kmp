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
package androidx.compose.remote.creation

import androidx.compose.remote.core.CoreDocument
import androidx.compose.remote.core.RcPlatformServices
import androidx.compose.remote.core.RemoteComposeBuffer
import androidx.compose.remote.core.RemoteComposeState
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.operations.BitmapFontData
import androidx.compose.remote.core.operations.DataMapIds
import androidx.compose.remote.core.operations.DrawTextOnCircle
import androidx.compose.remote.core.operations.FloatConstant
import androidx.compose.remote.core.operations.Header
import androidx.compose.remote.core.operations.NamedVariable
import androidx.compose.remote.core.RemotePathBase
import androidx.compose.remote.core.operations.PathCombine
import androidx.compose.remote.core.operations.TextData
import androidx.compose.remote.core.operations.TextLength
import androidx.compose.remote.core.operations.TouchExpression
import androidx.compose.remote.core.operations.Utils
import androidx.compose.remote.core.operations.layout.managers.BoxLayout
import androidx.compose.remote.core.operations.layout.modifiers.LayoutComputeOperation.Companion.TYPE_MEASURE
import androidx.compose.remote.core.operations.layout.modifiers.LayoutComputeOperation.Companion.TYPE_POSITION
import androidx.compose.remote.core.operations.layout.modifiers.ScrollModifierOperation
import androidx.compose.remote.core.operations.paint.PaintBundle
import androidx.compose.remote.core.operations.utilities.AnimatedFloatExpression
import androidx.compose.remote.core.operations.utilities.AnimatedFloatExpression.Companion.A_DEREF
import androidx.compose.remote.core.operations.utilities.AnimatedFloatExpression.Companion.MUL
import androidx.compose.remote.core.operations.utilities.AnimatedFloatExpression.Companion.toMathName
import androidx.compose.remote.core.operations.utilities.ImageScaling
import androidx.compose.remote.core.operations.utilities.IntegerExpressionEvaluator
import androidx.compose.remote.core.operations.utilities.NanMap
import androidx.compose.remote.core.types.IntegerConstant
import androidx.compose.remote.core.types.LongConstant
import androidx.compose.remote.creation.actions.Action
import androidx.compose.remote.creation.modifiers.ComponentLayoutChanges
import androidx.compose.remote.creation.modifiers.ComponentLayoutChangesWriter
import androidx.compose.remote.creation.modifiers.RecordingModifier
import androidx.compose.remote.creation.profile.Profile

open class RemoteComposeWriter {
    internal var mBuffer: RemoteComposeBuffer
    internal var mState: RemoteComposeState = RemoteComposeState()
    protected var mPlatform: RcPlatformServices
    private var mOriginalWidth: Int = 0
    private var mOriginalHeight: Int = 0
    private var mContentDescription: String? = null
    protected var mHasForceSendingNewPaint: Boolean = false

    protected val mPainter: RcPaint = RcPaint(this)

    private var mWriterCallback: Any? = null
    private var mInsertPoint: Int = -1
    private var mStartGlobalSection: Int = -1

    val rcPaint: RcPaint get() = mPainter
    val writerCallback: Any? get() = mWriterCallback

    constructor(
        creationDisplayInfo: CreationDisplayInfo,
        contentDescription: String?,
        profile: Profile,
        writerCallback: Any?,
    ) : this(
        profile,
        hTag(Header.DOC_WIDTH, creationDisplayInfo.width),
        hTag(Header.DOC_HEIGHT, creationDisplayInfo.height),
        hTag(Header.DOC_CONTENT_DESCRIPTION, contentDescription ?: ""),
        hTag(Header.DOC_PROFILES, profile.operationsProfiles),
    ) {
        this.mWriterCallback = writerCallback
    }

    constructor(
        creationDisplayInfo: CreationDisplayInfo,
        contentDescription: String?,
        profile: Profile,
    ) : this(
        profile,
        hTag(Header.DOC_WIDTH, creationDisplayInfo.width),
        hTag(Header.DOC_HEIGHT, creationDisplayInfo.height),
        hTag(Header.DOC_CONTENT_DESCRIPTION, contentDescription ?: ""),
        hTag(Header.DOC_PROFILES, profile.operationsProfiles),
    )

    constructor(profile: Profile, vararg tags: HTag) {
        this.mPlatform = profile.platform
        mBuffer = RemoteComposeBuffer(profile.apiLevel)

        val w = HTag.getValue(tags, Header.DOC_WIDTH.toInt())
        val h = HTag.getValue(tags, Header.DOC_HEIGHT.toInt())
        val d = HTag.getValue(tags, Header.DOC_CONTENT_DESCRIPTION.toInt())
        val profiles = HTag.getProfiles(tags)

        if (w is Int) mOriginalWidth = w
        if (h is Int) mOriginalHeight = h
        if (d is String) mContentDescription = d

        val supportedOperations = profile.getSupportedOperations()
        if (supportedOperations != null) {
            mBuffer.setVersion(profile.apiLevel, profile.operationsProfiles, supportedOperations)
        } else {
            mBuffer.setVersion(profile.apiLevel, profiles)
        }

        mBuffer.addHeader(HTag.getTags(tags), HTag.getValues(tags))
    }

    constructor(
        width: Int, height: Int, contentDescription: String?,
        platform: RcPlatformServices,
    ) {
        this.mPlatform = platform
        mBuffer = RemoteComposeBuffer()
        header(width, height, contentDescription, 1f, 0)
        mOriginalWidth = width
        mOriginalHeight = height
        mContentDescription = contentDescription
    }

    constructor(
        width: Int, height: Int, contentDescription: String,
        apiLevel: Int, profiles: Int, platform: RcPlatformServices,
    ) : this(
        platform, apiLevel,
        hTag(Header.DOC_WIDTH, width),
        hTag(Header.DOC_HEIGHT, height),
        hTag(Header.DOC_CONTENT_DESCRIPTION, contentDescription),
        hTag(Header.DOC_PROFILES, profiles),
    )

    constructor(platform: RcPlatformServices, apiLevel: Int, vararg tags: HTag) {
        this.mPlatform = platform
        mBuffer = RemoteComposeBuffer(apiLevel)

        val w = HTag.getValue(tags, Header.DOC_WIDTH.toInt())
        val h = HTag.getValue(tags, Header.DOC_HEIGHT.toInt())
        val d = HTag.getValue(tags, Header.DOC_CONTENT_DESCRIPTION.toInt())
        val profiles = HTag.getProfiles(tags)

        if (w is Int) mOriginalWidth = w
        if (h is Int) mOriginalHeight = h
        if (d is String) mContentDescription = d

        mBuffer.setVersion(apiLevel, profiles)
        mBuffer.addHeader(HTag.getTags(tags), HTag.getValues(tags))
        if (apiLevel == 6 && profiles == 0) {
            if (mContentDescription != null) {
                val contentDescriptionId = addText(mContentDescription!!)
                mBuffer.addRootContentDescription(contentDescriptionId)
            }
        }
    }

    constructor(platform: RcPlatformServices, vararg tags: HTag) :
        this(platform, CoreDocument.DOCUMENT_API_LEVEL, *tags)

    constructor(profile: Profile, buffer: RemoteComposeBuffer, vararg tags: HTag) {
        this.mPlatform = profile.platform
        mBuffer = buffer

        val w = HTag.getValue(tags, Header.DOC_WIDTH.toInt())
        val h = HTag.getValue(tags, Header.DOC_HEIGHT.toInt())
        val d = HTag.getValue(tags, Header.DOC_CONTENT_DESCRIPTION.toInt())

        if (w is Int) mOriginalWidth = w
        if (h is Int) mOriginalHeight = h
        if (d is String) mContentDescription = d

        mBuffer.addHeader(HTag.getTags(tags), HTag.getValues(tags))
    }

    protected var mMaxValidFloatExpressionOperation: Int =
        AnimatedFloatExpression().getMaxOpForLevel(CoreDocument.DOCUMENT_API_LEVEL)

    fun areFloatExpressionOperationsValid(f: Float): Boolean {
        if (f.isNaN()) {
            val id = Utils.idFromNan(f)
            return id != 0 && id < mMaxValidFloatExpressionOperation
        }
        return true
    }

    fun validateOps(ops: FloatArray) {
        for (i in ops.indices) {
            if (!areFloatExpressionOperationsValid(ops[i])) {
                val str = toMathName(ops[i])
                throw IllegalArgumentException(
                    "Invalid operation: ${Utils.idFromNan(ops[i])}($str)"
                )
            }
        }
    }

    fun reset() {
        mCacheComponentWidthValues.clear()
        mCacheComponentHeightValues.clear()
        mBuffer.reset(1000000)
        mState.reset()
        header(mOriginalWidth, mOriginalHeight, mContentDescription, 1f, 0)
    }

    fun header(
        width: Int, height: Int, contentDescription: String?,
        density: Float, capabilities: Long,
    ) {
        mBuffer.header(width, height, density, capabilities)
        if (contentDescription != null) {
            val contentDescriptionId = addText(contentDescription)
            mBuffer.addRootContentDescription(contentDescriptionId)
        }
    }

    fun pathCombine(path1: Int, path2: Int, op: Byte): Int {
        val id = nextId()
        mBuffer.pathCombine(id, path1, path2, op)
        return id
    }

    fun performHaptic(feedbackConstant: Int) {
        mBuffer.performHaptic(feedbackConstant)
    }

    fun getColorAttribute(baseColor: Int, type: Short): Float {
        val id = mState.createNextAvailableId()
        mBuffer.getColorAttribute(id, baseColor, type)
        return Utils.asNan(id)
    }

    fun addAction(vararg actions: Action?) {
        for (action in actions) {
            action?.write(this)
        }
    }

    fun textSubtext(txtId: Int, start: Float, len: Float): Int {
        val id = mState.createNextAvailableId()
        mBuffer.textSubtext(id, txtId, start, len)
        return id
    }

    fun bitmapTextMeasure(textId: Int, bmFontId: Int, measureWidth: Int): Float {
        val id = mState.createNextAvailableId()
        mBuffer.bitmapTextMeasure(id, textId, bmFontId, measureWidth)
        return Utils.asNan(id)
    }

    fun addMatrixMultiply(matrixId: Float, from: FloatArray?, out: FloatArray) {
        addMatrixMultiply(matrixId, 0.toShort(), from, out)
    }

    fun addMatrixMultiply(matrixId: Float, type: Short, from: FloatArray?, out: FloatArray) {
        val outId = IntArray(out.size)
        for (i in out.indices) {
            outId[i] = mState.createNextAvailableId()
            out[i] = Utils.asNan(outId[i])
        }
        mBuffer.addMatrixVectorMath(matrixId, type, from ?: FloatArray(0), outId)
    }

    fun checkAndClearForceSendingNewPaint(): Boolean {
        val result = mHasForceSendingNewPaint
        mHasForceSendingNewPaint = false
        return result
    }

    fun wakeIn(seconds: Float) {
        mBuffer.wakeIn(seconds)
    }

    fun addPathExpression(
        expressionX: FloatArray, expressionY: FloatArray,
        start: Float, end: Float, count: Float, flags: Int,
    ): Int {
        val id = mState.createNextAvailableId()
        mBuffer.addPathExpression(id, expressionX, expressionY, start, end, count, flags)
        return id
    }

    fun addPolarPathExpression(
        expressionR: FloatArray, start: Float, end: Float,
        count: Float, centerX: Float, centerY: Float, flags: Int,
    ): Int {
        val id = mState.createNextAvailableId()
        mBuffer.addPathExpression(
            id, expressionR, floatArrayOf(centerX, centerY),
            start, end, count, Rc.PathExpression.POLAR_PATH or flags,
        )
        return id
    }

    fun encodeToByteArray(): ByteArray = mBuffer.getBuffer().cloneBytes()

    /** Used to create the tag values in the header */
    class HTag(val mTag: Short, val mValue: Any) {
        companion object {
            fun getProfiles(tags: Array<out HTag>): Int {
                for (tag in tags) {
                    if (tag.mTag == Header.DOC_PROFILES) return tag.mValue as Int
                }
                return 0
            }

            fun getValue(tags: Array<out HTag>, tag: Int): Any? {
                for (t in tags) {
                    if (t.mTag.toInt() == tag) return t.mValue
                }
                return null
            }

            fun getTags(tags: Array<out HTag>): ShortArray {
                return ShortArray(tags.size) { tags[it].mTag }
            }

            fun getValues(tags: Array<out HTag>): Array<Any> {
                return Array(tags.size) { tags[it].mValue }
            }
        }
    }

    fun buffer(): ByteArray = mBuffer.getBuffer().buffer
    fun bufferSize(): Int = mBuffer.getBuffer().size
    fun getBuffer(): RemoteComposeBuffer = mBuffer

    fun createShader(shaderString: String): RemoteComposeShader =
        RemoteComposeShader(shaderString, this)

    fun setTheme(theme: Int) = mBuffer.setTheme(theme)

    fun drawBitmap(image: Any, width: Int, height: Int, contentDescription: String?) {
        val imageId = storeBitmap(image)
        val contentDescriptionId = if (contentDescription != null) addText(contentDescription) else 0
        mBuffer.drawBitmap(imageId, width, height, 0, 0, width, height, 0, 0, width, height, contentDescriptionId)
    }

    fun setRootContentBehavior(scroll: Int, alignment: Int, sizing: Int, mode: Int) {
        mBuffer.setRootContentBehavior(scroll, alignment, sizing, mode)
    }

    fun addClickArea(
        id: Int, contentDescription: String?,
        left: Float, top: Float, right: Float, bottom: Float, metadata: String?,
    ) {
        val contentDescriptionId = if (contentDescription != null) addText(contentDescription) else 0
        val metadataId = if (metadata != null) addText(metadata) else 0
        mBuffer.addClickArea(id, contentDescriptionId, left, top, right, bottom, metadataId)
    }

    fun drawArc(left: Float, top: Float, right: Float, bottom: Float, startAngle: Float, sweepAngle: Float) {
        mBuffer.addDrawArc(left, top, right, bottom, startAngle, sweepAngle)
    }

    fun drawSector(left: Float, top: Float, right: Float, bottom: Float, startAngle: Float, sweepAngle: Float) {
        mBuffer.addDrawSector(left, top, right, bottom, startAngle, sweepAngle)
    }

    fun drawBitmap(image: Any, left: Float, top: Float, right: Float, bottom: Float, contentDescription: String?) {
        val imageId = storeBitmap(image)
        val contentDescriptionId = if (contentDescription != null) addText(contentDescription) else 0
        mBuffer.addDrawBitmap(imageId, left, top, right, bottom, contentDescriptionId)
    }

    fun drawBitmap(imageId: Int, left: Float, top: Float, right: Float, bottom: Float, contentDescription: String?) {
        val contentDescriptionId = if (contentDescription != null) addText(contentDescription) else 0
        mBuffer.addDrawBitmap(imageId, left, top, right, bottom, contentDescriptionId)
    }

    fun drawBitmap(imageId: Int, left: Float, top: Float, contentDescription: String?) {
        val imageWidth = mPlatform.getImageWidth(imageId)
        val imageHeight = mPlatform.getImageHeight(imageId)
        drawBitmap(imageId, left, top, imageWidth.toFloat(), imageHeight.toFloat(), contentDescription)
    }

    fun drawScaledBitmap(
        image: Any, srcLeft: Float, srcTop: Float, srcRight: Float, srcBottom: Float,
        dstLeft: Float, dstTop: Float, dstRight: Float, dstBottom: Float,
        scaleType: Int, scaleFactor: Float, contentDescription: String?,
    ) {
        val imageId = storeBitmap(image)
        val contentDescriptionId = if (contentDescription != null) addText(contentDescription) else 0
        mBuffer.drawScaledBitmap(
            imageId, srcLeft, srcTop, srcRight, srcBottom,
            dstLeft, dstTop, dstRight, dstBottom, scaleType, scaleFactor, contentDescriptionId,
        )
    }

    fun drawScaledBitmap(
        imageId: Int, srcLeft: Float, srcTop: Float, srcRight: Float, srcBottom: Float,
        dstLeft: Float, dstTop: Float, dstRight: Float, dstBottom: Float,
        scaleType: Int, scaleFactor: Float, contentDescription: String?,
    ) {
        val contentDescriptionId = if (contentDescription != null) addText(contentDescription) else 0
        mBuffer.drawScaledBitmap(
            imageId, srcLeft, srcTop, srcRight, srcBottom,
            dstLeft, dstTop, dstRight, dstBottom, scaleType, scaleFactor, contentDescriptionId,
        )
    }

    fun drawCircle(centerX: Float, centerY: Float, radius: Float) = mBuffer.addDrawCircle(centerX, centerY, radius)
    fun drawLine(x1: Float, y1: Float, x2: Float, y2: Float) = mBuffer.addDrawLine(x1, y1, x2, y2)
    fun drawOval(left: Float, top: Float, right: Float, bottom: Float) = mBuffer.addDrawOval(left, top, right, bottom)

    fun drawPath(path: Any) {
        var id = mState.dataGetId(path)
        if (id == -1) id = addPathData(path)
        mBuffer.addDrawPath(id)
    }

    fun drawPath(pathId: Int) = mBuffer.addDrawPath(pathId)
    fun drawRect(left: Float, top: Float, right: Float, bottom: Float) = mBuffer.addDrawRect(left, top, right, bottom)

    fun drawRoundRect(left: Float, top: Float, right: Float, bottom: Float, radiusX: Float, radiusY: Float) {
        mBuffer.addDrawRoundRect(left, top, right, bottom, radiusX, radiusY)
    }

    fun textCreateId(text: String): Int = addText(text)

    fun textMerge(id1: Int, id2: Int): Int {
        val textId = nextId()
        return mBuffer.textMerge(textId, id1, id2)
    }

    fun drawTextOnPath(text: String, path: Any, hOffset: Float, vOffset: Float) {
        val textId = addText(text)
        drawTextOnPath(textId, path, hOffset, vOffset)
    }

    fun drawTextOnPath(textId: Int, path: Any, hOffset: Float, vOffset: Float) {
        var pathId = mState.dataGetId(path)
        if (pathId == -1) pathId = addPathData(path)
        mBuffer.addDrawTextOnPath(textId, pathId, hOffset, vOffset)
    }

    fun drawTextOnCircle(
        textId: Int, centerX: Float, centerY: Float, radius: Float,
        startAngle: Float, warpRadiusOffset: Float,
        alignment: DrawTextOnCircle.Alignment, placement: DrawTextOnCircle.Placement,
    ) {
        mBuffer.addDrawTextOnCircle(textId, centerX, centerY, radius, startAngle, warpRadiusOffset, alignment, placement)
    }

    fun drawTextRun(
        text: String, start: Int, end: Int, contextStart: Int, contextEnd: Int,
        x: Float, y: Float, rtl: Boolean,
    ) {
        val textId = addText(text)
        mBuffer.addDrawTextRun(textId, start, end, contextStart, contextEnd, x, y, rtl)
    }

    fun drawTextRun(
        textId: Int, start: Int, end: Int, contextStart: Int, contextEnd: Int,
        x: Float, y: Float, rtl: Boolean,
    ) {
        mBuffer.addDrawTextRun(textId, start, end, contextStart, contextEnd, x, y, rtl)
    }

    fun drawBitmapFontTextRun(textId: Int, bitmapFontId: Int, start: Int, end: Int, x: Float, y: Float) {
        mBuffer.addDrawBitmapFontTextRun(textId, bitmapFontId, start, end, x, y)
    }

    fun drawBitmapFontTextRunOnPath(
        textId: Int, bitmapFontId: Int, path: Any, start: Int, end: Int, yAdj: Float,
    ) {
        var pathId = mState.dataGetId(path)
        if (pathId == -1) pathId = addPathData(path)
        mBuffer.addDrawBitmapFontTextRunOnPath(textId, bitmapFontId, pathId, start, end, yAdj)
    }

    fun drawTextAnchored(str: String, x: Float, y: Float, panX: Float, panY: Float, flags: Int) {
        val textId = addText(str)
        mBuffer.drawTextAnchored(textId, x, y, panX, panY, flags)
    }

    fun drawTextAnchored(strId: Int, x: Float, y: Float, panX: Float, panY: Float, flags: Int) {
        mBuffer.drawTextAnchored(strId, x, y, panX, panY, flags)
    }

    fun drawTweenPath(path1: Any, path2: Any, tween: Float, start: Float, stop: Float) {
        var path1Id = mState.dataGetId(path1)
        if (path1Id == -1) path1Id = addPathData(path1)
        var path2Id = mState.dataGetId(path2)
        if (path2Id == -1) path2Id = addPathData(path2)
        mBuffer.addDrawTweenPath(path1Id, path2Id, tween, start, stop)
    }

    fun drawBitmapTextAnchored(
        text: String, bitmapFontId: Int, start: Float, end: Float,
        x: Float, y: Float, panX: Float, panY: Float,
    ) {
        val textId = addText(text)
        mBuffer.drawBitmapTextAnchored(textId, bitmapFontId, start, end, x, y, panX, panY)
    }

    fun drawBitmapTextAnchored(
        textId: Int, bitmapFontId: Int, start: Float, end: Float,
        x: Float, y: Float, panX: Float, panY: Float,
    ) {
        mBuffer.drawBitmapTextAnchored(textId, bitmapFontId, start, end, x, y, panX, panY)
    }

    fun drawTweenPath(path1Id: Int, path2Id: Int, tween: Float, start: Float, stop: Float) {
        mBuffer.addDrawTweenPath(path1Id, path2Id, tween, start, stop)
    }

    fun addPathData(path: Any): Int {
        val pathData = mPlatform.pathToFloatArray(path)
            ?: throw IllegalArgumentException("Invalid path data")
        val id = mState.cacheData(path)
        return mBuffer.addPathData(id, pathData)
    }

    fun addPathData(path: Any, winding: Int): Int {
        val pathData = mPlatform.pathToFloatArray(path)
            ?: throw IllegalArgumentException("Invalid path data")
        val id = mState.cacheData(path)
        return mBuffer.addPathData(id, pathData, winding)
    }

    fun pathTween(pid1: Int, pid2: Int, tween: Float): Int {
        val out = mState.createNextAvailableId()
        return mBuffer.pathTween(out, pid1, pid2, tween)
    }

    fun pathCreate(x: Float, y: Float): Int {
        val out = mState.createNextAvailableId()
        return mBuffer.pathCreate(out, x, y)
    }

    fun pathAppend(pathId: Int, vararg path: Float) = mBuffer.pathAppend(pathId, *path)
    fun pathAppendLineTo(pathId: Int, x: Float, y: Float) =
        mBuffer.pathAppend(pathId, RemotePathBase.LINE_NAN, 0f, 0f, x, y)
    fun pathAppendQuadTo(pathId: Int, x1: Float, y1: Float, x2: Float, y2: Float) =
        mBuffer.pathAppend(pathId, RemotePathBase.QUADRATIC_NAN, 0f, 0f, x1, y1, x2, y2)
    fun pathAppendMoveTo(pathId: Int, x: Float, y: Float) =
        mBuffer.pathAppend(pathId, RemotePathBase.MOVE_NAN, x, y)
    fun pathAppendClose(pathId: Int) = mBuffer.pathAppend(pathId, RemotePathBase.CLOSE_NAN)
    fun pathAppendReset(pathId: Int) = mBuffer.pathAppend(pathId, Utils.asNan(17))

    fun addPathString(path: String): Int = addPathData(mPlatform.parsePath(path))

    fun skew(skewX: Float, skewY: Float) = mBuffer.addMatrixSkew(skewX, skewY)
    fun rotate(angle: Float, centerX: Float, centerY: Float) = mBuffer.addMatrixRotate(angle, centerX, centerY)
    fun rotate(angle: Float) = mBuffer.addMatrixRotate(angle, Float.NaN, Float.NaN)
    fun matrixFromPath(pathId: Int, fraction: Float, vOffset: Float, flags: Int) =
        mBuffer.setMatrixFromPath(pathId, fraction, vOffset, flags)
    fun save() = mBuffer.addMatrixSave()
    fun restore() = mBuffer.addMatrixRestore()
    fun translate(dx: Float, dy: Float) = mBuffer.addMatrixTranslate(dx, dy)
    fun scale(scaleX: Float, scaleY: Float, centerX: Float, centerY: Float) =
        mBuffer.addMatrixScale(scaleX, scaleY, centerX, centerY)
    fun scale(scaleX: Float, scaleY: Float) = mBuffer.addMatrixScale(scaleX, scaleY)
    fun addClipPath(pathId: Int) = mBuffer.addClipPath(pathId)
    fun clipRect(left: Float, top: Float, right: Float, bottom: Float) =
        mBuffer.addClipRect(left, top, right, bottom)

    fun addFloatConstant(value: Float): Float {
        val id = mState.cacheFloat(value)
        return mBuffer.addFloat(id, value)
    }

    fun reserveFloatVariable(): Float {
        val id = mState.createNextAvailableId()
        return Utils.asNan(id)
    }

    private val mCacheComponentWidthValues: HashMap<Int, Float> = HashMap()
    private val mCacheComponentHeightValues: HashMap<Int, Float> = HashMap()

    fun addComponentWidthValue(): Float {
        val lastId = mBuffer.getLastComponentId()
        mCacheComponentWidthValues[lastId]?.let { return it }
        val id = reserveFloatVariable()
        mBuffer.addComponentWidthValue(Utils.idFromNan(id))
        mCacheComponentWidthValues[lastId] = id
        return id
    }

    fun addComponentHeightValue(): Float {
        val lastId = mBuffer.getLastComponentId()
        mCacheComponentHeightValues[lastId]?.let { return it }
        val id = reserveFloatVariable()
        mBuffer.addComponentHeightValue(Utils.idFromNan(id))
        mCacheComponentHeightValues[lastId] = id
        return id
    }

    fun addColor(color: Int): Int {
        val id = mState.createNextAvailableId()
        mBuffer.addColor(id, color)
        return id
    }

    fun addNamedColor(name: String, color: Int): Int {
        val id = addColor(color)
        mBuffer.setNamedVariable(id, name, NamedVariable.COLOR_TYPE)
        return id
    }

    fun setColorName(id: Int, name: String) = mBuffer.setNamedVariable(id, name, NamedVariable.COLOR_TYPE)
    fun setStringName(id: Int, name: String) = mBuffer.setNamedVariable(id, name, NamedVariable.STRING_TYPE)

    fun addNamedString(name: String, initialValue: String): Int {
        val id = mState.createNextAvailableId()
        mBuffer.setNamedVariable(id, name, NamedVariable.STRING_TYPE)
        TextData.apply(mBuffer.getBuffer(), id, initialValue)
        return id
    }

    fun addNamedInt(name: String, initialValue: Int): Long {
        val id = mState.createNextAvailableId()
        mBuffer.setNamedVariable(id, name, NamedVariable.INT_TYPE)
        IntegerConstant.apply(mBuffer.getBuffer(), id, initialValue)
        mState.updateInteger(id, initialValue)
        return id.toLong() + 0x100000000L
    }

    fun addNamedFloat(name: String, initialValue: Float): Float {
        val id = mState.createNextAvailableId()
        mBuffer.setNamedVariable(id, name, NamedVariable.FLOAT_TYPE)
        FloatConstant.apply(mBuffer.getBuffer(), id, initialValue)
        mState.updateFloat(id, initialValue)
        return Utils.asNan(id)
    }

    fun addNamedBitmap(name: String, initialValue: Any): Int {
        val id = storeBitmap(initialValue)
        mBuffer.setNamedVariable(id, name, NamedVariable.IMAGE_TYPE)
        mState.updateObject(id, initialValue)
        return id
    }

    fun addBitmapUrl(url: String): Int {
        var imageId = mState.dataGetId(url)
        if (imageId == -1) {
            imageId = mState.cacheData(url)
            mBuffer.storeBitmapUrl(imageId, url)
        }
        return imageId
    }

    fun addNamedBitmapUrl(name: String, url: String): Int {
        val id = addBitmapUrl(url)
        mBuffer.setNamedVariable(id, name, NamedVariable.IMAGE_TYPE)
        return id
    }

    fun addNamedLong(name: String, initialValue: Long): Int {
        val id = mState.createNextAvailableId()
        mBuffer.setNamedVariable(id, name, NamedVariable.LONG_TYPE)
        LongConstant.apply(mBuffer.getBuffer(), id, initialValue)
        return id
    }

    fun addColorExpression(color1: Int, color2: Int, tween: Float): Short {
        val id = mState.createNextAvailableId()
        mBuffer.addColorExpression(id, color1, color2, tween)
        return id.toShort()
    }

    fun addColorExpression(colorId1: Short, color2: Int, tween: Float): Short {
        val id = mState.createNextAvailableId()
        mBuffer.addColorExpression(id, colorId1, color2, tween)
        return id.toShort()
    }

    fun addColorExpression(color1: Int, colorId2: Short, tween: Float): Short {
        val id = mState.createNextAvailableId()
        mBuffer.addColorExpression(id, color1, colorId2, tween)
        return id.toShort()
    }

    fun addColorExpression(colorId1: Short, colorId2: Short, tween: Float): Short {
        val id = mState.createNextAvailableId()
        mBuffer.addColorExpression(id, colorId1, colorId2, tween)
        return id.toShort()
    }

    fun addColorExpression(hue: Float, sat: Float, value: Float): Short {
        val id = mState.createNextAvailableId()
        mBuffer.addColorExpression(id, hue, sat, value)
        return id.toShort()
    }

    fun addColorExpression(alpha: Int, hue: Float, sat: Float, value: Float): Short {
        val id = mState.createNextAvailableId()
        mBuffer.addColorExpression(id, alpha, hue, sat, value)
        return id.toShort()
    }

    fun addColorExpression(alpha: Float, red: Float, green: Float, blue: Float): Short {
        val id = mState.createNextAvailableId()
        mBuffer.addColorExpression(id, alpha, red, green, blue)
        return id.toShort()
    }

    fun addThemedColor(lightName: String?, lightValue: Int, darkName: String?, darkValue: Int): Short {
        val lightId = mState.createNextAvailableId()
        val darkId = mState.createNextAvailableId()
        val lightMode: Int
        if (Rc.System.sLightMode == 0f) {
            lightMode = mState.createNextAvailableId()
            Rc.System.sLightMode = Utils.asNan(lightMode)
        } else {
            lightMode = Utils.idFromNan(Rc.System.sLightMode)
        }

        if (lightName != null) mBuffer.setNamedVariable(lightId, lightName, NamedVariable.COLOR_TYPE)
        if (darkName != null) mBuffer.setNamedVariable(darkId, darkName, NamedVariable.COLOR_TYPE)

        val retId = mState.createNextAvailableId()
        mBuffer.addColor(lightId, lightValue)
        mBuffer.addColor(darkId, darkValue)
        mBuffer.addFloat(lightMode, 0f)

        setTheme(Rc.Theme.DARK)
        startLoop(0, 0f, 1f, 1f)
        mBuffer.addColorExpression(retId, darkId.toShort(), lightId.toShort(), 0f)
        endLoop()
        setTheme(Rc.Theme.LIGHT)
        startLoop(0, 0f, 1f, 1f)
        mBuffer.addColorExpression(retId, darkId.toShort(), lightId.toShort(), 1f)
        endLoop()
        setTheme(Rc.Theme.UNSPECIFIED)

        return retId.toShort()
    }

    fun floatExpression(vararg value: Float): Float {
        val id = mState.cacheData(value)
        mBuffer.addAnimatedFloat(id, *value)
        return Utils.asNan(id)
    }

    fun floatExpression(value: FloatArray, animation: FloatArray?): Float {
        val id = mState.cacheData(value)
        mBuffer.addAnimatedFloat(id, value, animation ?: floatArrayOf())
        return Utils.asNan(id)
    }

    fun addInteger(value: Int): Long {
        val id = mState.cacheInteger(value)
        mBuffer.addInteger(id, value)
        return id.toLong() + 0x100000000L
    }

    fun addLong(value: Long): Int {
        val id = mState.createNextAvailableId()
        mBuffer.addLong(id, value)
        return id
    }

    fun addBoolean(value: Boolean): Int {
        val id = mState.createNextAvailableId()
        mBuffer.addBoolean(id, value)
        return id
    }

    fun mapLookup(mapId: Int, str: String): Int {
        val strId = addText(str)
        return mapLookup(mapId, strId)
    }

    fun mapLookup(mapId: Int, strId: Int): Int {
        val hash = mapId + strId * 33
        var id = mState.dataGetId(hash)
        if (id == -1) {
            id = mState.cacheData(hash)
            mBuffer.mapLookup(id, mapId, strId)
        }
        return id
    }

    fun addText(text: String): Int {
        var id = mState.dataGetId(text)
        if (id == -1) {
            id = mState.cacheData(text)
            mBuffer.addText(id, text)
        }
        return id
    }

    fun textMeasure(textId: Int, mode: Int): Float {
        val id = mState.cacheData(textId + mode * 31)
        mBuffer.textMeasure(id, textId, mode)
        return Utils.asNan(id)
    }

    fun textLength(textId: Int): Float {
        val id = mState.cacheData(textId + (TextLength.id() shl 16))
        mBuffer.textLength(id, textId)
        return Utils.asNan(id)
    }

    fun easing(maxTime: Float, maxAcceleration: Float, maxVelocity: Float): FloatArray {
        return floatArrayOf(Float.fromBits(0), maxTime, maxAcceleration, maxVelocity)
    }

    fun addTouch(
        defValue: Float, min: Float, max: Float, touchMode: Int,
        velocityId: Float, touchEffects: Int,
        touchSpec: FloatArray?, easingSpec: FloatArray?, vararg exp: Float,
    ): Float {
        val id = mState.createNextAvailableId()
        mBuffer.addTouchExpression(id, defValue, min, max, velocityId, touchEffects, exp, touchMode, touchSpec, easingSpec)
        return Utils.asNan(id)
    }

    fun spring(stiffness: Float, damping: Float, stopThreshold: Float, boundaryMode: Int): FloatArray {
        return floatArrayOf(0f, stiffness, damping, stopThreshold, Float.fromBits(boundaryMode))
    }

    fun bitmapAttribute(bitmapId: Int, attribute: Short): Float {
        val id = mState.createNextAvailableId()
        mBuffer.bitmapAttribute(id, bitmapId, attribute)
        return Utils.asNan(id)
    }

    fun textAttribute(textId: Int, attribute: Short): Float {
        val id = mState.createNextAvailableId()
        mBuffer.textAttribute(id, textId, attribute)
        return Utils.asNan(id)
    }

    /** DataMap for creating maps of typed data */
    class DataMap {
        val name: String
        val type: Types
        var textValue: String = ""
        var floatValue: Float = 0f
        var booleanValue: Boolean = false
        var longValue: Long = 0L
        var intValue: Int = 0

        enum class Types(val value: Byte) {
            STRING(DataMapIds.TYPE_STRING),
            INT(DataMapIds.TYPE_INT),
            FLOAT(DataMapIds.TYPE_FLOAT),
            LONG(DataMapIds.TYPE_LONG),
            BOOLEAN(DataMapIds.TYPE_BOOLEAN),
        }

        constructor(name: String, value: String) { this.name = name; type = Types.STRING; textValue = value }
        constructor(name: String, value: Float) { this.name = name; type = Types.FLOAT; floatValue = value }
        constructor(name: String, value: Long) { this.name = name; type = Types.LONG; longValue = value }
        constructor(name: String, value: Int) { this.name = name; type = Types.INT; intValue = value }
        constructor(name: String, value: Boolean) { this.name = name; type = Types.BOOLEAN; booleanValue = value }
    }

    private fun encodeData(vararg data: DataMap): Int {
        val names = Array(data.size) { data[it].name }
        val ids = IntArray(data.size)
        val types = ByteArray(data.size)
        for (i in data.indices) {
            val item = data[i]
            val id: Int = when (item.type) {
                DataMap.Types.STRING -> addText(item.textValue)
                DataMap.Types.INT -> {
                    val iid = mState.cacheInteger(item.intValue)
                    mBuffer.addInteger(iid, item.intValue)
                    iid
                }
                DataMap.Types.FLOAT -> Utils.idFromNan(addFloatConstant(item.floatValue))
                DataMap.Types.LONG -> addLong(item.longValue)
                DataMap.Types.BOOLEAN -> addBoolean(item.booleanValue)
            }
            ids[i] = id
            types[i] = item.type.value
        }
        val id = mState.cacheData(ids, NanMap.TYPE_ARRAY)
        mBuffer.addMap(id, names, types, ids)
        return id
    }

    fun addDataMap(vararg data: DataMap?): Int {
        @Suppress("UNCHECKED_CAST")
        return encodeData(*(data.filterNotNull().toTypedArray()))
    }

    fun addDataMap(keys: Array<String>, ids: IntArray): Float {
        val id = mState.cacheData(ids, NanMap.TYPE_ARRAY)
        mBuffer.addMap(id, keys, null, ids)
        return id.toFloat()
    }

    fun integerExpression(mask: Int, vararg value: Int): Long {
        val id = mState.cacheData(value)
        mBuffer.addIntegerExpression(id, mask, value)
        return id.toLong() + 0x100000000L
    }

    fun integerExpression(vararg v: Long): Long {
        var mask = 0
        for (i in v.indices) {
            if (v[i] > Int.MAX_VALUE) mask = mask or (1 shl i)
        }
        val vint = IntArray(v.size) { v[it].toInt() }
        return integerExpression(mask, *vint)
    }

    fun asFloatId(id: Long): Float = mBuffer.asFloatId((id and 0xFFFFFFFL).toInt())

    fun timeAttribute(longID: Int, type: Short, vararg args: Int): Float {
        val id = mState.createNextAvailableId()
        mBuffer.timeAttribute(id, longID, type, *args)
        return Utils.asNan(id)
    }

    fun exp(vararg value: Float): FloatArray = value

    fun anim(duration: Float, type: Int, spec: FloatArray?, initialValue: Float, wrap: Float): FloatArray =
        RemoteComposeBuffer.packAnimation(duration, type, spec, initialValue, wrap)

    fun anim(duration: Float, type: Int, spec: FloatArray?, initialValue: Float): FloatArray =
        RemoteComposeBuffer.packAnimation(duration, type, spec, initialValue, Float.NaN)

    fun anim(duration: Float, type: Int, spec: FloatArray?): FloatArray =
        RemoteComposeBuffer.packAnimation(duration, type, spec, Float.NaN, Float.NaN)

    fun anim(duration: Float, type: Int): FloatArray =
        RemoteComposeBuffer.packAnimation(duration, type, null, Float.NaN, Float.NaN)

    fun anim(duration: Float): FloatArray =
        RemoteComposeBuffer.packAnimation(duration, RemoteComposeBuffer.EASING_CUBIC_STANDARD, null, Float.NaN, Float.NaN)

    fun idLookup(arrayId: Float, index: Float): Int {
        val id = mState.createNextAvailableId()
        mBuffer.idLookup(id, arrayId, index)
        return id
    }

    fun textLookup(arrayId: Float, index: Float): Int {
        val hash = (Float.floatToIntBits(arrayId).toLong() shl 32) + Float.floatToIntBits(index)
        val id = mState.cacheData(hash)
        mBuffer.textLookup(id, arrayId, index)
        return id
    }

    fun textLookup(arrayId: Float, indexId: Int): Int {
        val hash = (Float.floatToIntBits(arrayId).toLong() shl 32) + Float.floatToIntBits(indexId.toFloat())
        val id = mState.cacheData(hash)
        mBuffer.textLookup(id, arrayId, indexId)
        return id
    }

    fun createTextFromFloat(value: Float, before: Int, after: Int, flags: Int): Int {
        val placeHolder = Utils.floatToString(value) + "(" + before + "," + after + "," + flags + ")"
        var id = mState.dataGetId(placeHolder)
        if (id == -1) {
            id = mState.cacheData(placeHolder)
        }
        return mBuffer.createTextFromFloat(id, value, before.toShort(), after.toShort(), flags)
    }

    fun createID(type: Int): Int = mState.createNextAvailableId(type)
    fun nextId(): Int = mState.createNextAvailableId()

    fun root(content: RemoteComposeWriterInterface) {
        mInsertPoint = mBuffer.getBuffer().size
        mBuffer.addRootStart()
        content.run()
        mBuffer.addContainerEnd()
    }

    fun startLoop(indexId: Int, from: Float, step: Float, until: Float) =
        mBuffer.addLoopStart(indexId, from, step, until)

    fun startLoopVar(from: Float, step: Float, until: Float): Float {
        val indexId = createID(0)
        mBuffer.addLoopStart(indexId, from, step, until)
        return asFloatId(indexId.toLong())
    }

    fun startLoop(count: Float): Float {
        val indexId = createID(0)
        startLoop(indexId, 0f, 1f, count)
        return asFloatId(indexId.toLong())
    }

    fun endLoop() = mBuffer.addLoopEnd()

    fun loop(indexId: Int, from: Float, step: Float, until: Float, content: RemoteComposeWriterInterface) {
        startLoop(indexId, from, step, until)
        content.run()
        endLoop()
    }

    fun loop(indexId: Int, from: Int, step: Int, until: Int, content: RemoteComposeWriterInterface) {
        startLoop(indexId, from.toFloat(), step.toFloat(), until.toFloat())
        content.run()
        endLoop()
    }

    fun conditionalOperations(type: Byte, a: Float, b: Float, content: RemoteComposeWriterInterface) {
        mBuffer.addConditionalOperations(type, a, b)
        content.run()
        endConditionalOperations()
    }

    fun conditionalOperations(type: Byte, a: Float, b: Float) =
        mBuffer.addConditionalOperations(type, a, b)

    fun endConditionalOperations() = mBuffer.endConditionalOperations()

    private fun addContentStart() {
        mBuffer.addContentStart()
        mHasForceSendingNewPaint = true
    }

    fun column(modifier: RecordingModifier, horizontal: Int, vertical: Int, content: RemoteComposeWriterInterface) {
        startColumn(modifier, horizontal, vertical)
        content.run()
        endColumn()
    }

    fun startColumn(modifier: RecordingModifier, horizontal: Int, vertical: Int) {
        mBuffer.addColumnStart(modifier.getComponentId(), -1, horizontal, vertical, modifier.getSpacedBy())
        for (m in modifier.getList()) m.write(this)
        addContentStart()
    }

    fun endColumn() { mBuffer.addContainerEnd(); mBuffer.addContainerEnd() }

    fun collapsibleColumn(modifier: RecordingModifier, horizontal: Int, vertical: Int, content: RemoteComposeWriterInterface) {
        startCollapsibleColumn(modifier, horizontal, vertical); content.run(); endCollapsibleColumn()
    }

    fun startCollapsibleColumn(modifier: RecordingModifier, horizontal: Int, vertical: Int) {
        mBuffer.addCollapsibleColumnStart(modifier.getComponentId(), -1, horizontal, vertical, modifier.getSpacedBy())
        for (m in modifier.getList()) m.write(this)
        addContentStart()
    }

    fun endCollapsibleColumn() { mBuffer.addContainerEnd(); mBuffer.addContainerEnd() }

    fun row(modifier: RecordingModifier, horizontal: Int, vertical: Int, content: RemoteComposeWriterInterface) {
        startRow(modifier, horizontal, vertical); content.run(); endRow()
    }

    fun startRow(modifier: RecordingModifier, horizontal: Int, vertical: Int) {
        mBuffer.addRowStart(modifier.getComponentId(), -1, horizontal, vertical, modifier.getSpacedBy())
        for (m in modifier.getList()) m.write(this)
        addContentStart()
    }

    fun endRow() { mBuffer.addContainerEnd(); mBuffer.addContainerEnd() }

    fun collapsibleRow(modifier: RecordingModifier, horizontal: Int, vertical: Int, content: RemoteComposeWriterInterface) {
        startCollapsibleRow(modifier, horizontal, vertical); content.run(); endCollapsibleRow()
    }

    fun startCollapsibleRow(modifier: RecordingModifier, horizontal: Int, vertical: Int) {
        mBuffer.addCollapsibleRowStart(modifier.getComponentId(), -1, horizontal, vertical, modifier.getSpacedBy())
        for (m in modifier.getList()) m.write(this)
        addContentStart()
    }

    fun endCollapsibleRow() { mBuffer.addContainerEnd(); mBuffer.addContainerEnd() }

    fun canvas(modifier: RecordingModifier, content: RemoteComposeWriterInterface) {
        startCanvas(modifier); content.run(); endCanvas()
    }

    fun drawComponentContent() = mBuffer.drawComponentContent()

    fun startCanvas(modifier: RecordingModifier) {
        mBuffer.addCanvasStart(modifier.getComponentId(), -1)
        for (m in modifier.getList()) m.write(this)
        addContentStart()
        mBuffer.addCanvasContentStart(-1)
    }

    fun endCanvas() { mBuffer.addContainerEnd(); mBuffer.addContainerEnd(); mBuffer.addContainerEnd() }

    fun startCanvasOperations() = mBuffer.addCanvasOperationsStart()
    fun endCanvasOperations() = mBuffer.addContainerEnd()
    fun startRunActions() = mBuffer.addRunActionsStart()
    fun endRunActions() = mBuffer.addContainerEnd()

    fun box(modifier: RecordingModifier, horizontal: Int, vertical: Int, content: RemoteComposeWriterInterface) {
        startBox(modifier, horizontal, vertical); content.run(); endBox()
    }

    fun startBox(modifier: RecordingModifier, horizontal: Int, vertical: Int) {
        mBuffer.addBoxStart(modifier.getComponentId(), -1, horizontal, vertical)
        for (m in modifier.getList()) m.write(this)
        addContentStart()
    }

    fun startBox(modifier: RecordingModifier) = startBox(modifier, BoxLayout.START, BoxLayout.TOP)
    fun endBox() { mBuffer.addContainerEnd(); mBuffer.addContainerEnd() }

    fun startFitBox(modifier: RecordingModifier, horizontal: Int, vertical: Int) {
        mBuffer.addFitBoxStart(modifier.getComponentId(), -1, horizontal, vertical)
        for (m in modifier.getList()) m.write(this)
        addContentStart()
    }

    fun endFitBox() { mBuffer.addContainerEnd(); mBuffer.addContainerEnd() }

    fun image(modifier: RecordingModifier, imageId: Int, scaleType: Int, alpha: Float) {
        mBuffer.addImage(modifier.getComponentId(), -1, imageId, scaleType, alpha)
        for (m in modifier.getList()) m.write(this)
        mBuffer.addContainerEnd()
    }

    fun stateLayout(modifier: RecordingModifier, indexId: Int, content: RemoteComposeWriterInterface) {
        startStateLayout(modifier, indexId); content.run(); endStateLayout()
    }

    fun startStateLayout(modifier: RecordingModifier, indexId: Int) {
        mBuffer.addStateLayout(modifier.getComponentId(), -1, 0, 0, indexId)
        for (m in modifier.getList()) m.write(this)
        addContentStart()
    }

    fun endStateLayout() { mBuffer.addContainerEnd(); mBuffer.addContainerEnd() }

    fun addModifierScroll(direction: Int, positionId: Float) {
        val max = reserveFloatVariable()
        val notchMax = reserveFloatVariable()
        val touchExpressionDirection =
            if (direction != 0) RemoteContext.FLOAT_TOUCH_POS_X else RemoteContext.FLOAT_TOUCH_POS_Y
        ScrollModifierOperation.apply(mBuffer.getBuffer(), direction, positionId, max, notchMax)
        mBuffer.addTouchExpression(
            Utils.idFromNan(positionId), 0f, 0f, max, 0f, 3,
            floatArrayOf(touchExpressionDirection, -1f, MUL),
            TouchExpression.STOP_GENTLY, null, null,
        )
        mBuffer.addContainerEnd()
    }

    fun addModifierScroll(direction: Int, positionId: Float, notches: Int) {
        val max = reserveFloatVariable()
        val notchMax = reserveFloatVariable()
        val touchExpressionDirection =
            if (direction != 0) RemoteContext.FLOAT_TOUCH_POS_X else RemoteContext.FLOAT_TOUCH_POS_Y
        ScrollModifierOperation.apply(mBuffer.getBuffer(), direction, positionId, max, notchMax)
        mBuffer.addTouchExpression(
            Utils.idFromNan(positionId), 0f, 0f, max, 0f, 3,
            floatArrayOf(touchExpressionDirection, -1f, MUL),
            TouchExpression.STOP_NOTCHES_EVEN,
            floatArrayOf(notches.toFloat(), notchMax), null,
        )
        mBuffer.addContainerEnd()
    }

    fun addModifierScroll(direction: Int) {
        val max = reserveFloatVariable()
        mBuffer.addModifierScroll(direction, max)
    }

    fun textComponent(
        modifier: RecordingModifier, textId: Int, color: Int,
        fontSize: Float, fontStyle: Int, fontWeight: Float, fontFamily: String?,
        textAlign: Int, overflow: Int, maxLines: Int, content: RemoteComposeWriterInterface,
    ) {
        startTextComponent(modifier, textId, color, fontSize, fontStyle, fontWeight, fontFamily, textAlign, overflow, maxLines)
        content.run()
        endTextComponent()
    }

    fun textComponent(
        modifier: RecordingModifier, textId: Int, color: Int, colorId: Int,
        fontSize: Float, fontStyle: Int, fontWeight: Float, fontFamily: String?,
        textAlign: Int, overflow: Int, maxLines: Int,
        letterSpacing: Float, lineHeightAdd: Float, lineHeightMultiplier: Float,
        lineBreakStrategy: Int, hyphenationFrequency: Int, justificationMode: Int,
        underline: Boolean, strikethrough: Boolean,
        fontAxis: Array<String>?, fontAxisValues: FloatArray?,
        autosize: Boolean, flags: Int, content: RemoteComposeWriterInterface,
    ) {
        startTextComponent(
            modifier, textId, color, colorId, fontSize, fontStyle, fontWeight, fontFamily,
            textAlign, overflow, maxLines, letterSpacing, lineHeightAdd, lineHeightMultiplier,
            lineBreakStrategy, hyphenationFrequency, justificationMode, underline, strikethrough,
            fontAxis, fontAxisValues, autosize, flags,
        )
        content.run()
        endTextComponent()
    }

    fun startTextComponent(
        modifier: RecordingModifier, textId: Int, color: Int,
        fontSize: Float, fontStyle: Int, fontWeight: Float, fontFamily: String?,
        textAlign: Int, overflow: Int, maxLines: Int,
    ) {
        startTextComponent(modifier, textId, color, fontSize, fontStyle, fontWeight, fontFamily,
            0.toShort(), textAlign.toShort(), overflow, maxLines)
    }

    fun startTextComponent(
        modifier: RecordingModifier, textId: Int, color: Int,
        fontSize: Float, fontStyle: Int, fontWeight: Float, fontFamily: String?,
        flags: Short, textAlign: Short, overflow: Int, maxLines: Int,
    ) {
        val fontFamilyId = if (fontFamily != null) addText(fontFamily) else -1
        mBuffer.addTextComponentStart(
            modifier.getComponentId(), -1, textId, color, fontSize, fontStyle, fontWeight,
            fontFamilyId, flags, textAlign, overflow, maxLines,
        )
        for (m in modifier.getList()) m.write(this)
        addContentStart()
    }

    fun startTextComponent(
        modifier: RecordingModifier, textId: Int, color: Int, colorId: Int,
        fontSize: Float, fontStyle: Int, fontWeight: Float, fontFamily: String?,
        textAlign: Int, overflow: Int, maxLines: Int,
        letterSpacing: Float, lineHeightAdd: Float, lineHeightMultiplier: Float,
        lineBreakStrategy: Int, hyphenationFrequency: Int, justificationMode: Int,
        underline: Boolean, strikethrough: Boolean,
        fontAxis: Array<String>?, fontAxisValues: FloatArray?,
        autosize: Boolean, flags: Int,
    ) {
        val fontFamilyId = if (fontFamily != null) addText(fontFamily) else -1
        var axis: IntArray? = null
        if (fontAxis != null) {
            axis = IntArray(fontAxis.size) { addText(fontAxis[it]) }
        }
        mBuffer.addTextComponentStart(
            modifier.getComponentId(), -1, textId, color, colorId, fontSize, fontStyle, fontWeight,
            fontFamilyId, textAlign, overflow, maxLines, letterSpacing, lineHeightAdd,
            lineHeightMultiplier, lineBreakStrategy, hyphenationFrequency, justificationMode,
            underline, strikethrough, axis, fontAxisValues, autosize, flags,
        )
        for (m in modifier.getList()) m.write(this)
        addContentStart()
    }

    fun endTextComponent() { mBuffer.addContainerEnd(); mBuffer.addContainerEnd() }

    fun box(modifier: RecordingModifier, horizontal: Int, vertical: Int) {
        mBuffer.addBoxStart(modifier.getComponentId(), -1, horizontal, vertical)
        for (m in modifier.getList()) m.write(this)
        mBuffer.addContainerEnd()
    }

    fun box(modifier: RecordingModifier) = box(modifier, BoxLayout.CENTER, BoxLayout.CENTER)

    fun addStringList(vararg strs: String?): Float {
        val ids = IntArray(strs.size) { textCreateId(strs[it] ?: "") }
        return addList(ids)
    }

    fun addStringList(vararg strIds: Int): Float {
        val id = mState.cacheData(strIds, NanMap.TYPE_ARRAY)
        mBuffer.addList(id, strIds)
        return Utils.asNan(id)
    }

    fun addList(listId: IntArray): Float {
        val id = mState.cacheData(listId, NanMap.TYPE_ARRAY)
        mBuffer.addList(id, listId)
        return Utils.asNan(id)
    }

    fun addFloatArray(values: FloatArray): Float {
        val id = mState.cacheData(values, NanMap.TYPE_ARRAY)
        mBuffer.addFloatArray(id, values)
        return Utils.asNan(id)
    }

    fun addFloatArray(size: Float): Float {
        val values = FloatArray(size.toInt())
        val id = mState.cacheData(values, NanMap.TYPE_ARRAY)
        mBuffer.addDynamicFloatArray(id, size)
        return Utils.asNan(id)
    }

    fun addFloatArray(id: Int, size: Float): Float {
        val values = FloatArray(size.toInt())
        mState.cacheData(id, values)
        mBuffer.addFloatArray(id, values)
        return Utils.asNan(id)
    }

    fun setArrayValue(id: Int, index: Float, value: Float) =
        mBuffer.setArrayValue(id, index, value)

    fun addDynamicFloatArray(size: Float): Float {
        val id = createID(NanMap.TYPE_ARRAY)
        mBuffer.addDynamicFloatArray(id, size)
        return Utils.asNan(id)
    }

    fun addDynamicFloatArray(id: Int, size: Float): Float {
        mBuffer.addDynamicFloatArray(id, size)
        return Utils.asNan(id)
    }

    fun addFloatList(values: FloatArray): Float {
        val listId = IntArray(values.size) {
            val iid = mState.cacheFloat(values[it])
            mBuffer.addFloat(iid, values[it])
            iid
        }
        return addList(listId)
    }

    fun addFloatMap(keys: Array<String>, values: FloatArray): Float {
        val listId = IntArray(values.size)
        val type = ByteArray(values.size)
        for (i in values.indices) {
            listId[i] = mState.cacheFloat(values[i])
            mBuffer.addFloat(listId[i], values[i])
            type[i] = DataMapIds.TYPE_FLOAT
        }
        val id = mState.cacheData(listId, NanMap.TYPE_ARRAY)
        mBuffer.addMap(id, keys, type, listId)
        return Utils.asNan(id)
    }

    fun storeBitmap(image: Any): Int {
        var imageId = mState.dataGetId(image)
        if (imageId == -1) {
            imageId = mState.cacheData(image)
            val data = mPlatform.imageToByteArray(image) ?: ByteArray(0)
            val imageWidth = mPlatform.getImageWidth(image)
            val imageHeight = mPlatform.getImageHeight(image)
            if (mPlatform.isAlpha8Image(image)) {
                mBuffer.storeBitmapA8(imageId, imageWidth, imageHeight, data)
            } else {
                mBuffer.storeBitmap(imageId, imageWidth, imageHeight, data)
            }
        }
        return imageId
    }

    fun addBitmap(image: Any): Int = storeBitmap(image)
    fun addBitmap(image: Any, name: String): Int {
        val id = storeBitmap(image)
        nameBitmapId(id, name)
        return id
    }

    fun addBitmapFont(glyphs: Array<BitmapFontData.Glyph>): Int {
        val id = mState.createNextAvailableId()
        return mBuffer.addBitmapFont(id, glyphs)
    }

    fun addBitmapFont(glyphs: Array<BitmapFontData.Glyph>, kerningTable: Map<String, Short>): Int {
        val id = mState.createNextAvailableId()
        return mBuffer.addBitmapFont(id, glyphs, kerningTable)
    }

    fun nameBitmapId(id: Int, omicron: String) = mBuffer.setBitmapName(id, omicron)

    fun createFloatId(): Float = asFloatId(createID(0).toLong())

    fun impulse(duration: Float, start: Float) = mBuffer.addImpulse(duration, start)
    fun impulse(duration: Float, start: Float, run: () -> Unit) {
        mBuffer.addImpulse(duration, start); run(); mBuffer.addImpulseEnd()
    }
    fun impulseProcess(run: () -> Unit) { mBuffer.addImpulseProcess(); run(); mBuffer.addImpulseEnd() }
    fun impulseProcess() = mBuffer.addImpulseProcess()
    fun impulseEnd() = mBuffer.addImpulseEnd()

    fun createParticles(variables: FloatArray, initialExpressions: Array<FloatArray>, particleCount: Int): Float {
        val id = createID(0)
        val index = asFloatId(id.toLong())
        val varId = IntArray(variables.size) {
            val vid = createID(0)
            variables[it] = asFloatId(vid.toLong())
            vid
        }
        mBuffer.addParticles(id, varId, initialExpressions, particleCount)
        return index
    }

    fun particlesLoop(id: Float, restart: FloatArray?, expressions: Array<FloatArray>, r: () -> Unit) {
        mBuffer.addParticlesLoop(Utils.idFromNan(id), restart, expressions)
        r()
        mBuffer.addParticleLoopEnd()
    }

    fun particlesComparison(
        id: Float, flags: Short, min: Float, max: Float,
        condition: FloatArray?, then1: Array<FloatArray>?, then2: Array<FloatArray>?,
        r: (() -> Unit)?,
    ) {
        mBuffer.addParticlesComparison(Utils.idFromNan(id), flags, min, max, condition, then1, then2)
        r?.invoke()
        mBuffer.addParticleLoopEnd()
    }

    fun particlesComparison(
        id: Float, flags: Short, min: Float, max: Float,
        condition: FloatArray?, then: Array<FloatArray>?, r: (() -> Unit)?,
    ) {
        mBuffer.addParticlesComparison(Utils.idFromNan(id), flags, min, max, condition, then, null)
        r?.invoke()
        mBuffer.addParticleLoopEnd()
    }

    fun createFloatFunction(args: FloatArray): Int {
        val fid = mState.createNextAvailableId()
        val intArgs = IntArray(args.size) {
            val aid = createID(0)
            args[it] = asFloatId(aid.toLong())
            aid
        }
        mBuffer.defineFloatFunction(fid, intArgs)
        return fid
    }

    fun endFloatFunction() = mBuffer.addEndFloatFunctionDef()
    fun callFloatFunction(id: Int, vararg args: Float) = mBuffer.callFloatFunction(id, args)
    fun addTimeLong(time: Long): Int = addLong(time)

    fun addDebugMessage(message: String) { val textId = addText(message); mBuffer.addDebugMessage(textId, 0f, 0) }
    fun addDebugMessage(message: String, value: Float) { val textId = addText(message); mBuffer.addDebugMessage(textId, value, 0) }
    fun addDebugMessage(message: String, value: Float, flag: Int) { val textId = addText(message); mBuffer.addDebugMessage(textId, value, flag) }
    fun addDebugMessage(textId: Int, value: Float, flag: Int) = mBuffer.addDebugMessage(textId, value, flag)

    fun matrixExpression(vararg exp: Float): Float {
        val id = mState.createNextAvailableId()
        mBuffer.addMatrixExpression(id, exp)
        return Utils.asNan(id)
    }

    fun addFont(data: ByteArray): Int {
        val id = mState.createNextAvailableId()
        mBuffer.addFont(id, 0, data)
        return id
    }

    fun createBitmap(width: Int, height: Int): Int {
        val id = mState.createNextAvailableId()
        return mBuffer.createBitmap(id, width.toShort(), height.toShort())
    }

    fun drawOnBitmap(bitmapId: Int, mode: Int, color: Int) = mBuffer.drawOnBitmap(bitmapId, mode, color)
    fun drawOnBitmap(bitmapId: Int) = mBuffer.drawOnBitmap(bitmapId, 0, 0)

    fun addComponentVisibilityOperation(valueId: Int) = mBuffer.addComponentVisibilityOperation(valueId)
    fun addWidthModifierOperation(type: Int, valueId: Float) = mBuffer.addWidthModifierOperation(type, valueId)
    fun addHeightModifierOperation(type: Int, valueId: Float) = mBuffer.addHeightModifierOperation(type, valueId)
    fun addModifierRipple() = mBuffer.addModifierRipple()
    fun addModifierZIndex(value: Float) = mBuffer.addModifierZIndex(value)
    fun addModifierMarquee(iterations: Int, animationMode: Int, repeatDelayMillis: Float, initialDelayMillis: Float, spacing: Float, velocity: Float) =
        mBuffer.addModifierMarquee(iterations, animationMode, repeatDelayMillis, initialDelayMillis, spacing, velocity)
    fun addHeightInModifierOperation(min: Float, max: Float) = mBuffer.addHeightInModifierOperation(min, max)
    fun addModifierGraphicsLayer(attributes: HashMap<Int, Any>) = mBuffer.addModifierGraphicsLayer(attributes)
    fun addTouchDownModifierOperation() = mBuffer.addTouchDownModifierOperation()
    fun addTouchUpModifierOperation() = mBuffer.addTouchUpModifierOperation()
    fun addTouchCancelModifierOperation() = mBuffer.addTouchCancelModifierOperation()
    fun addContainerEnd() = mBuffer.addContainerEnd()
    fun addModifierOffset(x: Float, y: Float) = mBuffer.addModifierOffset(x, y)
    fun addModifierBackground(color: Int, shape: Int) = mBuffer.addModifierBackground(color, shape)
    fun addDynamicModifierBackground(colorId: Int, shape: Int) = mBuffer.addDynamicModifierBackground(colorId, shape)
    fun addModifierBackground(r: Float, g: Float, b: Float, a: Float, shape: Int) = mBuffer.addModifierBackground(r, g, b, a, shape)
    fun addAlignByModifier(line: Float) = mBuffer.addModifierAlignBy(line)
    fun addClipRectModifier() = mBuffer.addClipRectModifier()
    fun addRoundClipRectModifier(topStart: Float, topEnd: Float, bottomStart: Float, bottomEnd: Float) =
        mBuffer.addRoundClipRectModifier(topStart, topEnd, bottomStart, bottomEnd)
    fun addWidthInModifierOperation(min: Float, max: Float) = mBuffer.addWidthInModifierOperation(min, max)
    fun addModifierPadding(left: Float, top: Float, right: Float, bottom: Float) = mBuffer.addModifierPadding(left, top, right, bottom)
    fun addDrawContentOperation() = mBuffer.addDrawContentOperation()

    fun addLayoutCompute(type: Int, commands: ComponentLayoutChangesWriter) {
        val boundsId = createID(NanMap.TYPE_ARRAY)
        val c: ComponentLayoutChanges = InternalComponentLayoutChanges(type, boundsId, this)
        mBuffer.startLayoutCompute(type, boundsId, false)
        addDynamicFloatArray(boundsId, 6f)
        commands.run(c)
        mBuffer.endLayoutCompute()
    }

    fun addSemanticsModifier(contentDescriptionId: Int, role: Byte, textId: Int, stateDescriptionId: Int, mode: Int, enabled: Boolean, clickable: Boolean) =
        mBuffer.addSemanticsModifier(contentDescriptionId, role, textId, stateDescriptionId, mode, enabled, clickable)
    fun addClickModifierOperation() = mBuffer.addClickModifierOperation()
    fun addCollapsiblePriorityModifier(orientation: Int, priority: Float) = mBuffer.addCollapsiblePriorityModifier(orientation, priority)
    fun addAnimationSpecModifier(animationId: Int, motionDuration: Float, motionEasingType: Int, visibilityDuration: Float, visibilityEasingType: Int, enterAnimation: Int, exitAnimation: Int) =
        mBuffer.addAnimationSpecModifier(animationId, motionDuration, motionEasingType, visibilityDuration, visibilityEasingType, enterAnimation, exitAnimation)
    fun addModifierBorder(width: Float, roundedCorner: Float, color: Int, shapeType: Int) = mBuffer.addModifierBorder(width, roundedCorner, color, shapeType)
    fun addModifierDynamicBorder(width: Float, roundedCorner: Float, colorId: Int, shapeType: Int) = mBuffer.addModifierDynamicBorder(width, roundedCorner, colorId, shapeType)
    fun addValueStringChangeActionOperation(destTextId: Int, srcTextId: Int) = mBuffer.addValueStringChangeActionOperation(destTextId, srcTextId)
    fun addValueIntegerExpressionChangeActionOperation(destIntegerId: Long, srcIntegerId: Long) = mBuffer.addValueIntegerExpressionChangeActionOperation(destIntegerId, srcIntegerId)
    fun addValueFloatChangeActionOperation(valueId: Int, value: Float) = mBuffer.addValueFloatChangeActionOperation(valueId, value)
    fun addValueIntegerChangeActionOperation(valueId: Int, value: Int) = mBuffer.addValueIntegerChangeActionOperation(valueId, value)
    fun addValueFloatExpressionChangeActionOperation(valueId: Int, value: Int) = mBuffer.addValueFloatExpressionChangeActionOperation(valueId, value)

    fun beginGlobal() {
        if (mStartGlobalSection != -1) throw RuntimeException("Trying to start a global section twice")
        mStartGlobalSection = mBuffer.getBuffer().size
    }

    fun endGlobal() {
        if (mStartGlobalSection == -1) throw RuntimeException("Trying to end a global section without a begin")
        val bytes = mBuffer.getBuffer().size - mStartGlobalSection
        mBuffer.getBuffer().moveBlock(mStartGlobalSection, mInsertPoint)
        mInsertPoint += bytes
        mStartGlobalSection = -1
    }

    private class InternalComponentLayoutChanges(
        private val mType: Int,
        boundsId: Int,
        private val mWriter: RemoteComposeWriter,
    ) : ComponentLayoutChanges {
        private val mBounds: Float = Utils.asNan(boundsId)

        private fun set(index: Int, value: Number) {
            if (mType == TYPE_MEASURE && index < 2) throw RuntimeException("Trying to set position value in a compute measure")
            if (mType == TYPE_POSITION && index > 1) throw RuntimeException("Trying to set measure value in a compute position")
            if (value is RFloat) {
                value.writer = mWriter
                mWriter.setArrayValue(Utils.idFromNan(mBounds), index.toFloat(), value.toFloat())
            } else {
                mWriter.setArrayValue(Utils.idFromNan(mBounds), index.toFloat(), value.toFloat())
            }
        }

        private fun get(index: Float): RFloat = RFloat(mWriter, floatArrayOf(mBounds, index, A_DEREF))

        override fun setX(value: Number) = set(0, value)
        override fun setY(value: Number) = set(1, value)
        override fun setWidth(value: Number) = set(2, value)
        override fun setHeight(value: Number) = set(3, value)
        override fun getX(): RFloat = get(0f)
        override fun getY(): RFloat = get(1f)
        override fun getWidth(): RFloat = get(2f)
        override fun getHeight(): RFloat = get(3f)
        override fun getParentWidth(): RFloat = get(4f)
        override fun getParentHeight(): RFloat = get(5f)
    }

    companion object {
        val TIME_IN_CONTINUOUS_SEC: Float = RemoteContext.FLOAT_CONTINUOUS_SEC
        val FONT_TYPE_DEFAULT: Int = PaintBundle.FONT_TYPE_DEFAULT
        val FONT_TYPE_SANS_SERIF: Int = PaintBundle.FONT_TYPE_SANS_SERIF
        val FONT_TYPE_SERIF: Int = PaintBundle.FONT_TYPE_SERIF
        val FONT_TYPE_MONOSPACE: Int = PaintBundle.FONT_TYPE_MONOSPACE

        val COMBINE_DIFFERENCE: Byte = PathCombine.OP_DIFFERENCE
        val COMBINE_INTERSECT: Byte = PathCombine.OP_INTERSECT
        val COMBINE_REVERSE_DIFFERENCE: Byte = PathCombine.OP_REVERSE_DIFFERENCE
        val COMBINE_UNION: Byte = PathCombine.OP_UNION
        val COMBINE_XOR: Byte = PathCombine.OP_XOR

        val IMAGE_SCALE_NONE: Int = ImageScaling.SCALE_NONE
        val IMAGE_SCALE_INSIDE: Int = ImageScaling.SCALE_INSIDE
        val IMAGE_SCALE_FILL_WIDTH: Int = ImageScaling.SCALE_FILL_WIDTH
        val IMAGE_SCALE_FILL_HEIGHT: Int = ImageScaling.SCALE_FILL_HEIGHT
        val IMAGE_SCALE_FIT: Int = ImageScaling.SCALE_FIT
        val IMAGE_SCALE_CROP: Int = ImageScaling.SCALE_CROP
        val IMAGE_SCALE_FILL_BOUNDS: Int = ImageScaling.SCALE_FILL_BOUNDS
        val IMAGE_SCALE_FIXED_SCALE: Int = ImageScaling.SCALE_FIXED_SCALE
        val IMAGE_REFERENCE: Int = 1 shl 8

        val ID_REFERENCE: Int = 1 shl 15
        val SNAP_WHEN_LESS: Int = 1 shl 10
        val SNAP_WHEN_MORE: Int = 2 shl 10
        val PROPAGATE_ANIMATION: Int = 4 shl 10

        val STOP_GENTLY: Byte = TouchExpression.STOP_GENTLY.toByte()
        val STOP_ENDS: Byte = TouchExpression.STOP_ENDS.toByte()
        val STOP_INSTANTLY: Byte = TouchExpression.STOP_INSTANTLY.toByte()
        val STOP_NOTCHES_EVEN: Byte = TouchExpression.STOP_NOTCHES_EVEN.toByte()
        val STOP_NOTCHES_PERCENTS: Byte = TouchExpression.STOP_NOTCHES_PERCENTS.toByte()
        val STOP_NOTCHES_ABSOLUTE: Byte = TouchExpression.STOP_NOTCHES_ABSOLUTE.toByte()
        val STOP_ABSOLUTE_POS: Byte = TouchExpression.STOP_ABSOLUTE_POS.toByte()

        val L_ADD: Long = 0x100000000L + IntegerExpressionEvaluator.I_ADD
        val L_SUB: Long = 0x100000000L + IntegerExpressionEvaluator.I_SUB
        val L_MUL: Long = 0x100000000L + IntegerExpressionEvaluator.I_MUL
        val L_DIV: Long = 0x100000000L + IntegerExpressionEvaluator.I_DIV
        val L_MOD: Long = 0x100000000L + IntegerExpressionEvaluator.I_MOD
        val L_SHL: Long = 0x100000000L + IntegerExpressionEvaluator.I_SHL
        val L_SHR: Long = 0x100000000L + IntegerExpressionEvaluator.I_SHR
        val L_USHR: Long = 0x100000000L + IntegerExpressionEvaluator.I_USHR
        val L_OR: Long = 0x100000000L + IntegerExpressionEvaluator.I_OR
        val L_AND: Long = 0x100000000L + IntegerExpressionEvaluator.I_AND
        val L_XOR: Long = 0x100000000L + IntegerExpressionEvaluator.I_XOR
        val L_COPY_SIGN: Long = 0x100000000L + IntegerExpressionEvaluator.I_COPY_SIGN
        val L_MIN: Long = 0x100000000L + IntegerExpressionEvaluator.I_MIN
        val L_MAX: Long = 0x100000000L + IntegerExpressionEvaluator.I_MAX
        val L_NEG: Long = 0x100000000L + IntegerExpressionEvaluator.I_NEG
        val L_ABS: Long = 0x100000000L + IntegerExpressionEvaluator.I_ABS
        val L_INCR: Long = 0x100000000L + IntegerExpressionEvaluator.I_INCR
        val L_DECR: Long = 0x100000000L + IntegerExpressionEvaluator.I_DECR
        val L_NOT: Long = 0x100000000L + IntegerExpressionEvaluator.I_NOT
        val L_SIGN: Long = 0x100000000L + IntegerExpressionEvaluator.I_SIGN
        val L_CLAMP: Long = 0x100000000L + IntegerExpressionEvaluator.I_CLAMP
        val L_IFELSE: Long = 0x100000000L + IntegerExpressionEvaluator.I_IFELSE
        val L_MAD: Long = 0x100000000L + IntegerExpressionEvaluator.I_MAD
        val L_VAR1: Long = 0x100000000L + IntegerExpressionEvaluator.I_VAR1
        val L_VAR2: Long = 0x100000000L + IntegerExpressionEvaluator.I_VAR2

        fun hTag(tag: Short, value: Any): HTag = HTag(tag, value)
        fun map(name: String, value: Float) = DataMap(name, value)
        fun map(name: String, value: Int) = DataMap(name, value)
        fun map(name: String, value: Long) = DataMap(name, value)
        fun map(name: String, value: String) = DataMap(name, value)
        fun map(name: String, value: Boolean) = DataMap(name, value)

        fun obtain(width: Int, height: Int, contentDescription: String, profile: Profile): RemoteComposeWriter =
            profile.create(CreationDisplayInfo(width, height, 1f), null)

        fun obtain(width: Int, height: Int, profile: Profile): RemoteComposeWriter =
            profile.create(CreationDisplayInfo(width, height, 1f), null)
    }
}

// Helpers needed for Float.floatToIntBits (Kotlin multiplatform)
private fun Float.Companion.floatToIntBits(value: Float): Int = value.toRawBits()
private fun Float.Companion.fromBits(bits: Int): Float = kotlin.Float.fromBits(bits)
