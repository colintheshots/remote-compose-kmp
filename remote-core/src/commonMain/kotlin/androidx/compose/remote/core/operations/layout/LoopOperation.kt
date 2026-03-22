package androidx.compose.remote.core.operations.layout

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.PaintOperation
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.VariableSupport
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.Container
import androidx.compose.remote.core.operations.Utils

class LoopOperation(
    private var mIndexVariableId: Int,
    private var mFrom: Float = 0f,
    private var mStep: Float = 1f,
    private var mUntil: Float
) : PaintOperation(), Container, VariableSupport {

    val mList: ArrayList<Operation> = ArrayList()
    private var mUntilOut: Float = mUntil
    private var mFromOut: Float = mFrom
    private var mStepOut: Float = mStep

    constructor(count: Int, indexId: Int) : this(indexId, 0f, 1f, count.toFloat())

    override fun registerListening(context: RemoteContext) {
        if (mUntil.isNaN()) context.listensTo(Utils.idFromNan(mUntil), this)
        if (mFrom.isNaN()) context.listensTo(Utils.idFromNan(mFrom), this)
        if (mStep.isNaN()) context.listensTo(Utils.idFromNan(mStep), this)
    }

    override fun updateVariables(context: RemoteContext) {
        mUntilOut = if (mUntil.isNaN()) context.getFloat(Utils.idFromNan(mUntil)) else mUntil
        mFromOut = if (mFrom.isNaN()) context.getFloat(Utils.idFromNan(mFrom)) else mFrom
        mStepOut = if (mStep.isNaN()) context.getFloat(Utils.idFromNan(mStep)) else mStep
        for (op in mList) { if (op is VariableSupport && op.isDirty()) op.updateVariables(context) }
    }

    override fun getList(): ArrayList<Operation> = mList
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mIndexVariableId, mFrom, mStep, mUntil) }
    override fun toString(): String = "LoopOperation"
    override fun deepToString(indent: String): String = indent + toString()

    override fun paint(context: PaintContext) {
        val rc = context.getContext()
        if (mIndexVariableId == 0) {
            var i = mFromOut; while (i < mUntilOut) { for (op in mList) { rc.incrementOpCount(); op.apply(rc) }; i += mStepOut }
        } else {
            var i = mFromOut; while (i < mUntilOut) {
                rc.loadFloat(mIndexVariableId, i)
                for (op in mList) {
                    if (op is VariableSupport && op.isDirty()) op.updateVariables(rc)
                    rc.incrementOpCount(); op.apply(rc)
                }
                i += mStepOut
            }
        }
    }

    fun estimateIterations(): Int =
        if (!(mUntil.isNaN() || mFrom.isNaN() || mStep.isNaN())) ((0.5f + (mUntil - mFrom) / mStep).toInt()) else 10

    companion object {
        fun name(): String = "Loop"
        fun apply(buffer: WireBuffer, indexId: Int, from: Float, step: Float, until: Float) {
            buffer.start(Operations.LOOP_START); buffer.writeInt(indexId)
            buffer.writeFloat(from); buffer.writeFloat(step); buffer.writeFloat(until)
        }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val indexId = buffer.readInt(); val from = buffer.readFloat()
            val step = buffer.readFloat(); val until = buffer.readFloat()
            operations.add(LoopOperation(indexId, from, step, until))
        }
    }
}
