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

import androidx.compose.remote.core.RemotePathBase as RemotePathBaseCore
import androidx.compose.remote.core.operations.Utils
import androidx.compose.remote.core.operations.utilities.AnimatedFloatExpression

/**
 * RemotePath implementation that manages the path buffer.
 * Wraps [RemotePathBaseCore] from the core module.
 */
open class RemotePathBaseWrapper(protected val wrappedRemotePath: RemotePathBaseCore) {
    companion object {
        const val MOVE: Int = 10
        const val LINE: Int = 11
        const val QUADRATIC: Int = 12
        const val CONIC: Int = 13
        const val CUBIC: Int = 14
        const val CLOSE: Int = 15
        const val DONE: Int = 16
        val MOVE_NAN: Float = Utils.asNan(MOVE)
        val LINE_NAN: Float = Utils.asNan(LINE)
        val QUADRATIC_NAN: Float = Utils.asNan(QUADRATIC)
        val CONIC_NAN: Float = Utils.asNan(CONIC)
        val CUBIC_NAN: Float = Utils.asNan(CUBIC)
        val CLOSE_NAN: Float = Utils.asNan(CLOSE)
        val DONE_NAN: Float = Utils.asNan(DONE)

        /** Creates an approximate circle using remote float expressions. */
        @Suppress("FloatingPointLiteralPrecision")
        fun createCirclePath(
            rc: RemoteComposeWriter,
            x: Float,
            y: Float,
            rad: Float,
        ): RemotePath {
            val k = 0.5522847498f
            val c = rc.floatExpression(rad, k, AnimatedFloatExpression.MUL)
            val path = RemotePath()
            val xc = rc.floatExpression(x, c, AnimatedFloatExpression.ADD)
            val yc = rc.floatExpression(y, c, AnimatedFloatExpression.ADD)
            val xr = rc.floatExpression(x, rad, AnimatedFloatExpression.ADD)
            val yr = rc.floatExpression(y, rad, AnimatedFloatExpression.ADD)

            val x_c = rc.floatExpression(x, c, AnimatedFloatExpression.SUB)
            val y_c = rc.floatExpression(y, c, AnimatedFloatExpression.SUB)
            val x_r = rc.floatExpression(x, rad, AnimatedFloatExpression.SUB)
            val y_r = rc.floatExpression(y, rad, AnimatedFloatExpression.SUB)
            path.moveTo(xr, y)
            // clockwise
            path.cubicTo(xr, yc, xc, yr, x, yr)
            path.cubicTo(x_c, yr, x_r, yc, x_r, y)
            path.cubicTo(x_r, y_c, x_c, y_r, x, y_r)
            path.cubicTo(xc, y_r, xr, y_c, xr, y)
            path.close()
            return path
        }
    }

    val pathArray: FloatArray
        get() = wrappedRemotePath.getPath()

    val currentX: Float
        get() = wrappedRemotePath.getCurrentX()

    val currentY: Float
        get() = wrappedRemotePath.getCurrentY()

    val size: Int
        get() = wrappedRemotePath.getSize()

    constructor() : this(RemotePathBaseCore())

    constructor(bufferSize: Int) : this(RemotePathBaseCore(bufferSize))

    constructor(pathData: String) : this(RemotePathBaseCore(pathData))

    /** Reset the path. */
    open fun reset() {
        wrappedRemotePath.reset()
    }

    /** Reserve space. */
    open fun incReserve(extraPtCount: Int) {
        wrappedRemotePath.incReserve(extraPtCount)
    }

    private fun add(type: Int) {
        wrappedRemotePath.add(type)
    }

    protected fun addMove(type: Int, a1: Float, a2: Float) {
        wrappedRemotePath.addMove(type, a1, a2)
    }

    protected fun add(type: Int, a1: Float, a2: Float) {
        wrappedRemotePath.add(type, a1, a2)
    }

    protected fun add(type: Int, a1: Float, a2: Float, a3: Float, a4: Float) {
        wrappedRemotePath.add(type, a1, a2, a3, a4)
    }

