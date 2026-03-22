package androidx.compose.remote.core.operations.layout

import androidx.compose.remote.core.CoreDocument
import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.PaintOperation
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.Container
import androidx.compose.remote.core.operations.TextData
import androidx.compose.remote.core.operations.Utils
import androidx.compose.remote.core.operations.layout.modifiers.ModifierOperation
import androidx.compose.remote.core.operations.paint.PaintBundle
import androidx.compose.remote.core.operations.utilities.ColorUtils
import androidx.compose.remote.core.operations.utilities.StringSerializer
import androidx.compose.remote.core.operations.utilities.easing.Easing
import androidx.compose.remote.core.operations.utilities.easing.FloatAnimation
import kotlin.math.max
import kotlin.math.min

class ClickModifierOperation : PaintOperation(), Container, ModifierOperation, DecoratorComponent, ClickHandler {

    var mAnimateRippleStart: Long = 0
    var mAnimateRippleX: Float = 0f
    var mAnimateRippleY: Float = 0f
    var mAnimateRippleDuration: Int = 1000
    var mWidth: Float = 0f
    var mHeight: Float = 0f
    val locationInWindow: FloatArray = FloatArray(2)
    val mPaint = PaintBundle()
    val mList: ArrayList<Operation> = ArrayList()

    fun animateRipple(x: Float, y: Float, timeStampMillis: Long) {
        mAnimateRippleStart = timeStampMillis; mAnimateRippleX = x; mAnimateRippleY = y
    }

    override fun getList(): ArrayList<Operation> = mList
    override fun write(buffer: WireBuffer) { Companion.apply(buffer) }
    override fun toString(): String = "ClickModifier"
    override fun deepToString(indent: String): String = indent + toString()

    override fun apply(context: RemoteContext) {
        for (op in mList) { if (op is TextData) { op.apply(context); context.incrementOpCount() } }
    }

    override fun paint(context: PaintContext) {
        if (mAnimateRippleStart == 0L) return
        context.needsRepaint()
        var progress = (context.getCurrentTimeMillis() - mAnimateRippleStart).toFloat() / mAnimateRippleDuration.toFloat()
        if (progress > 1f) mAnimateRippleStart = 0
        progress = min(1f, progress)
        context.save(); context.savePaint(); mPaint.reset()
        val anim1 = FloatAnimation(Easing.CUBIC_STANDARD, 1f, null, Float.NaN, Float.NaN)
        anim1.setInitialValue(0f); anim1.setTargetValue(1f)
        val tween = anim1.get(progress)
        val anim2 = FloatAnimation(Easing.CUBIC_STANDARD, 0.5f, null, Float.NaN, Float.NaN)
        anim2.setInitialValue(0f); anim2.setTargetValue(1f)
        val tweenRadius = anim2.get(progress)
        val startColor = ColorUtils.createColor(250, 250, 250, 180)
        val endColor = ColorUtils.createColor(200, 200, 200, 0)
        val paintedColor = Utils.interpolateColor(startColor, endColor, tween)
        val radius = max(mWidth, mHeight) * tweenRadius
        mPaint.setColor(paintedColor)
        context.applyPaint(mPaint); context.clipRect(0f, 0f, mWidth, mHeight)
        context.drawCircle(mAnimateRippleX, mAnimateRippleY, radius)
        context.restorePaint(); context.restore()
    }

    override fun layout(context: RemoteContext, component: Component, width: Float, height: Float) {
        mWidth = width; mHeight = height
    }

    override fun onClick(context: RemoteContext, document: CoreDocument, component: Component, x: Float, y: Float) {
        if (!component.isVisible()) return
        locationInWindow[0] = 0f; locationInWindow[1] = 0f
        component.getLocationInWindow(locationInWindow)
        if (context.isAnimationEnabled()) animateRipple(x - locationInWindow[0], y - locationInWindow[1], context.getClock().millis())
        for (o in mList) { if (o is ActionOperation) o.runAction(context, document, component, x, y) }
        context.hapticEffect(3)
    }

    override fun serializeToString(indent: Int, serializer: StringSerializer) {
        serializer.append(indent, "CLICK_MODIFIER")
    }

    companion object {
        fun name(): String = "ClickModifier"
        fun apply(buffer: WireBuffer) { buffer.start(Operations.MODIFIER_CLICK) }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { operations.add(ClickModifierOperation()) }
    }
}
