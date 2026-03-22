package androidx.compose.remote.core.operations.layout.modifiers

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.PaintOperation
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.utilities.StringSerializer

class ClipRectModifierOperation : PaintOperation(), ModifierOperation {

    override fun paint(context: PaintContext) {}
    override fun write(buffer: WireBuffer) { buffer.start(Operations.MODIFIER_CLIP_RECT) }
    override fun apply(context: RemoteContext) {}
    override fun toString(): String = "ClipRectModifierOperation"
    override fun deepToString(indent: String): String = indent + toString()
    override fun serializeToString(indent: Int, serializer: StringSerializer) {
        serializer.append(indent, "ClipRectModifierOperation")
    }

    companion object {
        fun name(): String = "ClipRectModifierOperation"
        fun id(): Int = Operations.MODIFIER_CLIP_RECT

        fun apply(buffer: WireBuffer) {
            buffer.start(Operations.MODIFIER_CLIP_RECT)
        }

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            operations.add(ClipRectModifierOperation())
        }
    }
}
