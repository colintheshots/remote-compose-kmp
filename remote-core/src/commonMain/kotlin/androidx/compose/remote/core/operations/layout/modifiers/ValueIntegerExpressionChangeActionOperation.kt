package androidx.compose.remote.core.operations.layout.modifiers

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.PaintOperation
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.utilities.StringSerializer

class ValueIntegerExpressionChangeActionOperation : PaintOperation(), ModifierOperation {

    override fun paint(context: PaintContext) {}
    override fun write(buffer: WireBuffer) { buffer.start(Operations.MODIFIER_VALUE_INTEGER_EXPRESSION_CHANGE) }
    override fun apply(context: RemoteContext) {}
    override fun toString(): String = "ValueIntegerExpressionChangeActionOperation"
    override fun deepToString(indent: String): String = indent + toString()
    override fun serializeToString(indent: Int, serializer: StringSerializer) {
        serializer.append(indent, "ValueIntegerExpressionChangeActionOperation")
    }

    companion object {
        fun name(): String = "ValueIntegerExpressionChangeActionOperation"
        fun id(): Int = Operations.MODIFIER_VALUE_INTEGER_EXPRESSION_CHANGE

        fun apply(buffer: WireBuffer, destIntegerId: Long = 0L, srcIntegerId: Long = 0L) {
            buffer.start(Operations.MODIFIER_VALUE_INTEGER_EXPRESSION_CHANGE)
            buffer.writeLong(destIntegerId)
            buffer.writeLong(srcIntegerId)
        }

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            operations.add(ValueIntegerExpressionChangeActionOperation())
        }
    }
}
