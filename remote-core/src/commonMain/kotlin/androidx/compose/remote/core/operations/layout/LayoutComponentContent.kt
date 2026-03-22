package androidx.compose.remote.core.operations.layout

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.WireBuffer

class LayoutComponentContent : Component {
    constructor(componentId: Int, x: Float, y: Float, width: Float, height: Float, parent: Component?, animationId: Int)
        : super(parent, componentId, animationId, x, y, width, height)
    constructor(componentId: Int) : super(null, componentId, 0, -1f, 0f, 0f, 0f)

    override fun getSerializedName(): String = "CONTENT"
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, componentId) }

    companion object {
        fun name(): String = "LayoutContent"
        fun id(): Int = Operations.LAYOUT_CONTENT
        fun apply(buffer: WireBuffer, componentId: Int) {
            buffer.start(Operations.LAYOUT_CONTENT); buffer.writeInt(componentId)
        }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val componentId = buffer.readInt()
            operations.add(LayoutComponentContent(componentId, 0f, 0f, 0f, 0f, null, -1))
        }
    }
}
