package androidx.compose.remote.core.operations.layout

import androidx.compose.remote.core.RemoteContext

interface DecoratorComponent {
    fun layout(context: RemoteContext, component: Component, width: Float, height: Float)
}
