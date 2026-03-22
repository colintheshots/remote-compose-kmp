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

/**
 * This class translates a series of floating point values into a continuous curve for use in an
 * easing function including quantize functions it is used with the "spline(0,0.3,0.3,0.5,...0.9,1)"
 * it should start at 0 and end at one 1
 */
class StepCurve(params: FloatArray, offset: Int, len: Int) : Easing() {

    private val mCurveFit: MonotonicCurveFit = genSpline(params, offset, len)

    override fun getDiff(x: Float): Float {
        if (x < 0f) return 0f
        if (x > 1f) return 0f
        return mCurveFit.getSlope(x.toDouble(), 0).toFloat()
    }

    override fun get(x: Float): Float {
        if (x < 0f) return 0f
        if (x > 1f) return 1f
        return mCurveFit.getPos(x.toDouble(), 0).toFloat()
    }

    companion object {
        private fun genSpline(values: FloatArray, off: Int, arrayLen: Int): MonotonicCurveFit {
            val length = arrayLen * 3 - 2
            val len = arrayLen - 1
            val gap = 1.0 / len
            val points = Array(length) { DoubleArray(1) }
            val time = DoubleArray(length)
            for (i in 0 until arrayLen) {
                val v = values[i + off].toDouble()
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
