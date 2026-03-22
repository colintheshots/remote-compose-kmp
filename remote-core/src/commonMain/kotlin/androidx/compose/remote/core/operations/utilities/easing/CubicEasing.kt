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
 * Cubic easing function similar to CSS cubic-bezier
 * given two control point x1,y1 and x2,y2 the function is defined as
 * f(x) = (1-x)^3 * x1 + 3 * (1-x)^2 * x * y1 + 3 * (1-x) * x^2 * y2 + x^3 * y2
 */
class CubicEasing : Easing {

    var mX1 = 0f
    var mY1 = 0f
    var mX2 = 0f
    var mY2 = 0f

    /** A Standard CubicEasing function */
    constructor() {
        setup(STANDARD[0], STANDARD[1], STANDARD[2], STANDARD[3])
    }

    internal constructor(type: Int) {
        mType = type
        config(type)
    }

    internal constructor(x1: Float, y1: Float, x2: Float, y2: Float) {
        setup(x1, y1, x2, y2)
    }

    fun config(type: Int) {
        when (type) {
            CUBIC_STANDARD -> setup(STANDARD)
            CUBIC_ACCELERATE -> setup(ACCELERATE)
            CUBIC_DECELERATE -> setup(DECELERATE)
            CUBIC_LINEAR -> setup(LINEAR)
            CUBIC_ANTICIPATE -> setup(ANTICIPATE)
            CUBIC_OVERSHOOT -> setup(OVERSHOOT)
        }
        mType = type
    }

    internal fun setup(values: FloatArray) {
        setup(values[0], values[1], values[2], values[3])
    }

    /**
     * Setup the cubic function
     * @param x1 the x value of the first point
     * @param y1 the y value of the first point
     * @param x2 the x value of the second point
     * @param y2 the y value of the second point
     */
    fun setup(x1: Float, y1: Float, x2: Float, y2: Float) {
        mX1 = x1
        mY1 = y1
        mX2 = x2
        mY2 = y2
    }

    private fun getX(t: Float): Float {
        val t1 = 1 - t
        val f1 = 3 * t1 * t1 * t
        val f2 = 3 * t1 * t * t
        val f3 = t * t * t
        return mX1 * f1 + mX2 * f2 + f3
    }

    private fun getY(t: Float): Float {
        val t1 = 1 - t
        val f1 = 3 * t1 * t1 * t
        val f2 = 3 * t1 * t * t
        val f3 = t * t * t
        return mY1 * f1 + mY2 * f2 + f3
    }

    /** binary search for the region and linear interpolate the answer */
    override fun getDiff(x: Float): Float {
        var t = 0.5f
        var range = 0.5f
        while (range > D_ERROR) {
            val tx = getX(t)
            range *= 0.5f
            if (tx < x) {
                t += range
            } else {
                t -= range
            }
        }
        val x1 = getX(t - range)
        val x2 = getX(t + range)
        val y1 = getY(t - range)
        val y2 = getY(t + range)
        return (y2 - y1) / (x2 - x1)
    }

    /** binary search for the region and linear interpolate the answer */
    override fun get(x: Float): Float {
        if (x <= 0.0f) return 0f
        if (x >= 1.0f) return 1.0f
        var t = 0.5f
        var range = 0.5f
        while (range > ERROR) {
            val tx = getX(t)
            range *= 0.5f
            if (tx < x) {
                t += range
            } else {
                t -= range
            }
        }
        val x1 = getX(t - range)
        val x2 = getX(t + range)
        val y1 = getY(t - range)
        val y2 = getY(t + range)
        return (y2 - y1) * (x - x1) / (x2 - x1) + y1
    }

    companion object {
        private const val ERROR = 0.01f
        private const val D_ERROR = 0.0001f

        private val STANDARD = floatArrayOf(0.4f, 0.0f, 0.2f, 1f)
        private val ACCELERATE = floatArrayOf(0.4f, 0.05f, 0.8f, 0.7f)
        private val DECELERATE = floatArrayOf(0.0f, 0.0f, 0.2f, 0.95f)
        private val LINEAR = floatArrayOf(1f, 1f, 0f, 0f)
        private val ANTICIPATE = floatArrayOf(0.36f, 0f, 0.66f, -0.56f)
        private val OVERSHOOT = floatArrayOf(0.34f, 1.56f, 0.64f, 1f)
    }
}
