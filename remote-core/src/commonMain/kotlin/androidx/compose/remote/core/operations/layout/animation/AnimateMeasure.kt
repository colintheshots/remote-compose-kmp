package androidx.compose.remote.core.operations.layout.animation
import androidx.compose.remote.core.operations.utilities.easing.Easing

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.operations.layout.Component
import androidx.compose.remote.core.operations.layout.DecoratorComponent
import androidx.compose.remote.core.operations.layout.measure.ComponentMeasure
import androidx.compose.remote.core.operations.layout.modifiers.PaddingModifierOperation
import androidx.compose.remote.core.operations.paint.PaintBundle
import androidx.compose.remote.core.operations.utilities.easing.FloatAnimation
import androidx.compose.remote.core.operations.utilities.easing.GeneralEasing

class AnimateMeasure(
    private val mStartTime: Long,
    private val mComponent: Component,
    private val mOriginal: ComponentMeasure,
    private val mTarget: ComponentMeasure,
    private var mDuration: Float,
    private var mDurationVisibilityChange: Float,
    private var mEnterAnimation: AnimationSpec.ANIMATION = AnimationSpec.ANIMATION.FADE_IN,
    private var mExitAnimation: AnimationSpec.ANIMATION = AnimationSpec.ANIMATION.FADE_OUT,
    private var mMotionEasingType: Int = Easing.CUBIC_STANDARD,
    private var mVisibilityEasingType: Int = Easing.CUBIC_ACCELERATE
) {
    private var mP: Float = 0f
    private var mVp: Float = 0f

    private var mMotionEasing = FloatAnimation(mMotionEasingType, mDuration / 1000f, null, 0f, Float.NaN)
    private var mVisibilityEasing = FloatAnimation(mVisibilityEasingType, mDurationVisibilityChange / 1000f, null, 0f, Float.NaN)
    private var mParticleAnimation: ParticleAnimation? = null
    val paint = PaintBundle()

    init {
        mMotionEasing.setTargetValue(1f)
        mVisibilityEasing.setTargetValue(1f)
        mComponent.mVisibility = mTarget.visibility
    }

    fun update(currentTime: Long) {
        val elapsed = currentTime - mStartTime
        mP = mMotionEasing.get(elapsed / mDuration)
        mVp = mVisibilityEasing.get(elapsed / mDurationVisibilityChange)
    }

    fun apply(context: RemoteContext) {
        update(context.currentTime)
        mComponent.x = getX()
        mComponent.y = getY()
        mComponent.width = getWidth()
        mComponent.height = getHeight()
        mComponent.updateVariables(context)

        var w = mComponent.width
        var h = mComponent.height
        for (op in mComponent.mList) {
            if (op is PaddingModifierOperation) {
                w -= op.getLeft() + op.getRight()
                h -= op.getTop() + op.getBottom()
            }
            if (op is DecoratorComponent) {
                op.layout(context, mComponent, w, h)
            }
        }
    }

    fun paint(context: PaintContext) {
        if (mOriginal.visibility != mTarget.visibility) {
            if (mTarget.isGone()) {
                when (mExitAnimation) {
                    AnimationSpec.ANIMATION.FADE_OUT -> {
                        context.save(); context.savePaint()
                        paint.reset(); paint.setColor(0f, 0f, 0f, 1f - mVp)
                        context.applyPaint(paint)
                        context.saveLayer(mComponent.x, mComponent.y, mComponent.width, mComponent.height)
                        mComponent.paintingComponent(context)
                        context.restore(); context.restorePaint(); context.restore()
                    }
                    else -> {
                        if (mParticleAnimation == null) mParticleAnimation = ParticleAnimation()
                        mParticleAnimation!!.animate(context, mComponent, mOriginal, mTarget, mVp)
                    }
                }
            } else if (mOriginal.isGone() && mTarget.isVisible()) {
                when (mEnterAnimation) {
                    AnimationSpec.ANIMATION.FADE_IN -> {
                        context.save(); context.savePaint()
                        paint.reset(); paint.setColor(0f, 0f, 0f, mVp)
                        context.applyPaint(paint)
                        context.saveLayer(mComponent.x, mComponent.y, mComponent.width, mComponent.height)
                        mComponent.paintingComponent(context)
                        context.restore(); context.restorePaint(); context.restore()
                    }
                    else -> {}
                }
            } else {
                mComponent.paintingComponent(context)
            }
        } else if (mTarget.isVisible()) {
            mComponent.paintingComponent(context)
        }
        if (mP >= 1f && mVp >= 1f) {
            mComponent.mVisibility = mTarget.visibility
        }
    }

    fun isDone(): Boolean = mP >= 1f && mVp >= 1f
    fun getX(): Float = mOriginal.x * (1 - mP) + mTarget.x * mP
    fun getY(): Float = mOriginal.y * (1 - mP) + mTarget.y * mP
    fun getWidth(): Float = mOriginal.w * (1 - mP) + mTarget.w * mP
    fun getHeight(): Float = mOriginal.h * (1 - mP) + mTarget.h * mP

    fun updateTarget(measure: ComponentMeasure, currentTime: Long) {
        mOriginal.x = getX(); mOriginal.y = getY()
        mOriginal.w = getWidth(); mOriginal.h = getHeight()
        if (mTarget.x != measure.x || mTarget.y != measure.y || mTarget.w != measure.w
            || mTarget.h != measure.h || mTarget.visibility != measure.visibility) {
            mTarget.x = measure.x; mTarget.y = measure.y
            mTarget.w = measure.w; mTarget.h = measure.h
            mTarget.visibility = measure.visibility
        }
    }
}
