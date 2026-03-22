package androidx.compose.remote.core.operations

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.utilities.AnimatedFloatExpression
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable
import kotlin.random.Random

class FloatConstant(var mId: Int, var mValue: Float) : Operation(), Serializable {

    init {
        if (Utils.idFromNan(mValue) == Utils.idFromNan(AnimatedFloatExpression.RAND)) {
            mValue = Random.nextFloat()
        }
    }

    fun update(from: FloatConstant) { mValue = from.mValue }

    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mId, mValue) }

    override fun toString(): String = "FloatConstant[$mId] = $mValue"

    override fun apply(context: RemoteContext) { context.loadFloat(mId, mValue) }

    override fun deepToString(indent: String): String = indent + toString()

    override fun serialize(serializer: MapSerializer) {
        serializer.addType(CLASS_NAME).add("id", mId).add("value", mValue)
    }

    companion object {
        private val OP_CODE = Operations.DATA_FLOAT
        private const val CLASS_NAME = "FloatConstant"

        fun name(): String = CLASS_NAME
        fun id(): Int = OP_CODE

        fun apply(buffer: WireBuffer, id: Int, value: Float) {
            buffer.start(OP_CODE)
            buffer.writeInt(id)
            buffer.writeFloat(value)
        }

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val id = buffer.readInt()
            val value = buffer.readFloat()
            operations.add(FloatConstant(id, value))
        }
    }
}
