package androidx.compose.remote.core.operations.layout

import androidx.compose.remote.core.CoreDocument
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.operations.utilities.StringSerializer

interface ActionOperation {
    fun serializeToString(indent: Int, serializer: StringSerializer)
    fun runAction(
        context: RemoteContext,
        document: CoreDocument,
        component: Component,
        x: Float,
        y: Float
    )
}
