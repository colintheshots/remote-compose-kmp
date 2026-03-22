// Copyright (C) 2024 The Android Open Source Project. Apache License 2.0.
package androidx.compose.remote.core.operations
import androidx.compose.remote.core.*
import androidx.compose.remote.core.operations.utilities.AnimatedFloatExpression
import androidx.compose.remote.core.operations.utilities.NanMap
import androidx.compose.remote.core.serialize.MapSerializer
class FloatFunctionCall(private val mId: Int, private val mArgs: FloatArray?) : PaintOperation(), VariableSupport {
    private val mOutArgs: FloatArray? = mArgs?.copyOf(); var mFunction: FloatFunctionDefine? = null; val mExp = AnimatedFloatExpression()
    override fun updateVariables(context: RemoteContext) { if (mOutArgs != null && mArgs != null) for (i in mArgs.indices) { val v = mArgs[i]; mOutArgs[i] = if (v.isNaN() && !AnimatedFloatExpression.isMathOperator(v) && !NanMap.isDataVariable(v)) context.getFloat(Utils.idFromNan(v)) else v } }
    override fun registerListening(context: RemoteContext) { mFunction = context.getObject(mId) as? FloatFunctionDefine; mArgs?.let { for (v in it) if (v.isNaN() && !AnimatedFloatExpression.isMathOperator(v) && !NanMap.isDataVariable(v)) context.listensTo(Utils.idFromNan(v), this) } }
    override fun markDirty() { super<PaintOperation>.markDirty() }
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mId, mArgs) }; override fun toString(): String = "callFunction[${Utils.idString(mId)}]"
    override fun deepToString(indent: String): String = indent + toString()
    override fun paint(context: PaintContext) { val rc = context.getContext(); val args = mFunction!!.getArgs(); for (j in mOutArgs!!.indices) { rc.loadFloat(args[j], mOutArgs[j]); updateVariables(rc) }; mFunction!!.execute(rc) }
    override fun serialize(serializer: MapSerializer) { serializer.addType("FunctionCall").add("id", mId) }
    companion object { private val OP_CODE = Operations.FUNCTION_CALL; private const val MAX_FLOAT_ARRAY_SIZE = 80; fun name(): String = "FunctionCall"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, id: Int, args: FloatArray?) { buffer.start(OP_CODE); buffer.writeInt(id); if (args != null) { buffer.writeInt(args.size); for (a in args) buffer.writeFloat(a) } else buffer.writeInt(0) }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { val id = buffer.readInt(); val argLen = buffer.readInt(); if (argLen > MAX_FLOAT_ARRAY_SIZE) throw RuntimeException("array too big"); val args = if (argLen > 0) FloatArray(argLen) { buffer.readFloat() } else null; operations.add(FloatFunctionCall(id, args)) } }
}
