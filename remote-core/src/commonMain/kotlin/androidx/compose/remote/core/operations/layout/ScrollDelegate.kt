package androidx.compose.remote.core.operations.layout

interface ScrollDelegate {
    fun getScrollX(currentValue: Float): Float
    fun getScrollY(currentValue: Float): Float
    fun handlesHorizontalScroll(): Boolean
    fun handlesVerticalScroll(): Boolean
    fun reset()
}
