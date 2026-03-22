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
package androidx.compose.remote.core.operations.utilities.touch

import kotlin.math.abs
import kotlin.math.sign
import kotlin.math.sqrt

/**
 * This computes a form of easing such that the values constrained to be consistent in velocity.
 * The easing function is also constrained by the configure to have:
 * a maximum time to stop, a maximum velocity, a maximum acceleration
 */
class VelocityEasing {

    private var mStartPos = 0f
    private var mStartV = 0f
    private var mEndPos = 0f
    private var mDuration = 0f

    private val mStage = arrayOf(Stage(1), Stage(2), Stage(3))
    private var mNumberOfStages = 0
    private var mEasing: Easing? = null
    private var mEasingAdapterDistance = 0.0
    private var mEasingAdapterA = 0.0
    private var mEasingAdapterB = 0.0
    private var mOneDimension = true
    private var mTotalEasingDuration = 0f

    /**
     * get the duration the easing will take
     *
     * @return the duration for the easing
     */
    fun getDuration(): Float {
        if (mEasing != null) return mTotalEasingDuration
        return mDuration
    }

    /**
     * Get the velocity at time t
     *
     * @param t time in seconds
     * @return the velocity units/second
     */
    fun getV(t: Float): Float {
        if (mEasing == null) {
            for (i in 0 until mNumberOfStages) {
                if (mStage[i].mEndTime > t) return mStage[i].getVel(t)
            }
            return 0f
        }
        val lastStages = mNumberOfStages - 1
        for (i in 0 until lastStages) {
            if (mStage[i].mEndTime > t) return mStage[i].getVel(t)
        }
        return getEasingDiff((t - mStage[lastStages].mStartTime).toDouble()).toFloat()
    }

    /**
     * Get the position t seconds after the configure
     *
     * @param t time in seconds
     * @return the position at time t
     */
    fun getPos(t: Float): Float {
        if (mEasing == null) {
            for (i in 0 until mNumberOfStages) {
                if (mStage[i].mEndTime > t) return mStage[i].getPos(t)
            }
            return mEndPos
        }
        val lastStages = mNumberOfStages - 1
        for (i in 0 until lastStages) {
            if (mStage[i].mEndTime > t) return mStage[i].getPos(t)
        }
        val ret = getEasing((t - mStage[lastStages].mStartTime).toDouble()).toFloat()
        return ret + mStage[lastStages].mStartPos
    }

    override fun toString(): String {
        var s = " "
        for (i in 0 until mNumberOfStages) {
            s += " $i ${mStage[i]}"
        }
        return s
    }

    /**
     * Configure the Velocity easing curve
     */
    fun config(
        currentPos: Float,
        destination: Float,
        currentVelocity: Float,
        maxTime: Float,
        maxAcceleration: Float,
        maxVelocity: Float,
        easing: Easing?
    ) {
        var pos = currentPos
        var velocity = currentVelocity
        if (pos == destination) pos += 1f
        mStartPos = pos
        mEndPos = destination
        if (easing != null) {
            this.mEasing = easing.clone()
        }
        val dir = sign(destination - pos)
        val maxV = maxVelocity * dir
        val maxA = maxAcceleration * dir
        if (velocity == 0.0f) velocity = 0.0001f * dir
        mStartV = velocity
        if (!rampDown(pos, destination, velocity, maxTime)) {
            if (!(mOneDimension && cruseThenRampDown(pos, destination, velocity, maxTime, maxA, maxV))) {
                if (!rampUpRampDown(pos, destination, velocity, maxA, maxV, maxTime)) {
                    rampUpCruseRampDown(pos, destination, velocity, maxA, maxV, maxTime)
                }
            }
        }
        if (mOneDimension) configureEasingAdapter()
    }

    private fun rampDown(
        currentPos: Float, destination: Float, currentVelocity: Float, maxTime: Float
    ): Boolean {
        val timeToDestination = 2 * ((destination - currentPos) / currentVelocity)
        if (timeToDestination > 0 && timeToDestination <= maxTime) {
            mNumberOfStages = 1
            mStage[0].setUp(currentVelocity, currentPos, 0f, 0f, destination, timeToDestination)
            mDuration = timeToDestination
            return true
        }
        return false
    }

    private fun cruseThenRampDown(
        currentPos: Float, destination: Float, currentVelocity: Float,
        maxTime: Float, maxA: Float, @Suppress("UNUSED_PARAMETER") maxV: Float
    ): Boolean {
        val timeToBreak = currentVelocity / maxA
        val brakeDist = currentVelocity * timeToBreak / 2
        val cruseDist = destination - currentPos - brakeDist
        val cruseTime = cruseDist / currentVelocity
        val totalTime = cruseTime + timeToBreak
        if (totalTime > 0 && totalTime < maxTime) {
            mNumberOfStages = 2
            mStage[0].setUp(currentVelocity, currentPos, 0f, currentVelocity, cruseDist, cruseTime)
            mStage[1].setUp(
                currentVelocity, currentPos + cruseDist, cruseTime,
                0f, destination, cruseTime + timeToBreak
            )
            mDuration = cruseTime + timeToBreak
            return true
        }
        return false
    }

