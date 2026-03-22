package androidx.compose.remote.core.operations.layout.modifiers

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.PaintOperation
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.layout.Component
import androidx.compose.remote.core.operations.layout.LayoutComponent
import androidx.compose.remote.core.operations.layout.ScrollDelegate
import androidx.compose.remote.core.operations.utilities.StringSerializer

class ScrollModifierOperation(
    private var mOrientation: Int
) : PaintOperation(), ModifierOperation, ScrollDelegate {

    private var mScrollX: Float = 0f
    private var mScrollY: Float = 0f

    fun getScrollX(): Float = mScrollX
    fun getScrollY(): Float = mScrollY

    fun inflate(parent: LayoutComponent) {}

    override fun getScrollX(currentValue: Float): Float = mScrollX
    override fun getScrollY(currentValue: Float): Float = mScrollY
    override fun handlesHorizontalScroll(): Boolean = mOrientation == 0 || mOrientation == 2
    override fun handlesVerticalScroll(): Boolean = mOrientation == 1 || mOrientation == 2
    override fun reset() { mScrollX = 0f; mScrollY = 0f }

    override fun paint(context: PaintContext) {}
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mOrientation) }
    override fun apply(context: RemoteContext) {}
    override fun toString(): String = "ScrollModifier($mOrientation)"
    override fun deepToString(indent: String): String = indent + toString()
    override fun serializeToString(indent: Int, serializer: StringSerializer) {
        serializer.append(indent, toString())
    }

    companion object {
        fun name(): String = "ScrollModifier"
        fun id(): Int = Operations.MODIFIER_SCROLL

        fun apply(buffer: WireBuffer, orientation: Int, min: Float = 0f, max: Float = 0f, initial: Float = 0f) {
            buffer.start(Operations.MODIFIER_SCROLL)
            buffer.writeInt(orientation)
            buffer.writeFloat(min)
            buffer.writeFloat(max)
            buffer.writeFloat(initial)
        }

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val orientation = buffer.readInt()
            operations.add(ScrollModifierOperation(orientation))
        }
    }
}
