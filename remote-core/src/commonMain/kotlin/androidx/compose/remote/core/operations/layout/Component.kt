package androidx.compose.remote.core.operations.layout

import androidx.compose.remote.core.CoreDocument
import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.PaintOperation
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.TouchListener
import androidx.compose.remote.core.VariableSupport
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.BitmapData
import androidx.compose.remote.core.operations.ComponentData
import androidx.compose.remote.core.operations.ComponentValue
import androidx.compose.remote.core.operations.Container
import androidx.compose.remote.core.operations.TextData
import androidx.compose.remote.core.operations.TouchExpression
import androidx.compose.remote.core.operations.layout.animation.AnimateMeasure
import androidx.compose.remote.core.operations.layout.animation.AnimationSpec
import androidx.compose.remote.core.operations.layout.measure.ComponentMeasure
import androidx.compose.remote.core.operations.layout.measure.Measurable
import androidx.compose.remote.core.operations.layout.measure.MeasurePass
import androidx.compose.remote.core.operations.paint.PaintBundle
import androidx.compose.remote.core.operations.utilities.StringSerializer

open class Component(
    var mParent: Component?,
    var componentId: Int,
    var mAnimationId: Int,
    var x: Float,
    var y: Float,
    var width: Float,
    var height: Float
) : PaintOperation(), Container, Measurable {

    companion object {
        private const val DEBUG = false
    }

    var mVisibility: Int = Visibility.VISIBLE
    var mScheduledVisibility: Int = Visibility.VISIBLE
    var mList: ArrayList<Operation> = ArrayList()
    var mPreTranslate: PaintOperation? = null
    var mNeedsMeasure: Boolean = true
    var mNeedsRepaint: Boolean = false
    var mAnimateMeasure: AnimateMeasure? = null
    var mAnimationSpec: AnimationSpec = AnimationSpec.DEFAULT
    var mFirstLayout: Boolean = true
    val mPaint: PaintBundle = PaintBundle()
    protected val mComponentValues: HashSet<ComponentValue> = HashSet()
    protected var mZIndex: Float = 0f
    private var mNeedsBoundsAnimation: Boolean = false

    var locationInWindow: FloatArray = FloatArray(2)

    fun markNeedsBoundsAnimation() {
        mNeedsBoundsAnimation = true
        if (mParent != null && !mParent!!.mNeedsBoundsAnimation) {
            mParent!!.markNeedsBoundsAnimation()
        }
    }

    fun clearNeedsBoundsAnimation() { mNeedsBoundsAnimation = false }
    fun needsBoundsAnimation(): Boolean = mNeedsBoundsAnimation
    open fun getZIndex(): Float = mZIndex

    override fun getList(): ArrayList<Operation> = mList

    fun getAnimationId(): Int = mAnimationId

    override fun apply(context: RemoteContext) {
        for (op in mList) {
            if (op is VariableSupport && op.isDirty()) {
                op.markNotDirty()
                (op as VariableSupport).updateVariables(context)
            }
        }
        super.apply(context)
    }

    protected fun updateComponentValues(context: RemoteContext) {
        for (v in mComponentValues) {
            if (context.getMode() == RemoteContext.ContextMode.DATA) {
                context.loadFloat(v.getValueId(), 1f)
            } else {
                when (v.getType()) {
                    ComponentValue.WIDTH -> context.loadFloat(v.getValueId(), width)
                    ComponentValue.HEIGHT -> context.loadFloat(v.getValueId(), height)
                }
            }
        }
    }

    fun setAnimationId(id: Int) { mAnimationId = id }

    constructor(parent: Component?, componentId: Int, animationId: Int, x: Float, y: Float, width: Float, height: Float, dummy: Boolean = false)
        : this(parent, componentId, animationId, x, y, width, height)

    constructor(component: Component) : this(
        component.mParent, component.componentId, component.mAnimationId,
        component.x, component.y, component.width, component.height
    ) {
        mList.addAll(component.mList)
        finalizeCreation()
    }

    open fun finalizeCreation() {
        for (op in mList) {
            if (op is Component) op.mParent = this
            if (op is AnimationSpec) {
                mAnimationSpec = op
                mAnimationId = mAnimationSpec.animationId
            }
        }
    }

    override fun needsMeasure(): Boolean = mNeedsMeasure

    fun setParent(parent: Component?) { mParent = parent }

    open fun updateVariables(context: RemoteContext) {
        val prev = context.mLastComponent
        context.mLastComponent = this
        if (mComponentValues.isNotEmpty()) updateComponentValues(context)
        context.mLastComponent = prev
    }

    fun addComponentValue(v: ComponentValue) { mComponentValues.add(v) }

    open fun minIntrinsicWidth(context: RemoteContext?): Float = width
    open fun maxIntrinsicWidth(context: RemoteContext?): Float = width
    open fun minIntrinsicHeight(context: RemoteContext?): Float = height
    open fun maxIntrinsicHeight(context: RemoteContext?): Float = height

    open fun inflate() {
        for (op in mList) {
            if (op is TouchListener) op.setComponent(this)
        }
    }

    fun getAnimationSpec(): AnimationSpec = mAnimationSpec
    fun setAnimationSpec(spec: AnimationSpec) { mAnimationSpec = spec }

    open fun registerVariables(context: RemoteContext) {}

    open fun getAlignValue(context: PaintContext, line: Float): Float = 0f
    open fun hasComputedLayout(): Boolean = false
    open fun applyComputedLayout(type: Int, context: PaintContext, m: ComponentMeasure, parent: ComponentMeasure): Boolean = false

    object Visibility {
        const val GONE = 0
        const val VISIBLE = 1
        const val INVISIBLE = 2
        const val OVERRIDE_GONE = 16
        const val OVERRIDE_VISIBLE = 32
        const val OVERRIDE_INVISIBLE = 64
        const val CLEAR_OVERRIDE = 128

        fun toString(value: Int): String = when {
            value == GONE -> "GONE"
            value == VISIBLE -> "VISIBLE"
            value == INVISIBLE -> "INVISIBLE"
            (value shr 4) > 0 -> when {
                (value and OVERRIDE_GONE) == OVERRIDE_GONE -> "OVERRIDE_GONE"
                (value and OVERRIDE_VISIBLE) == OVERRIDE_VISIBLE -> "OVERRIDE_VISIBLE"
                (value and OVERRIDE_INVISIBLE) == OVERRIDE_INVISIBLE -> "OVERRIDE_INVISIBLE"
                else -> "$value"
            }
            else -> "$value"
        }

        fun isGone(value: Int): Boolean =
            if ((value shr 4) > 0) (value and OVERRIDE_GONE) == OVERRIDE_GONE else value == GONE

        fun isVisible(value: Int): Boolean =
            if ((value shr 4) > 0) (value and OVERRIDE_VISIBLE) == OVERRIDE_VISIBLE else value == VISIBLE

        fun isInvisible(value: Int): Boolean =
            if ((value shr 4) > 0) (value and OVERRIDE_INVISIBLE) == OVERRIDE_INVISIBLE else value == INVISIBLE

        fun hasOverride(value: Int): Boolean = (value shr 4) > 0
        fun clearOverride(value: Int): Int = value and 15
        fun add(value: Int, visibility: Int): Int {
            var v = (value and 15) + visibility
            if ((v and CLEAR_OVERRIDE) == CLEAR_OVERRIDE) v = v and 15
            return v
        }
    }

    fun isVisible(): Boolean {
        if (mParent == null || !Visibility.isVisible(mVisibility)) return Visibility.isVisible(mVisibility)
        return mParent!!.isVisible()
    }
    fun isGone(): Boolean = Visibility.isGone(mVisibility)
    fun isInvisible(): Boolean = Visibility.isInvisible(mVisibility)

    open fun setVisibility(visibility: Int) {
        if (visibility != mVisibility || visibility != mScheduledVisibility) {
            mScheduledVisibility = visibility
            invalidateMeasure()
        }
    }

    override fun suitableForTransition(o: Operation): Boolean {
        if (o !is Component) return false
        if (mList.size != o.mList.size) return false
        for (i in mList.indices) {
            val o1 = mList[i]; val o2 = o.mList[i]
            if (o1 is Component && o2 is Component && !o1.suitableForTransition(o2)) return false
            if (o1 is PaintOperation && !o1.suitableForTransition(o2)) return false
        }
        return true
    }

    override fun measure(context: PaintContext, minWidth: Float, maxWidth: Float, minHeight: Float, maxHeight: Float, measure: MeasurePass) {
        val m = measure.get(this)
        m.w = width; m.h = height
    }

    override fun layout(context: RemoteContext, measure: MeasurePass) {
        val m = measure.get(this)
        if (!mFirstLayout && context.isAnimationEnabled() && mAnimationSpec.isAnimationEnabled()
            && m.allowsAnimation && this !is LayoutComponentContent) {
            if (mAnimateMeasure == null) {
                val origin = ComponentMeasure(componentId, x, y, width, height, mVisibility)
                val target = ComponentMeasure(componentId, m.x, m.y, m.w, m.h, m.visibility)
                if (!target.same(origin)) {
                    mAnimateMeasure = AnimateMeasure(
                        context.currentTime, this, origin, target,
                        mAnimationSpec.motionDuration, mAnimationSpec.visibilityDuration,
                        mAnimationSpec.enterAnimation, mAnimationSpec.exitAnimation,
                        mAnimationSpec.motionEasingType, mAnimationSpec.visibilityEasingType
                    )
                }
            } else {
                mAnimateMeasure!!.updateTarget(m, context.currentTime)
            }
        } else {
            mVisibility = m.visibility
        }
        if (mAnimateMeasure == null) {
            width = m.w; height = m.h
            setLayoutPosition(m.x, m.y)
            updateComponentValues(context)
            clearNeedsBoundsAnimation()
        } else {
            mAnimateMeasure!!.apply(context)
            updateComponentValues(context)
            markNeedsBoundsAnimation()
        }
        mFirstLayout = false
    }

    override fun animatingBounds(context: RemoteContext) {
        if (mAnimateMeasure != null) {
            mAnimateMeasure!!.apply(context)
            updateComponentValues(context)
        } else {
            clearNeedsBoundsAnimation()
        }
        for (op in mList) {
            if (op is Measurable) op.animatingBounds(context)
        }
    }

    fun contains(cx: Float, cy: Float): Boolean {
        locationInWindow[0] = 0f; locationInWindow[1] = 0f
        getLocationInWindow(locationInWindow)
        val lx1 = locationInWindow[0]; val lx2 = lx1 + width
        val ly1 = locationInWindow[1]; val ly2 = ly1 + height
        return cx >= lx1 && cx < lx2 && cy >= ly1 && cy < ly2
    }

    open fun getScrollX(): Float = 0f
    open fun getScrollY(): Float = 0f

    open fun onClick(context: RemoteContext, document: CoreDocument, cx: Float, cy: Float) {
        val isUnconditional = cx == -1f && cy == -1f
        if (!isUnconditional && !contains(cx, cy)) return
        val sx = if (isUnconditional) -1f else cx - getScrollX()
        val sy = if (isUnconditional) -1f else cy - getScrollY()
        for (op in mList) {
            if (op is Component) op.onClick(context, document, sx, sy)
            if (op is ClickHandler) op.onClick(context, document, this, sx, sy)
        }
    }

    open fun onTouchDown(context: RemoteContext, document: CoreDocument, cx: Float, cy: Float) {
        if (!contains(cx, cy)) return
        val sx = cx - getScrollX(); val sy = cy - getScrollY()
        for (op in mList) {
            if (op is Component) op.onTouchDown(context, document, sx, sy)
            if (op is TouchHandler) op.onTouchDown(context, document, this, sx, sy)
            if (op is TouchExpression) { op.updateVariables(context); op.touchDown(context, sx, sy); document.appliedTouchOperation(this) }
        }
    }

    open fun onTouchUp(context: RemoteContext, document: CoreDocument, cx: Float, cy: Float, dx: Float, dy: Float, force: Boolean) {
        if (!force && !contains(cx, cy)) return
        val sx = cx - getScrollX(); val sy = cy - getScrollY()
        for (op in mList) {
            if (op is Component) op.onTouchUp(context, document, sx, sy, dx, dy, force)
            if (op is TouchHandler) op.onTouchUp(context, document, this, sx, sy, dx, dy)
            if (op is TouchExpression) { op.updateVariables(context); op.touchUp(context, sx, sy, dx, dy) }
        }
    }

    open fun onTouchCancel(context: RemoteContext, document: CoreDocument, cx: Float, cy: Float, force: Boolean) {
        if (!force && !contains(cx, cy)) return
        val sx = cx - getScrollX(); val sy = cy - getScrollY()
        for (op in mList) {
            if (op is Component) op.onTouchCancel(context, document, sx, sy, force)
            if (op is TouchHandler) op.onTouchCancel(context, document, this, sx, sy)
            if (op is TouchExpression) { op.updateVariables(context); op.touchUp(context, sx, sy, 0f, 0f) }
        }
    }

    open fun onTouchDrag(context: RemoteContext, document: CoreDocument, cx: Float, cy: Float, force: Boolean) {
        if (!force && !contains(cx, cy)) return
        val sx = cx - getScrollX(); val sy = cy - getScrollY()
        for (op in mList) {
            if (op is Component) op.onTouchDrag(context, document, sx, sy, force)
            if (op is TouchHandler) op.onTouchDrag(context, document, this, sx, sy)
            if (op is TouchExpression) { op.updateVariables(context); op.touchDrag(context, cx, cy) }
        }
    }

    open fun getLocationInWindow(value: FloatArray, forSelf: Boolean = true) {
        value[0] += x; value[1] += y
        mParent?.getLocationInWindow(value, false)
    }

    fun getLocationInWindow(value: FloatArray) { getLocationInWindow(value, true) }

    override fun toString(): String =
        "COMPONENT(<$componentId> ${this::class.simpleName}) [$x,$y - $width x $height] ${textContent()} Visibility (${Visibility.toString(mVisibility)}) "

    protected open fun getSerializedName(): String = "COMPONENT"

    fun serializeToString(indent: Int, serializer: StringSerializer) {
        serializer.append(indent, "${getSerializedName()} [$componentId:$mAnimationId] = [$x, $y, $width, $height] ${Visibility.toString(mVisibility)}")
    }

    override fun write(buffer: WireBuffer) {}

    fun getRoot(): RootLayoutComponent {
        if (this is RootLayoutComponent) return this
        var p = mParent
        while (p !is RootLayoutComponent) {
            if (p == null) throw Exception("No RootLayoutComponent found")
            p = p.mParent
        }
        return p
    }

    override fun deepToString(indent: String): String {
        val builder = StringBuilder()
        builder.append(indent).append(toString()).append("\n")
        val indent2 = "  $indent"
        for (op in mList) { builder.append(op.deepToString(indent2)).append("\n") }
        return builder.toString()
    }

    fun invalidateMeasure() {
        needsRepaint()
        mNeedsMeasure = true
        var p = mParent
        while (p != null) { p.mNeedsMeasure = true; p = p.mParent }
    }

    fun needsRepaint() {
        try { getRoot().mNeedsRepaint = true } catch (_: Exception) {}
    }

    fun content(): String {
        val builder = StringBuilder()
        for (op in mList) { builder.append("- ").append(op).append("\n") }
        return builder.toString()
    }

    fun textContent(): String = ""

    fun debugBox(component: Component, context: PaintContext) {
        context.savePaint()
        mPaint.reset()
        mPaint.setColor(0, 0, 255, 255)
        context.applyPaint(mPaint)
        context.drawLine(0f, 0f, component.width, 0f)
        context.drawLine(component.width, 0f, component.width, component.height)
        context.drawLine(component.width, component.height, 0f, component.height)
        context.drawLine(0f, component.height, 0f, 0f)
        context.restorePaint()
    }

    fun setLayoutPosition(newX: Float, newY: Float) { x = newX; y = newY }
    fun getTranslateX(): Float = if (mParent != null) x - mParent!!.x else 0f
    fun getTranslateY(): Float = if (mParent != null) y - mParent!!.y else 0f

    open fun paintingComponent(context: PaintContext) {
        mPreTranslate?.paint(context)
        val prev = context.getContext().mLastComponent
        context.getContext().mLastComponent = this
        context.save()
        context.translate(x, y)
        if (context.isVisualDebug()) debugBox(this, context)
        for (op in mList) {
            if (op.isDirty() && op is VariableSupport) { (op as VariableSupport).updateVariables(context.getContext()); op.markNotDirty() }
            if (op is PaintOperation) { op.paint(context); context.getContext().incrementOpCount() }
            else { op.apply(context.getContext()); context.getContext().incrementOpCount() }
        }
        context.restore()
        context.getContext().mLastComponent = prev
    }

    fun applyAnimationAsNeeded(context: PaintContext): Boolean {
        if (context.isAnimationEnabled() && mAnimateMeasure != null) {
            mAnimateMeasure!!.paint(context)
            if (mAnimateMeasure!!.isDone()) { mAnimateMeasure = null; clearNeedsBoundsAnimation(); needsRepaint() }
            else markNeedsBoundsAnimation()
            return true
        }
        return false
    }

    override fun paint(context: PaintContext) {
        if (applyAnimationAsNeeded(context)) return
        if (isGone() || isInvisible()) return
        paintingComponent(context)
    }

    fun getComponents(components: ArrayList<Component>) {
        for (op in mList) { if (op is Component) components.add(op) }
    }

    fun getData(data: ArrayList<Operation>) {
        for (op in mList) { if (op is TextData || op is BitmapData || op is ComponentData) data.add(op) }
    }

    fun getComponentCount(): Int {
        var count = 0
        for (op in mList) { if (op is Component) count += 1 + op.getComponentCount() }
        return count
    }

    fun getPaintId(): Int = if (mAnimationId != -1) mAnimationId else componentId
    fun doesNeedsRepaint(): Boolean = mNeedsRepaint

    fun getComponent(cid: Int): Component? {
        if (componentId == cid || mAnimationId == cid) return this
        for (c in mList) {
            if (c is Component) { val s = c.getComponent(cid); if (s != null) return s }
        }
        return null
    }

    open fun <T> selfOrModifier(operationClass: kotlin.reflect.KClass<T>): T? where T : Any {
        if (operationClass.isInstance(this)) @Suppress("UNCHECKED_CAST") return this as T
        for (op in mList) {
            if (operationClass.isInstance(op)) @Suppress("UNCHECKED_CAST") return op as T
        }
        return null
    }
}
