package androidx.compose.remote.core.operations.layout.modifiers

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.PaintOperation
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.layout.Component
import androidx.compose.remote.core.operations.utilities.StringSerializer

class AlignByModifierOperation(private var mLine: Float) : PaintOperation(), ModifierOperation {

    private var mParent: Component? = null

    fun setParent(parent: Component) { mParent = parent }

    fun getValue(context: PaintContext): Float {
        val parent = mParent ?: return 0f
        return parent.getAlignValue(context, mLine)
    }

    override fun paint(context: PaintContext) {}
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mLine) }
    override fun apply(context: RemoteContext) {}
    override fun toString(): String = "AlignByModifier($mLine)"
    override fun deepToString(indent: String): String = indent + toString()
    override fun serializeToString(indent: Int, serializer: StringSerializer) {
        serializer.append(indent, toString())
    }

    companion object {
        fun name(): String = "AlignByModifier"
        fun id(): Int = Operations.MODIFIER_ALIGN_BY

        fun apply(buffer: WireBuffer, line: Float, flags: Int = 0) {
            buffer.start(Operations.MODIFIER_ALIGN_BY)
            buffer.writeFloat(line)
        }

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val line = buffer.readFloat()
            operations.add(AlignByModifierOperation(line))
        }
    }
}
