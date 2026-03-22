package androidx.compose.remote.core.operations.layout

import androidx.compose.remote.core.CoreDocument
import androidx.compose.remote.core.RemoteContext

interface TouchHandler : TouchOperation {
    fun onTouchDown(
        context: RemoteContext,
        document: CoreDocument,
        component: Component,
        x: Float,
        y: Float
    )

    fun onTouchUp(
        context: RemoteContext,
        document: CoreDocument,
        component: Component,
        x: Float,
        y: Float,
        dx: Float,
        dy: Float
    )

    fun onTouchDrag(
        context: RemoteContext,
        document: CoreDocument,
        component: Component,
        x: Float,
        y: Float
    )

    fun onTouchCancel(
        context: RemoteContext,
        document: CoreDocument,
        component: Component,
        x: Float,
        y: Float
    )
}
