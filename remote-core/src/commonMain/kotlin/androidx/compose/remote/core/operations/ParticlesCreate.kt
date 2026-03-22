// Copyright (C) 2024 The Android Open Source Project. Apache License 2.0.
package androidx.compose.remote.core.operations
import androidx.compose.remote.core.*
import androidx.compose.remote.core.operations.utilities.AnimatedFloatExpression
import androidx.compose.remote.core.operations.utilities.NanMap
import androidx.compose.remote.core.serialize.MapSerializer
class ParticlesCreate(private val mId: Int, val mVarId: IntArray, private val mEquations: Array<FloatArray>, private val mParticleCount: Int) : PaintOperation(), VariableSupport {
    private val mOutEquations = Array(mEquations.size) { mEquations[it].copyOf() }; val mParticles = Array(mParticleCount) { FloatArray(mVarId.size) }
    private val mIndexeVars: IntArray; val mExp = AnimatedFloatExpression()
    init { val v1Int = AnimatedFloatExpression.VAR1.toRawBits(); val idx = mutableListOf<Int>(); for (j in mEquations.indices) for (k in mEquations[j].indices) if (mEquations[j][k].isNaN() && mEquations[j][k].toRawBits() == v1Int) idx.add(j * mEquations.size + k); mIndexeVars = idx.toIntArray() }
    override fun updateVariables(context: RemoteContext) { for (i in mEquations.indices) for (j in mEquations[i].indices) { val v = mEquations[i][j]; mOutEquations[i][j] = if (v.isNaN() && !AnimatedFloatExpression.isMathOperator(v) && !NanMap.isDataVariable(v)) context.getFloat(Utils.idFromNan(v)) else v } }
    override fun registerListening(context: RemoteContext) { context.putObject(mId, this); for (eq in mEquations) for (v in eq) if (v.isNaN() && !AnimatedFloatExpression.isMathOperator(v) && !NanMap.isDataVariable(v)) context.listensTo(Utils.idFromNan(v), this) }
    override fun markDirty() { super<PaintOperation>.markDirty() }
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mId, mVarId, mEquations, mParticleCount) }; override fun toString(): String = "ParticlesCreate[${Utils.idString(mId)}]"
    override fun deepToString(indent: String): String = indent + toString()
    override fun paint(context: PaintContext) { for (i in mParticles.indices) initializeParticle(i) }
    fun initializeParticle(pNo: Int) { for (j in mParticles[pNo].indices) { for (k in mIndexeVars.indices) { val pos = mIndexeVars[k]; mOutEquations[pos / mOutEquations.size][pos % mOutEquations.size] = pNo.toFloat() }; mParticles[pNo][j] = mExp.eval(mOutEquations[j], mOutEquations[j].size) } }
    fun getParticles(): Array<FloatArray> = mParticles; fun getVariableIds(): IntArray = mVarId; fun getEquations(): Array<FloatArray> = mOutEquations
    override fun serialize(serializer: MapSerializer) {}
    companion object { private val OP_CODE = Operations.PARTICLE_DEFINE; private const val MAX_FLOAT_ARRAY = 2000; private const val MAX_EQU_LENGTH = 32; fun name(): String = "ParticlesCreate"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, id: Int, varId: IntArray, equations: Array<FloatArray>, particleCount: Int) { buffer.start(OP_CODE); buffer.writeInt(id); buffer.writeInt(particleCount); buffer.writeInt(varId.size); for (i in varId.indices) { buffer.writeInt(varId[i]); buffer.writeInt(equations[i].size); for (v in equations[i]) buffer.writeFloat(v) } }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { val id = buffer.readInt(); val pc = buffer.readInt(); val vl = buffer.readInt(); if (vl > MAX_FLOAT_ARRAY) throw RuntimeException("too many"); val vi = IntArray(vl); val eq = Array(vl) { FloatArray(0) }; for (i in 0 until vl) { vi[i] = buffer.readInt(); val el = buffer.readInt(); if (el > MAX_EQU_LENGTH) throw RuntimeException("too long"); eq[i] = FloatArray(el) { buffer.readFloat() } }; operations.add(ParticlesCreate(id, vi, eq, pc)) } }
}
