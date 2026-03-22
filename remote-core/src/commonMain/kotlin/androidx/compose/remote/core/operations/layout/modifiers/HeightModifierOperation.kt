package androidx.compose.remote.core.operations.layout.modifiers

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.WireBuffer

class HeightModifierOperation(
    type: Int,
    value: Float = 0f
) : DimensionModifierOperation(type, value) {

    private var mHeightIn: HeightInModifierOperation? = null

    fun getHeightIn(): HeightInModifierOperation? = mHeightIn
    fun setHeightIn(heightIn: HeightInModifierOperation?) { mHeightIn = heightIn }

    override fun write(buffer: WireBuffer) {
        apply(buffer, getType(), getValue())
    }

    override fun toString(): String = "HeightModifier(${DimensionModifierOperation.Type.toString(getType())}, ${getValue()})"

    companion object {
        fun name(): String = "HeightModifier"
        fun id(): Int = Operations.MODIFIER_HEIGHT

        fun apply(buffer: WireBuffer, type: Int, value: Float) {
            buffer.start(Operations.MODIFIER_HEIGHT)
            buffer.writeInt(type)
            buffer.writeFloat(value)
        }

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val type = buffer.readInt()
            val value = buffer.readFloat()
            operations.add(HeightModifierOperation(type, value))
        }
    }
}
