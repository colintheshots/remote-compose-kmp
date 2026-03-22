package androidx.compose.remote.core.operations.layout.modifiers

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.PaintOperation
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.utilities.StringSerializer

class ValueFloatExpressionChangeActionOperation : PaintOperation(), ModifierOperation {

    override fun paint(context: PaintContext) {}
    override fun write(buffer: WireBuffer) { buffer.start(Operations.MODIFIER_VALUE_FLOAT_EXPRESSION_CHANGE) }
    override fun apply(context: RemoteContext) {}
    override fun toString(): String = "ValueFloatExpressionChangeActionOperation"
    override fun deepToString(indent: String): String = indent + toString()
    override fun serializeToString(indent: Int, serializer: StringSerializer) {
        serializer.append(indent, "ValueFloatExpressionChangeActionOperation")
    }

    companion object {
        fun name(): String = "ValueFloatExpressionChangeActionOperation"
        fun id(): Int = Operations.MODIFIER_VALUE_FLOAT_EXPRESSION_CHANGE

        fun apply(buffer: WireBuffer, valueId: Int = 0, value: Int = 0) {
            buffer.start(Operations.MODIFIER_VALUE_FLOAT_EXPRESSION_CHANGE)
            buffer.writeInt(valueId)
            buffer.writeInt(value)
        }

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            operations.add(ValueFloatExpressionChangeActionOperation())
        }
    }
}
