/*
 * Copyright (C) 2025 The Android Open Source Project
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
package androidx.compose.remote.core.operations.utilities

import androidx.compose.remote.core.operations.Utils
import kotlin.math.cos
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * This is designed to algorithmically generate a path from a set of points or expressions that
 * describe the points.
 */
class PathGenerator {
    private var mLinear: Linear? = null
    private var mMonotonic: Monotonic? = null
    private var mSpline: Spline? = null
    private val mExpression = AnimatedFloatExpression()
    private var mXData = FloatArray(0)
    private var mYData = FloatArray(0)

    /**
     * Calculate the length of the path that will be returned
     */
    fun getReturnLength(len: Int, loop: Boolean): Int {
        var ret = 3 // move to
        ret += if (loop) len * 9 + 1 else (len - 1) * 9
        return ret
    }

    /**
     * Build a path from the given points.
     */
    fun getPath(dest: FloatArray, x: FloatArray, y: FloatArray, mode: Int, loop: Boolean): Int {
        return when (mode) {
            LINEAR, MONOTONIC -> {
                if (mMonotonic == null) mMonotonic = Monotonic()
                mMonotonic!!.asPath(x, y, loop).copyPoints(dest)
            }
            else -> {
                if (mSpline == null) mSpline = Spline()
                mSpline!!.asPath(x, y, loop).copyPoints(dest)
            }
        }
    }

    /**
     * Build a path from Float expressions
     */
    fun getPath(
        dest: FloatArray,
        expressionX: FloatArray,
        expressionY: FloatArray,
        min: Float,
        max: Float,
        count: Int,
        mode: Int,
        loop: Boolean,
        ca: CollectionsAccess?
    ): Int {
        if (mXData.size != count) {
            mXData = FloatArray(count)
            mYData = FloatArray(count)
        }
        val gap = max - min
        val step = if (loop) gap / count.toFloat() else gap / (count - 1).toFloat()
        if (ca == null) {
            for (i in mXData.indices) {
                val v = min + i * step
                mXData[i] = mExpression.eval(expressionX, expressionX.size, v)
                mYData[i] = mExpression.eval(expressionY, expressionY.size, v)
            }
        } else {
            for (i in mXData.indices) {
                val v = min + i * step
                mXData[i] = mExpression.eval(ca, expressionX, expressionX.size, v)
                mYData[i] = mExpression.eval(ca, expressionY, expressionY.size, v)
            }
        }
        return when (mode) {
            LINEAR -> {
                if (mLinear == null) mLinear = Linear()
                mLinear!!.asPath(mXData, mYData, loop).copyPoints(dest)
            }
            MONOTONIC -> {
                if (mMonotonic == null) mMonotonic = Monotonic()
                mMonotonic!!.asPath(mXData, mYData, loop).copyPoints(dest)
            }
            else -> {
                if (mSpline == null) mSpline = Spline()
                mSpline!!.asPath(mXData, mYData, loop).copyPoints(dest)
            }
        }
    }

    /**
     * Generate a polar path
     */
    fun getPolarPath(
        dest: FloatArray,
        expressionRad: FloatArray,
        coord: FloatArray,
        start: Float,
        end: Float,
        count: Int,
        mode: Int,
        loop: Boolean,
        ca: CollectionsAccess?
    ): Int {
        if (mXData.size != count) {
            mXData = FloatArray(count)
            mYData = FloatArray(count)
        }
        val gap = end - start
        val step = if (loop) gap / count.toFloat() else gap / (count - 1).toFloat()
        if (ca == null) {
            for (i in mXData.indices) {
                val v = start + i * step
                val r = mExpression.eval(expressionRad, expressionRad.size, v)
                mXData[i] = coord[0] + r * cos(v)
                mYData[i] = coord[1] + r * sin(v)
            }
        } else {
            for (i in mXData.indices) {
                val v = start + i * step
                val r = mExpression.eval(ca, expressionRad, expressionRad.size, v)
                mXData[i] = coord[0] + r * cos(v)
                mYData[i] = coord[1] + r * sin(v)
            }
        }
        return when (mode) {
            LINEAR -> {
                if (mLinear == null) mLinear = Linear()
                mLinear!!.asPath(mXData, mYData, loop).copyPoints(dest)
            }
            MONOTONIC -> {
                if (mMonotonic == null) mMonotonic = Monotonic()
                mMonotonic!!.asPath(mXData, mYData, loop).copyPoints(dest)
            }
            else -> {
                if (mSpline == null) mSpline = Spline()
                mSpline!!.asPath(mXData, mYData, loop).copyPoints(dest)
            }
        }
    }

