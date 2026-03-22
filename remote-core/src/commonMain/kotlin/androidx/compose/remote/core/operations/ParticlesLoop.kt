// Copyright (C) 2024 The Android Open Source Project. Apache License 2.0.
package androidx.compose.remote.core.operations
import androidx.compose.remote.core.*
import androidx.compose.remote.core.operations.utilities.AnimatedFloatExpression
import androidx.compose.remote.core.operations.utilities.NanMap
import androidx.compose.remote.core.serialize.MapSerializer
class ParticlesLoop(private val mId: Int, private val mRestart: FloatArray?, private val mEquations: Array<FloatArray>) : PaintOperation(), VariableSupport, Container {
    private val mOutRestart: FloatArray? = mRestart?.copyOf(); private val mOutEquations = Array(mEquations.size) { mEquations[it].copyOf() }
    private var mVarId: IntArray = IntArray(0); private var mParticles: Array<FloatArray> = emptyArray(); var mParticlesSource: ParticlesCreate? = null
    override fun getList(): ArrayList<Operation> = mList; private val mList = ArrayList<Operation>(); val mExp = AnimatedFloatExpression()
    override fun updateVariables(context: RemoteContext) {
        mOutRestart?.let { for (i in mRestart!!.indices) { val v = mRestart[i]; it[i] = if (v.isNaN() && !AnimatedFloatExpression.isMathOperator(v) && !NanMap.isDataVariable(v)) context.getFloat(Utils.idFromNan(v)) else v } }
        for (i in mEquations.indices) for (j in mEquations[i].indices) { val v = mEquations[i][j]; mOutEquations[i][j] = if (v.isNaN() && !AnimatedFloatExpression.isMathOperator(v) && !NanMap.isDataVariable(v)) context.getFloat(Utils.idFromNan(v)) else v }
    }
    override fun registerListening(context: RemoteContext) { mParticlesSource = context.getObject(mId) as ParticlesCreate; mParticles = mParticlesSource!!.getParticles(); mVarId = mParticlesSource!!.getVariableIds()
        mRestart?.let { for (v in it) if (v.isNaN() && !AnimatedFloatExpression.isMathOperator(v) && !NanMap.isDataVariable(v)) context.listensTo(Utils.idFromNan(v), this) }
        for (eq in mEquations) for (v in eq) if (v.isNaN() && !AnimatedFloatExpression.isMathOperator(v) && !NanMap.isDataVariable(v)) context.listensTo(Utils.idFromNan(v), this) }
    override fun markDirty() { super<PaintOperation>.markDirty() }
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mId, mRestart, mEquations) }; override fun toString(): String = "ParticlesLoop[${Utils.idString(mId)}]"
    override fun deepToString(indent: String): String = indent + toString()
    override fun paint(context: PaintContext) {
        val rc = context.getContext(); val ca = rc.getCollectionsAccess()!!
        for (i in mParticles.indices) { for (j in mParticles[i].indices) { rc.loadFloat(mVarId[j], mParticles[i][j]); updateVariables(rc) }
            for (j in mParticles[i].indices) { mParticles[i][j] = mExp.eval(ca, mOutEquations[j], mOutEquations[j].size); rc.loadFloat(mVarId[j], mParticles[i][j]) }
            if (mOutRestart != null) { for (k in mRestart!!.indices) { val v = mRestart[k]; mOutRestart[k] = if (v.isNaN() && !AnimatedFloatExpression.isMathOperator(v) && !NanMap.isDataVariable(v)) rc.getFloat(Utils.idFromNan(v)) else v }; if (mExp.eval(ca, mOutRestart, mOutRestart.size) > 0) mParticlesSource!!.initializeParticle(i) }
            for (op in mList) { if (op is VariableSupport) (op as VariableSupport).updateVariables(rc); rc.incrementOpCount(); op.apply(rc) } }; context.needsRepaint()
    }
    override fun serialize(serializer: MapSerializer) { serializer.addType("ParticlesLoop").add("id", mId) }
    companion object { private val OP_CODE = Operations.PARTICLE_LOOP; private const val MAX_FLOAT_ARRAY = 2000; private const val MAX_EQU_LENGTH = 32; fun name(): String = "ParticlesLoop"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, id: Int, restart: FloatArray?, equations: Array<FloatArray>) { buffer.start(OP_CODE); buffer.writeInt(id); if (restart != null) { buffer.writeInt(restart.size); for (v in restart) buffer.writeFloat(v) } else buffer.writeInt(0); buffer.writeInt(equations.size); for (eq in equations) { buffer.writeInt(eq.size); for (v in eq) buffer.writeFloat(v) } }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { val id = buffer.readInt(); val rl = buffer.readInt(); val restart = if (rl > 0) { if (rl > MAX_EQU_LENGTH) throw RuntimeException("too long"); FloatArray(rl) { buffer.readFloat() } } else null
            val vl = buffer.readInt(); if (vl > MAX_FLOAT_ARRAY) throw RuntimeException("too many"); val eq = Array(vl) { val el = buffer.readInt(); if (el > MAX_EQU_LENGTH) throw RuntimeException("too long"); FloatArray(el) { buffer.readFloat() } }; operations.add(ParticlesLoop(id, restart, eq)) } }
}
