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
package androidx.compose.remote.core

import androidx.compose.remote.core.operations.PathData
import androidx.compose.remote.core.operations.Utils
import kotlin.math.max

/** Common RemotePath implementation that manages the path buffer. */
open class RemotePathBase {

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 1024

        const val MOVE = 10
        const val LINE = 11
        const val QUADRATIC = 12
        const val CONIC = 13
        const val CUBIC = 14
        const val CLOSE = 15
        const val DONE = 16

        val MOVE_NAN = Utils.asNan(MOVE)
        val LINE_NAN = Utils.asNan(LINE)
        val QUADRATIC_NAN = Utils.asNan(QUADRATIC)
        val CONIC_NAN = Utils.asNan(CONIC)
        val CUBIC_NAN = Utils.asNan(CUBIC)
        val CLOSE_NAN = Utils.asNan(CLOSE)
        val DONE_NAN = Utils.asNan(DONE)
    }

    var mMaxSize: Int
    var mPath: FloatArray
    var mCx: Float = 0f
    var mCy: Float = 0f
    var mSize: Int = 0

    constructor(bufferSize: Int) {
        mMaxSize = bufferSize
        mPath = FloatArray(mMaxSize)
    }

    constructor() {
        mMaxSize = DEFAULT_BUFFER_SIZE
        mPath = FloatArray(mMaxSize)
    }

    constructor(pathData: String) {
        mMaxSize = DEFAULT_BUFFER_SIZE
        mPath = FloatArray(mMaxSize)
        parsePathData(pathData)
    }

    fun reset() { mSize = 0 }
    fun getCurrentX(): Float = mCx
    fun getCurrentY(): Float = mCy
    fun getSize(): Int = mSize
    fun getPath(): FloatArray = mPath

    private fun resize(need: Int) {
        if (mSize + need >= mMaxSize) {
            mMaxSize = max(mMaxSize * 2, mSize + need)
            mPath = mPath.copyOf(mMaxSize)
        }
    }

    fun incReserve(extraPtCount: Int) { mSize = 0 }

    fun add(type: Int) { resize(1); mPath[mSize++] = Utils.asNan(type) }

    fun addMove(type: Int, a1: Float, a2: Float) {
        resize(3); mPath[mSize++] = Utils.asNan(type); mPath[mSize++] = a1; mPath[mSize++] = a2
    }

    fun add(type: Int, a1: Float, a2: Float) {
        resize(3); mPath[mSize++] = Utils.asNan(type); mSize += 2
        mPath[mSize++] = a1; mPath[mSize++] = a2
    }

    fun add(type: Int, a1: Float, a2: Float, a3: Float, a4: Float) {
        resize(5); mPath[mSize++] = Utils.asNan(type); mSize += 2
        mPath[mSize++] = a1; mPath[mSize++] = a2; mPath[mSize++] = a3; mPath[mSize++] = a4
    }

    fun add(type: Int, a1: Float, a2: Float, a3: Float, a4: Float, a5: Float) {
        resize(6); mPath[mSize++] = Utils.asNan(type); mSize += 2
        mPath[mSize++] = a1; mPath[mSize++] = a2; mPath[mSize++] = a3
        mPath[mSize++] = a4; mPath[mSize++] = a5
    }

    fun add(type: Int, a1: Float, a2: Float, a3: Float, a4: Float, a5: Float, a6: Float) {
        resize(7); mPath[mSize++] = Utils.asNan(type); mSize += 2
        mPath[mSize++] = a1; mPath[mSize++] = a2; mPath[mSize++] = a3
        mPath[mSize++] = a4; mPath[mSize++] = a5; mPath[mSize++] = a6
    }

    fun moveTo(x: Float, y: Float) { addMove(MOVE, x, y); mCx = x; mCy = y }
    fun rMoveTo(dx: Float, dy: Float) { mCx += dx; mCy += dy; add(MOVE, mCx, mCy) }

    fun quadTo(x1: Float, y1: Float, x2: Float, y2: Float) {
        add(QUADRATIC, x1, y1, x2, y2); mCx = x2; mCy = y2
    }

    fun rQuadTo(dx1: Float, dy1: Float, dx2: Float, dy2: Float) {
        add(QUADRATIC, dx1 + mCx, dy1 + mCx, dx2 + mCx, dy2 + mCx)
        mCx += dx2; mCy += dy2
    }

    fun conicTo(x1: Float, y1: Float, x2: Float, y2: Float, weight: Float) {
        add(CONIC, x1, y1, x2, y2, weight); mCx = x2; mCy = y2
    }

    fun rConicTo(dx1: Float, dy1: Float, dx2: Float, dy2: Float, weight: Float) {
        add(CONIC, dx1 + mCx, dy1 + mCy, dx2 + mCx, dy2 + mCy, weight)
        mCx += dx2; mCy += dy2
    }

    fun lineTo(x: Float, y: Float) { add(LINE, x, y); mCx = x; mCy = y }

    fun rLineTo(dx: Float, dy: Float) { mCx += dx; mCy += dy; add(LINE, mCx, mCy) }

    fun cubicTo(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float) {
        add(CUBIC, x1, y1, x2, y2, x3, y3); mCx = x3; mCy = y3
    }

    fun rCubicTo(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float) {
        add(CUBIC, x1 + mCx, y1 + mCy, x2 + mCx, y2 + mCy, x3 + mCx, y3 + mCy)
        mCx += x3; mCy += y3
    }

    fun close() { add(CLOSE) }
    fun rewind() { mSize = 0 }
    fun isEmpty(): Boolean = mSize == 0

    open fun arcTo(left: Float, top: Float, right: Float, bottom: Float, startAngle: Float, sweepAngle: Float, forceMoveTo: Boolean) {
        addArc(left, top, right, bottom, startAngle, sweepAngle, forceMoveTo)
    }

    open fun addArc(left: Float, top: Float, right: Float, bottom: Float, startAngle: Float, sweepAngle: Float, forceMoveTo: Boolean) {
        throw UnsupportedOperationException("Not implemented in base")
    }

    private fun parsePathData(pathData: String) {
        val cords = FloatArray(6)
        val commands = pathData.split("(?=[MmZzLlHhVvCcSsQqTtAa])".toRegex()).filter { it.isNotEmpty() }
        for (command in commands) {
            val cmd = command[0]
            val values = command.substring(1).trim().split("[,\\s]+".toRegex()).filter { it.isNotEmpty() }
            when (cmd) {
                'M' -> moveTo(values[0].toFloat(), values[1].toFloat())
                'L' -> { var i = 0; while (i < values.size) { lineTo(values[i].toFloat(), values[i + 1].toFloat()); i += 2 } }
                'H' -> { for (v in values) lineTo(v.toFloat(), cords[1]) }
                'C' -> { var i = 0; while (i < values.size) { cubicTo(values[i].toFloat(), values[i+1].toFloat(), values[i+2].toFloat(), values[i+3].toFloat(), values[i+4].toFloat(), values[i+5].toFloat()); i += 6 } }
                'S' -> { var i = 0; while (i < values.size) { cubicTo(2*cords[0]-cords[2], 2*cords[1]-cords[3], values[i].toFloat(), values[i+1].toFloat(), values[i+2].toFloat(), values[i+3].toFloat()); i += 4 } }
                'Z' -> close()
                else -> throw IllegalArgumentException("Unsupported command: $cmd")
            }
            if (cmd != 'Z' && cmd != 'H') {
                cords[0] = values[values.size - 2].toFloat()
                cords[1] = values[values.size - 1].toFloat()
                if (cmd == 'C' || cmd == 'S') {
                    cords[2] = values[values.size - 4].toFloat()
                    cords[3] = values[values.size - 3].toFloat()
                }
            }
        }
    }

    override fun toString(): String {
        val builder = StringBuilder()
        var i = 0
        while (i < mSize) {
            when (Utils.idFromNan(mPath[i])) {
                PathData.MOVE -> { i++; builder.append("moveTo(${mPath[i]}, ${mPath[i+1]})\n"); i += 2 }
                PathData.LINE -> { i += 3; builder.append("lineTo(${mPath[i]}, ${mPath[i+1]})\n"); i += 2 }
                PathData.QUADRATIC -> { i += 3; builder.append("quadTo(${mPath[i]}, ${mPath[i+1]}, ${mPath[i+2]}, ${mPath[i+3]})\n"); i += 4 }
                PathData.CONIC -> { i += 3; builder.append("conicTo(${mPath[i]}, ${mPath[i+1]}, ${mPath[i+2]}, ${mPath[i+3]}, ${mPath[i+4]})\n"); i += 5 }
                PathData.CUBIC -> { i += 3; builder.append("cubicTo(${mPath[i]}, ${mPath[i+1]}, ${mPath[i+2]}, ${mPath[i+3]}, ${mPath[i+4]}, ${mPath[i+5]})\n"); i += 6 }
                PathData.CLOSE -> { builder.append("close()\n"); i++ }
                PathData.DONE -> { builder.append("done()\n"); i++ }
                else -> return builder.toString()
            }
        }
        return builder.toString()
    }

    fun createFloatArray(): FloatArray = mPath.copyOf(mSize)
}
