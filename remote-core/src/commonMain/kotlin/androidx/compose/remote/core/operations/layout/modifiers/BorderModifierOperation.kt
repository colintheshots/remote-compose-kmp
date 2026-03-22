package androidx.compose.remote.core.operations.layout.modifiers

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.VariableSupport
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.Utils
import androidx.compose.remote.core.operations.paint.PaintBundle
import androidx.compose.remote.core.operations.utilities.StringSerializer

class BorderModifierOperation(
    private var mBorderWidth: Float,
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
        if (Utils.isVariable(mColor.toFloat())) context.listensTo(mColor, this)
    }

    override fun updateVariables(context: RemoteContext) {
        mOutColor = if (Utils.isVariable(mColor.toFloat())) context.getColor(mColor) else mColor
    }

    override fun paint(context: PaintContext) {
        context.savePaint()
        mPaint.reset()
        mPaint.setStyle(PaintBundle.STYLE_STROKE)
        mPaint.setStrokeWidth(mBorderWidth)
        mPaint.setColor(mOutColor)
        context.applyPaint(mPaint)
        val inset = mBorderWidth / 2f
        when (mShapeType) {
            ShapeType.CIRCLE -> {
                val r = kotlin.math.min(mWidth, mHeight) / 2f - inset
                context.drawCircle(mWidth / 2f, mHeight / 2f, r)
            }
            ShapeType.OVAL -> context.drawOval(inset, inset, mWidth - inset, mHeight - inset)
            ShapeType.ROUNDED_RECTANGLE -> {
                if (mCornerRadius > 0f) {
                    context.drawRoundRect(inset, inset, mWidth - inset, mHeight - inset, mCornerRadius, mCornerRadius)
                } else {
                    context.drawRoundRect(inset, inset, mWidth - inset, mHeight - inset, mCornerRadiusTL, mCornerRadiusTR, mCornerRadiusBR, mCornerRadiusBL)
                }
            }
            else -> context.drawRect(inset, inset, mWidth - inset, mHeight - inset)
        }
        context.restorePaint()
    }

    override fun write(buffer: WireBuffer) {
        buffer.start(Operations.MODIFIER_BORDER)
        buffer.writeFloat(mBorderWidth)
        buffer.writeInt(mColor)
        buffer.writeFloat(mCornerRadius)
        buffer.writeFloat(mCornerRadiusTL)
        buffer.writeFloat(mCornerRadiusTR)
        buffer.writeFloat(mCornerRadiusBL)
        buffer.writeFloat(mCornerRadiusBR)
        buffer.writeInt(mShapeType)
    }

    override fun toString(): String = "BorderModifier"
    override fun serializeToString(indent: Int, serializer: StringSerializer) {
        serializer.append(indent, "BORDER_MODIFIER")
    }

    companion object {
        const val COLOR_REF = 1

        fun name(): String = "BorderModifier"
        fun id(): Int = Operations.MODIFIER_BORDER

        fun apply(buffer: WireBuffer, flags: Int, colorId: Int, unused1: Int, unused2: Int,
                  borderWidth: Float, cornerRadius: Float, r: Float, g: Float, b: Float, a: Float, shapeType: Int) {
            val color = if (flags == COLOR_REF) colorId
            else ((a * 255).toInt() shl 24) or ((r * 255).toInt() shl 16) or ((g * 255).toInt() shl 8) or (b * 255).toInt()
            buffer.start(Operations.MODIFIER_BORDER)
            buffer.writeFloat(borderWidth)
            buffer.writeInt(color)
            buffer.writeFloat(cornerRadius)
            buffer.writeFloat(0f) // tl
            buffer.writeFloat(0f) // tr
            buffer.writeFloat(0f) // bl
            buffer.writeFloat(0f) // br
            buffer.writeInt(shapeType)
        }

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val borderWidth = buffer.readFloat()
            val color = buffer.readInt()
            val cornerRadius = buffer.readFloat()
            val tl = buffer.readFloat()
            val tr = buffer.readFloat()
            val bl = buffer.readFloat()
            val br = buffer.readFloat()
            val shapeType = buffer.readInt()
            operations.add(BorderModifierOperation(borderWidth, color, cornerRadius, tl, tr, bl, br, shapeType))
        }
    }
}
