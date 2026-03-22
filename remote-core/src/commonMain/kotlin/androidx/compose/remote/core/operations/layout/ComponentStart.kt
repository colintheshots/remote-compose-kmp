package androidx.compose.remote.core.operations.layout

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.Container

class ComponentStart(
    var mType: Int = DEFAULT,
    var mComponentId: Int,
    var mWidth: Float,
    var mHeight: Float
) : Operation(), Container {

    var mX: Float = 0f
    var mY: Float = 0f
    val mList: ArrayList<Operation> = ArrayList()

    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mType, mComponentId, mWidth, mHeight) }
    override fun toString(): String = "COMPONENT_START (type $mType ${typeDescription(mType)}) - ($mX, $mY - $mWidth x $mHeight)"
    override fun deepToString(indent: String): String = indent + toString()
    override fun apply(context: RemoteContext) {}
    override fun getList(): ArrayList<Operation> = mList

    companion object {
        const val UNKNOWN = -1; const val DEFAULT = 0; const val ROOT_LAYOUT = 1
        const val LAYOUT = 2; const val LAYOUT_CONTENT = 3; const val SCROLL_CONTENT = 4
        const val BUTTON = 5; const val CHECKBOX = 6; const val TEXT = 7
        const val CURVED_TEXT = 8; const val STATE_HOST = 9; const val CUSTOM = 10
        const val LOTTIE = 11; const val IMAGE = 12; const val STATE_BOX_CONTENT = 13
        const val LAYOUT_BOX = 14; const val LAYOUT_ROW = 15; const val LAYOUT_COLUMN = 16

        fun typeDescription(type: Int): String = when (type) {
            DEFAULT -> "DEFAULT"; ROOT_LAYOUT -> "ROOT_LAYOUT"; LAYOUT -> "LAYOUT"
            LAYOUT_CONTENT -> "CONTENT"; TEXT -> "TEXT"; IMAGE -> "IMAGE"
            else -> "UNKNOWN"
        }
        fun name(): String = "ComponentStart"
        fun id(): Int = Operations.COMPONENT_START

        fun apply(buffer: WireBuffer, type: Int, componentId: Int, width: Float, height: Float) {
            buffer.start(Operations.COMPONENT_START)
            buffer.writeInt(type); buffer.writeInt(componentId)
            buffer.writeFloat(width); buffer.writeFloat(height)
        }

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val type = buffer.readInt(); val componentId = buffer.readInt()
            val width = buffer.readFloat(); val height = buffer.readFloat()
            operations.add(ComponentStart(type, componentId, width, height))
        }
    }
}