    protected fun add(type: Int, a1: Float, a2: Float, a3: Float, a4: Float, a5: Float) {
        wrappedRemotePath.add(type, a1, a2, a3, a4, a5)
    }

    protected fun add(type: Int, a1: Float, a2: Float, a3: Float, a4: Float, a5: Float, a6: Float) {
        wrappedRemotePath.add(type, a1, a2, a3, a4, a5, a6)
    }

    /**
     * Set the beginning of the next contour to the point (x,y).
     *
     * @param x The x-coordinate of the start of a new contour
     * @param y The y-coordinate of the start of a new contour
     */
    open fun moveTo(x: Float, y: Float) {
        wrappedRemotePath.moveTo(x, y)
    }

    /**
     * Set the beginning of the next contour relative to the last point on the
     * previous contour. If there is no previous contour, this is treated the
     * same as moveTo().
     */
    open fun rMoveTo(dx: Float, dy: Float) {
        wrappedRemotePath.rMoveTo(dx, dy)
    }

    /** Add a quadratic bezier from the last point. */
    open fun quadTo(x1: Float, y1: Float, x2: Float, y2: Float) {
        wrappedRemotePath.quadTo(x1, y1, x2, y2)
    }

    /** Same as quadTo, but coordinates are relative to the last point. */
    open fun rQuadTo(dx1: Float, dy1: Float, dx2: Float, dy2: Float) {
        wrappedRemotePath.rQuadTo(dx1, dy1, dx2, dy2)
    }

    /** Add a conic bezier from the last point. */
    open fun conicTo(x1: Float, y1: Float, x2: Float, y2: Float, weight: Float) {
        wrappedRemotePath.conicTo(x1, y1, x2, y2, weight)
    }

    /** Same as conicTo, but coordinates are relative. */
    open fun rConicTo(dx1: Float, dy1: Float, dx2: Float, dy2: Float, weight: Float) {
        wrappedRemotePath.rConicTo(dx1, dy1, dx2, dy2, weight)
    }

    /** Add a line from the last point to the specified point (x,y). */
    open fun lineTo(x: Float, y: Float) {
        wrappedRemotePath.lineTo(x, y)
    }

    /** Same as lineTo, but coordinates are relative. */
    open fun rLineTo(dx: Float, dy: Float) {
        wrappedRemotePath.rLineTo(dx, dy)
    }

    /** Add a cubic bezier from the last point. */
    open fun cubicTo(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float) {
        wrappedRemotePath.cubicTo(x1, y1, x2, y2, x3, y3)
    }

    /** Same as cubicTo, but coordinates are relative. */
    open fun rCubicTo(
        dx1: Float,
        dy1: Float,
        dx2: Float,
        dy2: Float,
        dx3: Float,
        dy3: Float,
    ) {
        wrappedRemotePath.rCubicTo(dx1, dy1, dx2, dy2, dx3, dy3)
    }

    /** Close the current contour. */
    open fun close() {
        wrappedRemotePath.close()
    }

    /** Rewinds the path: clears lines and curves but keeps internal data structure. */
    open fun rewind() {
        wrappedRemotePath.rewind()
    }

    /** Returns true if the path is empty (contains no lines or curves). */
    open fun isEmpty(): Boolean = wrappedRemotePath.isEmpty()

    /** Append the specified arc to the path as a new contour. */
    open fun addArc(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        startAngle: Float,
        sweepAngle: Float,
    ) {
        wrappedRemotePath.addArc(left, top, right, bottom, startAngle, sweepAngle, false)
    }

    /** Append the specified arc to the path as a new contour. */
    open fun addArc(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        startAngle: Float,
        sweepAngle: Float,
        forceMoveTo: Boolean,
    ) {
        wrappedRemotePath.addArc(left, top, right, bottom, startAngle, sweepAngle, forceMoveTo)
    }

    override fun toString(): String = wrappedRemotePath.toString()

    /** Creates a float array of the same size as the pathArray. */
    open fun createFloatArray(): FloatArray = wrappedRemotePath.createFloatArray()
}
