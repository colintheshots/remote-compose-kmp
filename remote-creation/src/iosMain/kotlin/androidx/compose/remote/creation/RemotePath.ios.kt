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

import androidx.compose.remote.core.operations.PathData
import androidx.compose.remote.core.operations.Utils

/**
 * iOS PlatformRectF -- a simple data class since iOS does not have a built-in
 * RectF equivalent that we can typealias to.
 */
actual class PlatformRectF(
    val leftVal: Float,
    val topVal: Float,
    val rightVal: Float,
    val bottomVal: Float,
)

internal actual val PlatformRectF.left: Float
    get() = this.leftVal

internal actual val PlatformRectF.top: Float
    get() = this.topVal

internal actual val PlatformRectF.right: Float
    get() = this.rightVal

internal actual val PlatformRectF.bottom: Float
    get() = this.bottomVal

/**
 * iOS PlatformPath -- wraps a list of path commands since we cannot use
 * android.graphics.Path on iOS.
 */
actual class PlatformPath {
    internal val commands = mutableListOf<PathCommand>()

    fun moveTo(x: Float, y: Float) { commands.add(PathCommand.MoveTo(x, y)) }
    fun lineTo(x: Float, y: Float) { commands.add(PathCommand.LineTo(x, y)) }
    fun quadTo(x1: Float, y1: Float, x2: Float, y2: Float) {
        commands.add(PathCommand.QuadTo(x1, y1, x2, y2))
    }
    fun cubicTo(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float) {
        commands.add(PathCommand.CubicTo(x1, y1, x2, y2, x3, y3))
    }
    fun conicTo(x1: Float, y1: Float, x2: Float, y2: Float, weight: Float) {
        commands.add(PathCommand.ConicTo(x1, y1, x2, y2, weight))
    }
    fun close() { commands.add(PathCommand.Close) }
    fun reset() { commands.clear() }
    fun addPath(other: PlatformPath) { commands.addAll(other.commands) }
}

/** Sealed class representing path commands for the iOS path implementation. */
sealed class PathCommand {
    data class MoveTo(val x: Float, val y: Float) : PathCommand()
    data class LineTo(val x: Float, val y: Float) : PathCommand()
    data class QuadTo(val x1: Float, val y1: Float, val x2: Float, val y2: Float) : PathCommand()
    data class CubicTo(
        val x1: Float, val y1: Float,
        val x2: Float, val y2: Float,
        val x3: Float, val y3: Float,
    ) : PathCommand()
    data class ConicTo(
        val x1: Float, val y1: Float,
        val x2: Float, val y2: Float,
        val weight: Float,
    ) : PathCommand()
    data object Close : PathCommand()
}

/**
 * iOS PlatformMatrix -- a simple 3x3 affine transform matrix.
 */
actual class PlatformMatrix {
    /** Matrix values stored as [a, b, c, d, tx, ty] for the affine transform. */
    private val values = floatArrayOf(1f, 0f, 0f, 1f, 0f, 0f)

    /**
     * Transform points in-place. Each pair of floats at src[srcOffset..] is
     * treated as an (x,y) coordinate, transformed, and stored to dst[dstOffset..].
     */
    fun mapPoints(dst: FloatArray, dstOffset: Int, src: FloatArray, srcOffset: Int, pointCount: Int) {
        val a = values[0]; val b = values[1]
        val c = values[2]; val d = values[3]
        val tx = values[4]; val ty = values[5]
        for (i in 0 until pointCount) {
            val si = srcOffset + i * 2
            val di = dstOffset + i * 2
            val x = src[si]
            val y = src[si + 1]
            dst[di] = a * x + c * y + tx
            dst[di + 1] = b * x + d * y + ty
        }
    }
}

