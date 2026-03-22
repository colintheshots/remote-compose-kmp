package androidx.compose.remote.core.operations.layout.modifiers

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.OperationInterface
import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.PaintOperation
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.VariableSupport
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.layout.Component
import androidx.compose.remote.core.operations.layout.DecoratorComponent
import androidx.compose.remote.core.operations.utilities.StringSerializer

class ComponentModifiers : PaintOperation(), ModifierOperation {

    private val mList: ArrayList<ModifierOperation> = ArrayList()
    private var mHasHorizontalScroll = false
    private var mHasVerticalScroll = false
    private var mHorizontalScrollDimension = 0f
    private var mVerticalScrollDimension = 0f

    fun getList(): ArrayList<ModifierOperation> = mList

    fun add(modifier: ModifierOperation) {
        mList.add(modifier)
    }

    fun hasHorizontalScroll(): Boolean = mHasHorizontalScroll
    fun hasVerticalScroll(): Boolean = mHasVerticalScroll
    fun getHorizontalScrollDimension(): Float = mHorizontalScrollDimension
    fun getVerticalScrollDimension(): Float = mVerticalScrollDimension

    fun setHorizontalScrollDimension(viewportWidth: Float, contentWidth: Float) {
        mHasHorizontalScroll = true
        mHorizontalScrollDimension = contentWidth
    }

    fun setVerticalScrollDimension(viewportHeight: Float, contentHeight: Float) {
        mHasVerticalScroll = true
        mVerticalScrollDimension = contentHeight
    }

    override fun paint(context: PaintContext) {
        for (op in mList) {
            if (op is VariableSupport && (op as OperationInterface).isDirty()) {
                (op as VariableSupport).updateVariables(context.getContext())
                (op as OperationInterface).markNotDirty()
            }
            if (op is PaintOperation) {
                (op as PaintOperation).paint(context)
            }
        }
    }

    fun layout(context: RemoteContext, component: Component, width: Float, height: Float) {
        for (op in mList) {
            if (op is DecoratorComponent) {
                (op as DecoratorComponent).layout(context, component, width, height)
            }
        }
    }

    override fun write(buffer: WireBuffer) {}
    override fun apply(context: RemoteContext) {}
    override fun toString(): String = "ComponentModifiers(${mList.size})"
    override fun deepToString(indent: String): String = indent + toString()

    override fun serializeToString(indent: Int, serializer: StringSerializer) {
        for (op in mList) {
            op.serializeToString(indent, serializer)
        }
    }
}
