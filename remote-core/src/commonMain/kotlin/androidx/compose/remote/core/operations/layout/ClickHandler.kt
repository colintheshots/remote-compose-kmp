package androidx.compose.remote.core.operations.layout

import androidx.compose.remote.core.CoreDocument
import androidx.compose.remote.core.RemoteContext

interface ClickHandler : TouchOperation {
    fun onClick(
        context: RemoteContext,
        document: CoreDocument,
        component: Component,
        x: Float,
        y: Float
    )
}
