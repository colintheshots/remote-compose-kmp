package androidx.compose.remote.core.operations.layout.modifiers

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.WireBuffer

class HeightInModifierOperation(min: Float, max: Float) : DimensionInModifierOperation(min, max) {

    override fun write(buffer: WireBuffer) {
        apply(buffer, getMin(), getMax())
    }

    override fun toString(): String = "HeightInModifier(${getMin()}, ${getMax()})"

    companion object {
        fun name(): String = "HeightInModifier"
        fun id(): Int = Operations.MODIFIER_HEIGHT_IN

        fun apply(buffer: WireBuffer, min: Float, max: Float) {
            buffer.start(Operations.MODIFIER_HEIGHT_IN)
            buffer.writeFloat(min)
            buffer.writeFloat(max)
        }

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val min = buffer.readFloat()
            val max = buffer.readFloat()
            operations.add(HeightInModifierOperation(min, max))
        }
    }
}
