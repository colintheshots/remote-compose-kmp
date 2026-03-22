package androidx.compose.remote.core.operations.layout.modifiers

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.PaintOperation
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.layout.AnimatableValue
import androidx.compose.remote.core.operations.utilities.StringSerializer

class GraphicsLayerModifierOperation(
    private var mRotationX: Float,
    private var mRotationY: Float,
    private var mRotationZ: Float,
    private var mScaleX: Float,
    private var mScaleY: Float,
    private var mTranslationX: Float,
    private var mTranslationY: Float,
    private var mAlpha: Float
) : PaintOperation(), ModifierOperation {

    private val mAnimRotationX = AnimatableValue(mRotationX)
    private val mAnimRotationY = AnimatableValue(mRotationY)
    private val mAnimRotationZ = AnimatableValue(mRotationZ)
    private val mAnimScaleX = AnimatableValue(mScaleX, false)
    private val mAnimScaleY = AnimatableValue(mScaleY, false)
    private val mAnimTranslationX = AnimatableValue(mTranslationX, false)
    private val mAnimTranslationY = AnimatableValue(mTranslationY, false)
    private val mAnimAlpha = AnimatableValue(mAlpha, false)

    fun fillInAttributes(attrs: HashMap<Int, Any>) {
        attrs[ROTATION_X] = mAnimRotationX.value
        attrs[ROTATION_Y] = mAnimRotationY.value
        attrs[ROTATION_Z] = mAnimRotationZ.value
        attrs[SCALE_X] = mAnimScaleX.value
        attrs[SCALE_Y] = mAnimScaleY.value
        attrs[TRANSLATION_X] = mAnimTranslationX.value
        attrs[TRANSLATION_Y] = mAnimTranslationY.value
        attrs[ALPHA] = mAnimAlpha.value
    }

    override fun paint(context: PaintContext) {
        mAnimRotationX.evaluate(context)
        mAnimRotationY.evaluate(context)
        mAnimRotationZ.evaluate(context)
        mAnimScaleX.evaluate(context)
        mAnimScaleY.evaluate(context)
        mAnimTranslationX.evaluate(context)
        mAnimTranslationY.evaluate(context)
        mAnimAlpha.evaluate(context)
    }

    override fun write(buffer: WireBuffer) {
        apply(buffer, mRotationX, mRotationY, mRotationZ, mScaleX, mScaleY, mTranslationX, mTranslationY, mAlpha)
    }

    override fun apply(context: RemoteContext) {}
    override fun toString(): String = "GraphicsLayerModifier"
    override fun deepToString(indent: String): String = indent + toString()
    override fun serializeToString(indent: Int, serializer: StringSerializer) {
        serializer.append(indent, "GRAPHICS_LAYER_MODIFIER")
    }

    companion object {
        const val SCALE_X = 0
        const val SCALE_Y = 1
        const val ROTATION_X = 2
        const val ROTATION_Y = 3
        const val ROTATION_Z = 4
        const val TRANSFORM_ORIGIN_X = 5
        const val TRANSFORM_ORIGIN_Y = 6
        const val TRANSLATION_X = 7
        const val TRANSLATION_Y = 8
        const val TRANSLATION_Z = 9
        const val SHADOW_ELEVATION = 10
        const val ALPHA = 11
        const val CAMERA_DISTANCE = 12
        const val COMPOSITING_STRATEGY = 13
        const val SPOT_SHADOW_COLOR = 14
        const val AMBIENT_SHADOW_COLOR = 15
        const val HAS_BLUR = 16
        const val BLUR_RADIUS_X = 17
        const val BLUR_RADIUS_Y = 18
        const val BLUR_TILE_MODE = 19
        const val SHAPE = 20
        const val SHAPE_RADIUS = 21

        const val SHAPE_RECT = 0
        const val SHAPE_ROUND_RECT = 1
        const val SHAPE_CIRCLE = 2

        const val TILE_MODE_CLAMP = 0
        const val TILE_MODE_REPEATED = 1
        const val TILE_MODE_MIRROR = 2
        const val TILE_MODE_DECAL = 3

        fun name(): String = "GraphicsLayerModifier"
        fun id(): Int = Operations.MODIFIER_GRAPHICS_LAYER

        fun apply(buffer: WireBuffer, attributes: HashMap<Int, Any>) {
            val rx = (attributes[ROTATION_X] as? Float) ?: 0f
            val ry = (attributes[ROTATION_Y] as? Float) ?: 0f
            val rz = (attributes[ROTATION_Z] as? Float) ?: 0f
            val sx = (attributes[SCALE_X] as? Float) ?: 1f
            val sy = (attributes[SCALE_Y] as? Float) ?: 1f
            val tx = (attributes[TRANSLATION_X] as? Float) ?: 0f
            val ty = (attributes[TRANSLATION_Y] as? Float) ?: 0f
            val alpha = (attributes[ALPHA] as? Float) ?: 1f
            apply(buffer, rx, ry, rz, sx, sy, tx, ty, alpha)
        }

        fun apply(buffer: WireBuffer, rx: Float, ry: Float, rz: Float, sx: Float, sy: Float, tx: Float, ty: Float, alpha: Float) {
            buffer.start(Operations.MODIFIER_GRAPHICS_LAYER)
            buffer.writeFloat(rx)
            buffer.writeFloat(ry)
            buffer.writeFloat(rz)
            buffer.writeFloat(sx)
            buffer.writeFloat(sy)
            buffer.writeFloat(tx)
            buffer.writeFloat(ty)
            buffer.writeFloat(alpha)
        }

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val rx = buffer.readFloat()
            val ry = buffer.readFloat()
            val rz = buffer.readFloat()
            val sx = buffer.readFloat()
            val sy = buffer.readFloat()
            val tx = buffer.readFloat()
            val ty = buffer.readFloat()
            val alpha = buffer.readFloat()
            operations.add(GraphicsLayerModifierOperation(rx, ry, rz, sx, sy, tx, ty, alpha))
        }
    }
}
