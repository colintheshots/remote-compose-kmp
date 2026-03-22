// Copyright (C) 2024 The Android Open Source Project. Apache License 2.0.
package androidx.compose.remote.core.operations
import androidx.compose.remote.core.*
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable
class ConditionalOperations(var mType: Byte, var mVarA: Float, var mVarB: Float) : PaintOperation(), Container, VariableSupport, Serializable {
    val mList = ArrayList<Operation>(); var mVarAOut = mVarA; var mVarBOut = mVarB
    override fun getList(): ArrayList<Operation> = mList
    override fun registerListening(context: RemoteContext) { if (mVarA.isNaN()) context.listensTo(Utils.idFromNan(mVarA), this); if (mVarB.isNaN()) context.listensTo(Utils.idFromNan(mVarB), this) }
    override fun updateVariables(context: RemoteContext) { mVarAOut = if (mVarA.isNaN()) context.getFloat(Utils.idFromNan(mVarA)) else mVarA; mVarBOut = if (mVarB.isNaN()) context.getFloat(Utils.idFromNan(mVarB)) else mVarB; for (op in mList) if (op is VariableSupport && (op as Operation).isDirty()) (op as VariableSupport).updateVariables(context) }
    override fun markDirty() { super<PaintOperation>.markDirty() }
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mType, mVarA, mVarB) }
    override fun toString(): String = "ConditionalOperations ${TYPE_STR[mType.toInt()]}"
    override fun deepToString(indent: String): String = (indent ?: "") + toString()
    override fun paint(context: PaintContext) {
        val rc = context.getContext(); val run = when (mType) { TYPE_EQ -> mVarAOut == mVarBOut; TYPE_NEQ -> mVarAOut != mVarBOut; TYPE_LT -> mVarAOut < mVarBOut; TYPE_LTE -> mVarAOut <= mVarBOut; TYPE_GT -> mVarAOut > mVarBOut; TYPE_GTE -> mVarAOut >= mVarBOut; else -> false }
        if (run) for (op in mList) { rc.incrementOpCount(); op.apply(rc) }
    }
    fun estimateIterations(): Int = 1
    override fun serialize(serializer: MapSerializer) { serializer.addType("ConditionalOperations").add("type", mType).add("varA", mVarA, mVarAOut).add("VarB", mVarB, mVarBOut).add("list", mList) }
    companion object {
        private val OP_CODE = Operations.CONDITIONAL_OPERATIONS; const val TYPE_EQ: Byte = 0; const val TYPE_NEQ: Byte = 1; const val TYPE_LT: Byte = 2; const val TYPE_LTE: Byte = 3; const val TYPE_GT: Byte = 4; const val TYPE_GTE: Byte = 5
        private val TYPE_STR = arrayOf("EQ", "NEQ", "LT", "LTE", "GT", "GTE")
        fun name(): String = "ConditionalOperations"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, type: Byte, a: Float, b: Float) { buffer.start(OP_CODE); buffer.writeByte(type.toInt()); buffer.writeFloat(a); buffer.writeFloat(b) }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { operations.add(ConditionalOperations(buffer.readByte().toByte(), buffer.readFloat(), buffer.readFloat())) }
    }
}
