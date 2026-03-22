package androidx.compose.remote.core.operations.layout.modifiers

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.WireBuffer

class WidthModifierOperation(
    type: Int,
    value: Float = 0f
) : DimensionModifierOperation(type, value) {

    private var mWidthIn: WidthInModifierOperation? = null

    fun getWidthIn(): WidthInModifierOperation? = mWidthIn
    fun setWidthIn(widthIn: WidthInModifierOperation?) { mWidthIn = widthIn }

    override fun write(buffer: WireBuffer) {
        apply(buffer, getType(), getValue())
    }

    override fun toString(): String = "WidthModifier(${DimensionModifierOperation.Type.toString(getType())}, ${getValue()})"

    companion object {
        fun name(): String = "WidthModifier"
        fun id(): Int = Operations.MODIFIER_WIDTH

        fun apply(buffer: WireBuffer, type: Int, value: Float) {
            buffer.start(Operations.MODIFIER_WIDTH)
            buffer.writeInt(type)
            buffer.writeFloat(value)
        }

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val type = buffer.readInt()
            val value = buffer.readFloat()
            operations.add(WidthModifierOperation(type, value))
        }
    }
}
