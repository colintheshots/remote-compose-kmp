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
class MonotonicCurveFit(time: DoubleArray, y: Array<DoubleArray>) {

    private val mT: DoubleArray
    private val mY: Array<DoubleArray>
    private val mTangent: Array<DoubleArray>
    private var mExtrapolate = true
    val mSlopeTemp: DoubleArray

    init {
        val n = time.size
        val dim = y[0].size
        mSlopeTemp = DoubleArray(dim)
        val slope = Array(n - 1) { DoubleArray(dim) }
        val tangent = Array(n) { DoubleArray(dim) }
        for (j in 0 until dim) {
            for (i in 0 until n - 1) {
                val dt = time[i + 1] - time[i]
                slope[i][j] = (y[i + 1][j] - y[i][j]) / dt
                if (i == 0) {
                    tangent[i][j] = slope[i][j]
                } else {
                    tangent[i][j] = (slope[i - 1][j] + slope[i][j]) * 0.5
                }
            }
            tangent[n - 1][j] = slope[n - 2][j]
        }

        for (i in 0 until n - 1) {
            for (j in 0 until dim) {
                if (slope[i][j] == 0.0) {
                    tangent[i][j] = 0.0
                    tangent[i + 1][j] = 0.0
                } else {
                    val a = tangent[i][j] / slope[i][j]
                    val b = tangent[i + 1][j] / slope[i][j]
                    val h = sqrt(a * a + b * b)
                    if (h > 9.0) {
                        val t = 3.0 / h
                        tangent[i][j] = t * a * slope[i][j]
                        tangent[i + 1][j] = t * b * slope[i][j]
                    }
                }
            }
        }
        mT = time
        mY = y
        mTangent = tangent
    }

    /**
     * Get the position of all curves at position t
     *
     * @param t the point on the spline
     * @param v the array to fill (for multiple curves)
     */
    fun getPos(t: Double, v: DoubleArray) {
        val n = mT.size
        val dim = mY[0].size
        if (mExtrapolate) {
            if (t <= mT[0]) {
                getSlope(mT[0], mSlopeTemp)
                for (j in 0 until dim) {
                    v[j] = mY[0][j] + (t - mT[0]) * mSlopeTemp[j]
                }
                return
            }
            if (t >= mT[n - 1]) {
                getSlope(mT[n - 1], mSlopeTemp)
                for (j in 0 until dim) {
                    v[j] = mY[n - 1][j] + (t - mT[n - 1]) * mSlopeTemp[j]
                }
                return
            }
        } else {
            if (t <= mT[0]) {
                for (j in 0 until dim) {
                    v[j] = mY[0][j]
                }
                return
            }
            if (t >= mT[n - 1]) {
                for (j in 0 until dim) {
                    v[j] = mY[n - 1][j]
                }
                return
            }
        }

        for (i in 0 until n - 1) {
            if (t == mT[i]) {
                for (j in 0 until dim) {
                    v[j] = mY[i][j]
                }
            }
            if (t < mT[i + 1]) {
                val h = mT[i + 1] - mT[i]
                val x = (t - mT[i]) / h
                for (j in 0 until dim) {
                    val y1 = mY[i][j]
                    val y2 = mY[i + 1][j]
                    val t1 = mTangent[i][j]
                    val t2 = mTangent[i + 1][j]
                    v[j] = interpolate(h, x, y1, y2, t1, t2)
                }
                return
            }
        }
    }

    /**
     * Get the position of all curves at position t
     *
     * @param t the point on the spline
     * @param v the array to fill
     */
    fun getPos(t: Double, v: FloatArray) {
        val n = mT.size
        val dim = mY[0].size
        if (mExtrapolate) {
            if (t <= mT[0]) {
                getSlope(mT[0], mSlopeTemp)
                for (j in 0 until dim) {
                    v[j] = (mY[0][j] + (t - mT[0]) * mSlopeTemp[j]).toFloat()
                }
                return
            }
            if (t >= mT[n - 1]) {
                getSlope(mT[n - 1], mSlopeTemp)
                for (j in 0 until dim) {
                    v[j] = (mY[n - 1][j] + (t - mT[n - 1]) * mSlopeTemp[j]).toFloat()
                }
                return
            }
        } else {
            if (t <= mT[0]) {
                for (j in 0 until dim) {
                    v[j] = mY[0][j].toFloat()
                }
                return
            }
            if (t >= mT[n - 1]) {
                for (j in 0 until dim) {
                    v[j] = mY[n - 1][j].toFloat()
                }
                return
            }
        }

        for (i in 0 until n - 1) {
            if (t == mT[i]) {
                for (j in 0 until dim) {
                    v[j] = mY[i][j].toFloat()
                }
            }
            if (t < mT[i + 1]) {
                val h = mT[i + 1] - mT[i]
                val x = (t - mT[i]) / h
                for (j in 0 until dim) {
                    val y1 = mY[i][j]
                    val y2 = mY[i + 1][j]
                    val t1 = mTangent[i][j]
                    val t2 = mTangent[i + 1][j]
                    v[j] = interpolate(h, x, y1, y2, t1, t2).toFloat()
                }
                return
            }
        }
    }

