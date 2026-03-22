package androidx.compose.remote.core.operations.layout

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.OperationInterface
import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.TouchListener
import androidx.compose.remote.core.VariableSupport
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.BitmapData
import androidx.compose.remote.core.operations.ComponentData
import androidx.compose.remote.core.operations.ComponentValue
import androidx.compose.remote.core.operations.MatrixRestore
import androidx.compose.remote.core.operations.MatrixSave
import androidx.compose.remote.core.operations.MatrixTranslate
import androidx.compose.remote.core.operations.layout.animation.AnimationSpec
import androidx.compose.remote.core.operations.layout.managers.CanvasLayout
import androidx.compose.remote.core.operations.layout.measure.ComponentMeasure
import androidx.compose.remote.core.operations.layout.modifiers.*

open class LayoutComponent(
    parent: Component?,
    componentId: Int,
    animationId: Int,
    x: Float,
    y: Float,
    width: Float,
    height: Float
) : Component(parent, componentId, animationId, x, y, width, height) {

    var mWidthModifier: WidthModifierOperation? = null
    var mHeightModifier: HeightModifierOperation? = null
    var mZIndexModifier: ZIndexModifierOperation? = null
    var mGraphicsLayerModifier: GraphicsLayerModifierOperation? = null

    var mPaddingLeft: Float = 0f
    var mPaddingRight: Float = 0f
    var mPaddingTop: Float = 0f
    var mPaddingBottom: Float = 0f

    var mScrollX: Float = 0f
    var mScrollY: Float = 0f

    var mHorizontalScrollDelegate: ScrollDelegate? = null
    var mVerticalScrollDelegate: ScrollDelegate? = null

    var mComponentModifiers: ComponentModifiers = ComponentModifiers()
    var mChildrenComponents: ArrayList<Component> = ArrayList()
    var mChildrenHaveZIndex: Boolean = false
    private var mDrawContentOperations: CanvasOperations? = null
    var mContent: LayoutComponentContent? = null
    var mComputedLayoutModifiers: ArrayList<LayoutComputeOperation>? = null

    protected val mCachedAttributes: HashMap<Int, Any> = HashMap()

    fun getWidthModifier(): WidthModifierOperation? = mWidthModifier
    fun getHeightModifier(): HeightModifierOperation? = mHeightModifier

    override fun getZIndex(): Float = mZIndexModifier?.getValue() ?: mZIndex

    override fun hasComputedLayout(): Boolean = mComputedLayoutModifiers != null
    override fun applyComputedLayout(type: Int, context: PaintContext, m: ComponentMeasure, parent: ComponentMeasure): Boolean {
        val modifiers = mComputedLayoutModifiers ?: return false
        var needsMeasure = false
        for (modifier in modifiers) {
            if (modifier.getType() == type) needsMeasure = needsMeasure or modifier.applyToMeasure(context, m, parent)
        }
        return needsMeasure
    }

    fun setCanvasOperations(operations: CanvasOperations?) { mDrawContentOperations = operations }

    override fun inflate() {
        val data = ArrayList<Operation>()
        val supportedOperations = ArrayList<Operation>()

        for (op in mList) {
            if (op is LayoutComponentContent) {
                mContent = op
                mContent!!.mParent = this
                mChildrenComponents.clear()
                op.getComponents(mChildrenComponents)
                if (mChildrenComponents.isEmpty() && mContent!!.mList.isNotEmpty()) {
                    val canvasContent = CanvasContent(-1, 0f, 0f, 0f, 0f, this, -1)
                    for (opc in mContent!!.mList) {
                        if (opc is BitmapData) {
                            canvasContent.mList.add(opc)
                            canvasContent.width = opc.getWidth().toFloat()
                            canvasContent.height = opc.getHeight().toFloat()
                        } else if (opc !is MatrixTranslate && opc !is MatrixSave && opc !is MatrixRestore) {
                            canvasContent.mList.add(opc)
                        }
                    }
                    if (canvasContent.mList.isNotEmpty()) {
                        mContent!!.mList.clear()
                        mChildrenComponents.add(canvasContent)
                        canvasContent.inflate()
                    }
                } else {
                    op.getData(data)
                }
            } else if (op is ModifierOperation) {
                if (op is ComponentVisibilityOperation) op.setParent(this)
                if (op is AlignByModifierOperation) op.setParent(this)
                if (op is LayoutComputeOperation) {
                    if (mComputedLayoutModifiers == null) mComputedLayoutModifiers = ArrayList()
                    op.setParent(this)
                    mComputedLayoutModifiers!!.add(op)
                }
                if (op is ScrollModifierOperation) op.inflate(this)
                mComponentModifiers.add(op)
            } else if (op is ComponentData) {
                supportedOperations.add(op)
                if (op is TouchListener) op.setComponent(this)
                if (op is LayoutComputeOperation) op.setParent(this)
            }
        }

        mList.clear()
        mList.addAll(data)
        mList.addAll(supportedOperations)
        for (op in mList) {
            if (op is ComponentValue) op.setComponentId(componentId)
        }
        mList.add(mComponentModifiers)
        for (c in mChildrenComponents) {
            c.mParent = this
            mList.add(c)
            if (c is LayoutComponent && c.mZIndexModifier != null) mChildrenHaveZIndex = true
        }

        x = 0f; y = 0f
        mPaddingLeft = 0f; mPaddingTop = 0f; mPaddingRight = 0f; mPaddingBottom = 0f

        var widthInConstraints: WidthInModifierOperation? = null
        var heightInConstraints: HeightInModifierOperation? = null

        for (op in mComponentModifiers.getList()) {
            when (op) {
                is PaddingModifierOperation -> {
                    mPaddingLeft += op.getLeft(); mPaddingTop += op.getTop()
                    mPaddingRight += op.getRight(); mPaddingBottom += op.getBottom()
                }
                is WidthModifierOperation -> if (mWidthModifier == null) mWidthModifier = op
                is HeightModifierOperation -> if (mHeightModifier == null) mHeightModifier = op
                is WidthInModifierOperation -> widthInConstraints = op
                is HeightInModifierOperation -> heightInConstraints = op
                is ZIndexModifierOperation -> mZIndexModifier = op
                is GraphicsLayerModifierOperation -> mGraphicsLayerModifier = op
                is AnimationSpec -> mAnimationSpec = op
                is ScrollDelegate -> {
                    val sd = op
                    if (sd.handlesHorizontalScroll()) mHorizontalScrollDelegate = sd
                    if (sd.handlesVerticalScroll()) mVerticalScrollDelegate = sd
                }
            }
        }

        if (mWidthModifier == null) mWidthModifier = WidthModifierOperation(DimensionModifierOperation.Type.WRAP)
        if (mHeightModifier == null) mHeightModifier = HeightModifierOperation(DimensionModifierOperation.Type.WRAP)
        if (widthInConstraints != null) mWidthModifier!!.setWidthIn(widthInConstraints)
        if (heightInConstraints != null) mHeightModifier!!.setHeightIn(heightInConstraints)

        if (mAnimationSpec !== AnimationSpec.DEFAULT) {
            for (c in mChildrenComponents) {
                if (c.getAnimationSpec() === AnimationSpec.DEFAULT) c.setAnimationSpec(mAnimationSpec)
            }
        }

        width = computeModifierDefinedWidth(null)
        height = computeModifierDefinedHeight(null)
    }

    override fun toString(): String = "UNKNOWN LAYOUT_COMPONENT"

    override fun getLocationInWindow(value: FloatArray, forSelf: Boolean) {
        value[0] += x + mPaddingLeft; value[1] += y + mPaddingTop
        mParent?.getLocationInWindow(value, false)
    }

    override fun getScrollX(): Float =
        mHorizontalScrollDelegate?.getScrollX(mScrollX) ?: mScrollX

    fun setScrollX(value: Float) { mScrollX = value }

    override fun getScrollY(): Float =
        mVerticalScrollDelegate?.getScrollY(mScrollY) ?: mScrollY

    fun setScrollY(value: Float) { mScrollY = value }

    override fun paint(context: PaintContext) {
        if (mDrawContentOperations != null) {
            context.save(); context.translate(x, y)
            mDrawContentOperations!!.paint(context)
            context.restore(); return
        }
        super.paint(context)
    }

    fun drawContent(context: PaintContext) {
        context.save(); context.translate(-x, -y)
        paintingComponent(context)
        context.restore()
    }

    override fun paintingComponent(context: PaintContext) {
        val prev = context.getContext().mLastComponent
        val rc = context.getContext()
        rc.mLastComponent = this
        context.save(); context.translate(x, y)
        if (context.isVisualDebug()) debugBox(this, context)
        if (mGraphicsLayerModifier != null) {
            context.startGraphicsLayer(width.toInt(), height.toInt())
            mCachedAttributes.clear()
            mGraphicsLayerModifier!!.fillInAttributes(mCachedAttributes)
            context.setGraphicsLayer(mCachedAttributes)
        }
        if (this !is CanvasLayout) {
            for (op in mList) {
                if (op is ComponentModifiers) continue
                if (op !is ComponentData) continue
                if (op is VariableSupport && (op as Operation).isDirty()) {
                    (op as VariableSupport).updateVariables(rc)
                }
                (op as Operation).apply(rc)
            }
        }
        mComponentModifiers.paint(context)
        val tx = mPaddingLeft + getScrollX()
        val ty = mPaddingTop + getScrollY()
        context.translate(tx, ty)
        if (mChildrenHaveZIndex) {
            val sorted = ArrayList(mChildrenComponents)
            sorted.sortBy { it.getZIndex() }
            for (child in sorted) {
                if (child.isDirty() && child is VariableSupport) { child.updateVariables(rc); child.markNotDirty() }
                rc.incrementOpCount(); child.paint(context)
            }
        } else {
            for (child in mChildrenComponents) {
                if (child.isDirty() && child is VariableSupport) { child.updateVariables(rc); child.markNotDirty() }
                rc.incrementOpCount(); child.paint(context)
            }
        }
        if (mGraphicsLayerModifier != null) context.endGraphicsLayer()
        context.translate(-tx, -ty); context.restore()
        rc.mLastComponent = prev
    }

    fun computeModifierDefinedWidth(context: RemoteContext?): Float {
        var s = 0f; var e = 0f; var w = 0f
        for (c in mComponentModifiers.getList()) {
            if (context != null && (c as OperationInterface).isDirty() && c is VariableSupport) {
                c.updateVariables(context); (c as OperationInterface).markNotDirty()
            }
            if (c is WidthModifierOperation) {
                if (c.getType() == DimensionModifierOperation.Type.EXACT || c.getType() == DimensionModifierOperation.Type.EXACT_DP) w = c.getValue()
                break
            }
            if (c is PaddingModifierOperation) { s += c.getLeft(); e += c.getRight() }
        }
        return s + w + e
    }

    fun computeModifierDefinedPaddingWidth(padding: FloatArray): Float {
        var s = 0f; var e = 0f
        for (c in mComponentModifiers.getList()) {
            if (c is PaddingModifierOperation) { s += c.getLeft(); e += c.getRight() }
        }
        padding[0] = s; padding[1] = e; return s + e
    }

    fun computeModifierDefinedHeight(context: RemoteContext?): Float {
        var t = 0f; var b = 0f; var h = 0f
        for (c in mComponentModifiers.getList()) {
            if (context != null && (c as OperationInterface).isDirty() && c is VariableSupport) {
                c.updateVariables(context); (c as OperationInterface).markNotDirty()
            }
            if (c is HeightModifierOperation) {
                if (c.getType() == DimensionModifierOperation.Type.EXACT || c.getType() == DimensionModifierOperation.Type.EXACT_DP) h = c.getValue()
                break
            }
            if (c is PaddingModifierOperation) { t += c.getTop(); b += c.getBottom() }
        }
        return t + h + b
    }

    fun computeModifierDefinedPaddingHeight(padding: FloatArray): Float {
        var t = 0f; var b = 0f
        for (c in mComponentModifiers.getList()) {
            if (c is PaddingModifierOperation) { t += c.getTop(); b += c.getBottom() }
        }
        padding[0] = t; padding[1] = b; return t + b
    }

    fun getComponentModifiers(): ComponentModifiers = mComponentModifiers
    fun getChildrenComponents(): ArrayList<Component> = mChildrenComponents

    override fun <T : Any> selfOrModifier(operationClass: kotlin.reflect.KClass<T>): T? {
        if (operationClass.isInstance(this)) @Suppress("UNCHECKED_CAST") return this as T
        for (op in mComponentModifiers.getList()) {
            if (operationClass.isInstance(op)) @Suppress("UNCHECKED_CAST") return op as T
        }
        return null
    }

    override fun registerVariables(context: RemoteContext) {
        mDrawContentOperations?.registerListening(context)
        for (op in mList) {
            if (op is VariableSupport) op.registerListening(context)
            if (op is ComponentValue) addComponentValue(op)
        }
    }
}
