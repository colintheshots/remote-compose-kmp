package androidx.compose.remote.core.operations

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable

class ColorConstant(var mColorId: Int, var mColor: Int) : Operation(), Serializable {

    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mColorId, mColor) }

    override fun toString(): String = "ColorConstant[$mColorId] = ${Utils.colorInt(mColor)}"

    override fun apply(context: RemoteContext) { context.loadColor(mColorId, mColor) }

    override fun deepToString(indent: String): String = indent + toString()

    override fun serialize(serializer: MapSerializer) {
        serializer.addType(CLASS_NAME)
            .add("color", Utils.colorInt(mColor))
            .add("colorId", mColorId)
    }

    companion object {
        private val OP_CODE = Operations.COLOR_CONSTANT
        private const val CLASS_NAME = "ColorConstant"

        fun name(): String = CLASS_NAME
        fun id(): Int = OP_CODE

        fun apply(buffer: WireBuffer, colorId: Int, color: Int) {
            buffer.start(OP_CODE)
            buffer.writeInt(colorId)
            buffer.writeInt(color)
        }

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val colorId = buffer.readInt()
            val color = buffer.readInt()
            operations.add(ColorConstant(colorId, color))
        }
    }
}
