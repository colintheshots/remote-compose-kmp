package androidx.compose.remote.core.operations.layout.modifiers

object ShapeType {
    const val RECTANGLE = 0
    const val ROUNDED_RECTANGLE = 1
    const val CIRCLE = 2
    const val OVAL = 3

    fun toString(type: Int): String = when (type) {
        RECTANGLE -> "RECTANGLE"
        ROUNDED_RECTANGLE -> "ROUNDED_RECTANGLE"
        CIRCLE -> "CIRCLE"
        OVAL -> "OVAL"
        else -> "UNKNOWN"
    }
}
