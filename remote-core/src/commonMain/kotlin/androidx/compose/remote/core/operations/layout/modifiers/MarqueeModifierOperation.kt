package androidx.compose.remote.core.operations.layout.modifiers

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.PaintOperation
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.utilities.StringSerializer

class MarqueeModifierOperation : PaintOperation(), ModifierOperation {

    override fun paint(context: PaintContext) {}
    override fun write(buffer: WireBuffer) { buffer.start(Operations.MODIFIER_MARQUEE) }
    override fun apply(context: RemoteContext) {}
    override fun toString(): String = "MarqueeModifierOperation"
    override fun deepToString(indent: String): String = indent + toString()
    override fun serializeToString(indent: Int, serializer: StringSerializer) {
        serializer.append(indent, "MarqueeModifierOperation")
    }

    companion object {
        fun name(): String = "MarqueeModifierOperation"
        fun id(): Int = Operations.MODIFIER_MARQUEE

        fun apply(buffer: WireBuffer, iterations: Int = 0, animationMode: Int = 0,
                  repeatDelayMillis: Float = 0f, initialDelayMillis: Float = 0f,
                  spacing: Float = 0f, velocity: Float = 0f) {
            buffer.start(Operations.MODIFIER_MARQUEE)
            buffer.writeInt(iterations)
            buffer.writeInt(animationMode)
            buffer.writeFloat(repeatDelayMillis)
            buffer.writeFloat(initialDelayMillis)
            buffer.writeFloat(spacing)
            buffer.writeFloat(velocity)
        }

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            operations.add(MarqueeModifierOperation())
        }
    }
}
