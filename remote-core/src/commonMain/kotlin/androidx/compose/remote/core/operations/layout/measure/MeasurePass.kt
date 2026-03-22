package androidx.compose.remote.core.operations.layout.measure

import androidx.compose.remote.core.operations.layout.Component

class MeasurePass {
    val mList: HashMap<Int, ComponentMeasure> = HashMap()

    fun clear() {
        mList.clear()
    }

    fun add(measure: ComponentMeasure) {
        if (measure.id == -1) {
            throw Exception("Component has no id!")
        }
        mList[measure.id] = measure
    }

    fun contains(id: Int): Boolean = mList.containsKey(id)

    fun get(c: Component): ComponentMeasure {
        return mList.getOrPut(c.componentId) {
            ComponentMeasure(c.componentId, c.x, c.y, c.width, c.height)
        }
    }

    fun get(id: Int): ComponentMeasure {
        return mList.getOrPut(id) {
            ComponentMeasure(id, 0f, 0f, 0f, 0f, Component.Visibility.GONE)
        }
    }
}
