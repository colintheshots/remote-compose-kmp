package androidx.compose.remote.core.operations.layout

import androidx.compose.remote.core.CoreDocument
import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.WireBuffer

class TouchDownModifierOperation : ListActionsOperation("TOUCH_DOWN_MODIFIER"), TouchHandler {
    override fun write(buffer: WireBuffer) { Companion.apply(buffer) }
    override fun toString(): String = "TouchDownModifier"
    override fun onTouchDown(context: RemoteContext, document: CoreDocument, component: Component, x: Float, y: Float) {
        if (applyActions(context, document, component, x, y, false)) document.appliedTouchOperation(component)
    }
    override fun onTouchUp(context: RemoteContext, document: CoreDocument, component: Component, x: Float, y: Float, dx: Float, dy: Float) {}
    override fun onTouchCancel(context: RemoteContext, document: CoreDocument, component: Component, x: Float, y: Float) {}
    override fun onTouchDrag(context: RemoteContext, document: CoreDocument, component: Component, x: Float, y: Float) {}

    companion object {
        fun name(): String = "TouchModifier"
        fun apply(buffer: WireBuffer) { buffer.start(Operations.MODIFIER_TOUCH_DOWN) }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { operations.add(TouchDownModifierOperation()) }
    }
}
