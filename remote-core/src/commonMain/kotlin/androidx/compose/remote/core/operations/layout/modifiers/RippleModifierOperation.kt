package androidx.compose.remote.core.operations.layout.modifiers

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.PaintOperation
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.utilities.StringSerializer

class RippleModifierOperation : PaintOperation(), ModifierOperation {

    override fun paint(context: PaintContext) {}
    override fun write(buffer: WireBuffer) { buffer.start(Operations.MODIFIER_RIPPLE) }
    override fun apply(context: RemoteContext) {}
    override fun toString(): String = "RippleModifierOperation"
    override fun deepToString(indent: String): String = indent + toString()
    override fun serializeToString(indent: Int, serializer: StringSerializer) {
        serializer.append(indent, "RippleModifierOperation")
    }

    companion object {
        fun name(): String = "RippleModifierOperation"
        fun id(): Int = Operations.MODIFIER_RIPPLE

        fun apply(buffer: WireBuffer) {
            buffer.start(Operations.MODIFIER_RIPPLE)
        }

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            operations.add(RippleModifierOperation())
        }
    }
}
