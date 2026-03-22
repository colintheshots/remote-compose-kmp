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
package androidx.compose.remote.player.compose.context

import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.RcPlatformServices
import androidx.compose.remote.core.operations.ClipPath
import androidx.compose.remote.core.operations.paint.PaintBundle
import androidx.compose.remote.player.compose.platform.PlatformBitmapSupport
import androidx.compose.remote.player.compose.platform.PlatformGraphicsLayerSupport
import androidx.compose.remote.player.compose.platform.PlatformPaintSupport
import androidx.compose.remote.player.compose.platform.PlatformTextSupport
import androidx.compose.remote.player.compose.utils.FloatsToPath
import androidx.compose.remote.player.compose.utils.copy
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.PathOperation
import kotlin.math.atan2

/**
 * A [PaintContext] implementation for [androidx.compose.remote.player.compose.RemoteComposePlayer].
 */
internal class ComposePaintContext(
    remoteContext: ComposeRemoteContext,
    private var canvas: Canvas,
) : PaintContext(remoteContext) {

    var paint = Paint()
    var paintList: MutableList<Paint> = mutableListOf()
    var mainCanvas: Canvas? = null
    var previousCanvas: Canvas? = null
    var canvasCache: MutableMap<Any, Canvas> = mutableMapOf()

    private val textSupport = PlatformTextSupport()
    private val bitmapSupport = PlatformBitmapSupport()
    private val graphicsLayerSupport = PlatformGraphicsLayerSupport()
    private val paintSupport = PlatformPaintSupport()

    private val cachedPaintChanges = paintSupport.createPaintChanges(
        remoteContext = remoteContext,
        getPaint = { this.paint },
    )

    override fun drawBitmap(
        imageId: Int,
        srcLeft: Int,
        srcTop: Int,
        srcRight: Int,
        srcBottom: Int,
        dstLeft: Int,
        dstTop: Int,
        dstRight: Int,
        dstBottom: Int,
        cdId: Int,
    ) {
        val ctx = mContext as ComposeRemoteContext
        if (ctx.mRemoteComposeState.containsId(imageId)) {
            val imageData = ctx.mRemoteComposeState.getFromId(imageId)
            if (imageData != null) {
                bitmapSupport.drawBitmap(
                    imageData, srcLeft, srcTop, srcRight, srcBottom,
                    dstLeft, dstTop, dstRight, dstBottom, canvas, paint
                )
            }
        }
    }

    override fun scale(scaleX: Float, scaleY: Float) {
        canvas.scale(scaleX, scaleY)
    }

    override fun translate(translateX: Float, translateY: Float) {
        canvas.translate(translateX, translateY)
    }

    override fun drawArc(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        startAngle: Float,
        sweepAngle: Float,
    ) {
        canvas.drawArc(left, top, right, bottom, startAngle, sweepAngle, false, paint)
    }

    override fun drawSector(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        startAngle: Float,
        sweepAngle: Float,
    ) {
        canvas.drawArc(left, top, right, bottom, startAngle, sweepAngle, true, paint)
    }

    override fun drawBitmap(id: Int, left: Float, top: Float, right: Float, bottom: Float) {
        val ctx = mContext as ComposeRemoteContext
        if (ctx.mRemoteComposeState.containsId(id)) {
            val imageData = ctx.mRemoteComposeState.getFromId(id)
            if (imageData != null) {
                bitmapSupport.drawBitmap(imageData, left, top, right, bottom, canvas, paint)
            }
        }
    }

    override fun drawCircle(centerX: Float, centerY: Float, radius: Float) {
        canvas.drawCircle(Offset(centerX, centerY), radius, paint)
    }

    override fun drawLine(x1: Float, y1: Float, x2: Float, y2: Float) {
        canvas.drawLine(Offset(x1, y1), Offset(x2, y2), paint)
    }

    override fun drawOval(left: Float, top: Float, right: Float, bottom: Float) {
        canvas.drawOval(left, top, right, bottom, paint)
    }

    override fun drawPath(id: Int, start: Float, end: Float) {
        canvas.drawPath(getPath(id, start, end), paint)
    }

    override fun drawRect(left: Float, top: Float, right: Float, bottom: Float) {
        canvas.drawRect(left, top, right, bottom, paint)
    }

    override fun savePaint() {
        paintList.add(paint.copy())
    }

    override fun restorePaint() {
        paint = paintList.removeAt(paintList.size - 1)
    }

    override fun replacePaint(paintBundle: PaintBundle) {
        paintSupport.resetPaint(paint)
        applyPaint(paintBundle)
    }

    override fun drawRoundRect(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        radiusX: Float,
        radiusY: Float,
    ) {
        canvas.drawRoundRect(left, top, right, bottom, radiusX, radiusY, paint)
    }

    override fun drawTextOnPath(textId: Int, pathId: Int, hOffset: Float, vOffset: Float) {
        val text = getText(textId) ?: return
        val nativePath = getNativePath(pathId, 0f, 1f)
        textSupport.drawTextOnPath(text, nativePath, hOffset, vOffset, canvas, paint)
    }

    override fun getTextBounds(textId: Int, start: Int, end: Int, flags: Int, bounds: FloatArray) {
        val str = getText(textId) ?: return
        val endSanitized = if (end == -1 || end > str.length) str.length else end
        textSupport.getTextBounds(str.substring(start, endSanitized), start, endSanitized, flags, bounds, paint)
    }

    override fun layoutComplexText(
        textId: Int,
        start: Int,
        end: Int,
        alignment: Int,
        overflow: Int,
        maxLines: Int,
        maxWidth: Float,
        letterSpacing: Float,
        lineHeightAdd: Float,
        lineHeightMultiplier: Float,
        lineBreakStrategy: Int,
        hyphenationFrequency: Int,
        justificationMode: Int,
        underline: Boolean,
        strikethrough: Boolean,
        flags: Int,
    ): RcPlatformServices.ComputedTextLayout? {
        val str = getText(textId) ?: return null
        val endSanitized = if (end == -1 || end > str.length) str.length else end
        return textSupport.layoutComplexText(
            str, start, endSanitized, alignment, overflow, maxLines, maxWidth, paint
        )
    }

    override fun drawTextRun(
        textId: Int,
        start: Int,
        end: Int,
        contextStart: Int,
        contextEnd: Int,
        x: Float,
        y: Float,
        rtl: Boolean,
    ) {
        var textToPaint = getText(textId) ?: return
        if (end == -1) {
            if (start != 0) {
                textToPaint = textToPaint.substring(start)
            }
        } else if (end > textToPaint.length) {
            textToPaint = textToPaint.substring(start)
        } else {
            textToPaint = textToPaint.substring(start, end)
        }
        textSupport.drawTextRun(textToPaint, x, y, canvas, paint)
    }

    override fun drawComplexText(computedTextLayout: RcPlatformServices.ComputedTextLayout?) {
        if (computedTextLayout == null) return
        textSupport.drawComplexText(computedTextLayout, canvas)
    }

    override fun drawTweenPath(
        path1Id: Int,
        path2Id: Int,
        tween: Float,
        start: Float,
        stop: Float,
    ) {
        canvas.drawPath(getPath(path1Id, path2Id, tween, start, stop), paint)
    }

    override fun tweenPath(out: Int, path1: Int, path2: Int, tween: Float) {
        val p: FloatArray = getPathArray(path1, path2, tween)
        val ctx = mContext as ComposeRemoteContext
        ctx.mRemoteComposeState.putPathData(out, p)
    }

    override fun combinePath(out: Int, path1: Int, path2: Int, operation: Byte) {
        val p1 = getPath(path1, 0f, 1f)
        val p2 = getPath(path2, 0f, 1f)
        val op = arrayOf(
            PathOperation.Difference,
            PathOperation.Intersect,
            PathOperation.ReverseDifference,
            PathOperation.Union,
            PathOperation.Xor,
        )
        val p = Path.combine(op[operation.toInt()], p1, p2)
        val ctx = mContext as ComposeRemoteContext
        ctx.mRemoteComposeState.putPath(out, p)
    }

    override fun applyPaint(mPaintData: PaintBundle) {
        mPaintData.applyPaintChange(this, cachedPaintChanges)
    }

    override fun matrixScale(scaleX: Float, scaleY: Float, centerX: Float, centerY: Float) {
        if (centerX.isNaN()) {
            canvas.scale(scaleX, scaleY)
        } else {
            canvas.translate(centerX, centerY)
            canvas.scale(scaleX, scaleY)
            canvas.translate(-centerX, -centerY)
        }
    }

    override fun matrixTranslate(translateX: Float, translateY: Float) {
        canvas.translate(translateX, translateY)
    }

    override fun matrixSkew(skewX: Float, skewY: Float) {
        canvas.skew(skewX, skewY)
    }

    override fun matrixRotate(rotate: Float, pivotX: Float, pivotY: Float) {
        if (pivotX.isNaN()) {
            canvas.rotate(rotate)
        } else {
            canvas.translate(pivotX, pivotY)
            canvas.rotate(rotate)
            canvas.translate(-pivotX, -pivotY)
        }
    }

    override fun matrixSave() {
        canvas.save()
    }

    override fun matrixRestore() {
        canvas.restore()
    }

    override fun clipRect(left: Float, top: Float, right: Float, bottom: Float) {
        canvas.clipRect(left, top, right, bottom)
    }

    override fun clipPath(pathId: Int, regionOp: Int) {
        val path = getPath(pathId, 0f, 1f)
        if (regionOp == ClipPath.DIFFERENCE) {
            canvas.clipPath(path, ClipOp.Difference)
        } else {
            canvas.clipPath(path, ClipOp.Intersect)
        }
    }

    override fun roundedClipRect(
        width: Float,
        height: Float,
        topStart: Float,
        topEnd: Float,
        bottomStart: Float,
        bottomEnd: Float,
    ) {
        val roundedPath = Path()
        val roundRect = RoundRect(
            left = 0f,
            top = 0f,
            right = width,
            bottom = height,
            topLeftCornerRadius = CornerRadius(topStart, topStart),
            topRightCornerRadius = CornerRadius(topEnd, topEnd),
            bottomRightCornerRadius = CornerRadius(bottomEnd, bottomEnd),
            bottomLeftCornerRadius = CornerRadius(bottomStart, bottomStart),
        )
        roundedPath.addRoundRect(roundRect)
        canvas.clipPath(roundedPath)
    }

    override fun reset() {
        paintSupport.resetPaint(paint)
    }

    override fun startGraphicsLayer(w: Int, h: Int) {
        previousCanvas = canvas
        val newCanvas = graphicsLayerSupport.startGraphicsLayer(w, h)
        if (newCanvas != null) {
            canvas = newCanvas
        }
    }

    override fun setGraphicsLayer(attributes: HashMap<Int, Any>) {
        graphicsLayerSupport.setGraphicsLayer(attributes)
    }

    override fun endGraphicsLayer() {
        previousCanvas?.let { canvas = it }
        graphicsLayerSupport.endGraphicsLayer(canvas)
    }

    override fun getText(id: Int): String? {
        return mContext.mRemoteComposeState.getFromId(id) as String?
    }

    override fun matrixFromPath(pathId: Int, fraction: Float, vOffset: Float, flags: Int) {
        val path = getPath(pathId, 0f, 1f)
        if (path.isEmpty) return

        val measure = PathMeasure()
        measure.setPath(path, false)

        val matrix = Matrix()
        getMatrixFromPath(measure, matrix, fraction, flags)
        canvas.concat(matrix)
    }

    override fun drawToBitmap(bitmapId: Int, mode: Int, color: Int) {
        if (mainCanvas == null) {
            mainCanvas = canvas
        }
        if (bitmapId == 0) {
            canvas = mainCanvas!!
            return
        }
        val bitmap = mContext.mRemoteComposeState.getFromId(bitmapId)!!
        if (canvasCache.containsKey(bitmap)) {
            canvas = canvasCache[bitmap]!!
            if ((mode and 1) == 0) {
                bitmapSupport.eraseBitmap(bitmap, color)
            }
            return
        }
        canvas = bitmapSupport.getCanvasForBitmap(bitmap)
        if ((mode and 1) == 0) {
            bitmapSupport.eraseBitmap(bitmap, color)
        }
        canvasCache[bitmap] = canvas
    }

    private fun getPath(path1Id: Int, path2Id: Int, tween: Float, start: Float, end: Float): Path {
        return getPath(getPathArray(path1Id, path2Id, tween), start, end)
    }

    private fun getPath(tmp: FloatArray, start: Float, end: Float): Path {
        val path = Path()
        FloatsToPath.genPath(path, tmp, start, end)
        return path
    }

    private fun getPath(id: Int, start: Float, end: Float): Path {
        val p: Path? = mContext.mRemoteComposeState.getPath(id) as Path?
        val w: Int = mContext.mRemoteComposeState.getPathWinding(id)
        if (p != null) {
            return p
        }
        val path = Path()
        val pathData: FloatArray? = mContext.mRemoteComposeState.getPathData(id)
        if (pathData != null) {
            FloatsToPath.genPath(path, pathData, start, end)
            if (w == 1) {
                path.fillType = PathFillType.EvenOdd
            }
            mContext.mRemoteComposeState.putPath(id, path)
        }
        return path
    }

    /**
     * Get a native path for drawTextOnPath. On Android this needs to be an android.graphics.Path.
     * On other platforms it returns a compose Path.
     */
    private fun getNativePath(id: Int, start: Float, end: Float): Any {
        return getPath(id, start, end)
    }

    private fun getPathArray(path1Id: Int, path2Id: Int, tween: Float): FloatArray {
        val ctx = mContext as ComposeRemoteContext
        if (tween == 0.0f) {
            return ctx.mRemoteComposeState.getPathData(path1Id)!!
        }
        if (tween == 1.0f) {
            return ctx.mRemoteComposeState.getPathData(path2Id)!!
        }

        val data1: FloatArray = ctx.mRemoteComposeState.getPathData(path1Id)!!
        val data2: FloatArray = ctx.mRemoteComposeState.getPathData(path2Id)!!
        val tmp = FloatArray(data2.size)
        for (i in tmp.indices) {
            if (data1[i].isNaN() || data2[i].isNaN()) {
                tmp[i] = data1[i]
            } else {
                tmp[i] = (data2[i] - data1[i]) * tween + data1[i]
            }
        }
        return tmp
    }

    private fun getMatrixFromPath(measure: PathMeasure, matrix: Matrix, fraction: Float, flags: Int) {
        val len = measure.length
        if (len == 0f) return

        val distanceOnPath = (len * fraction) % len
        val position = measure.getPosition(distanceOnPath)
        matrix.translate(position.x, position.y)

        // Check if tangent/rotation is requested (TANGENT_MATRIX_FLAG = 2)
        if ((flags and 2) != 0) {
            val tangent = measure.getTangent(distanceOnPath)
            val angleRadians = atan2(tangent.y, tangent.x)
            val angleDegrees = (angleRadians * 180.0 / kotlin.math.PI).toFloat()
            matrix.rotateZ(angleDegrees)
        }
    }
}
