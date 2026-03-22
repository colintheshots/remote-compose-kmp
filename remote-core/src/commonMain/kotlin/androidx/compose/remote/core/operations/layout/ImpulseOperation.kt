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

class ImpulseOperation(
    private var mDuration: Float,
    private var mStartAt: Float
) : PaintOperation(), VariableSupport, Container {

    val mList: ArrayList<Operation> = ArrayList()
    private var mOutDuration: Float = mDuration
    private var mOutStartAt: Float = mStartAt
    private var mInitialPass: Boolean = true
    private var mProcess: ImpulseProcess? = null

    override fun registerListening(context: RemoteContext) {
        if (mProcess == null && mList.isNotEmpty()) {
            val last = mList.last()
            if (last is ImpulseProcess) { mProcess = last; mList.remove(last) }
        }
        if (mStartAt.isNaN()) context.listensTo(Utils.idFromNan(mStartAt), this)
        if (mDuration.isNaN()) context.listensTo(Utils.idFromNan(mDuration), this)
        for (op in mList) { if (op is VariableSupport) op.registerListening(context) }
        mProcess?.registerListening(context)
    }

    override fun updateVariables(context: RemoteContext) {
        mOutDuration = if (mDuration.isNaN()) context.getFloat(Utils.idFromNan(mDuration)) else mDuration
        mOutStartAt = if (mStartAt.isNaN()) context.getFloat(Utils.idFromNan(mStartAt)) else mStartAt
        mProcess?.updateVariables(context)
    }

    override fun getList(): ArrayList<Operation> = mList
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mDuration, mStartAt) }
    override fun toString(): String = "ImpulseOperation"
    override fun deepToString(indent: String): String = indent + toString()

    override fun paint(context: PaintContext) {
        val rc = context.getContext()
        if (rc.getAnimationTime() <= mOutStartAt + mOutDuration) {
            if (mInitialPass) {
                for (op in mList) {
                    if (op is VariableSupport && op.isDirty()) (op as VariableSupport).updateVariables(rc)
                    rc.incrementOpCount(); op.apply(rc)
                }
                mInitialPass = false
            } else {
                rc.incrementOpCount(); mProcess?.paint(context)
            }
        } else { mInitialPass = true }
    }

    fun setProcess(impulseProcess: ImpulseProcess) { mProcess = impulseProcess }
    fun estimateIterations(): Int = if (mDuration.isNaN()) 10 else (mDuration * 60).toInt()

    companion object {
        fun name(): String = "ImpulseOperation"
        fun apply(buffer: WireBuffer, duration: Float, startAt: Float) {
            buffer.start(Operations.IMPULSE_START); buffer.writeFloat(duration); buffer.writeFloat(startAt)
        }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            operations.add(ImpulseOperation(buffer.readFloat(), buffer.readFloat()))
        }
    }
}
