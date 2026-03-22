// Copyright (C) 2024 The Android Open Source Project. Apache License 2.0.
package androidx.compose.remote.core.operations
import androidx.compose.remote.core.*
import androidx.compose.remote.core.operations.utilities.AnimatedFloatExpression
import androidx.compose.remote.core.operations.utilities.NanMap
import androidx.compose.remote.core.operations.utilities.PathGenerator
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable
class PathExpression(private val mInstanceId: Int, private val mExpressionX: FloatArray, private val mExpressionY: FloatArray, private val mMin: Float, private val mMax: Float, private var mCount: Float, private val mFlags: Int) : Operation(), VariableSupport, Serializable {
    private val mPathGenerator = PathGenerator(); private var mOutputPath = FloatArray(0); private val mOutExpressionX = FloatArray(mExpressionX.size); private val mOutExpressionY = FloatArray(mExpressionY.size)
    private var mOutMin = mMin; private var mOutMax = mMax; private val mOutCount = mCount; private var mPathChanged = true; private val mWinding = (mFlags and WINDING_MASK) shr 24
    override fun updateVariables(context: RemoteContext) {
        if (mMax.isNaN()) mOutMax = context.getFloat(Utils.idFromNan(mMax)); if (mMin.isNaN()) mOutMin = context.getFloat(Utils.idFromNan(mMin)); if (mCount.isNaN()) mCount = context.getFloat(Utils.idFromNan(mCount))
        for (i in mExpressionX.indices) { val v = mExpressionX[i]; mOutExpressionX[i] = if (v.isNaN() && !AnimatedFloatExpression.isMathOperator(v) && !NanMap.isDataVariable(v) && !NanMap.isVar1(v)) context.getFloat(Utils.idFromNan(v)) else v }
        for (i in mExpressionY.indices) { val v = mExpressionY[i]; mOutExpressionY[i] = if (v.isNaN() && !AnimatedFloatExpression.isMathOperator(v) && !NanMap.isDataVariable(v) && !NanMap.isVar1(v)) context.getFloat(Utils.idFromNan(v)) else v }
        mPathChanged = true
    }
    override fun registerListening(context: RemoteContext) {
        if (mMax.isNaN()) context.listensTo(Utils.idFromNan(mMax), this); if (mMin.isNaN()) context.listensTo(Utils.idFromNan(mMin), this); if (mCount.isNaN()) context.listensTo(Utils.idFromNan(mCount), this)
        for (v in mExpressionX) if (v.isNaN() && !AnimatedFloatExpression.isMathOperator(v) && !NanMap.isDataVariable(v)) context.listensTo(Utils.idFromNan(v), this)
        for (v in mExpressionY) if (v.isNaN() && !AnimatedFloatExpression.isMathOperator(v) && !NanMap.isDataVariable(v)) context.listensTo(Utils.idFromNan(v), this)
    }
    override fun markDirty() { super<Operation>.markDirty() }
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mInstanceId, mExpressionX, mExpressionY, mMin, mMax, mCount, mFlags) }
    override fun toString(): String = "PathExpression[$mInstanceId]"
    override fun deepToString(indent: String): String = indent + "PathExpression[id=$mInstanceId]"
    override fun apply(context: RemoteContext) {
        if (mPathChanged) {
            val loop = (mFlags and 0x1) == LOOP; val len = mPathGenerator.getReturnLength(mOutCount.toInt(), loop)
            if (mOutputPath.size != len) mOutputPath = FloatArray(len)
            if ((mFlags and POLAR) == POLAR) mPathGenerator.getPolarPath(mOutputPath, mOutExpressionX, mOutExpressionY, mOutMin, mOutMax, mOutCount.toInt(), mFlags and 0x6, loop, context.getCollectionsAccess()!!)
            else mPathGenerator.getPath(mOutputPath, mOutExpressionX, mOutExpressionY, mOutMin, mOutMax, mOutCount.toInt(), mFlags and 0x6, loop, context.getCollectionsAccess()!!)
            context.loadPathData(mInstanceId, mWinding, mOutputPath)
        }; mPathChanged = false
    }
    override fun serialize(serializer: MapSerializer) { serializer.addType("PathExpression").add("id", mInstanceId).add("flags", mFlags).add("count", mCount).add("min", mMin).add("max", mMax).addPath("expressionX", mExpressionX).addPath("expressionY", mExpressionY) }
    companion object {
        private val OP_CODE = Operations.PATH_EXPRESSION; private const val MAX_EXPRESSION_LENGTH = 32; const val LOOP = 1; const val MONOTONIC = 2; const val LINEAR = 4; const val POLAR = 8; const val WINDING_MASK = 0x3000000
        fun name(): String = "PathExpression"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, id: Int, expressionX: FloatArray, expressionY: FloatArray?, min: Float, max: Float, count: Float, flags: Int) {
            buffer.start(OP_CODE); buffer.writeInt(id); buffer.writeInt(flags); buffer.writeFloat(min); buffer.writeFloat(max); buffer.writeFloat(count)
            buffer.writeInt(expressionX.size); for (d in expressionX) buffer.writeFloat(d)
            if (expressionY == null) buffer.writeInt(0) else { buffer.writeInt(expressionY.size); for (d in expressionY) buffer.writeFloat(d) }
        }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val id = buffer.readInt(); val flags = buffer.readInt(); val min = buffer.readFloat(); val max = buffer.readFloat(); val count = buffer.readFloat()
            var len = buffer.readInt(); if (len > MAX_EXPRESSION_LENGTH) throw RuntimeException("Path too long"); val ex = FloatArray(len) { buffer.readFloat() }
            len = buffer.readInt(); if (len > MAX_EXPRESSION_LENGTH) throw RuntimeException("Path too long"); val ey = FloatArray(len) { buffer.readFloat() }
            operations.add(PathExpression(id, ex, ey, min, max, count, flags))
        }
    }
}
