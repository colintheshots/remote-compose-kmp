// Copyright (C) 2024 The Android Open Source Project. Apache License 2.0.
package androidx.compose.remote.core.operations
import androidx.compose.remote.core.*
import androidx.compose.remote.core.operations.utilities.AnimatedFloatExpression
import androidx.compose.remote.core.operations.utilities.NanMap
import androidx.compose.remote.core.serialize.MapSerializer
class ParticlesCompare(private val mId: Int, private val mFlags: Short, private val mMin: Float, private val mMax: Float, private val mExpression: FloatArray?, private val mEquations1: Array<FloatArray>?, private val mEquations2: Array<FloatArray>?) : PaintOperation(), VariableSupport, Container {
    private val mOutExpression = mExpression?.copyOf(); private val mOutEquations1 = mEquations1?.let { Array(it.size) { i -> it[i].copyOf() } }; private val mOutEquations2 = mEquations2?.let { Array(it.size) { i -> it[i].copyOf() } }
    private var mOutMin = mMin; private var mOutMax = mMax; private var mVarId = IntArray(0); private var mParticles: Array<FloatArray> = emptyArray(); var mParticlesSource: ParticlesCreate? = null
    override fun getList(): ArrayList<Operation> = mList; private val mList = ArrayList<Operation>(); val mExp = AnimatedFloatExpression()
    override fun updateVariables(context: RemoteContext) {
        mOutMin = if (mMin.isNaN()) context.getFloat(Utils.idFromNan(mMin)) else mMin; mOutMax = if (mMax.isNaN()) context.getFloat(Utils.idFromNan(mMax)) else mMax
        updateArr(context, mExpression, mOutExpression); updateNested(context, mEquations1, mOutEquations1); updateNested(context, mEquations2, mOutEquations2)
    }
    private fun updateArr(ctx: RemoteContext, src: FloatArray?, dst: FloatArray?) { if (src == null || dst == null) return; for (i in src.indices) { val v = src[i]; dst[i] = if (v.isNaN() && !AnimatedFloatExpression.isMathOperator(v) && !NanMap.isDataVariable(v)) ctx.getFloat(Utils.idFromNan(v)) else v } }
    private fun updateNested(ctx: RemoteContext, src: Array<FloatArray>?, dst: Array<FloatArray>?) { if (src == null || dst == null) return; for (i in src.indices) updateArr(ctx, src[i], dst[i]) }
    private fun register(context: RemoteContext, values: FloatArray?) { values?.let { for (v in it) if (v.isNaN() && !AnimatedFloatExpression.isMathOperator(v) && !NanMap.isDataVariable(v)) context.listensTo(Utils.idFromNan(v), this) } }
    override fun registerListening(context: RemoteContext) { mParticlesSource = context.getObject(mId) as? ParticlesCreate; mParticlesSource?.let { mParticles = it.getParticles(); mVarId = it.getVariableIds() }; register(context, mExpression); mEquations1?.let { for (eq in it) register(context, eq) }; mEquations2?.let { for (eq in it) register(context, eq) } }
    override fun markDirty() { super<PaintOperation>.markDirty() }
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mId, mFlags, mMin, mMax, mExpression, mEquations1, mEquations2) }; override fun toString(): String = "ParticlesCompare[${Utils.idString(mId)}]"
    override fun deepToString(indent: String): String = indent + toString()
    override fun paint(context: PaintContext) {
        val rc = context.getContext(); val ca = rc.getCollectionsAccess()!!
        val start = if (mOutMin < 0) 0 else mOutMin.toInt(); val end = if (mOutMax < 0) mParticles.size else mOutMax.toInt()
        for (i in start until end) { val p = mParticles[i]; for (j in p.indices) rc.loadFloat(mVarId[j], p[j]); updateArr(rc, mExpression, mOutExpression)
            val v = if (mOutExpression != null) mExp.eval(ca, mOutExpression, mOutExpression.size) else 0f; rc.incrementOpCount()
            if (v > 0 && mOutEquations1 != null) { updateNested(rc, mEquations1, mOutEquations1); for (j in p.indices) { p[j] = mExp.eval(ca, mOutEquations1[j], mOutEquations1[j].size); rc.loadFloat(mVarId[j], p[j]) }; for (op in mList) { if (op is VariableSupport) (op as VariableSupport).updateVariables(rc); rc.incrementOpCount(); op.apply(rc) }; context.needsRepaint() } }
    }
    override fun serialize(serializer: MapSerializer) { serializer.addType("ParticlesCompare").add("id", mId) }
    companion object { private val OP_CODE = Operations.PARTICLE_COMPARE; private const val MAX_FLOAT_ARRAY = 2000; private const val MAX_EQU_LENGTH = 46; fun name(): String = "ParticlesCompare"; fun id(): Int = OP_CODE
        private fun writeFloats(buffer: WireBuffer, values: FloatArray?) { if (values == null) { buffer.writeInt(0); return }; buffer.writeInt(values.size); for (v in values) buffer.writeFloat(v) }
        fun apply(buffer: WireBuffer, id: Int, flags: Short, min: Float, max: Float, compare: FloatArray?, equations1: Array<FloatArray>?, equations2: Array<FloatArray>?) { buffer.start(OP_CODE); buffer.writeInt(id); buffer.writeShort(flags.toInt()); buffer.writeFloat(min); buffer.writeFloat(max); writeFloats(buffer, compare)
            if (equations1 == null) buffer.writeInt(0) else { buffer.writeInt(equations1.size); for (eq in equations1) writeFloats(buffer, eq) }; if (equations2 == null) buffer.writeInt(0) else { buffer.writeInt(equations2.size); for (eq in equations2) writeFloats(buffer, eq) } }
        private fun readFloats(buffer: WireBuffer): FloatArray? { val len = buffer.readInt(); if (len > MAX_EQU_LENGTH) throw RuntimeException("too long"); if (len == 0) return null; return FloatArray(len) { buffer.readFloat() } }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { val id = buffer.readInt(); val flags = buffer.readShort().toShort(); val min = buffer.readFloat(); val max = buffer.readFloat(); val exp = readFloats(buffer)
            val r1l = buffer.readInt(); if (r1l > MAX_FLOAT_ARRAY) throw RuntimeException("too many"); val eq1 = if (r1l == 0) null else Array(r1l) { readFloats(buffer)!! }
            val r2l = buffer.readInt(); val eq2 = if (r2l == 0) null else Array(r2l) { readFloats(buffer)!! }; operations.add(ParticlesCompare(id, flags, min, max, exp, eq1, eq2)) } }
}
