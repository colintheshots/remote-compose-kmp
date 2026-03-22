package androidx.compose.remote.core.operations.layout.measure

import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.RemoteContext

interface Measurable {
    fun measure(
        context: PaintContext,
        minWidth: Float,
        maxWidth: Float,
        minHeight: Float,
        maxHeight: Float,
        measure: MeasurePass
    )

    fun layout(context: RemoteContext, measure: MeasurePass)

    fun needsMeasure(): Boolean

    fun animatingBounds(context: RemoteContext)
}
