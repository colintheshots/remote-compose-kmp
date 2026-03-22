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
package androidx.compose.remote.core.operations

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.TouchListener
import androidx.compose.remote.core.VariableSupport
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.utilities.AnimatedFloatExpression
import androidx.compose.remote.core.operations.utilities.NanMap
import androidx.compose.remote.core.operations.utilities.touch.VelocityEasing
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.max

class TouchExpression(
    private var mId: Int,
    var mSrcExp: FloatArray,
    private var mDefValue: Float,
    private var mMin: Float,
    private var mMax: Float,
    var mTouchEffects: Int,
    var mVelocityId: Float,
    var mStopMode: Int,
    var mStopSpec: FloatArray?,
    easingSpec: FloatArray?
) : Operation(), ComponentData, VariableSupport, TouchListener, Serializable {

    private var mOutDefValue: Float = mDefValue
    var mMode: Int = if (STOP_ABSOLUTE_POS == mStopMode) 1 else 0
    var mOutMax: Float = mMax
    var mOutMin: Float = mMin
    var mValue: Float = 0f
    var mMaxAtDown: Float = Float.NaN
    var mMinAtDown: Float = Float.NaN
    var mUnmodified: Boolean = true
    private var mPreCalcValue: FloatArray? = null
    private var mLastChange: Float = Float.NaN
    private var mLastCalculatedValue: Float = Float.NaN
    var mExp: AnimatedFloatExpression = AnimatedFloatExpression()
    var mWrapMode: Boolean = false
    private val mEasyTouch = VelocityEasing()
    private var mEasingToStop = false
    private var mTouchUpTime = 0f
    var mCurrentValue: Float = Float.NaN
    private var mTouchDown = false
    var mMaxTime: Float = 1f
    var mMaxAcceleration: Float = 5f
    var mMaxVelocity: Float = 7f
    var mNotches: FloatArray? = null
    var mOutStopSpec: FloatArray? = null
    var mScrLeft = 0f; var mScrRight = 0f; var mScrTop = 0f; var mScrBottom = 0f
    var mValueAtDown = 0f; var mDownTouchValue = 0f; var mLastValue = 0f

    init {
        if (mStopSpec != null) mOutStopSpec = mStopSpec!!.copyOf()
        if (mMin.isNaN() && Utils.idFromNan(mMin) == 0) mWrapMode = true
        else mOutMin = mMin
        easingSpec?.let {
            if (it.size >= 4 && it[0].toRawBits() == 0) {
                mMaxTime = it[1]; mMaxAcceleration = it[2]; mMaxVelocity = it[3]
            }
        }
    }

    override fun updateVariables(context: RemoteContext) {
        val src = mSrcExp
        if (mPreCalcValue == null || mPreCalcValue!!.size != src.size) mPreCalcValue = FloatArray(src.size)
        if (mStopSpec != null && (mOutStopSpec == null || mOutStopSpec!!.size != mStopSpec!!.size))
            mOutStopSpec = FloatArray(mStopSpec!!.size)
        if (mMax.isNaN()) mOutMax = context.getFloat(Utils.idFromNan(mMax))
        if (mMin.isNaN()) mOutMin = context.getFloat(Utils.idFromNan(mMin))
        if (mDefValue.isNaN()) mOutDefValue = context.getFloat(Utils.idFromNan(mDefValue))
        for (i in src.indices) {
            val v = src[i]
            if (v.isNaN() && !AnimatedFloatExpression.isMathOperator(v) && !NanMap.isDataVariable(v))
                mPreCalcValue!![i] = context.getFloat(Utils.idFromNan(v))
            else mPreCalcValue!![i] = src[i]
        }
        mStopSpec?.let { spec ->
            for (i in spec.indices) {
                val v = spec[i]
                mOutStopSpec!![i] = if (v.isNaN()) context.getFloat(Utils.idFromNan(v)) else v
            }
        }
    }

    override fun registerListening(context: RemoteContext) {
        if (mMax.isNaN()) context.listensTo(Utils.idFromNan(mMax), this)
        if (mMin.isNaN()) context.listensTo(Utils.idFromNan(mMin), this)
        if (mDefValue.isNaN()) context.listensTo(Utils.idFromNan(mDefValue), this)
        context.addTouchListener(this)
        for (v in mSrcExp) {
            if (v.isNaN() && !AnimatedFloatExpression.isMathOperator(v) && !NanMap.isDataVariable(v))
                context.listensTo(Utils.idFromNan(v), this)
        }
        mStopSpec?.let { for (v in it) { if (v.isNaN()) context.listensTo(Utils.idFromNan(v), this) } }
    }

    override fun markDirty() { super<Operation>.markDirty() }
    override fun setComponent(component: androidx.compose.remote.core.operations.layout.Component?) {}

    override fun apply(context: RemoteContext) {
        if (mUnmodified) { mCurrentValue = mOutDefValue; context.loadFloat(mId, wrap(mCurrentValue)); return }
        if (mEasingToStop) {
            val time = context.getAnimationTime() - mTouchUpTime
            var value = mEasyTouch.getPos(time)
            mCurrentValue = value
            value = if (mWrapMode) wrap(value) else min(max(value, mOutMin), mOutMax)
            context.loadFloat(mId, value)
            if (mEasyTouch.getDuration() < time) mEasingToStop = false
            context.needsRepaint()
            return
        }
        if (mTouchDown) {
            var value = mExp.eval(context.getCollectionsAccess()!!, mPreCalcValue!!, mPreCalcValue!!.size)
            if (mMode == 0) value = mValueAtDown + (value - mDownTouchValue)
            value = if (mWrapMode) wrap(value) else min(max(value, mOutMin), mOutMax)
            mCurrentValue = value
        }
        context.loadFloat(mId, wrap(mCurrentValue))
    }

    private fun wrap(pos: Float): Float {
        if (!mWrapMode) return pos
        var p = pos % mOutMax
        if (p < 0) p += mOutMax
        return p
    }

    override fun touchDown(context: RemoteContext, x: Float, y: Float) {
        if (!(x >= mScrLeft && x <= mScrRight && y >= mScrTop && y <= mScrBottom)) return
        mEasingToStop = false; mTouchDown = true; mUnmodified = false
        if (mMode == 0) {
            mValueAtDown = context.getFloat(mId)
            mDownTouchValue = mExp.eval(context.getCollectionsAccess()!!, mPreCalcValue!!, mPreCalcValue!!.size)
        }
        context.needsRepaint()
    }

    override fun touchUp(context: RemoteContext, x: Float, y: Float, dx: Float, dy: Float) {
        if (!mTouchDown) return
        mTouchDown = false
        if (mStopMode == STOP_INSTANTLY) return
        val v = mExp.eval(context.getCollectionsAccess()!!, mPreCalcValue!!, mPreCalcValue!!.size)
        val dt = 0.0001f
        for (i in mSrcExp.indices) {
            if (mSrcExp[i].isNaN()) {
                val id = Utils.idFromNan(mSrcExp[i])
                if (id == RemoteContext.ID_TOUCH_POS_X) mPreCalcValue!![i] = x + dx * dt
                else if (id == RemoteContext.ID_TOUCH_POS_Y) mPreCalcValue!![i] = y + dy * dt
            }
        }
        val vdt = mExp.eval(context.getCollectionsAccess()!!, mPreCalcValue!!, mPreCalcValue!!.size)
        val slope = (vdt - v) / dt
        val value = context.getFloat(mId)
        mTouchUpTime = context.getAnimationTime()
        val dest = value + slope / 2f
        val time = min(2f, mMaxTime * abs(dest - value) / (2 * mMaxVelocity))
        mEasyTouch.config(value, dest, slope, time, mMaxAcceleration, mMaxVelocity, null)
        mEasingToStop = true
        context.needsRepaint()
    }

    override fun touchDrag(context: RemoteContext, x: Float, y: Float) {
        if (!mTouchDown) return
        apply(context); context.needsRepaint()
    }

    override fun write(buffer: WireBuffer) {
        apply(buffer, mId, mValue, mMin, mMax, mVelocityId, mTouchEffects, mSrcExp, mStopMode, mNotches, null)
    }

    override fun toString(): String = "TouchExpression[$mId]"
    override fun deepToString(indent: String): String = indent + toString()

    override fun serialize(serializer: MapSerializer) {
        serializer.addType(CLASS_NAME).add("id", mId)
            .add("defValue", mDefValue, mOutDefValue)
            .add("min", mMin, mOutMin).add("max", mMax, mOutMax)
            .add("mode", mMode).addFloatExpressionSrc("srcExp", mSrcExp)
    }

    companion object {
        private val OP_CODE = Operations.TOUCH_EXPRESSION
        private const val CLASS_NAME = "TouchExpression"
        const val MAX_EXPRESSION_SIZE = 32
        const val STOP_GENTLY = 0; const val STOP_INSTANTLY = 1; const val STOP_ENDS = 2
        const val STOP_NOTCHES_EVEN = 3; const val STOP_NOTCHES_PERCENTS = 4
        const val STOP_NOTCHES_ABSOLUTE = 5; const val STOP_ABSOLUTE_POS = 6
        const val STOP_NOTCHES_SINGLE_EVEN = 7

        fun name(): String = CLASS_NAME
        fun id(): Int = OP_CODE

        fun apply(buffer: WireBuffer, id: Int, value: Float, min: Float, max: Float,
                  velocityId: Float, touchEffects: Int, exp: FloatArray, touchMode: Int,
                  touchSpec: FloatArray?, easingSpec: FloatArray?) {
            buffer.start(OP_CODE); buffer.writeInt(id); buffer.writeFloat(value)
            buffer.writeFloat(min); buffer.writeFloat(max); buffer.writeFloat(velocityId)
            buffer.writeInt(touchEffects); buffer.writeInt(exp.size)
            for (v in exp) buffer.writeFloat(v)
            var len = touchSpec?.size ?: 0
            buffer.writeInt((touchMode shl 16) or len)
            for (i in 0 until len) buffer.writeFloat(touchSpec!![i])
            len = easingSpec?.size ?: 0
            buffer.writeInt(len)
            for (i in 0 until len) buffer.writeFloat(easingSpec!![i])
        }

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val id = buffer.readInt(); val startValue = buffer.readFloat()
            val min = buffer.readFloat(); val max = buffer.readFloat()
            val velocityId = buffer.readFloat(); val touchEffects = buffer.readInt()
            val len = buffer.readInt(); val valueLen = len and 0xFFFF
            if (valueLen > MAX_EXPRESSION_SIZE) throw RuntimeException("Float expression too long")
            val exp = FloatArray(valueLen) { buffer.readFloat() }
            val stopLogic = buffer.readInt(); val stopLen = stopLogic and 0xFFFF; val stopMode = stopLogic shr 16
            val stopsData = FloatArray(stopLen) { buffer.readFloat() }
            val easingLen = buffer.readInt()
            val easingData = FloatArray(easingLen) { buffer.readFloat() }
            operations.add(TouchExpression(id, exp, startValue, min, max, touchEffects, velocityId, stopMode, stopsData, easingData))
        }
    }
}
