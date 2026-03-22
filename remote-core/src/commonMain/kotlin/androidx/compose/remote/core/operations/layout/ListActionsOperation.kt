package androidx.compose.remote.core.operations.layout

import androidx.compose.remote.core.CoreDocument
import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.PaintOperation
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.Container
import androidx.compose.remote.core.operations.TextData
import androidx.compose.remote.core.operations.layout.modifiers.ModifierOperation
import androidx.compose.remote.core.operations.utilities.StringSerializer

abstract class ListActionsOperation(
    val mOperationName: String
) : PaintOperation(), Container, ModifierOperation, DecoratorComponent {

    protected var mWidth: Float = 0f
    protected var mHeight: Float = 0f
    private val mLocationInWindow = FloatArray(2)
    val mList: ArrayList<Operation> = ArrayList()

    override fun getList(): ArrayList<Operation> = mList
    override fun toString(): String = mOperationName

    override fun apply(context: RemoteContext) {
        for (op in mList) { if (op is TextData) { op.apply(context); context.incrementOpCount() } }
    }

    override fun deepToString(indent: String): String = indent + toString()
    override fun paint(context: PaintContext) {}

    override fun layout(context: RemoteContext, component: Component, width: Float, height: Float) {
        mWidth = width; mHeight = height
    }

    override fun serializeToString(indent: Int, serializer: StringSerializer) {
        serializer.append(indent, mOperationName)
        for (o in mList) { if (o is ActionOperation) o.serializeToString(indent + 1, serializer) }
    }

    fun applyActions(context: RemoteContext, document: CoreDocument, component: Component, x: Float, y: Float, force: Boolean): Boolean {
        if (!force && !component.isVisible()) return false
        if (!force && !component.contains(x, y)) return false
        mLocationInWindow[0] = 0f; mLocationInWindow[1] = 0f
        component.getLocationInWindow(mLocationInWindow)
        for (o in mList) { if (o is ActionOperation) o.runAction(context, document, component, x, y) }
        return true
    }
}
