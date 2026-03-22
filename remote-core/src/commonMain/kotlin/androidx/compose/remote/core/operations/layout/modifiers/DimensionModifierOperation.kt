package androidx.compose.remote.core.operations.layout.modifiers

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.VariableSupport
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.Utils
import androidx.compose.remote.core.operations.utilities.StringSerializer

abstract class DimensionModifierOperation(
    private var mType: Int,
    private var mValue: Float = 0f
) : Operation(), ModifierOperation, VariableSupport {

    object Type {
        const val WRAP = 0
        const val FILL = 1
        const val EXACT = 2
        const val WEIGHT = 3
        const val EXACT_DP = 4
        const val INTRINSIC_MIN = 5

        fun toString(type: Int): String = when (type) {
            WRAP -> "WRAP"
            FILL -> "FILL"
            EXACT -> "EXACT"
            WEIGHT -> "WEIGHT"
            EXACT_DP -> "EXACT_DP"
            INTRINSIC_MIN -> "INTRINSIC_MIN"
            else -> "UNKNOWN"
        }
    }

    private var mOutValue: Float = mValue
    private var mIsVariable: Boolean = false

    init {
        if (Utils.isVariable(mValue)) {
            mIsVariable = true
        }
        if (mType == Type.EXACT || mType == Type.EXACT_DP) {
            mOutValue = mValue
        }
    }

    fun getType(): Int = mType
    fun getValue(): Float = mOutValue
    fun isWrap(): Boolean = mType == Type.WRAP
    fun isFill(): Boolean = mType == Type.FILL
    fun hasWeight(): Boolean = mType == Type.WEIGHT
    fun isIntrinsicMin(): Boolean = mType == Type.INTRINSIC_MIN

    override fun registerListening(context: RemoteContext) {
        if (mIsVariable) {
            context.listensTo(Utils.idFromNan(mValue), this)
        }
    }

    override fun updateVariables(context: RemoteContext) {
        if (mIsVariable) {
            mOutValue = context.getFloat(Utils.idFromNan(mValue))
        }
    }

    override fun apply(context: RemoteContext) {}
    override fun write(buffer: WireBuffer) {}
    override fun deepToString(indent: String): String = indent + toString()
    override fun serializeToString(indent: Int, serializer: StringSerializer) {
        serializer.append(indent, toString())
    }
}
