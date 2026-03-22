package androidx.compose.remote.core.operations.layout.managers

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.layout.Component
import androidx.compose.remote.core.operations.layout.measure.ComponentMeasure
import androidx.compose.remote.core.operations.layout.measure.MeasurePass
import androidx.compose.remote.core.operations.layout.measure.Size
import androidx.compose.remote.core.operations.layout.modifiers.LayoutComputeOperation
import kotlin.math.max

open class BoxLayout(
    parent: Component?, componentId: Int, animationId: Int,
    x: Float, y: Float, width: Float, height: Float,
    var mHorizontalPositioning: Int, var mVerticalPositioning: Int
) : LayoutManager(parent, componentId, animationId, x, y, width, height) {
    constructor(parent: Component?, componentId: Int, animationId: Int, hp: Int, vp: Int)
        : this(parent, componentId, animationId, 0f, 0f, 0f, 0f, hp, vp)
    override fun toString(): String = "BOX [$componentId:$mAnimationId] ($x, $y - $width x $height) $mVisibility"
    override fun getSerializedName(): String = "BOX"
    override fun computeWrapSize(context: PaintContext, minWidth: Float, maxWidth: Float, minHeight: Float, maxHeight: Float,
        horizontalWrap: Boolean, verticalWrap: Boolean, measure: MeasurePass, size: Size) {
        val parent = measure.get(this)
        for (c in mChildrenComponents) { c.measure(context, 0f, maxWidth, 0f, maxHeight, measure); val m = measure.get(c)
            if (c.hasComputedLayout() && c.applyComputedLayout(LayoutComputeOperation.TYPE_MEASURE, context, m, parent)) c.measure(context, m.w, m.w, m.h, m.h, measure)
            if (!m.isGone()) { size.width = max(size.width, m.w); size.height = max(size.height, m.h) } }
    }
    override fun computeSize(context: PaintContext, minWidth: Float, maxWidth: Float, minHeight: Float, maxHeight: Float, measure: MeasurePass) {
        val parent = measure.get(this)
        for (child in mChildrenComponents) { child.measure(context, minWidth, maxWidth, minHeight, maxHeight, measure)
            if (child.hasComputedLayout()) { val m = measure.get(child); if (child.applyComputedLayout(LayoutComputeOperation.TYPE_MEASURE, context, m, parent)) child.measure(context, m.w, m.w, m.h, m.h, measure) } }
    }
    override fun internalLayoutMeasure(context: PaintContext, measure: MeasurePass) {
        val sm = measure.get(this); val sw = sm.w - mPaddingLeft - mPaddingRight; val sh = sm.h - mPaddingTop - mPaddingBottom
        for (child in mChildrenComponents) { val m = measure.get(child)
            m.y = when (mVerticalPositioning) { TOP -> 0f; CENTER -> (sh - m.h) / 2f; BOTTOM -> sh - m.h; else -> 0f }
            m.x = when (mHorizontalPositioning) { START -> 0f; CENTER -> (sw - m.w) / 2f; END -> sw - m.w; else -> 0f }
            if (child.hasComputedLayout()) child.applyComputedLayout(LayoutComputeOperation.TYPE_POSITION, context, m, sm) }
    }
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, componentId, mAnimationId, mHorizontalPositioning, mVerticalPositioning) }
    companion object {
        const val START = 1; const val CENTER = 2; const val END = 3; const val TOP = 4; const val BOTTOM = 5
        fun name(): String = "BoxLayout"; fun id(): Int = Operations.LAYOUT_BOX
        fun apply(buffer: WireBuffer, cid: Int, aid: Int, hp: Int, vp: Int) { buffer.start(Operations.LAYOUT_BOX); buffer.writeInt(cid); buffer.writeInt(aid); buffer.writeInt(hp); buffer.writeInt(vp) }
        fun read(buffer: WireBuffer, ops: MutableList<Operation>) { ops.add(BoxLayout(null, buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt())) }
    }
}
