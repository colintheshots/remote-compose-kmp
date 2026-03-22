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

import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable

/** Support Animation of the FloatExpression */
class FloatAnimation : Easing, Serializable {
    var mSpec: FloatArray
    // mSpec[0] = duration
    // int(mSpec[1]) = num_of_param << 16 | type
    // mSpec[2..1+num_of_param] params
    // mSpec[2+num_of_param] starting Value
    var mEasingCurve: Easing? = null

    private var mDuration = 1f
    private var mWrap = Float.NaN
    private var mInitialValue = Float.NaN
    private var mTargetValue = Float.NaN
    private var mDirectionalSnap = 0
    var mOffset = 0f
    private var mPropagate = false

    override fun toString(): String {
        var str = "type $mType"
        if (!mInitialValue.isNaN()) {
            str += " $mInitialValue"
        }
        if (!mTargetValue.isNaN()) {
            str += " -> $mTargetValue"
        }
        if (!mWrap.isNaN()) {
            str += "  % $mWrap"
        }
        return str
    }

    /**
     * Create an animation based on a float encoding of the animation
     *
     * @param description the float encoding of the animation
     */
    constructor(vararg description: Float) {
        mSpec = description
        mType = CUBIC_STANDARD
        setAnimationDescription(description)
    }

    /**
     * Create an animation based on the parameters
     *
     * @param type The type of animation
     * @param duration The duration of the animation
     * @param description The float parameters describing the animation
     * @param initialValue The initial value of the float (NaN if none)
     * @param wrap The wrap value of the animation NaN if it does not wrap
     */
    constructor(
        type: Int,
        duration: Float,
        description: FloatArray?,
        initialValue: Float,
        wrap: Float
    ) {
        mSpec = FloatArray(0)
        mType = CUBIC_STANDARD
        setAnimationDescription(packToFloatArray(duration, type, description, initialValue, wrap))
    }

    /**
     * Create an animation based on a float encoding of the animation
     *
     * @param description the float encoding of the animation
     */
    fun setAnimationDescription(description: FloatArray) {
        mSpec = description
        mDuration = if (mSpec.isEmpty()) 1f else mSpec[0]
        var len = 0
        if (mSpec.size > 1) {
            val numType = mSpec[1].toRawBits()
            mType = numType and 0xFF
            val wrap = ((numType shr 8) and 0x1) > 0
            val init = ((numType shr 8) and 0x2) > 0
            val directional = (numType shr 10) and 0x3
            val propagate = ((numType shr 12) and 0x1) > 0
            len = (numType shr 16) and 0xFFFF
            var off = 2 + len
            if (init) {
                mInitialValue = mSpec[off++]
            }
            if (wrap) {
                mWrap = mSpec[off]
            }
            mDirectionalSnap = directional
            mPropagate = propagate
        }
        create(mType, description, 2, len)
    }

    private fun create(type: Int, params: FloatArray?, offset: Int, len: Int) {
        when (type) {
            CUBIC_STANDARD,
            CUBIC_ACCELERATE,
            CUBIC_DECELERATE,
            CUBIC_LINEAR,
            CUBIC_ANTICIPATE,
            CUBIC_OVERSHOOT -> mEasingCurve = CubicEasing(type)
            CUBIC_CUSTOM -> mEasingCurve = CubicEasing(
                params!![offset + 0],
                params[offset + 1],
                params[offset + 2],
                params[offset + 3]
            )
            EASE_OUT_BOUNCE -> mEasingCurve = BounceCurve(type)
            EASE_OUT_ELASTIC -> mEasingCurve = ElasticOutCurve()
            SPLINE_CUSTOM -> mEasingCurve = StepCurve(params!!, offset, len)
        }
    }

    /**
     * Get the duration the interpolate is to take
     *
     * @return duration in seconds
     */
    fun getDuration(): Float = mDuration

    /**
     * Set the initial Value
     *
     * @param value the value to set
     */
    fun setInitialValue(value: Float) {
        mInitialValue = if (mWrap.isNaN()) {
            value
        } else {
            value % mWrap
        }
        setScaleOffset()
    }

    /**
     * Set the target value to interpolate to
     *
     * @param value the value to set
     */
    fun setTargetValue(value: Float) {
        mTargetValue = value
        if (!mWrap.isNaN()) {
            mInitialValue = wrap(mWrap, mInitialValue)
            mTargetValue = wrap(mWrap, mTargetValue)
            if (mInitialValue.isNaN()) {
                mInitialValue = mTargetValue
            }

            val dist = wrapDistance(mWrap, mInitialValue, mTargetValue)
            if ((dist > 0) && (mTargetValue < mInitialValue)) {
                mTargetValue += mWrap
            } else if ((dist < 0) && mDirectionalSnap != 0) {
                if (mDirectionalSnap == 1 && mTargetValue > mInitialValue) {
                    mInitialValue = mTargetValue
                }
                if (mDirectionalSnap == 2 && mTargetValue < mInitialValue) {
                    mInitialValue = mTargetValue
                }
                mTargetValue -= mWrap
            }
        }
        setScaleOffset()
    }

    /**
     * Get the target value
     *
     * @return the target value
     */
    fun getTargetValue(): Float = mTargetValue

    private fun setScaleOffset() {
        if (!mInitialValue.isNaN() && !mTargetValue.isNaN()) {
            mOffset = mInitialValue
        } else {
            mOffset = 0f
        }
    }

