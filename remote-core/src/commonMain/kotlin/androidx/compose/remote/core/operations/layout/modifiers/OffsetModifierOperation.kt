package androidx.compose.remote.core.operations.layout.modifiers

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.PaintOperation
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.utilities.StringSerializer

class OffsetModifierOperation : PaintOperation(), ModifierOperation {

    override fun paint(context: PaintContext) {}
    override fun write(buffer: WireBuffer) { buffer.start(Operations.MODIFIER_OFFSET) }
    override fun apply(context: RemoteContext) {}
    override fun toString(): String = "OffsetModifierOperation"
    override fun deepToString(indent: String): String = indent + toString()
    override fun serializeToString(indent: Int, serializer: StringSerializer) {
        serializer.append(indent, "OffsetModifierOperation")
    }

    companion object {
        fun name(): String = "OffsetModifierOperation"
        fun id(): Int = Operations.MODIFIER_OFFSET

        fun apply(buffer: WireBuffer, x: Float = 0f, y: Float = 0f) {
            buffer.start(Operations.MODIFIER_OFFSET)
            buffer.writeFloat(x)
            buffer.writeFloat(y)
        }

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            operations.add(OffsetModifierOperation())
        }
    }
}
