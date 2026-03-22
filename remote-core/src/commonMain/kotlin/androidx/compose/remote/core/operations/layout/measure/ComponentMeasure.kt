package androidx.compose.remote.core.operations.layout.measure

import androidx.compose.remote.core.operations.layout.Component

class ComponentMeasure(
    var id: Int = -1,
    var x: Float = 0f,
    var y: Float = 0f,
    var w: Float = 0f,
    var h: Float = 0f,
    var visibility: Int = Component.Visibility.VISIBLE
) {
    var allowsAnimation: Boolean = true
        private set

    constructor(component: Component) : this(
        component.componentId,
        component.x,
        component.y,
        component.width,
        component.height,
        component.mVisibility
    )

    fun setAllowsAnimation(value: Boolean) {
        allowsAnimation = value
    }

    fun copyFrom(m: ComponentMeasure) {
        x = m.x
        y = m.y
        w = m.w
        h = m.h
        visibility = m.visibility
    }

    fun same(m: ComponentMeasure): Boolean {
        return x == m.x && y == m.y && w == m.w && h == m.h && visibility == m.visibility
    }

    fun isGone(): Boolean = Component.Visibility.isGone(visibility)
    fun isVisible(): Boolean = Component.Visibility.isVisible(visibility)
    fun isInvisible(): Boolean = Component.Visibility.isInvisible(visibility)

    fun clearVisibilityOverride() {
        visibility = Component.Visibility.clearOverride(visibility)
    }

    fun addVisibilityOverride(value: Int) {
        visibility = Component.Visibility.clearOverride(visibility)
        visibility = Component.Visibility.add(visibility, value)
    }
}
