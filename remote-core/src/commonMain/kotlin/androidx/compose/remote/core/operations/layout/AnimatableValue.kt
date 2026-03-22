package androidx.compose.remote.core.operations.layout
import androidx.compose.remote.core.operations.utilities.easing.Easing

import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.operations.Utils
import androidx.compose.remote.core.operations.utilities.easing.FloatAnimation
import androidx.compose.remote.core.operations.utilities.easing.GeneralEasing

class AnimatableValue(
    rawValue: Float,
    private var mAnimateValueChanges: Boolean = true
) {
    private var mIsVariable: Boolean = false
    private var mId: Int = 0
    var value: Float = 0f
        private set

    private var mAnimate: Boolean = false
    private var mAnimateTargetTime: Long = 0
    private var mAnimateDuration: Float = 300f
    private var mTargetRotationX: Float = 0f
    private var mStartRotationX: Float = 0f
    private var mLastUpdate: Long = 0L
    private var mMotionEasingType: Int = Easing.CUBIC_STANDARD
    private var mMotionEasing: FloatAnimation? = null

    init {
        if (Utils.isVariable(rawValue)) {
            mId = Utils.idFromNan(rawValue)
            mIsVariable = true
        } else {
            value = rawValue
        }
    }

    fun evaluate(context: PaintContext): Float {
        if (!mIsVariable) return value
        val newValue = context.getContext().mRemoteComposeState.getFloat(mId)
        val timeMillis = context.getCurrentTimeMillis()
        if (newValue != value) {
            val interval = timeMillis - mLastUpdate
            mAnimateValueChanges = interval > mAnimateDuration && mLastUpdate != 0L
            mLastUpdate = timeMillis
        }
        if (!mAnimateValueChanges) {
            value = newValue
        } else {
            if (newValue != value && !mAnimate) {
                mStartRotationX = value
                mTargetRotationX = newValue
                mAnimate = true
                mAnimateTargetTime = timeMillis
                mMotionEasing = FloatAnimation(mMotionEasingType, mAnimateDuration / 1000f, null, 0f, Float.NaN)
                mMotionEasing!!.setTargetValue(1f)
            }
            if (mAnimate) {
                val elapsed = (timeMillis - mAnimateTargetTime).toFloat()
                val p = mMotionEasing!!.get(elapsed / mAnimateDuration)
                value = (1 - p) * mStartRotationX + p * mTargetRotationX
                if (p >= 1f) mAnimate = false
            } else {
                value = mTargetRotationX
            }
        }
        return value
    }

    override fun toString(): String = "AnimatableValue{mId=$mId}"
}
