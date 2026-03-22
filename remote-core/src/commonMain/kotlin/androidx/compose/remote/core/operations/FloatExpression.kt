package androidx.compose.remote.core.operations

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.VariableSupport
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.utilities.AnimatedFloatExpression
import androidx.compose.remote.core.operations.utilities.NanMap
import androidx.compose.remote.core.operations.utilities.easing.FloatAnimation
import androidx.compose.remote.core.operations.utilities.easing.SpringStopEngine
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable
import androidx.compose.remote.core.serialize.SerializeTags
import kotlin.math.abs

class FloatExpression(
    var mId: Int,
    var mSrcValue: FloatArray,
    var mSrcAnimation: FloatArray?
) : Operation(), ComponentData, VariableSupport, Serializable {

    var mFloatAnimation: FloatAnimation? = null
    private var mSpring: SpringStopEngine? = null
    var mPreCalcValue: FloatArray? = null
    private var mLastChange: Float = Float.NaN
    private var mLastCalculatedValue: Float = Float.NaN
    val mExp = AnimatedFloatExpression()
    var mLastAnimatedValue: Float = Float.NaN

    init {
        mSrcAnimation?.let { anim ->
            if (anim.size > 4 && anim[0] == 0f) {
                mSpring = SpringStopEngine(anim)
            } else {
                mFloatAnimation = FloatAnimation(*anim)
            }
        }
    }

    override fun updateVariables(context: RemoteContext) {
        val src = mSrcValue
        if (mPreCalcValue == null || mPreCalcValue!!.size != src.size) {
            mPreCalcValue = FloatArray(src.size)
        }
        val pre = mPreCalcValue!!
        var valueChanged = false
        for (i in src.indices) {
            val v = src[i]
            if (v.isNaN() && !AnimatedFloatExpression.isMathOperator(v) && !NanMap.isDataVariable(v)) {
                val id = Utils.idFromNan(v)
                var newValue = context.getFloat(id)
                if (id == RemoteContext.ID_DENSITY && newValue == 0f) newValue = 1f
                if (mFloatAnimation != null || mSpring != null) {
                    if (pre[i] != newValue) { valueChanged = true; pre[i] = newValue }
                } else {
                    pre[i] = newValue
                }
            } else {
                pre[i] = src[i]
            }
        }
        var v = mLastCalculatedValue
        if (valueChanged) {
            v = mExp.eval(pre, pre.size)
            if (v != mLastCalculatedValue) {
                mLastChange = context.getAnimationTime()
                mLastCalculatedValue = v
            } else {
                valueChanged = false
            }
        }
        if (valueChanged && mFloatAnimation != null) {
            val fa = mFloatAnimation!!
            if (fa.getTargetValue().isNaN()) fa.setInitialValue(v)
            else fa.setInitialValue(fa.getTargetValue())
            fa.setTargetValue(v)
        } else if (valueChanged && mSpring != null) {
            mSpring!!.setTargetValue(v)
        }
    }

    override fun registerListening(context: RemoteContext) {
        for (v in mSrcValue) {
            if (v.isNaN() && !AnimatedFloatExpression.isMathOperator(v) && !NanMap.isDataVariable(v)) {
                context.listensTo(Utils.idFromNan(v), this)
            }
        }
    }

    override fun apply(context: RemoteContext) {
        val t = context.getAnimationTime()
        if (mLastChange.isNaN()) mLastChange = t
        val pre = mPreCalcValue ?: return
        if (mFloatAnimation != null) {
            val fa = mFloatAnimation!!
            if (mLastCalculatedValue.isNaN()) {
                mLastCalculatedValue = mExp.eval(context.getCollectionsAccess()!!, pre, pre.size)
                fa.setTargetValue(mLastCalculatedValue)
                if (fa.getInitialValue().isNaN()) fa.setInitialValue(mLastCalculatedValue)
            }
            val lastComputedValue = fa.get(t - mLastChange)
            if (lastComputedValue != mLastAnimatedValue || t - mLastChange <= fa.getDuration()) {
                mLastAnimatedValue = lastComputedValue
                context.loadFloat(mId, lastComputedValue)
                context.needsRepaint()
                markDirty()
            }
        } else if (mSpring != null) {
            val sp = mSpring!!
            val lastComputedValue = sp.get(t)
            val epsilon = 0.01f
            if (lastComputedValue != mLastAnimatedValue || abs(sp.getTargetValue() - lastComputedValue) > epsilon) {
                mLastAnimatedValue = lastComputedValue
                context.loadFloat(mId, lastComputedValue)
                context.needsRepaint()
            }
        } else {
            val v = mExp.eval(context.getCollectionsAccess()!!, pre, pre.size)
            context.loadFloat(mId, v)
        }
    }

    fun evaluate(context: RemoteContext): Float {
        updateVariables(context)
        val t = context.getAnimationTime()
        if (mLastChange.isNaN()) mLastChange = t
        return mExp.eval(context.getCollectionsAccess()!!, mPreCalcValue!!, mPreCalcValue!!.size)
    }

    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mId, mSrcValue, mSrcAnimation) }

    override fun toString(): String {
        val labels = arrayOfNulls<String>(mSrcValue.size)
        for (i in mSrcValue.indices) {
            if (mSrcValue[i].isNaN()) labels[i] = "[${Utils.idStringFromNan(mSrcValue[i])}]"
        }
        val toDisplay = mPreCalcValue ?: mSrcValue
        return "FloatExpression[$mId] = (${AnimatedFloatExpression.toString(toDisplay, labels)})"
    }

    override fun deepToString(indent: String): String = indent + toString()

    override fun serialize(serializer: MapSerializer) {
        serializer.addTags(SerializeTags.EXPRESSION)
            .addType(CLASS_NAME).add("id", mId)
            .addFloatExpressionSrc("srcValues", mSrcValue)
            .add("animation", mFloatAnimation)
    }

    companion object {
        private val OP_CODE = Operations.ANIMATED_FLOAT
        private const val CLASS_NAME = "FloatExpression"
        const val MAX_EXPRESSION_SIZE = 32

        fun name(): String = CLASS_NAME
        fun id(): Int = OP_CODE

        fun apply(buffer: WireBuffer, id: Int, value: FloatArray, animation: FloatArray?) {
            buffer.start(OP_CODE)
            buffer.writeInt(id)
            var len = value.size
            if (len > MAX_EXPRESSION_SIZE) throw RuntimeException("Expression too long")
            if (animation != null) len = len or (animation.size shl 16)
            buffer.writeInt(len)
            for (v in value) buffer.writeFloat(v)
            animation?.let { for (v in it) buffer.writeFloat(v) }
        }

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val id = buffer.readInt()
            val len = buffer.readInt()
            val valueLen = len and 0xFFFF
            if (valueLen > MAX_EXPRESSION_SIZE) throw RuntimeException("Float expression too long")
            val animLen = (len shr 16) and 0xFFFF
            val values = FloatArray(valueLen) { buffer.readFloat() }
            val animation = if (animLen != 0) FloatArray(animLen) { buffer.readFloat() } else null
            operations.add(FloatExpression(id, values, animation))
        }
    }
}
