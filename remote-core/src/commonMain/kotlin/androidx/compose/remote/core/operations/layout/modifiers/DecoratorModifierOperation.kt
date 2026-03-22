package androidx.compose.remote.core.operations.layout.modifiers

import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.PaintOperation
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.layout.Component
import androidx.compose.remote.core.operations.layout.DecoratorComponent
import androidx.compose.remote.core.operations.utilities.StringSerializer

abstract class DecoratorModifierOperation : PaintOperation(), ModifierOperation, DecoratorComponent {
    protected var mWidth: Float = 0f
    protected var mHeight: Float = 0f

    override fun layout(context: RemoteContext, component: Component, width: Float, height: Float) {
        mWidth = width
        mHeight = height
    }

    override fun deepToString(indent: String): String = indent + toString()
}
