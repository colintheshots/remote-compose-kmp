package androidx.compose.remote.core.operations.layout

import androidx.compose.remote.core.CoreDocument
import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.WireBuffer

class TouchCancelModifierOperation : ListActionsOperation("TOUCH_CANCEL_MODIFIER"), TouchHandler {
    override fun write(buffer: WireBuffer) { Companion.apply(buffer) }
    override fun toString(): String = "TouchCancelModifier"
    override fun onTouchDown(context: RemoteContext, document: CoreDocument, component: Component, x: Float, y: Float) {}
    override fun onTouchUp(context: RemoteContext, document: CoreDocument, component: Component, x: Float, y: Float, dx: Float, dy: Float) {}
    override fun onTouchCancel(context: RemoteContext, document: CoreDocument, component: Component, x: Float, y: Float) {
        applyActions(context, document, component, x, y, true)
    }
    override fun onTouchDrag(context: RemoteContext, document: CoreDocument, component: Component, x: Float, y: Float) {}

    companion object {
        fun name(): String = "TouchCancelModifier"
        fun apply(buffer: WireBuffer) { buffer.start(Operations.MODIFIER_TOUCH_CANCEL) }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { operations.add(TouchCancelModifierOperation()) }
    }
}
