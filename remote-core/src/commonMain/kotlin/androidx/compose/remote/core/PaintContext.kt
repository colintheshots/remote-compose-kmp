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

import androidx.compose.remote.core.operations.paint.PaintBundle

/** Specify an abstract paint context used by RemoteCompose commands to draw */
abstract class PaintContext(context: RemoteContext) {

    var mContext: RemoteContext = context

    private var mNeedsRepaint: Boolean = false

    fun getContext(): RemoteContext = mContext

    /**
     * Returns true if the needsRepaint flag is set
     *
     * @return true if the document asks to be repainted
     */
    fun doesNeedsRepaint(): Boolean = mNeedsRepaint

    /** Clear the needsRepaint flag */
    fun clearNeedsRepaint() {
        mNeedsRepaint = false
    }

    fun setContext(context: RemoteContext) {
        this.mContext = context
    }

    /** convenience function to call matrixSave() */
    open fun save() {
        matrixSave()
    }

    /** convenience function to call matrixRestore() */
    open fun restore() {
        matrixRestore()
    }

    /** convenience function to call matrixSave() */
    open fun saveLayer(x: Float, y: Float, width: Float, height: Float) {
        // TODO
        matrixSave()
    }

    fun getCurrentTimeMillis(): Long = mContext.getClock().millis()

    /**
     * Draw a bitmap
     *
     * @param imageId
     * @param srcLeft
     * @param srcTop
     * @param srcRight
     * @param srcBottom
     * @param dstLeft
     * @param dstTop
     * @param dstRight
     * @param dstBottom
     * @param cdId
     */
    abstract fun drawBitmap(
        imageId: Int,
        srcLeft: Int,
        srcTop: Int,
        srcRight: Int,
        srcBottom: Int,
        dstLeft: Int,
        dstTop: Int,
        dstRight: Int,
        dstBottom: Int,
        cdId: Int
    )

    /**
     * scale the following commands
     *
     * @param scaleX horizontal scale factor
     * @param scaleY vertical scale factor
     */
    abstract fun scale(scaleX: Float, scaleY: Float)

    /**
     * Rotate the following commands
     *
     * @param translateX horizontal translation
     * @param translateY vertical translation
     */
    abstract fun translate(translateX: Float, translateY: Float)

    /**
     * Draw an arc
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @param startAngle
     * @param sweepAngle
     */
    abstract fun drawArc(
        left: Float, top: Float, right: Float, bottom: Float,
        startAngle: Float, sweepAngle: Float
    )

    /**
     * Draw a sector
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @param startAngle
     * @param sweepAngle
     */
    abstract fun drawSector(
        left: Float, top: Float, right: Float, bottom: Float,
        startAngle: Float, sweepAngle: Float
    )

    /**
     * Draw a bitmap
     *
     * @param id
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    abstract fun drawBitmap(id: Int, left: Float, top: Float, right: Float, bottom: Float)

    /**
     * Draw a circle
     *
     * @param centerX
     * @param centerY
     * @param radius
     */
    abstract fun drawCircle(centerX: Float, centerY: Float, radius: Float)

    /**
     * Draw a line
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    abstract fun drawLine(x1: Float, y1: Float, x2: Float, y2: Float)

    /**
     * Draw an oval
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    abstract fun drawOval(left: Float, top: Float, right: Float, bottom: Float)

    /**
     * Draw a path
     *
     * @param id the path id
     * @param start starting point of the path where we start drawing it
     * @param end ending point of the path where we stop drawing it
     */
    abstract fun drawPath(id: Int, start: Float, end: Float)

    /**
     * Draw a rectangle
     *
     * @param left left coordinate of the rectangle
     * @param top top coordinate of the rectangle
     * @param right right coordinate of the rectangle
     * @param bottom bottom coordinate of the rectangle
     */
    abstract fun drawRect(left: Float, top: Float, right: Float, bottom: Float)

    /** this caches the paint to a paint stack */
    abstract fun savePaint()

    /** This restores the paint from the paint stack */
    abstract fun restorePaint()

    /**
     * Replace the current paint with the PaintBundle
     *
     * @param paintBundle
     */
    abstract fun replacePaint(paintBundle: PaintBundle)

    /**
     * draw a round rect
     *
     * @param left left coordinate of the rectangle
     * @param top top coordinate of the rectangle
     * @param right right coordinate of the rectangle
     * @param bottom bottom coordinate of the rectangle
     * @param radiusX horizontal radius of the rounded corner
     * @param radiusY vertical radius of the rounded corner
     */
    abstract fun drawRoundRect(
        left: Float, top: Float, right: Float, bottom: Float,
        radiusX: Float, radiusY: Float
    )

