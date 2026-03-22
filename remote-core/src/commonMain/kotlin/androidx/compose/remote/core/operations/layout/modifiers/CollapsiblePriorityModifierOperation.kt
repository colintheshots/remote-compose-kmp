package androidx.compose.remote.core.operations.layout.modifiers

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.PaintOperation
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.utilities.StringSerializer

class CollapsiblePriorityModifierOperation(
    private var mPriority: Float,
    private var mOrientation: Int
) : PaintOperation(), ModifierOperation {

    fun getPriority(): Float = mPriority
    fun getOrientation(): Int = mOrientation

    override fun paint(context: PaintContext) {}
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mPriority, mOrientation) }
    override fun apply(context: RemoteContext) {}
    override fun toString(): String = "CollapsiblePriorityModifier($mPriority, $mOrientation)"
    override fun deepToString(indent: String): String = indent + toString()
    override fun serializeToString(indent: Int, serializer: StringSerializer) {
        serializer.append(indent, toString())
    }

    companion object {
        fun name(): String = "CollapsiblePriorityModifier"
        fun id(): Int = Operations.MODIFIER_COLLAPSIBLE_PRIORITY

        fun apply(buffer: WireBuffer, priority: Float, orientation: Int) {
            buffer.start(Operations.MODIFIER_COLLAPSIBLE_PRIORITY)
            buffer.writeFloat(priority)
            buffer.writeInt(orientation)
        }

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val priority = buffer.readFloat()
            val orientation = buffer.readInt()
            operations.add(CollapsiblePriorityModifierOperation(priority, orientation))
        }
    }
}