    private fun rampUpRampDown(
        currentPos: Float, destination: Float, currentVelocity: Float,
        maxA: Float, maxVelocity: Float, maxTime: Float
    ): Boolean {
        val peakV = sign(maxA) *
            sqrt(abs(maxA * (destination - currentPos) + currentVelocity * currentVelocity / 2))
        if (maxVelocity / peakV > 1) {
            var t1 = (peakV - currentVelocity) / maxA
            var d1 = (peakV + currentVelocity) * t1 / 2 + currentPos
            var t2 = peakV / maxA
            mNumberOfStages = 2
            mStage[0].setUp(currentVelocity, currentPos, 0f, peakV, d1, t1)
            mStage[1].setUp(peakV, d1, t1, 0f, destination, t2 + t1)
            mDuration = t2 + t1
            if (mDuration > maxTime) return false
            if (mDuration < maxTime / 2) {
                t1 = mDuration / 2
                t2 = t1
                val newPeakV = (2 * (destination - currentPos) / t1 - currentVelocity) / 2
                d1 = (newPeakV + currentVelocity) * t1 / 2 + currentPos
                mNumberOfStages = 2
                mStage[0].setUp(currentVelocity, currentPos, 0f, newPeakV, d1, t1)
                mStage[1].setUp(newPeakV, d1, t1, 0f, destination, t2 + t1)
                mDuration = t2 + t1
                if (mDuration > maxTime) return false
            }
            return true
        }
        return false
    }

    private fun rampUpCruseRampDown(
        currentPos: Float, destination: Float, currentVelocity: Float,
        @Suppress("UNUSED_PARAMETER") maxA: Float,
        @Suppress("UNUSED_PARAMETER") maxV: Float, maxTime: Float
    ) {
        val t1 = maxTime / 3
        val t2 = t1 * 2
        val distance = destination - currentPos
        val dt2 = t2 - t1
        val dt3 = maxTime - t2
        val v1 = (2 * distance - currentVelocity * t1) / (t1 + 2 * dt2 + dt3)
        mDuration = maxTime
        val d1 = (currentVelocity + v1) * t1 / 2
        val d2 = (v1 + v1) * (t2 - t1) / 2
        mNumberOfStages = 3
        mStage[0].setUp(currentVelocity, currentPos, 0f, v1, currentPos + d1, t1)
        mStage[1].setUp(v1, currentPos + d1, t1, v1, currentPos + d1 + d2, t2)
        mStage[2].setUp(v1, currentPos + d1 + d2, t2, 0f, destination, maxTime)
        mDuration = maxTime
    }

    internal fun getEasing(t: Double): Double {
        val gx = t * t * mEasingAdapterA + t * mEasingAdapterB
        return if (gx > 1) {
            mEasingAdapterDistance
        } else {
            mEasing!!.get(gx) * mEasingAdapterDistance
        }
    }

    private fun getEasingDiff(t: Double): Double {
        val gx = t * t * mEasingAdapterA + t * mEasingAdapterB
        return if (gx > 1) {
            0.0
        } else {
            mEasing!!.getDiff(gx) * mEasingAdapterDistance *
                (t * mEasingAdapterA + mEasingAdapterB)
        }
    }

    internal fun configureEasingAdapter() {
        if (mEasing == null) return
        val last = mNumberOfStages - 1
        val initialVelocity = mStage[last].mStartV
        val distance = mStage[last].mEndPos - mStage[last].mStartPos
        val baseVel = mEasing!!.getDiff(0.0)
        mEasingAdapterB = initialVelocity / (baseVel * distance)
        mEasingAdapterA = 1 - mEasingAdapterB
        mEasingAdapterDistance = distance.toDouble()
        val easingDuration =
            (sqrt(4 * mEasingAdapterA + mEasingAdapterB * mEasingAdapterB) - mEasingAdapterB) /
                (2 * mEasingAdapterA)
        mTotalEasingDuration = (easingDuration + mStage[last].mStartTime).toFloat()
    }

    interface Easing {
        fun get(t: Double): Double
        fun getDiff(t: Double): Double
        fun clone(): Easing
    }

    internal class Stage(val mStage: Int) {
        var mStartV = 0f
        var mStartPos = 0f
        var mStartTime = 0f
        var mEndV = 0f
        var mEndPos = 0f
        var mEndTime = 0f
        var mDeltaV = 0f
        var mDeltaT = 0f

        fun setUp(
            startV: Float, startPos: Float, startTime: Float,
            endV: Float, endPos: Float, endTime: Float
        ) {
            mStartV = startV
            mStartPos = startPos
            mStartTime = startTime
            mEndV = endV
            mEndTime = endTime
            mEndPos = endPos
            mDeltaV = mEndV - mStartV
            mDeltaT = mEndTime - mStartTime
        }

        fun getPos(t: Float): Float {
            val dt = t - mStartTime
            val pt = dt / mDeltaT
            val v = mStartV + mDeltaV * pt
            return dt * (mStartV + v) / 2 + mStartPos
        }

        fun getVel(t: Float): Float {
            val dt = t - mStartTime
            val pt = dt / (mEndTime - mStartTime)
            return mStartV + mDeltaV * pt
        }
    }
}