    /**
     * Draw a round rect with individual corner radii
     */
    open fun drawRoundRect(
        left: Float, top: Float, right: Float, bottom: Float,
        tl: Float, tr: Float, br: Float, bl: Float
    ) {
        // Default implementation: use average radius
        val avgRadius = (tl + tr + br + bl) / 4f
        drawRoundRect(left, top, right, bottom, avgRadius, avgRadius)
    }

    /**
     * Draw the text glyphs on the provided path
     *
     * @param textId id of the text
     * @param pathId id of the path
     * @param hOffset horizontal offset
     * @param vOffset vertical offset
     */
    abstract fun drawTextOnPath(textId: Int, pathId: Int, hOffset: Float, vOffset: Float)

    /**
     * Return the dimensions (left, top, right, bottom). Relative to a drawTextRun x=0, y=0;
     *
     * @param textId
     * @param start
     * @param end if end is -1 it means the whole string
     * @param flags how to measure:
     *   - TEXT_MEASURE_MONOSPACE_WIDTH - measure as a monospace font
     *   - TEXT_MEASURE_FONT_HEIGHT - measure bounds of the given string using the max ascend
     *     and descent of the font (not just of the measured text).
     *   - TEXT_MEASURE_SPACES - make sure to include leading/trailing spaces in the measure
     *   - TEXT_COMPLEX - complex text
     * @param bounds the bounds (left, top, right, bottom)
     */
    abstract fun getTextBounds(textId: Int, start: Int, end: Int, flags: Int, bounds: FloatArray)

    /**
     * Compute complex text layout
     *
     * @param textId
     * @param start
     * @param end if end is -1 it means the whole string
     * @param alignment draw the text aligned start/center/end in the available space if > text
     *     length
     * @param overflow overflow behavior when text length > max width
     * @param maxLines maximum number of lines to display
     * @param maxWidth maximum width to layout the text
     * @param flags how to measure:
     *   - TEXT_MEASURE_MONOSPACE_WIDTH - measure as a monospace font
     *   - TEXT_MEASURE_FONT_HEIGHT - measure bounds of the given string using the max ascend
     *     and descent of the font (not just of the measured text).
     *   - TEXT_MEASURE_SPACES - make sure to include leading/trailing spaces in the measure
     *   - TEXT_COMPLEX - complex text
     *
     * @return an instance of a ComputedTextLayout (typically if complex text drawing is used)
     */
    abstract fun layoutComplexText(
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
        useUnderline: Boolean,
        strikethrough: Boolean,
        flags: Int
    ): RcPlatformServices.ComputedTextLayout?

    /**
     * Draw a text starting at x,y
     *
     * @param textId reference to the text
     * @param start
     * @param end
     * @param contextStart
     * @param contextEnd
     * @param x
     * @param y
     * @param rtl
     */
    abstract fun drawTextRun(
        textId: Int,
        start: Int,
        end: Int,
        contextStart: Int,
        contextEnd: Int,
        x: Float,
        y: Float,
        rtl: Boolean
    )

    /**
     * Draw a complex text (multilines, etc.)
     *
     * @param computedTextLayout pre-computed text layout
     */
    abstract fun drawComplexText(computedTextLayout: RcPlatformServices.ComputedTextLayout?)

    /**
     * Draw an interpolation between two paths
     *
     * @param path1Id
     * @param path2Id
     * @param tween 0.0 = is path1 1.0 is path2
     * @param start
     * @param end
     */
    abstract fun drawTweenPath(
        path1Id: Int, path2Id: Int, tween: Float, start: Float, end: Float
    )

    /**
     * Interpolate between two path and return the resulting path
     *
     * @param out the interpolated path
     * @param path1 start path
     * @param path2 end path
     * @param tween interpolation value from 0 (start path) to 1 (end path)
     */
    abstract fun tweenPath(out: Int, path1: Int, path2: Int, tween: Float)

    /**
     * Perform a between two path and return the resulting path
     *
     * @param out the interpolated path
     * @param path1 start path
     * @param path2 end path
     * @param operation 0 = difference , 1 = intersection, 2 = reverse_difference, 3 = union, 4 =
     *     xor
     */
    abstract fun combinePath(out: Int, path1: Int, path2: Int, operation: Byte)

    /**
     * This applies changes to the current paint
     *
     * @param mPaintData the list of changes
     */
    abstract fun applyPaint(mPaintData: PaintBundle)

    /**
     * Scale the rendering by scaleX and saleY (1.0 = no scale). Scaling is done about
     * centerX,centerY.
     *
     * @param scaleX
     * @param scaleY
     * @param centerX
     * @param centerY
     */
    abstract fun matrixScale(scaleX: Float, scaleY: Float, centerX: Float, centerY: Float)

