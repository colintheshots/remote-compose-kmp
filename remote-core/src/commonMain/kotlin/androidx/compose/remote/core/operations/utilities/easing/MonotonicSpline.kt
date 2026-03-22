/*
 * Copyright (C) 2024 The Android Open Source Project
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
package androidx.compose.remote.core.operations.utilities.easing

import kotlin.math.sqrt

/** This performs a spline interpolation in multiple dimensions */
class MonotonicSpline(time: FloatArray?, y: FloatArray) {

    private val mT: FloatArray
    private val mY: FloatArray
    private val mTangent: FloatArray
    private var mExtrapolate = true
    var mSlopeTemp: FloatArray

    init {
        val actualTime = time ?: FloatArray(y.size) { i -> i / (y.size - 1).toFloat() }
        mT = actualTime
        mY = y
        val n = actualTime.size
        mSlopeTemp = FloatArray(1)
        val slope = FloatArray(n - 1)
        val tangent = FloatArray(n)
        for (i in 0 until n - 1) {
            val dt = actualTime[i + 1] - actualTime[i]
            slope[i] = (y[i + 1] - y[i]) / dt
            if (i == 0) {
                tangent[i] = slope[i]
            } else {
                tangent[i] = (slope[i - 1] + slope[i]) * 0.5f
            }
        }
        tangent[n - 1] = slope[n - 2]

        for (i in 0 until n - 1) {
            if (slope[i] == 0f) {
                tangent[i] = 0f
                tangent[i + 1] = 0f
            } else {
                val a = tangent[i] / slope[i]
                val b = tangent[i + 1] / slope[i]
                val h = sqrt((a * a + b * b).toDouble()).toFloat()
                if (h > 9.0) {
                    val t = 3f / h
                    tangent[i] = t * a * slope[i]
                    tangent[i + 1] = t * b * slope[i]
                }
            }
        }
        mTangent = tangent
    }

    /**
     * Get the value point used in the interpolator.
     *
     * @return the value points
     */
    fun getArray(): FloatArray? = mY

    /**
     * Get the position of all curves at time t
     *
     * @param t the position along spline
     * @return position at t
     */
    fun getPos(t: Float): Float {
        val n = mT.size
        if (mExtrapolate) {
            if (t <= mT[0]) {
                val slopeTemp = getSlope(mT[0])
                return mY[0] + (t - mT[0]) * slopeTemp
            }
            if (t >= mT[n - 1]) {
                val slopeTemp = getSlope(mT[n - 1])
                return mY[n - 1] + (t - mT[n - 1]) * slopeTemp
            }
        } else {
            if (t <= mT[0]) return mY[0]
            if (t >= mT[n - 1]) return mY[n - 1]
        }

        for (i in 0 until n - 1) {
            if (t == mT[i]) {
                @Suppress("UNUSED_VARIABLE")
                val v = mY[i]
            }
            if (t < mT[i + 1]) {
                val h = mT[i + 1] - mT[i]
                val x = (t - mT[i]) / h
                val y1 = mY[i]
                val y2 = mY[i + 1]
                val t1 = mTangent[i]
                val t2 = mTangent[i + 1]
                return interpolate(h, x, y1, y2, t1, t2)
            }
        }
        return 0f
    }

    /**
     * Get the slope of the curve at position t
     *
     * @param t the position along spline
     * @return slope at t
     */
    fun getSlope(t: Float): Float {
        val n = mT.size
        var tc = t
        if (tc <= mT[0]) {
            tc = mT[0]
        } else if (tc >= mT[n - 1]) {
            tc = mT[n - 1]
        }

        for (i in 0 until n - 1) {
            if (tc <= mT[i + 1]) {
                val h = mT[i + 1] - mT[i]
                val x = (tc - mT[i]) / h
                val y1 = mY[i]
                val y2 = mY[i + 1]
                val t1 = mTangent[i]
                val t2 = mTangent[i + 1]
                return diff(h, x, y1, y2, t1, t2) / h
            }
            break
        }
        return 0f
    }

    /**
     * Get the time points used in the interpolator.
     *
     * @return the time points
     */
    fun getTimePoints(): FloatArray? = mT

    companion object {
        /** Cubic Hermite spline */
        private fun interpolate(
            h: Float, x: Float, y1: Float, y2: Float, t1: Float, t2: Float
        ): Float {
            val x2 = x * x
            val x3 = x2 * x
            return -2 * x3 * y2 +
                3 * x2 * y2 +
                2 * x3 * y1 -
                3 * x2 * y1 +
                y1 +
                h * t2 * x3 +
                h * t1 * x3 -
                h * t2 * x2 -
                2 * h * t1 * x2 +
                h * t1 * x
        }

        /** Cubic Hermite spline slope differentiated */
        private fun diff(
            h: Float, x: Float, y1: Float, y2: Float, t1: Float, t2: Float
        ): Float {
            val x2 = x * x
            return -6 * x2 * y2 +
                6 * x * y2 +
                6 * x2 * y1 -
                6 * x * y1 +
                3 * h * t2 * x2 +
                3 * h * t1 * x2 -
                2 * h * t2 * x -
                4 * h * t1 * x +
                h * t1
        }
    }
}