    /** get the value at time t in seconds since start */
    override fun get(t: Float): Float {
        if (mDirectionalSnap == 1 && mTargetValue < mInitialValue) {
            mInitialValue = mTargetValue
            return mTargetValue
        }
        if (mDirectionalSnap == 2 && mTargetValue > mInitialValue) {
            mInitialValue = mTargetValue
            return mTargetValue
        }
        return mEasingCurve!!.get(t / mDuration) * (mTargetValue - mInitialValue) + mInitialValue
    }

    /** get the slope of the easing function at at x */
    override fun getDiff(t: Float): Float {
        return mEasingCurve!!.getDiff(t / mDuration) * (mTargetValue - mInitialValue)
    }

    /**
     * @return if you should propagate the animation
     */
    fun isPropagate(): Boolean = mPropagate

    /**
     * Get the initial value
     *
     * @return the initial value
     */
    fun getInitialValue(): Float = mInitialValue

    override fun serialize(serializer: MapSerializer) {
        serializer
            .addType("FloatAnimation")
            .add("initialValue", mInitialValue)
            .add("targetValue", mTargetValue)
            .add("duration", mDuration)
            .add("easing", getString(mEasingCurve!!.getType()))
    }

    companion object {
        private fun wrap(wrap: Float, value: Float): Float {
            var v = value % wrap
            if (v < 0) {
                v += wrap
            }
            return v
        }

        /**
         * packs spec into a float array
         */
        fun packToFloatArray(
            duration: Float,
            type: Int,
            spec: FloatArray?,
            initialValue: Float,
            wrap: Float
        ): FloatArray {
            var count = 0

            if (!initialValue.isNaN()) count++
            if (spec != null) count++
            if (spec != null || type != CUBIC_STANDARD) {
                count++
                count += spec?.size ?: 0
            }
            if (!initialValue.isNaN()) count++
            if (!wrap.isNaN()) count++
            if (duration != 1f || count > 0) count++
            if (!wrap.isNaN() || !initialValue.isNaN()) count++
            val ret = FloatArray(count)
            var pos = 0
            val specLen = spec?.size ?: 0

            if (ret.isNotEmpty()) {
                ret[pos++] = duration
            }
            if (ret.size > 1) {
                val wrapBit = if (wrap.isNaN()) 0 else 1
                val initBit = if (initialValue.isNaN()) 0 else 2
                val bits = type or ((wrapBit or initBit) shl 8)
                ret[pos++] = Float.fromBits(specLen shl 16 or bits)
            }
            if (specLen > 0) {
                spec!!.copyInto(ret, pos, 0, spec.size)
                pos += spec.size
            }
            if (!initialValue.isNaN()) {
                ret[pos++] = initialValue
            }
            if (!wrap.isNaN()) {
                ret[pos] = wrap
            }
            return ret
        }

        /**
         * Useful to debug the packed form of an animation string
         *
         * @param description the float encoding of the animation
         * @return a string describing the animation
         */
        fun unpackAnimationToString(description: FloatArray): String {
            val spec = description
            val dur = if (spec.isEmpty()) 1f else spec[0]
            var len = 0
            var type = 0
            var wrapValue = Float.NaN
            var initialValue = Float.NaN
            var directionalSnap = 0
            var propagate = false
            if (spec.size > 1) {
                val numType = spec[1].toRawBits()
                type = numType and 0xFF
                val wrap = ((numType shr 8) and 0x1) > 0
                val init = ((numType shr 8) and 0x2) > 0
                directionalSnap = (numType shr 10) and 0x3
                propagate = ((numType shr 12) and 0x1) > 0
                len = (numType shr 16) and 0xFFFF
                var off = 2 + len
                if (init) {
                    initialValue = spec[off++]
                }
                if (wrap) {
                    wrapValue = spec[off]
                }
            }
            val params = description
            val offset = 2

            val typeStr = when (type) {
                CUBIC_STANDARD -> "CUBIC_STANDARD"
                CUBIC_ACCELERATE -> "CUBIC_ACCELERATE"
                CUBIC_DECELERATE -> "CUBIC_DECELERATE"
                CUBIC_LINEAR -> "CUBIC_LINEAR"
                CUBIC_ANTICIPATE -> "CUBIC_ANTICIPATE"
                CUBIC_OVERSHOOT -> "CUBIC_OVERSHOOT"
                CUBIC_CUSTOM -> buildString {
                    append("CUBIC_CUSTOM (")
                    append("${params[offset + 0]} ")
                    append("${params[offset + 1]} ")
                    append("${params[offset + 2]} ")
                    append("${params[offset + 3]} )")
                }
                EASE_OUT_BOUNCE -> "EASE_OUT_BOUNCE"
                EASE_OUT_ELASTIC -> "EASE_OUT_ELASTIC"
                SPLINE_CUSTOM -> buildString {
                    append("SPLINE_CUSTOM (")
                    for (i in offset until offset + len) {
                        append("${params[i]} ")
                    }
                    append(")")
                }
                else -> "UNKNOWN"
            }

            var str = "$dur $typeStr"
            if (!initialValue.isNaN()) str += " init =$initialValue"
            if (!wrapValue.isNaN()) str += " wrap =$wrapValue"
            if (directionalSnap != 0) str += " directionalSnap=$directionalSnap"
            if (propagate) str += " propagate"
            return str
        }
    }

    internal fun wrapDistance(wrap: Float, from: Float, to: Float): Float {
        var delta = (to - from) % 360
        if (delta < -wrap / 2) {
            delta += wrap
        } else if (delta > wrap / 2) {
            delta -= wrap
        }
        return delta
    }
}
