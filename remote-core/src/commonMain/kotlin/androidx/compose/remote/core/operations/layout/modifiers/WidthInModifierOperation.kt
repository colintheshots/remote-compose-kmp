package androidx.compose.remote.core.operations.layout.modifiers

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.WireBuffer

class WidthInModifierOperation(min: Float, max: Float) : DimensionInModifierOperation(min, max) {

    override fun write(buffer: WireBuffer) {
        apply(buffer, getMin(), getMax())
    }

    override fun toString(): String = "WidthInModifier(${getMin()}, ${getMax()})"

    companion object {
        fun name(): String = "WidthInModifier"
        fun id(): Int = Operations.MODIFIER_WIDTH_IN

        fun apply(buffer: WireBuffer, min: Float, max: Float) {
            buffer.start(Operations.MODIFIER_WIDTH_IN)
            buffer.writeFloat(min)
            buffer.writeFloat(max)
        }

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val min = buffer.readFloat()
            val max = buffer.readFloat()
            operations.add(WidthInModifierOperation(min, max))
        }
    }
}
