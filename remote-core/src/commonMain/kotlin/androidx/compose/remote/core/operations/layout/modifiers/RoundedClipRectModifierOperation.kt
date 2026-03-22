package androidx.compose.remote.core.operations.layout.modifiers

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.PaintOperation
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.utilities.StringSerializer

class RoundedClipRectModifierOperation : PaintOperation(), ModifierOperation {

    override fun paint(context: PaintContext) {}
    override fun write(buffer: WireBuffer) { buffer.start(Operations.MODIFIER_CLIP_ROUNDED_RECT) }
    override fun apply(context: RemoteContext) {}
    override fun toString(): String = "RoundedClipRectModifierOperation"
    override fun deepToString(indent: String): String = indent + toString()
    override fun serializeToString(indent: Int, serializer: StringSerializer) {
        serializer.append(indent, "RoundedClipRectModifierOperation")
    }

    companion object {
        fun name(): String = "RoundedClipRectModifierOperation"
        fun id(): Int = Operations.MODIFIER_CLIP_ROUNDED_RECT

        fun apply(buffer: WireBuffer, topStart: Float = 0f, topEnd: Float = 0f, bottomStart: Float = 0f, bottomEnd: Float = 0f) {
            buffer.start(Operations.MODIFIER_CLIP_ROUNDED_RECT)
            buffer.writeFloat(topStart)
            buffer.writeFloat(topEnd)
            buffer.writeFloat(bottomStart)
            buffer.writeFloat(bottomEnd)
        }

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            operations.add(RoundedClipRectModifierOperation())
        }
    }
}
