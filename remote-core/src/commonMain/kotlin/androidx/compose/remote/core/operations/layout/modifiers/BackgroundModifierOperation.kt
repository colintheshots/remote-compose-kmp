package androidx.compose.remote.core.operations.layout.modifiers

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.VariableSupport
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.Utils
import androidx.compose.remote.core.operations.layout.Component
import androidx.compose.remote.core.operations.paint.PaintBundle
import androidx.compose.remote.core.operations.utilities.StringSerializer

class BackgroundModifierOperation(
    private var mColor: Int,
    private var mCornerRadius: Float,
    private var mCornerRadiusTL: Float,
    private var mCornerRadiusTR: Float,
    private var mCornerRadiusBL: Float,
    private var mCornerRadiusBR: Float,
    private val mShapeType: Int
) : DecoratorModifierOperation(), VariableSupport {

    private var mOutColor: Int = mColor
    private val mPaint = PaintBundle()

    override fun registerListening(context: RemoteContext) {
        if (Utils.isVariable(mColor.toFloat())) {
            context.listensTo(mColor, this)
        }
    }

    override fun updateVariables(context: RemoteContext) {
        mOutColor = if (Utils.isVariable(mColor.toFloat())) context.getColor(mColor) else mColor
    }

    override fun paint(context: PaintContext) {
        context.savePaint()
        mPaint.reset()
        mPaint.setStyle(PaintBundle.STYLE_FILL)
        mPaint.setColor(mOutColor)
        context.applyPaint(mPaint)
        when (mShapeType) {
            ShapeType.CIRCLE -> {
                val r = kotlin.math.min(mWidth, mHeight) / 2f
                context.drawCircle(mWidth / 2f, mHeight / 2f, r)
            }
            ShapeType.OVAL -> context.drawOval(0f, 0f, mWidth, mHeight)
            ShapeType.ROUNDED_RECTANGLE -> {
                if (mCornerRadius > 0f) {
                    context.drawRoundRect(0f, 0f, mWidth, mHeight, mCornerRadius, mCornerRadius)
                } else {
                    context.drawRoundRect(0f, 0f, mWidth, mHeight, mCornerRadiusTL, mCornerRadiusTR, mCornerRadiusBR, mCornerRadiusBL)
                }
            }
            else -> context.drawRect(0f, 0f, mWidth, mHeight)
        }
        context.restorePaint()
    }

    override fun write(buffer: WireBuffer) {
        buffer.start(Operations.MODIFIER_BACKGROUND)
        buffer.writeInt(mColor)
        buffer.writeFloat(mCornerRadius)
        buffer.writeFloat(mCornerRadiusTL)
        buffer.writeFloat(mCornerRadiusTR)
        buffer.writeFloat(mCornerRadiusBL)
        buffer.writeFloat(mCornerRadiusBR)
        buffer.writeInt(mShapeType)
    }

    override fun toString(): String = "BackgroundModifier"
    override fun serializeToString(indent: Int, serializer: StringSerializer) {
        serializer.append(indent, "BACKGROUND_MODIFIER")
    }

    companion object {
        const val COLOR_REF = 1

        fun name(): String = "BackgroundModifier"
        fun id(): Int = Operations.MODIFIER_BACKGROUND

        fun apply(buffer: WireBuffer, flags: Int, colorId: Int, unused1: Int, unused2: Int,
                  r: Float, g: Float, b: Float, a: Float, shapeType: Int) {
            val color = if (flags == COLOR_REF) colorId
            else ((a * 255).toInt() shl 24) or ((r * 255).toInt() shl 16) or ((g * 255).toInt() shl 8) or (b * 255).toInt()
            buffer.start(Operations.MODIFIER_BACKGROUND)
            buffer.writeInt(flags)
            buffer.writeInt(color)
            buffer.writeFloat(r)
            buffer.writeFloat(g)
            buffer.writeFloat(b)
            buffer.writeFloat(a)
            buffer.writeInt(shapeType)
        }

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val color = buffer.readInt()
            val cornerRadius = buffer.readFloat()
            val tl = buffer.readFloat()
            val tr = buffer.readFloat()
            val bl = buffer.readFloat()
            val br = buffer.readFloat()
            val shapeType = buffer.readInt()
            operations.add(BackgroundModifierOperation(color, cornerRadius, tl, tr, bl, br, shapeType))
        }
    }
}
