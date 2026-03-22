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

/** The standard interface to Easing functions */
abstract class Easing {
    var mType: Int = 0

    /**
     * get the value at point x
     *
     * @param x the position at which to get the slope
     * @return the value at the point
     */
    abstract fun get(x: Float): Float

    /**
     * get the slope of the easing function at at x
     *
     * @param x the position at which to get the slope
     * @return the slope
     */
    abstract fun getDiff(x: Float): Float

    /**
     * get the type of easing function
     *
     * @return the type of easing function
     */
    open fun getType(): Int = mType

    companion object {
        /** cubic Easing function that accelerates and decelerates */
        const val CUBIC_STANDARD = 1

        /** cubic Easing function that accelerates */
        const val CUBIC_ACCELERATE = 2

        /** cubic Easing function that decelerates */
        const val CUBIC_DECELERATE = 3

        /** cubic Easing function that just linearly interpolates */
        const val CUBIC_LINEAR = 4

        /** cubic Easing function that goes backwards and then accelerates */
        const val CUBIC_ANTICIPATE = 5

        /** cubic Easing function that overshoots and then goes back */
        const val CUBIC_OVERSHOOT = 6

        /** cubic Easing function that you customize */
        const val CUBIC_CUSTOM = 11

        /** a monotonic spline Easing function that you customize */
        const val SPLINE_CUSTOM = 12

        /** a bouncing Easing function */
        const val EASE_OUT_BOUNCE = 13

        /** a elastic Easing function */
        const val EASE_OUT_ELASTIC = 14

        /**
         * Returns a string representation for the given value. Used during serialization.
         */
        fun getString(value: Int): String = when (value) {
            CUBIC_STANDARD -> "CUBIC_STANDARD"
            CUBIC_ACCELERATE -> "CUBIC_ACCELERATE"
            CUBIC_DECELERATE -> "CUBIC_DECELERATE"
            CUBIC_LINEAR -> "CUBIC_LINEAR"
            CUBIC_ANTICIPATE -> "CUBIC_ANTICIPATE"
            CUBIC_OVERSHOOT -> "CUBIC_OVERSHOOT"
            CUBIC_CUSTOM -> "CUBIC_CUSTOM"
            SPLINE_CUSTOM -> "SPLINE_CUSTOM"
            EASE_OUT_BOUNCE -> "EASE_OUT_BOUNCE"
            EASE_OUT_ELASTIC -> "EASE_OUT_ELASTIC"
            else -> "INVALID_CURVE_TYPE[$value]"
        }
    }
}
