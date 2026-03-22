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

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

fun RemotePath.moveTo(x: Number, y: Number) {
    moveTo(x.toFloat(), y.toFloat())
}

fun RemotePath.cubicTo(
    x1: Number,
    y1: Number,
    x2: Number,
    y2: Number,
    x3: Number,
    y3: Number,
) {
    cubicTo(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat(), x3.toFloat(), y3.toFloat())
}

fun RemotePath.lineTo(x: Number, y: Number) {
    lineTo(x.toFloat(), y.toFloat())
}

/**
 * Creates a dynamic circle path. The parameters can be NaN float IDs.
 * The number of elements can be set to integrate with path morphing functions.
 */
fun createDynamicCircle(
    writer: RemoteComposeWriter,
    n: Int,
    radius: Float,
    cx: Float,
    cy: Float,
): RemotePath {
    val ret = RemotePath()
    val rad = RFloat(writer, radius)
    val controlPointDistance = rad * ((4.0 / 3.0) * tan(kotlin.math.PI / (2f * n))).toFloat()
    val centerX = RFloat(writer, cx)
    val centerY = RFloat(writer, cy)
    if (n < 3) {
        throw IllegalArgumentException("n must be greater than or equal to 3")
    }
    for (i in 0 until n) {
        val startAngle = ((i / n.toDouble()) * kotlin.math.PI * 2).toFloat()
        val endAngle = (((i + 1) / n.toDouble()) * kotlin.math.PI * 2).toFloat()

        val startX = centerX + (rad * cos(startAngle))
        val startY = centerY + (rad * sin(startAngle))

        val endX = centerX + (rad * cos(endAngle))
        val endY = centerY + (rad * sin(endAngle))

        val control1X = centerX + rad * cos(startAngle) - controlPointDistance * sin(startAngle)
        val control1Y = centerY + rad * sin(startAngle) + controlPointDistance * cos(startAngle)

        val control2X = centerX + rad * cos(endAngle) + controlPointDistance * sin(endAngle)
        val control2Y = centerY + rad * sin(endAngle) - controlPointDistance * cos(endAngle)

        if (i == 0) {
            ret.moveTo(startX.toFloat(), startY.toFloat())
        }

        ret.cubicTo(
            control1X.toFloat(),
            control1Y.toFloat(),
            control2X.toFloat(),
            control2Y.toFloat(),
            endX.toFloat(),
            endY.toFloat(),
        )
    }
    ret.close()
    return ret
}

/**
 * Creates a Squircle path based on center coordinates, radius, and corner radius.
 *
 * @param rc The RemoteComposeWriter to use.
 * @param cx The x-coordinate of the squircle's center.
 * @param cy The y-coordinate of the squircle's center.
 * @param radius The radius of the squircle (distance from center to side).
 * @param cornerRadius The radius of the corners.
 */
fun createSquirclePath(
    rc: RemoteComposeWriter,
    cx: Float,
    cy: Float,
    radius: Float,
    cornerRadius: Float,
): RemotePath {
    return createSquirclePath(
        rc,
        RFloat(rc, cx),
        RFloat(rc, cy),
        RFloat(rc, radius),
        RFloat(rc, cornerRadius),
    )
}

/**
 * Creates a Squircle path based on center coordinates, radius, and corner radius.
 *
 * @param rc The RemoteComposeWriter to use.
 * @param cx The x-coordinate of the squircle's center.
 * @param cy The y-coordinate of the squircle's center.
 * @param radius The radius of the squircle (distance from center to side).
 * @param cornerRadius The radius of the corners.
 */
fun createSquirclePath(
    rc: RemoteComposeWriter,
    cx: RFloat,
    cy: RFloat,
    radius: RFloat,
    cornerRadius: RFloat,
): RemotePath {
    val squirclePath = RemotePath()

    val halfWidth = radius
    val halfHeight = radius

    squirclePath.moveTo(cx + halfWidth - cornerRadius, cy - halfHeight)

    val controlOffset = cornerRadius * 0.55228475f
    val left = cx - halfWidth
    val top = cy - halfHeight
    val right = cx + halfWidth
    val bottom = cy + halfHeight
    // flush the calculations
    left.toFloat()
    top.toFloat()
    right.toFloat()
    bottom.toFloat()

    squirclePath.cubicTo(
        right - cornerRadius + controlOffset,
        top,
        right,
        top + controlOffset,
        right,
        top + cornerRadius,
    )

    squirclePath.lineTo(cx + halfWidth, bottom - cornerRadius)

    squirclePath.cubicTo(
        right,
        bottom - cornerRadius + controlOffset,
        right - cornerRadius + controlOffset,
        bottom,
        right - cornerRadius,
        bottom,
    )

    squirclePath.lineTo(cx - halfWidth + cornerRadius, cy + halfHeight)

    squirclePath.cubicTo(
        left + cornerRadius - controlOffset,
        bottom,
        left,
        bottom - cornerRadius + controlOffset,
        left,
        bottom - cornerRadius,
    )

    // Left edge
    squirclePath.lineTo(cx - halfWidth, top + cornerRadius)

    // Top-left corner
    squirclePath.cubicTo(
        left,
        top + cornerRadius - controlOffset,
        left + cornerRadius - controlOffset,
        top,
        left + cornerRadius,
        top,
    )

    squirclePath.close()
    return squirclePath
}
