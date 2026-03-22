package androidx.compose.remote.core.operations.layout.managers

import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.operations.layout.Component
import androidx.compose.remote.core.operations.layout.LayoutComponent
import androidx.compose.remote.core.operations.layout.measure.ComponentMeasure
import androidx.compose.remote.core.operations.layout.measure.Measurable
import androidx.compose.remote.core.operations.layout.measure.MeasurePass
import androidx.compose.remote.core.operations.layout.measure.Size
import kotlin.math.max
import kotlin.math.min

abstract class LayoutManager(
    parent: Component?, componentId: Int, animationId: Int,
    x: Float, y: Float, width: Float, height: Float
) : LayoutComponent(parent, componentId, animationId, x, y, width, height), Measurable {

    val mCachedWrapSize = Size(0f, 0f)

    open fun applyVisibility(selfWidth: Float, selfHeight: Float, measure: MeasurePass): Boolean = false
    open fun internalLayoutMeasure(context: PaintContext, measure: MeasurePass) {}
    open fun computeWrapSize(context: PaintContext, minWidth: Float, maxWidth: Float, minHeight: Float, maxHeight: Float,
        horizontalWrap: Boolean, verticalWrap: Boolean, measure: MeasurePass, size: Size) {}

    override fun minIntrinsicHeight(context: RemoteContext?): Float {
        var h = computeModifierDefinedHeight(context); for (c in mChildrenComponents) h = max(c.minIntrinsicHeight(context), h); return h
    }
    override fun minIntrinsicWidth(context: RemoteContext?): Float {
        var w = computeModifierDefinedWidth(context); for (c in mChildrenComponents) w = max(c.minIntrinsicWidth(context), w); return w
    }

    open fun computeSize(context: PaintContext, minWidth: Float, maxWidth: Float, minHeight: Float, maxHeight: Float, measure: MeasurePass) {}

    protected fun childrenHaveHorizontalWeights(): Boolean {
        for (c in mChildrenComponents) { if (c is LayoutManager && c.getWidthModifier()?.hasWeight() == true) return true }; return false
    }
    protected fun childrenHaveVerticalWeights(): Boolean {
        for (c in mChildrenComponents) { if (c is LayoutManager && c.getHeightModifier()?.hasWeight() == true) return true }; return false
    }

    open fun isInHorizontalFill(): Boolean = mWidthModifier!!.isFill()
    open fun isInVerticalFill(): Boolean = mHeightModifier!!.isFill()

    override fun measure(context: PaintContext, minWidth: Float, maxWidth: Float, minHeight: Float, maxHeight: Float, measure: MeasurePass) {
        var mw = minWidth; var mxw = maxWidth; var mh = minHeight; var mxh = maxHeight
        var measuredWidth = min(mxw, computeModifierDefinedWidth(context.getContext()))
        var measuredHeight = min(mxh, computeModifierDefinedHeight(context.getContext()))
        if (mWidthModifier!!.isIntrinsicMin()) mxw = minIntrinsicWidth(context.getContext()) + mPaddingLeft + mPaddingRight
        if (mHeightModifier!!.isIntrinsicMin()) mxh = minIntrinsicHeight(context.getContext()) + mPaddingTop + mPaddingBottom
        val insetMaxWidth = mxw - mPaddingLeft - mPaddingRight
        val insetMaxHeight = mxh - mPaddingTop - mPaddingBottom
        var hasHorizontalWrap = false; var hasVerticalWrap = false
        if (isInHorizontalFill()) { measuredWidth = mxw; mw = insetMaxWidth }
        else if (mWidthModifier!!.hasWeight()) measuredWidth = max(measuredWidth, computeModifierDefinedWidth(context.getContext()))
        else { measuredWidth = max(measuredWidth, mw); measuredWidth = min(measuredWidth, mxw); hasHorizontalWrap = mWidthModifier!!.isWrap() || mWidthModifier!!.isIntrinsicMin() }
        if (isInVerticalFill()) { measuredHeight = mxh; mh = insetMaxHeight }
        else if (mHeightModifier!!.hasWeight()) measuredHeight = max(measuredHeight, computeModifierDefinedHeight(context.getContext()))
        else { measuredHeight = max(measuredHeight, mh); measuredHeight = min(measuredHeight, mxh); hasVerticalWrap = mHeightModifier!!.isWrap() || mHeightModifier!!.isIntrinsicMin() }
        if (mw == mxw) measuredWidth = mxw; if (mh == mxh) measuredHeight = mxh
        if (hasHorizontalWrap || hasVerticalWrap) {
            mCachedWrapSize.width = 0f; mCachedWrapSize.height = 0f
            computeWrapSize(context, mw, insetMaxWidth, mh, insetMaxHeight, mWidthModifier!!.isWrap(), mHeightModifier!!.isWrap(), measure, mCachedWrapSize)
            val sv = measure.get(this).visibility
            if (Visibility.hasOverride(sv) && mScheduledVisibility != sv) mScheduledVisibility = sv
            if (hasHorizontalWrap) { measuredWidth = mCachedWrapSize.width + mPaddingLeft + mPaddingRight; measuredWidth = max(measuredWidth, mw) }
            if (hasVerticalWrap) { measuredHeight = mCachedWrapSize.height + mPaddingTop + mPaddingBottom; measuredHeight = max(measuredHeight, mh) }
        } else {
            computeSize(context, 0f, measuredWidth - mPaddingLeft - mPaddingRight, 0f, measuredHeight - mPaddingTop - mPaddingBottom, measure)
        }
        if (mContent != null) { val cm = measure.get(mContent!!); cm.x = 0f; cm.y = 0f; cm.w = measuredWidth; cm.h = measuredHeight }
        measuredWidth = max(measuredWidth, mw); measuredHeight = max(measuredHeight, mh)
        val m = measure.get(this); m.w = measuredWidth; m.h = measuredHeight; m.visibility = mScheduledVisibility
        internalLayoutMeasure(context, measure)
    }

    override fun layout(context: RemoteContext, measure: MeasurePass) {
        super.layout(context, measure); val self = measure.get(this)
        mComponentModifiers.layout(context, this, self.w, self.h)
        for (c in mChildrenComponents) c.layout(context, measure); mNeedsMeasure = false
    }

    fun selfLayout(context: RemoteContext, measure: MeasurePass) {
        super.layout(context, measure); val self = measure.get(this)
        mComponentModifiers.layout(context, this, self.w, self.h); mNeedsMeasure = false
    }
}
