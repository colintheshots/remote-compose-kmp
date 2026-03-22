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

import android.graphics.PathMeasure
import android.os.Build
import androidx.compose.remote.core.operations.PathData
import androidx.compose.remote.core.operations.Utils
import kotlin.math.max
import kotlin.math.min

actual typealias PlatformRectF = android.graphics.RectF

actual typealias PlatformPath = android.graphics.Path

actual typealias PlatformMatrix = android.graphics.Matrix

internal actual val PlatformRectF.left: Float
    get() = this.left

internal actual val PlatformRectF.top: Float
    get() = this.top

internal actual val PlatformRectF.right: Float
    get() = this.right

internal actual val PlatformRectF.bottom: Float
    get() = this.bottom

actual class RemotePath : RemotePathBaseWrapper {

    actual constructor() : super()

    actual constructor(bufferSize: Int) : super(bufferSize)

    actual constructor(pathData: String) : super(pathData)

    actual val path: PlatformPath
        get() {
            val result = PlatformPath()
            genPath(result, pathArray, size, Float.NaN, Float.NaN)
            return result
        }

    private var mCachePath: PlatformPath? = PlatformPath()
    private var mCacheMeasure: PathMeasure? = PathMeasure()

    private fun genPath(
        retPath: PlatformPath,
        pathArray: FloatArray,
        length: Int,
        startSection: Float,
        stopSection: Float,
    ) {
        var i = 0
        mCachePath = if (mCachePath == null) PlatformPath() else mCachePath

        while (i < length) {
            when (Utils.idFromNan(pathArray[i])) {
                PathData.MOVE -> {
                    i++
                    mCachePath!!.moveTo(pathArray[i + 0], pathArray[i + 1])
                    i += 2
                }
                PathData.LINE -> {
                    i += 3
                    mCachePath!!.lineTo(pathArray[i + 0], pathArray[i + 1])
                    i += 2
                }
                PathData.QUADRATIC -> {
                    i += 3
                    mCachePath!!.quadTo(
                        pathArray[i + 0],
                        pathArray[i + 1],
                        pathArray[i + 2],
                        pathArray[i + 3],
                    )
                    i += 4
                }
                PathData.CONIC -> {
                    i += 3
                    if (Build.VERSION.SDK_INT >= 34) {
                        mCachePath!!.conicTo(
                            pathArray[i + 0],
                            pathArray[i + 1],
                            pathArray[i + 2],
                            pathArray[i + 3],
                            pathArray[i + 4],
                        )
                    }
                    i += 5
                }
                PathData.CUBIC -> {
                    i += 3
                    mCachePath!!.cubicTo(
                        pathArray[i + 0],
                        pathArray[i + 1],
                        pathArray[i + 2],
                        pathArray[i + 3],
                        pathArray[i + 4],
                        pathArray[i + 5],
                    )
                    i += 6
                }
                PathData.CLOSE -> {
                    mCachePath!!.close()
                    i++
                }
                PathData.DONE -> i++
                else -> {
                    System.err.println("RemotePath Odd command " + Utils.idFromNan(pathArray[i]))
                    i++
                }
            }
        }

        retPath.reset()
        if (startSection.isNaN() && stopSection.isNaN()) {
            retPath.addPath(mCachePath!!)
            return
        }
        val start = if (startSection.isNaN()) 0f else startSection
        val stop = if (stopSection.isNaN()) 1f else stopSection

        if (start > stop) {
            retPath.addPath(mCachePath!!)
            return
        }
        mCacheMeasure = if (mCacheMeasure == null) PathMeasure() else mCacheMeasure
        if (stop > 1) {
            val seg = min(stop, 1f)
            mCacheMeasure!!.setPath(mCachePath, false)
            val len = mCacheMeasure!!.length
            val scaleStart = ((start + 1) % 1) * len
            val scaleStop = ((seg + 1) % 1) * len
            mCacheMeasure!!.getSegment(scaleStart, scaleStop, retPath, true)
            retPath.addPath(mCachePath!!)
            return
        }

        mCacheMeasure!!.setPath(mCachePath, false)
        val len = mCacheMeasure!!.length
        val scaleStart = max(start, 0f) * len
        val scaleStop = min(stop, 1f) * len
        mCacheMeasure!!.getSegment(scaleStart, scaleStop, retPath, true)
        retPath.addPath(mCachePath!!)
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
                    if (Build.VERSION.SDK_INT >= 34) {
                        matrix.mapPoints(pathArray, i, pathArray, i, 2)
                    }
                    i += 5
                }
                PathData.CUBIC -> {
                    i += 3
                    matrix.mapPoints(pathArray, i, pathArray, i, 3)
                    i += 6
                }
                PathData.CLOSE,
                PathData.DONE -> i++
                else -> {
                    System.err.println(" Odd command " + Utils.idFromNan(pathArray[i]))
                    i++
                }
            }
        }
    }

    actual fun addArc(oval: PlatformRectF, startAngle: Float, sweepAngle: Float) {
        addArc(oval.left, oval.top, oval.right, oval.bottom, startAngle, sweepAngle)
    }

    actual fun arcTo(oval: PlatformRectF, startAngle: Float, sweepAngle: Float) {
        addArc(oval.left, oval.top, oval.right, oval.bottom, startAngle, sweepAngle, false)
    }

    actual fun arcTo(
        oval: PlatformRectF,
        startAngle: Float,
        sweepAngle: Float,
        forceMoveTo: Boolean,
    ) {
        addArc(oval.left, oval.top, oval.right, oval.bottom, startAngle, sweepAngle, forceMoveTo)
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
