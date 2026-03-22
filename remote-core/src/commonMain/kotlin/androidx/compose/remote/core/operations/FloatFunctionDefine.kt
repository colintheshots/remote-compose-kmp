// Copyright (C) 2024 The Android Open Source Project. Apache License 2.0.
package androidx.compose.remote.core.operations
import androidx.compose.remote.core.*
import androidx.compose.remote.core.operations.utilities.AnimatedFloatExpression
class FloatFunctionDefine(private val mId: Int, private val mFloatVarId: IntArray) : Operation(), VariableSupport, Container {
    private val mList = ArrayList<Operation>(); val mExp = AnimatedFloatExpression()
    override fun getList(): ArrayList<Operation> = mList
    override fun updateVariables(context: RemoteContext) {}; override fun registerListening(context: RemoteContext) { context.putObject(mId, this) }
    override fun markDirty() { super<Operation>.markDirty() }
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mId, mFloatVarId) }; override fun toString(): String = "FloatFunctionDefine[${Utils.idString(mId)}]"
    override fun deepToString(indent: String): String = indent + toString(); override fun apply(context: RemoteContext) {}
    fun getArgs(): IntArray = mFloatVarId
    fun execute(context: RemoteContext) { for (op in mList) { if (op is VariableSupport) (op as VariableSupport).updateVariables(context); context.incrementOpCount(); op.apply(context) } }
    companion object { private val OP_CODE = Operations.FUNCTION_DEFINE; private const val MAX_ARGUMENTS = 32; fun name(): String = "FunctionDefine"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, id: Int, varId: IntArray) { buffer.start(OP_CODE); buffer.writeInt(id); buffer.writeInt(varId.size); for (v in varId) buffer.writeInt(v) }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { val id = buffer.readInt(); val varLen = buffer.readInt(); if (varLen > MAX_ARGUMENTS) throw IllegalArgumentException("Too many arguments"); operations.add(FloatFunctionDefine(id, IntArray(varLen) { buffer.readInt() })) } }
}
