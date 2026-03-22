package androidx.compose.remote.core.operations.layout.modifiers

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.VariableSupport
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.Utils
import androidx.compose.remote.core.operations.utilities.StringSerializer

abstract class DimensionInModifierOperation(
    private var mMin: Float,
    private var mMax: Float
) : Operation(), ModifierOperation, VariableSupport {

    private var mOutMin: Float = mMin
    private var mOutMax: Float = mMax

    fun getMin(): Float = mOutMin
    fun getMax(): Float = mOutMax

    override fun registerListening(context: RemoteContext) {
        if (Utils.isVariable(mMin)) {
            context.listensTo(Utils.idFromNan(mMin), this)
        }
        if (Utils.isVariable(mMax)) {
            context.listensTo(Utils.idFromNan(mMax), this)
        }
    }

    override fun updateVariables(context: RemoteContext) {
        mOutMin = if (mMin.isNaN()) context.getFloat(Utils.idFromNan(mMin)) else mMin
        mOutMax = if (mMax.isNaN()) context.getFloat(Utils.idFromNan(mMax)) else mMax
    }

    override fun apply(context: RemoteContext) {}
    override fun write(buffer: WireBuffer) {}
    override fun deepToString(indent: String): String = indent + toString()
    override fun serializeToString(indent: Int, serializer: StringSerializer) {
        serializer.append(indent, toString())
    }
}