    /**
     * Get the position of the jth curve at position t
     *
     * @param t the position
     * @param j the curve to get
     * @return the position
     */
    fun getPos(t: Double, j: Int): Double {
        val n = mT.size
        if (mExtrapolate) {
            if (t <= mT[0]) {
                return mY[0][j] + (t - mT[0]) * getSlope(mT[0], j)
            }
            if (t >= mT[n - 1]) {
                return mY[n - 1][j] + (t - mT[n - 1]) * getSlope(mT[n - 1], j)
            }
        } else {
            if (t <= mT[0]) return mY[0][j]
            if (t >= mT[n - 1]) return mY[n - 1][j]
        }

        for (i in 0 until n - 1) {
            if (t == mT[i]) return mY[i][j]
            if (t < mT[i + 1]) {
                val h = mT[i + 1] - mT[i]
                val x = (t - mT[i]) / h
                val y1 = mY[i][j]
                val y2 = mY[i + 1][j]
                val t1 = mTangent[i][j]
                val t2 = mTangent[i + 1][j]
                return interpolate(h, x, y1, y2, t1, t2)
            }
        }
        return 0.0 // should never reach here
    }

    /**
     * Get the slope of all the curves at position t
     *
     * @param t the position
     * @param v the array to fill
     */
    fun getSlope(t: Double, v: DoubleArray) {
        val n = mT.size
        val dim = mY[0].size
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
                for (j in 0 until dim) {
                    val y1 = mY[i][j]
                    val y2 = mY[i + 1][j]
                    val t1 = mTangent[i][j]
                    val t2 = mTangent[i + 1][j]
                    v[j] = diff(h, x, y1, y2, t1, t2) / h
                }
                break
            }
        }
    }

    /**
     * Get the slope of the j curve at position t
     *
     * @param t the position
     * @param j the curve to get the value at
     * @return the slope
     */
    fun getSlope(t: Double, j: Int): Double {
        val n = mT.size
        var tc = t
        if (tc < mT[0]) {
            tc = mT[0]
        } else if (tc >= mT[n - 1]) {
            tc = mT[n - 1]
        }
        for (i in 0 until n - 1) {
            if (tc <= mT[i + 1]) {
                val h = mT[i + 1] - mT[i]
                val x = (tc - mT[i]) / h
                val y1 = mY[i][j]
                val y2 = mY[i + 1][j]
                val t1 = mTangent[i][j]
                val t2 = mTangent[i + 1][j]
                return diff(h, x, y1, y2, t1, t2) / h
            }
        }
        return 0.0 // should never reach here
    }

    /**
     * Get the time point used to create the curve
     *
     * @return the time points used to create the curve
     */
    fun getTimePoints(): DoubleArray = mT

    companion object {
        /** Cubic Hermite spline */
        private fun interpolate(
            h: Double, x: Double, y1: Double, y2: Double, t1: Double, t2: Double
        ): Double {
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
            h: Double, x: Double, y1: Double, y2: Double, t1: Double, t2: Double
        ): Double {
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

        /**
         * This builds a monotonic spline to be used as a wave function
         *
         * @param configString the configuration string
         * @return the curve
         */
        fun buildWave(configString: String): MonotonicCurveFit {
            val values = mutableListOf<Double>()
            val start = configString.indexOf('(') + 1
            val end = configString.indexOf(')')
            val content = configString.substring(start, end)
            for (part in content.split(',')) {
                values.add(part.trim().toDouble())
            }
            return buildWave(values.toDoubleArray())
        }

        private fun buildWave(values: DoubleArray): MonotonicCurveFit {
            val length = values.size * 3 - 2
            val len = values.size - 1
            val gap = 1.0 / len
            val points = Array(length) { DoubleArray(1) }
            val time = DoubleArray(length)
            for (i in values.indices) {
                val v = values[i]
                points[i + len][0] = v
                time[i + len] = i * gap
                if (i > 0) {
                    points[i + len * 2][0] = v + 1
                    time[i + len * 2] = i * gap + 1

                    points[i - 1][0] = v - 1 - gap
                    time[i - 1] = i * gap + -1 - gap
                }
            }
            return MonotonicCurveFit(time, points)
        }
    }
}