    /**
     * Translate the rendering
     *
     * @param translateX
     * @param translateY
     */
    abstract fun matrixTranslate(translateX: Float, translateY: Float)

    /**
     * Skew the rendering
     *
     * @param skewX
     * @param skewY
     */
    abstract fun matrixSkew(skewX: Float, skewY: Float)

    /**
     * Rotate the rendering. Note rotates are cumulative.
     *
     * @param rotate angle to rotate
     * @param pivotX x-coordinate about which to rotate
     * @param pivotY y-coordinate about which to rotate
     */
    abstract fun matrixRotate(rotate: Float, pivotX: Float, pivotY: Float)

    /** Save the current state of the transform */
    abstract fun matrixSave()

    /** Restore the previously saved state of the transform */
    abstract fun matrixRestore()

    /**
     * Set the clip to a rectangle. Drawing outside the current clip region will have no effect
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    abstract fun clipRect(left: Float, top: Float, right: Float, bottom: Float)

    /**
     * Clip based on a path.
     *
     * @param pathId
     * @param regionOp
     */
    abstract fun clipPath(pathId: Int, regionOp: Int)

    /**
     * Clip based on a round rect
     *
     * @param width
     * @param height
     * @param topStart
     * @param topEnd
     * @param bottomStart
     * @param bottomEnd
     */
    abstract fun roundedClipRect(
        width: Float,
        height: Float,
        topStart: Float,
        topEnd: Float,
        bottomStart: Float,
        bottomEnd: Float
    )

    /** Reset the paint */
    abstract fun reset()

    /**
     * Returns true if the context is in debug mode
     *
     * @return true if in debug mode, false otherwise
     */
    fun isDebug(): Boolean = mContext.isBasicDebug()

    /**
     * Returns true if layout animations are enabled
     *
     * @return true if animations are enabled, false otherwise
     */
    fun isAnimationEnabled(): Boolean = mContext.isAnimationEnabled()

    /**
     * Utility function to log comments
     *
     * @param content the content to log
     */
    fun log(content: String) {
        println("[LOG] $content")
    }

    /** Indicates the document needs to be repainted */
    fun needsRepaint() {
        mNeedsRepaint = true
    }

    /**
     * Repaint in the given time
     *
     * @param seconds the delay in seconds to the next render loop pass
     */
    fun wakeIn(seconds: Float) {
        mContext.mRemoteComposeState.wakeIn(seconds)
    }

    /**
     * Starts a graphics layer
     *
     * @param w
     * @param h
     */
    abstract fun startGraphicsLayer(w: Int, h: Int)

    /**
     * Starts a graphics layer
     *
     * @param attributes
     */
    abstract fun setGraphicsLayer(attributes: HashMap<Int, Any>)

    /** Ends a graphics layer */
    abstract fun endGraphicsLayer()

    fun isVisualDebug(): Boolean = mContext.isVisualDebug()

    /**
     * Returns a String from an id
     *
     * @param id
     * @return the string if found
     */
    abstract fun getText(id: Int): String?

    /**
     * Returns true if the document has been encoded for at least the given version MAJOR.MINOR
     *
     * @param major major version number
     * @param minor minor version number
     * @param patch patch version number
     * @return true if the document was written at least with the given version
     */
    fun supportsVersion(major: Int, minor: Int, patch: Int): Boolean =
        mContext.supportsVersion(major, minor, patch)

    /**
     * Sets the Matrix from the path
     *
     * @param pathId id of the path
     * @param fraction fractional position in the path to use
     * @param vOffset vertical offset
     * @param flags flags
     */
    abstract fun matrixFromPath(pathId: Int, fraction: Float, vOffset: Float, flags: Int)

    /**
     * Redirect drawing to a bitmap (0 = back to main canvas)
     *
     * @param bitmapId id of bitmap to draw to or 0 to draw to the canvas
     * @param mode flags support init of bitmap 0 = clear to color, 1 = no clear
     * @param color set the initial color of the bitmap
     */
    abstract fun drawToBitmap(bitmapId: Int, mode: Int, color: Int)

    companion object {
        const val TEXT_MEASURE_MONOSPACE_WIDTH: Int = 0x01
        const val TEXT_MEASURE_FONT_HEIGHT: Int = 0x02
        const val TEXT_MEASURE_SPACES: Int = 0x04
        const val TEXT_COMPLEX: Int = 0x08
        const val TEXT_MEASURE_AUTOSIZE: Int = 0x10
    }
}
