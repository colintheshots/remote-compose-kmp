package androidx.compose.remote.core.operations.layout.modifiers

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.PaintOperation
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.VariableSupport
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.Utils
import androidx.compose.remote.core.operations.utilities.StringSerializer

class ZIndexModifierOperation(
    private var mValue: Float
) : PaintOperation(), ModifierOperation, VariableSupport {

    private var mOutValue: Float = mValue

    fun getValue(): Float = mOutValue

    override fun registerListening(context: RemoteContext) {
        if (Utils.isVariable(mValue)) {
            context.listensTo(Utils.idFromNan(mValue), this)
        }
    }

    override fun updateVariables(context: RemoteContext) {
        mOutValue = if (mValue.isNaN()) context.getFloat(Utils.idFromNan(mValue)) else mValue
    }

    override fun paint(context: PaintContext) {}

    override fun write(buffer: WireBuffer) {
        apply(buffer, mValue)
    }

    override fun toString(): String = "ZIndexModifier($mOutValue)"
    override fun deepToString(indent: String): String = indent + toString()
    override fun serializeToString(indent: Int, serializer: StringSerializer) {
        serializer.append(indent, toString())
    }

    companion object {
        fun name(): String = "ZIndexModifier"
        fun id(): Int = Operations.MODIFIER_Z_INDEX

        fun apply(buffer: WireBuffer, value: Float) {
            buffer.start(Operations.MODIFIER_Z_INDEX)
            buffer.writeFloat(value)
        }

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val value = buffer.readFloat()
            operations.add(ZIndexModifierOperation(value))
        }
    }
}
