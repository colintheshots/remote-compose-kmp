package androidx.compose.remote.core.operations.layout.modifiers

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.PaintOperation
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.layout.Component
import androidx.compose.remote.core.operations.layout.measure.ComponentMeasure
import androidx.compose.remote.core.operations.utilities.StringSerializer

class LayoutComputeOperation(
    private var mType: Int
) : PaintOperation(), ModifierOperation {

    private var mParent: Component? = null

    fun getType(): Int = mType
    fun setParent(parent: Component) { mParent = parent }

    fun applyToMeasure(context: PaintContext, m: ComponentMeasure, parent: ComponentMeasure): Boolean {
        // TODO: implement compute logic
        return false
    }

    override fun paint(context: PaintContext) {}
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mType) }
    override fun apply(context: RemoteContext) {}
    override fun toString(): String = "LayoutComputeOperation($mType)"
    override fun deepToString(indent: String): String = indent + toString()
    override fun serializeToString(indent: Int, serializer: StringSerializer) {
        serializer.append(indent, toString())
    }

    companion object {
        const val TYPE_MEASURE = 0
        const val TYPE_POSITION = 1

        fun name(): String = "LayoutCompute"
        fun id(): Int = Operations.MODIFIER_LAYOUT_COMPUTE

        fun apply(buffer: WireBuffer, type: Int, boundsId: Int = 0, animateChanges: Boolean = false) {
            buffer.start(Operations.MODIFIER_LAYOUT_COMPUTE)
            buffer.writeInt(type)
            buffer.writeInt(boundsId)
            buffer.writeByte(if (animateChanges) 1 else 0)
        }

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val type = buffer.readInt()
            operations.add(LayoutComputeOperation(type))
        }
    }
}
