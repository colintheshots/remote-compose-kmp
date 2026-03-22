package androidx.compose.remote.core.operations.layout.modifiers

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.PaintOperation
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.utilities.StringSerializer

class ValueFloatChangeActionOperation : PaintOperation(), ModifierOperation {

    override fun paint(context: PaintContext) {}
    override fun write(buffer: WireBuffer) { buffer.start(Operations.MODIFIER_VALUE_FLOAT_CHANGE) }
    override fun apply(context: RemoteContext) {}
    override fun toString(): String = "ValueFloatChangeActionOperation"
    override fun deepToString(indent: String): String = indent + toString()
    override fun serializeToString(indent: Int, serializer: StringSerializer) {
        serializer.append(indent, "ValueFloatChangeActionOperation")
    }

    companion object {
        fun name(): String = "ValueFloatChangeActionOperation"
        fun id(): Int = Operations.MODIFIER_VALUE_FLOAT_CHANGE

        fun apply(buffer: WireBuffer, valueId: Int = 0, value: Float = 0f) {
            buffer.start(Operations.MODIFIER_VALUE_FLOAT_CHANGE)
            buffer.writeInt(valueId)
            buffer.writeFloat(value)
        }

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            operations.add(ValueFloatChangeActionOperation())
        }
    }
}
