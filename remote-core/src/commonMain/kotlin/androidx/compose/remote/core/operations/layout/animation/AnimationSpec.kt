package androidx.compose.remote.core.operations.layout.animation
import androidx.compose.remote.core.operations.utilities.easing.Easing

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.layout.modifiers.ModifierOperation
import androidx.compose.remote.core.operations.utilities.StringSerializer
import androidx.compose.remote.core.operations.utilities.easing.GeneralEasing

class AnimationSpec(
    val animationId: Int = -1,
    val motionDuration: Float = 600f,
    val motionEasingType: Int = Easing.CUBIC_STANDARD,
    val visibilityDuration: Float = 500f,
    val visibilityEasingType: Int = Easing.CUBIC_STANDARD,
    val enterAnimation: ANIMATION = ANIMATION.FADE_IN,
    val exitAnimation: ANIMATION = ANIMATION.FADE_OUT
) : Operation(), ModifierOperation {

    fun isAnimationEnabled(): Boolean = animationId != 0

    enum class ANIMATION {
        FADE_IN, FADE_OUT, SLIDE_LEFT, SLIDE_RIGHT,
        SLIDE_TOP, SLIDE_BOTTOM, ROTATE, PARTICLE
    }

    override fun write(buffer: WireBuffer) {
        apply(buffer, animationId, motionDuration, motionEasingType,
            visibilityDuration, visibilityEasingType, enterAnimation.ordinal, exitAnimation.ordinal)
    }

    override fun apply(context: RemoteContext) {}
    override fun toString(): String = "ANIMATION_SPEC ($motionDuration ms)"
    override fun deepToString(indent: String): String = indent + toString()

    override fun serializeToString(indent: Int, serializer: StringSerializer) {
        serializer.append(indent, "ANIMATION_SPEC = [$motionDuration, $motionEasingType, $visibilityDuration, $visibilityEasingType, $enterAnimation, $exitAnimation]")
    }

    companion object {
        val DEFAULT = AnimationSpec()
        val DISABLED = AnimationSpec(animationId = 0)

        fun name(): String = "AnimationSpec"
        fun id(): Int = Operations.ANIMATION_SPEC

        fun intToAnimation(value: Int): ANIMATION = when (value) {
            0 -> ANIMATION.FADE_IN
            1 -> ANIMATION.FADE_OUT
            2 -> ANIMATION.SLIDE_LEFT
            3 -> ANIMATION.SLIDE_RIGHT
            4 -> ANIMATION.SLIDE_TOP
            5 -> ANIMATION.SLIDE_BOTTOM
            6 -> ANIMATION.ROTATE
            7 -> ANIMATION.PARTICLE
            else -> ANIMATION.FADE_IN
        }

        fun apply(buffer: WireBuffer, animationId: Int, motionDuration: Float, motionEasingType: Int,
                  visibilityDuration: Float, visibilityEasingType: Int, enterAnimation: Int, exitAnimation: Int) {
            buffer.start(Operations.ANIMATION_SPEC)
            buffer.writeInt(animationId)
            buffer.writeFloat(motionDuration)
            buffer.writeInt(motionEasingType)
            buffer.writeFloat(visibilityDuration)
            buffer.writeInt(visibilityEasingType)
            buffer.writeInt(enterAnimation)
            buffer.writeInt(exitAnimation)
        }

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val animationId = buffer.readInt()
            val motionDuration = buffer.readFloat()
            val motionEasingType = buffer.readInt()
            val visibilityDuration = buffer.readFloat()
            val visibilityEasingType = buffer.readInt()
            val enterAnimation = intToAnimation(buffer.readInt())
            val exitAnimation = intToAnimation(buffer.readInt())
            operations.add(AnimationSpec(animationId, motionDuration, motionEasingType,
                visibilityDuration, visibilityEasingType, enterAnimation, exitAnimation))
        }
    }
}
