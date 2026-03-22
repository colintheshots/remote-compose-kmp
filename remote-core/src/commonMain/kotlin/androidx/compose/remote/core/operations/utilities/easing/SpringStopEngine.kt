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

/**
 * This contains the class to provide the logic for an animation to come to a stop using a spring
 * model.
 */
class SpringStopEngine {
    var mDamping = 0.5

    @Suppress("unused")
    private var mInitialized = false

    private var mStiffness: Double = 0.0
    private var mTargetPos: Double = 0.0

    @Suppress("unused")
    private var mLastVelocity: Double = 0.0

    private var mLastTime: Float = 0f
    private var mPos: Float = 0f
    private var mV: Float = 0f
    private var mMass: Float = 0f
    private var mStopThreshold: Float = 0f
    private var mBoundaryMode = 0

    constructor()

    /**
     * get the value the spring is pulling towards
     *
     * @return the value the spring is pulling towards
     */
    fun getTargetValue(): Float = mTargetPos.toFloat()

    /**
     * get the value the spring is starting from
     *
     * @param v the value the spring is starting from
     */
    fun setInitialValue(v: Float) {
        mPos = v
    }

    /**
     * set the value the spring is pulling towards
     *
     * @param v the value the spring is pulling towards
     */
    fun setTargetValue(v: Float) {
        mTargetPos = v.toDouble()
    }

    /**
     * Create a spring engine with the parameters encoded as an array of floats
     *
     * @param parameters the parameters to use
     */
    constructor(parameters: FloatArray) {
        if (parameters[0] != 0f) {
            throw RuntimeException(" parameter[0] should be 0")
        }
        springParameters(
            1f,
            parameters[1],
            parameters[2],
            parameters[3],
            parameters[4].toRawBits()
        )
    }

    /**
     * Config the spring starting conditions
     *
     * @param currentPos the current position of the spring
     * @param target the target position of the spring
     * @param currentVelocity the current velocity of the spring
     */
    fun springStart(currentPos: Float, target: Float, currentVelocity: Float) {
        mTargetPos = target.toDouble()
        mInitialized = false
        mPos = currentPos
        mLastVelocity = currentVelocity.toDouble()
        mLastTime = 0f
    }

    /**
     * Config the spring parameters
     *
     * @param mass The mass of the spring
     * @param stiffness The stiffness of the spring
     * @param damping The dampening factor
     * @param stopThreshold how low energy must you be to stop
     * @param boundaryMode The boundary behaviour
     */
    fun springParameters(
        mass: Float,
        stiffness: Float,
        damping: Float,
        stopThreshold: Float,
        boundaryMode: Int
    ) {
        mDamping = damping.toDouble()
        mInitialized = false
        mStiffness = stiffness.toDouble()
        mMass = mass
        mStopThreshold = stopThreshold
        mBoundaryMode = boundaryMode
        mLastTime = 0f
    }

    /**
     * get the velocity of the spring at a time
     *
     * @param time the time to get the velocity at
     * @return the velocity of the spring at a time
     */
    @Suppress("UNUSED_PARAMETER")
    fun getVelocity(time: Float): Float = mV

    /**
     * get the position of the spring at a time
     *
     * @param time the time to get the position at
     * @return the position of the spring at a time
     */
    fun get(time: Float): Float {
        compute((time - mLastTime).toDouble())
        mLastTime = time
        if (isStopped()) {
            mPos = mTargetPos.toFloat()
        }
        return mPos
    }

    /**
     * get the acceleration of the spring
     *
     * @return the acceleration of the spring
     */
    fun getAcceleration(): Float {
        val k = mStiffness
        val c = mDamping
        val x = (mPos - mTargetPos)
        return ((-k * x - c * mV) / mMass).toFloat()
    }

    /**
     * get the velocity of the spring
     *
     * @return the velocity of the spring
     */
    fun getVelocity(): Float = 0f

    /**
     * is the spring stopped
     *
     * @return true if the spring is stopped
     */
    fun isStopped(): Boolean {
        val x = (mPos - mTargetPos)
        val k = mStiffness
        val v = mV.toDouble()
        val m = mMass.toDouble()
        val energy = v * v * m + k * x * x
        val maxDef = sqrt(energy / k)
        return maxDef <= mStopThreshold
    }

    /**
     * increment the spring position over time dt
     *
     * @param dt the time to increment the spring position over
     */
    private fun compute(dt: Double) {
        if (dt <= 0) return

        val k = mStiffness
        val c = mDamping
        val overSample = (1 + 9 / (sqrt(mStiffness / mMass) * dt * 4)).toInt()
        val stepDt = dt / overSample

        for (i in 0 until overSample) {
            val x = (mPos - mTargetPos)
            val a = (-k * x - c * mV) / mMass
            val avgV = mV + a * stepDt / 2
            val avgX = mPos + stepDt * avgV / 2 - mTargetPos
            val refinedA = (-avgX * k - avgV * c) / mMass

            val dv = refinedA * stepDt
            val finalAvgV = mV + dv / 2
            mV += dv.toFloat()
            mPos += (finalAvgV * stepDt).toFloat()
            if (mBoundaryMode > 0) {
                if (mPos < 0 && ((mBoundaryMode and 1) == 1)) {
                    mPos = -mPos
                    mV = -mV
                }
                if (mPos > 1 && ((mBoundaryMode and 2) == 2)) {
                    mPos = 2 - mPos
                    mV = -mV
                }
            }
        }
    }
}