    private class Path(bufferSize: Int) {
        var mPath = FloatArray(bufferSize)
        var mSize = 0
        var mMaxSize = bufferSize
        private var mCx = 0f
        private var mCy = 0f

        fun copyPoints(dest: FloatArray): Int {
            requireNotNull(dest) { "points null" }
            require(dest.size >= mSize) { "points too small ${dest.size} < $mSize" }
            mPath.copyInto(dest, endIndex = mSize)
            return mSize
        }

        fun moveTo(x: Float, y: Float) {
            mPath[mSize++] = MOVE_NAN
            mPath[mSize++] = x
            mPath[mSize++] = y
            mCx = x
            mCy = y
        }

        fun cubicTo(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float) {
            mPath[mSize++] = CUBIC_NAN
            mPath[mSize++] = mCx
            mPath[mSize++] = mCy
            mPath[mSize++] = x1
            mPath[mSize++] = y1
            mPath[mSize++] = x2
            mPath[mSize++] = y2
            mPath[mSize++] = x3
            mPath[mSize++] = y3
            mCx = x3
            mCy = y3
        }

        fun closePath() {
            mPath[mSize++] = CLOSE_NAN
        }

        fun reset() {
            mSize = 0
        }

        companion object {
            const val MOVE = 10
            const val CUBIC = 14
            const val CLOSE = 15
            val MOVE_NAN = Utils.asNan(MOVE)
            val CUBIC_NAN = Utils.asNan(CUBIC)
            val CLOSE_NAN = Utils.asNan(CLOSE)
        }
    }

    /** Monotonic spline path generator */
    private class Monotonic {
        private var mH = FloatArray(0)
        private var mDxSeg = FloatArray(0)
        private var mDySeg = FloatArray(0)
        private var mDxTan = FloatArray(0)
        private var mDyTan = FloatArray(0)
        private var mPath = Path(233)

        fun asPath(x: FloatArray, y: FloatArray, loop: Boolean): Path {
            requireNotNull(x) { "x/y null" }
            requireNotNull(y) { "x/y null" }
            require(x.size == y.size) { "x/y length mismatch" }
            val n = x.size
            val segs = if (loop) n else n - 1
            if (segs != mH.size) {
                mPath = Path(segs * 10)
                mH = FloatArray(segs)
                mDxSeg = FloatArray(segs)
                mDySeg = FloatArray(segs)
                val tans = if (loop) segs else segs + 1
                mDxTan = FloatArray(tans)
                mDyTan = FloatArray(tans)
            }
            mPath.reset()
            if (n == 0) return mPath
            mPath.moveTo(x[0], y[0])
            if (n == 1) return mPath

            for (i0 in 0 until segs) {
                val i1 = (i0 + 1) % n
                val sx = x[i1] - x[i0]
                val sy = y[i1] - y[i0]
                var dist = sqrt((sx * sx + sy * sy).toDouble()).toFloat()
                if (dist == 0f) dist = 1e-12f
                mH[i0] = dist
                mDxSeg[i0] = sx / dist
                mDySeg[i0] = sy / dist
            }
            monotoneTangents(mDxTan, mDxSeg, mH, loop)
            monotoneTangents(mDyTan, mDySeg, mH, loop)

            for (i0 in 0 until segs) {
                val i1 = (i0 + 1) % n
                val hi = mH[i0].toDouble()
                val c1x = (x[i0] + mDxTan[i0] * hi / 3.0).toFloat()
                val c1y = (y[i0] + mDyTan[i0] * hi / 3.0).toFloat()
                val c2x = (x[i1] - mDxTan[i1] * hi / 3.0).toFloat()
                val c2y = (y[i1] - mDyTan[i1] * hi / 3.0).toFloat()
                mPath.cubicTo(c1x, c1y, c2x, c2y, x[i1], y[i1])
            }
            if (loop) mPath.closePath()
            return mPath
        }

        private fun monotoneTangents(d: FloatArray, delta: FloatArray, h: FloatArray, loop: Boolean) {
            val segs = delta.size
            val n = if (loop) segs else segs + 1
            for (i in 0 until n) {
                val prev = (i - 1 + segs) % segs
                val next = i % segs
                if (!loop && i == 0) {
                    d[i] = delta[0]
                } else if (!loop && i == n - 1) {
                    d[i] = delta[segs - 1]
                } else {
                    val dp = delta[prev]
                    val dn = delta[next]
                    if (dp == 0.0f || dn == 0.0f || dp.sign != dn.sign) {
                        d[i] = 0.0f
                    } else {
                        val w1 = 2 * h[next] + h[prev]
                        val w2 = h[next] + 2 * h[prev]
                        d[i] = (w1 + w2) / (w1 / dp + w2 / dn)
                    }
                }
            }
            for (i in 0 until segs) {
                if (delta[i] == 0.0f) {
                    d[i] = 0.0f
                    d[(i + 1) % n] = 0.0f
                } else {
                    val a = d[i] / delta[i]
                    val b = d[(i + 1) % n] / delta[i]
                    val s = a * a + b * b
                    if (s > 9.0f) {
                        val t = 3.0f / sqrt(s)
                        d[i] = t * a * delta[i]
                        d[(i + 1) % n] = t * b * delta[i]
                    }
                }
            }
        }
    }

