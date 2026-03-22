package androidx.compose.remote.core.operations.layout.modifiers

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.PaintOperation
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.VariableSupport
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.Utils
import androidx.compose.remote.core.operations.layout.Component
import androidx.compose.remote.core.operations.layout.DecoratorComponent
import androidx.compose.remote.core.operations.utilities.StringSerializer

class PaddingModifierOperation(
    private var mLeft: Float,
    private var mTop: Float,
    private var mRight: Float,
    private var mBottom: Float
) : PaintOperation(), ModifierOperation, DecoratorComponent, VariableSupport {

    private var mOutLeft: Float = mLeft
    private var mOutTop: Float = mTop
    private var mOutRight: Float = mRight
    private var mOutBottom: Float = mBottom

    fun getLeft(): Float = mOutLeft
    fun getTop(): Float = mOutTop
    fun getRight(): Float = mOutRight
    fun getBottom(): Float = mOutBottom

    override fun registerListening(context: RemoteContext) {
        if (Utils.isVariable(mLeft)) context.listensTo(Utils.idFromNan(mLeft), this)
        if (Utils.isVariable(mTop)) context.listensTo(Utils.idFromNan(mTop), this)
        if (Utils.isVariable(mRight)) context.listensTo(Utils.idFromNan(mRight), this)
        if (Utils.isVariable(mBottom)) context.listensTo(Utils.idFromNan(mBottom), this)
    }

    override fun updateVariables(context: RemoteContext) {
        mOutLeft = if (mLeft.isNaN()) context.getFloat(Utils.idFromNan(mLeft)) else mLeft
        mOutTop = if (mTop.isNaN()) context.getFloat(Utils.idFromNan(mTop)) else mTop
        mOutRight = if (mRight.isNaN()) context.getFloat(Utils.idFromNan(mRight)) else mRight
        mOutBottom = if (mBottom.isNaN()) context.getFloat(Utils.idFromNan(mBottom)) else mBottom
    }

    override fun layout(context: RemoteContext, component: Component, width: Float, height: Float) {}

    override fun paint(context: PaintContext) {
        context.translate(mOutLeft, mOutTop)
    }

    override fun write(buffer: WireBuffer) {
        apply(buffer, mLeft, mTop, mRight, mBottom)
    }

    override fun toString(): String = "PaddingModifier($mOutLeft, $mOutTop, $mOutRight, $mOutBottom)"
    override fun deepToString(indent: String): String = indent + toString()
    override fun serializeToString(indent: Int, serializer: StringSerializer) {
        serializer.append(indent, toString())
    }

    companion object {
        fun name(): String = "PaddingModifier"
        fun id(): Int = Operations.MODIFIER_PADDING

        fun apply(buffer: WireBuffer, left: Float, top: Float, right: Float, bottom: Float) {
            buffer.start(Operations.MODIFIER_PADDING)
            buffer.writeFloat(left)
            buffer.writeFloat(top)
            buffer.writeFloat(right)
            buffer.writeFloat(bottom)
        }

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val left = buffer.readFloat()
            val top = buffer.readFloat()
            val right = buffer.readFloat()
            val bottom = buffer.readFloat()
            operations.add(PaddingModifierOperation(left, top, right, bottom))
        }
    }
}
