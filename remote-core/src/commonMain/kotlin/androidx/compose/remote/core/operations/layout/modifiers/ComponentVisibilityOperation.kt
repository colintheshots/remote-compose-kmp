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
import androidx.compose.remote.core.operations.utilities.StringSerializer

class ComponentVisibilityOperation(
    private var mVisibilityValue: Int
) : PaintOperation(), ModifierOperation, VariableSupport {

    private var mParent: Component? = null
    private var mOutValue: Int = mVisibilityValue

    fun setParent(parent: Component) { mParent = parent }

    override fun registerListening(context: RemoteContext) {
        if (Utils.isVariable(mVisibilityValue.toFloat())) {
            context.listensTo(mVisibilityValue, this)
        }
    }

    override fun updateVariables(context: RemoteContext) {
        val newValue = if (Utils.isVariable(mVisibilityValue.toFloat()))
            context.getInteger(mVisibilityValue) else mVisibilityValue
        if (newValue != mOutValue) {
            mOutValue = newValue
            mParent?.setVisibility(mOutValue)
        }
    }

    override fun paint(context: PaintContext) {}
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mVisibilityValue) }
    override fun toString(): String = "ComponentVisibility($mOutValue)"
    override fun deepToString(indent: String): String = indent + toString()
    override fun serializeToString(indent: Int, serializer: StringSerializer) {
        serializer.append(indent, toString())
    }

    companion object {
        fun name(): String = "ComponentVisibility"
        fun id(): Int = Operations.MODIFIER_VISIBILITY

        fun apply(buffer: WireBuffer, value: Int) {
            buffer.start(Operations.MODIFIER_VISIBILITY)
            buffer.writeInt(value)
        }

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val value = buffer.readInt()
            operations.add(ComponentVisibilityOperation(value))
        }
    }
}