    /** Spline path generator */
    private class Spline {
        private var mH = FloatArray(0)
        private var mDxSeg = FloatArray(0)
        private var mDySeg = FloatArray(0)
        private var mDxTan = FloatArray(0)
        private var mDyTan = FloatArray(0)
        private var mPath = Path(2)

        fun asPath(x: FloatArray, y: FloatArray, loop: Boolean): Path {
            requireNotNull(x) { "x/y null" }
            requireNotNull(y) { "x/y null" }
            require(x.size == y.size) { "x/y length mismatch" }
            val n = x.size
            mPath.reset()
            val segs = if (loop) n else n - 1
            if (segs != mH.size) {
                mPath = Path(x.size * 10)
                mH = FloatArray(segs)
                mDxSeg = FloatArray(segs)
                mDySeg = FloatArray(segs)
                val tans = if (loop) segs else segs + 1
                mDxTan = FloatArray(tans)
                mDyTan = FloatArray(tans)
            }
            if (n == 0) return mPath
            mPath.moveTo(x[0], y[0])
            if (n == 1) return mPath

            for (i0 in 0 until segs) {
                val i1 = (i0 + 1) % n
                val sx = x[i1] - x[i0]
                val sy = y[i1] - y[i0]
                var dist = sqrt((sx * sx + sy * sy).toDouble()).toFloat()
                if (dist == 0f) dist = 1e-12f
                mH[i0] = dist
                mDxSeg[i0] = sx / dist
                mDySeg[i0] = sy / dist
            }
            smoothTangents(mDxTan, mDxSeg, mH, loop)
            smoothTangents(mDyTan, mDySeg, mH, loop)

            for (i0 in 0 until segs) {
                val i1 = (i0 + 1) % n
                val hi = mH[i0]
                val c1x = x[i0] + mDxTan[i0] * hi / 3.0f
                val c1y = y[i0] + mDyTan[i0] * hi / 3.0f
                val c2x = x[i1] - mDxTan[i1] * hi / 3.0f
                val c2y = y[i1] - mDyTan[i1] * hi / 3.0f
                mPath.cubicTo(c1x, c1y, c2x, c2y, x[i1], y[i1])
            }
            if (loop) mPath.closePath()
            return mPath
        }

        private fun smoothTangents(d: FloatArray, delta: FloatArray, h: FloatArray, loop: Boolean) {
            val segs = delta.size
            val n = if (loop) segs else segs + 1
            if (loop) {
                for (i in 0 until n) {
                    val im1 = (i - 1 + segs) % segs
                    val ip0 = i % segs
                    d[i] = (h[im1] * delta[ip0] + h[ip0] * delta[im1]) / (h[im1] + h[ip0])
                }
            } else {
                d[0] = delta[0]
                d[n - 1] = delta[segs - 1]
                for (i in 1 until n - 1) {
                    val hm1 = h[i - 1]
                    val hi = h[i]
                    d[i] = (hm1 * delta[i] + hi * delta[i - 1]) / (hm1 + hi)
                }
            }
        }
    }

    /** Linear path generator */
    private class Linear {
        private var mPath = Path(2)

        fun asPath(x: FloatArray, y: FloatArray, loop: Boolean): Path {
            requireNotNull(x) { "x/y null" }
            requireNotNull(y) { "x/y null" }
            require(x.size == y.size) { "x/y length mismatch" }
            val n = x.size
            mPath.reset()
            val segs = if (loop) n else n - 1
            if (x.size * 10 != mPath.mMaxSize) {
                mPath = Path(x.size * 10)
            }
            if (n == 0) return mPath
            mPath.moveTo(x[0], y[0])
            if (n == 1) return mPath

            for (i0 in 0 until segs) {
                val i1 = (i0 + 1) % n
                mPath.cubicTo(x[i0], y[i0], x[i1], y[i1], x[i1], y[i1])
            }
            if (loop) mPath.closePath()
            return mPath
        }
    }

    companion object {
        const val SPLINE = 0
        const val MONOTONIC = 2
        const val LINEAR = 4
    }
}
