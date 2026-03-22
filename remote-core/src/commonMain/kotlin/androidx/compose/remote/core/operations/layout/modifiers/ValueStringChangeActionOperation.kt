package androidx.compose.remote.core.operations.layout.modifiers

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.PaintOperation
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.utilities.StringSerializer

class ValueStringChangeActionOperation : PaintOperation(), ModifierOperation {

    override fun paint(context: PaintContext) {}
    override fun write(buffer: WireBuffer) { buffer.start(Operations.MODIFIER_VALUE_STRING_CHANGE) }
    override fun apply(context: RemoteContext) {}
    override fun toString(): String = "ValueStringChangeActionOperation"
    override fun deepToString(indent: String): String = indent + toString()
    override fun serializeToString(indent: Int, serializer: StringSerializer) {
        serializer.append(indent, "ValueStringChangeActionOperation")
    }

    companion object {
        fun name(): String = "ValueStringChangeActionOperation"
        fun id(): Int = Operations.MODIFIER_VALUE_STRING_CHANGE

        fun apply(buffer: WireBuffer, destTextId: Int = 0, srcTextId: Int = 0) {
            buffer.start(Operations.MODIFIER_VALUE_STRING_CHANGE)
            buffer.writeInt(destTextId)
            buffer.writeInt(srcTextId)
        }

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            operations.add(ValueStringChangeActionOperation())
        }
    }
}
