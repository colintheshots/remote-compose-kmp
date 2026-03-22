package androidx.compose.remote.core.operations.layout.managers

import androidx.compose.remote.core.operations.layout.Component
import androidx.compose.remote.core.operations.layout.LayoutComponent
import androidx.compose.remote.core.operations.layout.modifiers.CollapsiblePriorityModifierOperation

object CollapsiblePriority {
    const val HORIZONTAL = 0; const val VERTICAL = 1
    fun getPriority(c: Component, orientation: Int): Float {
        if (c is LayoutComponent) { val p = c.selfOrModifier(CollapsiblePriorityModifierOperation::class); if (p != null && p.getOrientation() == orientation) return p.getPriority() }
        return Float.MAX_VALUE
    }
    fun sortWithPriorities(components: ArrayList<Component>, orientation: Int): ArrayList<Component> {
        val sorted = ArrayList(components); sorted.sortWith { t1, t2 -> (getPriority(t2, orientation) - getPriority(t1, orientation)).toInt() }; return sorted
    }
}
