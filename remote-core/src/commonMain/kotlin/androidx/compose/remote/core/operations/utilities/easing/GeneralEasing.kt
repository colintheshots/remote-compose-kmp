/*
 * Copyright (C) 2023 The Android Open Source Project
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

/** Provides an interface to create easing functions */
class GeneralEasing : Easing() {
    var mEasingData: FloatArray = FloatArray(0)
    var mEasingCurve: Easing = CubicEasing(CUBIC_STANDARD)

    /**
     * Set the curve based on the float encoding of it
     *
     * @param data the float encoding of the curve
     */
    fun setCurveSpecification(data: FloatArray) {
        mEasingData = data
        createEngine()
    }

    /**
     * Get the float encoding of the curve
     *
     * @return the float encoding of the curve
     */
    fun getCurveSpecification(): FloatArray = mEasingData

    internal fun createEngine() {
        val type = Float.fromBits(mEasingData[0].toRawBits())
            .toRawBits()
        when (type) {
            CUBIC_STANDARD,
            CUBIC_ACCELERATE,
            CUBIC_DECELERATE,
            CUBIC_LINEAR,
            CUBIC_ANTICIPATE,
            CUBIC_OVERSHOOT -> mEasingCurve = CubicEasing(type)
            CUBIC_CUSTOM -> mEasingCurve =
                CubicEasing(mEasingData[1], mEasingData[2], mEasingData[3], mEasingData[5])
            EASE_OUT_BOUNCE -> mEasingCurve = BounceCurve(type)
        }
    }

    /** get the value at point x */
    override fun get(x: Float): Float = mEasingCurve.get(x)

    /** get the slope of the easing function at at x */
    override fun getDiff(x: Float): Float = mEasingCurve.getDiff(x)

    override fun getType(): Int = mEasingCurve.getType()
}
