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

/** Platform-specific rectangle type. */
expect class PlatformRectF

/** Platform-specific path type. */
expect class PlatformPath

/** Platform-specific matrix type. */
expect class PlatformMatrix

internal expect val PlatformRectF.left: Float
internal expect val PlatformRectF.top: Float
internal expect val PlatformRectF.right: Float
internal expect val PlatformRectF.bottom: Float

/**
 * Expect declaration for RemotePath -- a platform-aware path class that wraps
 * [androidx.compose.remote.core.RemotePathBase] and adds platform-specific
 * path generation and transformation.
 */
expect class RemotePath {
    constructor(bufferSize: Int)

    constructor()

    constructor(pathData: String)

    fun reset()

    fun incReserve(extraPtCount: Int)

    fun moveTo(x: Float, y: Float)

    fun rMoveTo(dx: Float, dy: Float)

    fun quadTo(x1: Float, y1: Float, x2: Float, y2: Float)

    fun rQuadTo(dx1: Float, dy1: Float, dx2: Float, dy2: Float)

    fun conicTo(x1: Float, y1: Float, x2: Float, y2: Float, weight: Float)

    fun rConicTo(dx1: Float, dy1: Float, dx2: Float, dy2: Float, weight: Float)

    fun lineTo(x: Float, y: Float)

    fun rLineTo(dx: Float, dy: Float)

    fun cubicTo(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float)

    fun rCubicTo(dx1: Float, dy1: Float, dx2: Float, dy2: Float, dx3: Float, dy3: Float)

    fun close()

    fun rewind()

    fun isEmpty(): Boolean

    fun createFloatArray(): FloatArray

    val path: PlatformPath

    fun transform(matrix: PlatformMatrix)

    fun addArc(oval: PlatformRectF, startAngle: Float, sweepAngle: Float)

    fun arcTo(oval: PlatformRectF, startAngle: Float, sweepAngle: Float)

    fun arcTo(oval: PlatformRectF, startAngle: Float, sweepAngle: Float, forceMoveTo: Boolean)

    fun arcTo(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        startAngle: Float,
        sweepAngle: Float,
        forceMoveTo: Boolean,
    )

    fun addArc(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        startAngle: Float,
        sweepAngle: Float,
        forceMoveTo: Boolean = false,
    )

    companion object {
        val MOVE: Int
        val LINE: Int
        val QUADRATIC: Int
        val CONIC: Int
        val CUBIC: Int
        val CLOSE: Int
        val DONE: Int
        val MOVE_NAN: Float
        val LINE_NAN: Float
        val QUADRATIC_NAN: Float
        val CONIC_NAN: Float
        val CUBIC_NAN: Float
        val CLOSE_NAN: Float
        val DONE_NAN: Float

        fun createCirclePath(
            rc: RemoteComposeWriter,
            x: Float,
            y: Float,
            rad: Float,
        ): RemotePath
    }
}