actual class RemotePath : RemotePathBaseWrapper {

    actual constructor() : super()

    actual constructor(bufferSize: Int) : super(bufferSize)

    actual constructor(pathData: String) : super(pathData)

    actual val path: PlatformPath
        get() {
            val result = PlatformPath()
            genPath(result, pathArray, size)
            return result
        }

    private fun genPath(
        retPath: PlatformPath,
        pathArray: FloatArray,
        length: Int,
    ) {
        var i = 0
        retPath.reset()

        while (i < length) {
            when (Utils.idFromNan(pathArray[i])) {
                PathData.MOVE -> {
                    i++
                    retPath.moveTo(pathArray[i], pathArray[i + 1])
                    i += 2
                }
                PathData.LINE -> {
                    i += 3
                    retPath.lineTo(pathArray[i], pathArray[i + 1])
                    i += 2
                }
                PathData.QUADRATIC -> {
                    i += 3
                    retPath.quadTo(
                        pathArray[i], pathArray[i + 1],
                        pathArray[i + 2], pathArray[i + 3],
                    )
                    i += 4
                }
                PathData.CONIC -> {
                    i += 3
                    retPath.conicTo(
                        pathArray[i], pathArray[i + 1],
                        pathArray[i + 2], pathArray[i + 3],
                        pathArray[i + 4],
                    )
                    i += 5
                }
                PathData.CUBIC -> {
                    i += 3
                    retPath.cubicTo(
                        pathArray[i], pathArray[i + 1],
                        pathArray[i + 2], pathArray[i + 3],
                        pathArray[i + 4], pathArray[i + 5],
                    )
                    i += 6
                }
                PathData.CLOSE -> {
                    retPath.close()
                    i++
                }
                PathData.DONE -> i++
                else -> i++ // skip unknown commands
            }
        }
    }

    actual fun transform(matrix: PlatformMatrix) {
        var i = 0
        while (i < size) {
            when (Utils.idFromNan(pathArray[i])) {
                PathData.MOVE -> {
                    i++
                    matrix.mapPoints(pathArray, i, pathArray, i, 1)
                    i += 2
                }
                PathData.LINE -> {
                    i += 3
                    matrix.mapPoints(pathArray, i, pathArray, i, 1)
                    i += 2
                }
                PathData.QUADRATIC -> {
                    i += 3
                    matrix.mapPoints(pathArray, i, pathArray, i, 2)
                    i += 4
                }
                PathData.CONIC -> {
                    i += 3
                    matrix.mapPoints(pathArray, i, pathArray, i, 2)
                    i += 5
                }
                PathData.CUBIC -> {
                    i += 3
                    matrix.mapPoints(pathArray, i, pathArray, i, 3)
                    i += 6
                }
                PathData.CLOSE,
                PathData.DONE -> i++
                else -> i++
            }
        }
    }

    actual fun addArc(oval: PlatformRectF, startAngle: Float, sweepAngle: Float) {
        addArc(oval.leftVal, oval.topVal, oval.rightVal, oval.bottomVal, startAngle, sweepAngle)
    }

    actual fun arcTo(oval: PlatformRectF, startAngle: Float, sweepAngle: Float) {
        addArc(oval.leftVal, oval.topVal, oval.rightVal, oval.bottomVal, startAngle, sweepAngle, false)
    }

    actual fun arcTo(
        oval: PlatformRectF,
        startAngle: Float,
        sweepAngle: Float,
        forceMoveTo: Boolean,
    ) {
        addArc(oval.leftVal, oval.topVal, oval.rightVal, oval.bottomVal, startAngle, sweepAngle, forceMoveTo)
    }

    actual fun arcTo(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        startAngle: Float,
        sweepAngle: Float,
        forceMoveTo: Boolean,
    ) {
        addArc(left, top, right, bottom, startAngle, sweepAngle, forceMoveTo)
    }

    actual override fun addArc(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        startAngle: Float,
        sweepAngle: Float,
        forceMoveTo: Boolean,
    ) {
        super.addArc(left, top, right, bottom, startAngle, sweepAngle, forceMoveTo)
    }

    actual companion object {
        actual val MOVE: Int = RemotePathBaseWrapper.MOVE
        actual val LINE: Int = RemotePathBaseWrapper.LINE
        actual val QUADRATIC: Int = RemotePathBaseWrapper.QUADRATIC
        actual val CONIC: Int = RemotePathBaseWrapper.CONIC
        actual val CUBIC: Int = RemotePathBaseWrapper.CUBIC
        actual val CLOSE: Int = RemotePathBaseWrapper.CLOSE
        actual val DONE: Int = RemotePathBaseWrapper.DONE
        actual val MOVE_NAN: Float = RemotePathBaseWrapper.MOVE_NAN
        actual val LINE_NAN: Float = RemotePathBaseWrapper.LINE_NAN
        actual val QUADRATIC_NAN: Float = RemotePathBaseWrapper.QUADRATIC_NAN
        actual val CONIC_NAN: Float = RemotePathBaseWrapper.CONIC_NAN
        actual val CUBIC_NAN: Float = RemotePathBaseWrapper.CUBIC_NAN
        actual val CLOSE_NAN: Float = RemotePathBaseWrapper.CLOSE_NAN
        actual val DONE_NAN: Float = RemotePathBaseWrapper.DONE_NAN

        actual fun createCirclePath(
            rc: RemoteComposeWriter,
            x: Float,
            y: Float,
            rad: Float,
        ): RemotePath = RemotePathBaseWrapper.createCirclePath(rc, x, y, rad)
    }
}
