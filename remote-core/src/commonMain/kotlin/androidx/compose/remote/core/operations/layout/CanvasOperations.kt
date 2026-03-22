package androidx.compose.remote.core.operations.layout

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.PaintOperation
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.VariableSupport
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.ComponentValue
import androidx.compose.remote.core.operations.Container

class CanvasOperations : PaintOperation(), VariableSupport, Container {
    val mList: ArrayList<Operation> = ArrayList()
    var mComponent: LayoutComponent? = null

    override fun registerListening(context: RemoteContext) {
        for (op in mList) {
            if (op is VariableSupport) op.registerListening(context)
            if (op is ComponentValue) mComponent?.addComponentValue(op)
        }
    }

    override fun updateVariables(context: RemoteContext) {
        for (op in mList) { if (op is VariableSupport) op.updateVariables(context) }
    }

    override fun getList(): ArrayList<Operation> = mList
    override fun write(buffer: WireBuffer) { Companion.apply(buffer) }
    override fun toString(): String = "CanvasOperations"
    override fun deepToString(indent: String): String = indent + toString()

    override fun paint(context: PaintContext) {
        val rc = context.getContext()
        for (op in mList) {
            if (op is VariableSupport && op.isDirty()) (op as VariableSupport).updateVariables(rc)
            rc.incrementOpCount(); op.apply(rc)
        }
    }

    fun setComponent(layoutComponent: LayoutComponent?) {
        mComponent = layoutComponent
        layoutComponent?.setCanvasOperations(this)
    }

    companion object {
        fun name(): String = "Loop"
        fun apply(buffer: WireBuffer) { buffer.start(Operations.CANVAS_OPERATIONS) }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { operations.add(CanvasOperations()) }
    }
}
