package androidx.compose.remote.core.operations.layout.managers

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.layout.Component
import androidx.compose.remote.core.operations.layout.measure.MeasurePass

class CanvasLayout : BoxLayout {
    constructor(parent: Component?, componentId: Int, animationId: Int, x: Float, y: Float, width: Float, height: Float)
        : super(parent, componentId, animationId, x, y, width, height, 0, 0)
    constructor(parent: Component?, componentId: Int, animationId: Int) : this(parent, componentId, animationId, 0f, 0f, 0f, 0f)
    override fun toString(): String = "CANVAS [$componentId:$mAnimationId]"
    override fun getSerializedName(): String = "CANVAS"
    override fun internalLayoutMeasure(context: PaintContext, measure: MeasurePass) {
        val sm = measure.get(this); val sw = sm.w - mPaddingLeft - mPaddingRight; val sh = sm.h - mPaddingTop - mPaddingBottom
        for (child in mChildrenComponents) { val m = measure.get(child); m.x = 0f; m.y = 0f; m.w = sw; m.h = sh }
    }
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, componentId, mAnimationId) }
    companion object {
        fun name(): String = "CanvasLayout"; fun id(): Int = Operations.LAYOUT_CANVAS
        fun apply(buffer: WireBuffer, cid: Int, aid: Int) { buffer.start(Operations.LAYOUT_CANVAS); buffer.writeInt(cid); buffer.writeInt(aid) }
        fun read(buffer: WireBuffer, ops: MutableList<Operation>) { ops.add(CanvasLayout(null, buffer.readInt(), buffer.readInt())) }
    }
}
