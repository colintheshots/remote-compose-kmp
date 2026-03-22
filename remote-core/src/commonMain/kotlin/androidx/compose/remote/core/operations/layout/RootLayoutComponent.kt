package androidx.compose.remote.core.operations.layout

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.PaintOperation
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.SerializableToString
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.layout.measure.Measurable
import androidx.compose.remote.core.operations.layout.measure.MeasurePass
import androidx.compose.remote.core.operations.layout.modifiers.ComponentModifiers
import androidx.compose.remote.core.operations.utilities.StringSerializer

class RootLayoutComponent : Component {
    private var mCurrentId: Int = -1
    private var mHasTouchListeners: Boolean = false

    constructor(componentId: Int, x: Float, y: Float, width: Float, height: Float, parent: Component?, animationId: Int)
        : super(parent, componentId, animationId, x, y, width, height)
    constructor(componentId: Int, x: Float, y: Float, width: Float, height: Float, parent: Component?)
        : super(parent, componentId, -1, x, y, width, height)
    constructor(componentId: Int) : super(null, componentId, 0, -1f, 0f, 0f, 0f)

    override fun toString(): String =
        "ROOT $componentId ($x, $y - $width x $height) ${Visibility.toString(mVisibility)}"

    fun setHasTouchListeners(value: Boolean) { mHasTouchListeners = value }
    fun getHasTouchListeners(): Boolean = mHasTouchListeners

    fun assignIds(lastId: Int) { mCurrentId = lastId; assignId(this) }

    private fun assignId(component: Component) {
        if (component.componentId == -1) { mCurrentId--; component.componentId = mCurrentId }
        for (op in component.mList) { if (op is Component) assignId(op) }
    }

    fun layout(context: RemoteContext) {
        if (!mNeedsMeasure) return
        mNeedsMeasure = false
        context.mLastComponent = this
        width = context.mWidth; height = context.mHeight
        val measurePass = MeasurePass()
        for (op in mList) {
            if (op is Measurable) {
                op.measure(context.getPaintContext()!!, 0f, width, 0f, height, measurePass)
                op.layout(context, measurePass)
            }
        }
    }

    override fun paint(context: PaintContext) {
        mNeedsRepaint = false
        val rc = context.getContext()
        rc.mLastComponent = this
        context.save()
        if (mParent == null) context.clipRect(0f, 0f, width, height)
        for (op in mList) {
            if (op is PaintOperation) { op.paint(context); rc.incrementOpCount() }
        }
        context.restore()
    }

    fun displayHierarchy(): String {
        val serializer = StringSerializer()
        displayHierarchy(this, 0, serializer)
        return serializer.toString()
    }

    fun displayHierarchy(component: Component, indent: Int, serializer: StringSerializer) {
        component.serializeToString(indent, serializer)
        for (c in component.mList) {
            when (c) {
                is ComponentModifiers -> c.serializeToString(indent + 1, serializer)
                is Component -> displayHierarchy(c, indent + 1, serializer)
                is SerializableToString -> c.serializeToString(indent + 1, serializer)
            }
        }
    }

    override fun write(buffer: WireBuffer) { Companion.apply(buffer, componentId) }

    companion object {
        fun name(): String = "RootLayout"
        fun id(): Int = Operations.LAYOUT_ROOT
        fun apply(buffer: WireBuffer, componentId: Int) {
            buffer.start(Operations.LAYOUT_ROOT); buffer.writeInt(componentId)
        }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val componentId = buffer.readInt()
            operations.add(RootLayoutComponent(componentId, 0f, 0f, 0f, 0f, null, -1))
        }
    }
}
