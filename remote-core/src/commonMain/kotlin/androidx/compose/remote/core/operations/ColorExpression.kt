package androidx.compose.remote.core.operations

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.VariableSupport
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable
import kotlin.Float.Companion.NaN

class ColorExpression : Operation, VariableSupport, Serializable, ComponentData {

    var mId: Int
    var mMode: Int
    var mColor1: Int = 0
    var mColor2: Int = 0
    var mTween: Float = 0.0f
    var mHue: Float = 0f
    var mSat: Float = 0f
    var mValue: Float = 0f
    var mOutHue: Float = 0f
    var mOutSat: Float = 0f
    var mOutValue: Float = 0f
    var mAlpha: Int = 0xFF
    private var mArgbAlpha: Float = 0.0f
    private var mArgbRed: Float = 0.0f
    private var mArgbGreen: Float = 0.0f
    private var mArgbBlue: Float = 0.0f
    private var mOutArgbAlpha: Float = 0.0f
    private var mOutArgbRed: Float = 0.0f
    private var mOutArgbGreen: Float = 0.0f
    private var mOutArgbBlue: Float = 0.0f
    var mOutTween: Float = 0.0f
    var mOutColor1: Int = 0
    var mOutColor2: Int = 0

    constructor(id: Int, hue: Float, sat: Float, value: Float) : super() {
        mId = id; mMode = HSV_MODE.toInt(); mAlpha = 0xFF
        mHue = hue; mOutHue = hue; mSat = sat; mOutSat = sat
        mValue = value; mOutValue = value
        mColor1 = hue.toRawBits()
        mColor2 = sat.toRawBits()
        mTween = value
    }

    constructor(id: Int, mode: Byte, alpha: Int, hue: Float, sat: Float, value: Float) : super() {
        require(mode == HSV_MODE) { "Invalid mode $mode" }
        mId = id; mMode = HSV_MODE.toInt(); mAlpha = alpha
        mHue = hue; mOutHue = hue; mSat = sat; mOutSat = sat
        mValue = value; mOutValue = value
        mColor1 = hue.toRawBits(); mColor2 = sat.toRawBits(); mTween = value
    }

    constructor(id: Int, mode: Int, color1: Int, color2: Int, tween: Float) : super() {
        mId = id; mMode = mode and 0xFF; mAlpha = (mode shr 16) and 0xFF
        if (mMode == HSV_MODE.toInt()) {
            mHue = Float.fromBits(color1); mOutHue = mHue
            mSat = Float.fromBits(color2); mOutSat = mSat
            mValue = tween; mOutValue = tween
        }
        mColor1 = color1; mColor2 = color2; mTween = tween
        mOutTween = tween; mOutColor1 = color1; mOutColor2 = color2
    }

    constructor(id: Int, mode: Byte, alpha: Float, red: Float, green: Float, blue: Float) : super() {
        require(mode == ARGB_MODE) { "Invalid mode $mode" }
        mId = id; mMode = ARGB_MODE.toInt()
        mArgbAlpha = alpha; mOutArgbAlpha = alpha
        mArgbRed = red; mOutArgbRed = red
        mArgbGreen = green; mOutArgbGreen = green
        mArgbBlue = blue; mOutArgbBlue = blue
    }

    override fun updateVariables(context: RemoteContext) {
        if (mMode == 4) {
            if (mHue.isNaN()) mOutHue = context.getFloat(Utils.idFromNan(mHue))
            if (mSat.isNaN()) mOutSat = context.getFloat(Utils.idFromNan(mSat))
            if (mValue.isNaN()) mOutValue = context.getFloat(Utils.idFromNan(mValue))
        }
        if (mMode == ARGB_MODE.toInt()) {
            if (mArgbAlpha.isNaN()) mOutArgbAlpha = context.getFloat(Utils.idFromNan(mArgbAlpha))
            if (mArgbRed.isNaN()) mOutArgbRed = context.getFloat(Utils.idFromNan(mArgbRed))
            if (mArgbGreen.isNaN()) mOutArgbGreen = context.getFloat(Utils.idFromNan(mArgbGreen))
            if (mArgbBlue.isNaN()) mOutArgbBlue = context.getFloat(Utils.idFromNan(mArgbBlue))
        }
        if (mTween.isNaN()) mOutTween = context.getFloat(Utils.idFromNan(mTween))
        if ((mMode and 1) == 1) mOutColor1 = context.getColor(mColor1)
        if ((mMode and 2) == 2) mOutColor2 = context.getColor(mColor2)
    }

    override fun registerListening(context: RemoteContext) {
        if (mMode == HSV_MODE.toInt()) {
            if (mHue.isNaN()) context.listensTo(Utils.idFromNan(mHue), this)
            if (mSat.isNaN()) context.listensTo(Utils.idFromNan(mSat), this)
            if (mValue.isNaN()) context.listensTo(Utils.idFromNan(mValue), this)
            return
        }
        if (mMode == ARGB_MODE.toInt()) {
            if (mArgbAlpha.isNaN()) context.listensTo(Utils.idFromNan(mArgbAlpha), this)
            if (mArgbRed.isNaN()) context.listensTo(Utils.idFromNan(mArgbRed), this)
            if (mArgbGreen.isNaN()) context.listensTo(Utils.idFromNan(mArgbGreen), this)
            if (mArgbBlue.isNaN()) context.listensTo(Utils.idFromNan(mArgbBlue), this)
            return
        }
        if (mTween.isNaN()) context.listensTo(Utils.idFromNan(mTween), this)
        if ((mMode and 1) == 1) context.listensTo(mColor1, this)
        if ((mMode and 2) == 2) context.listensTo(mColor2, this)
    }

    override fun apply(context: RemoteContext) {
        if (mMode == HSV_MODE.toInt()) {
            context.loadColor(mId, (mAlpha shl 24) or (0xFFFFFF and Utils.hsvToRgb(mOutHue, mOutSat, mOutValue)))
            return
        }
        if (mMode == ARGB_MODE.toInt()) {
            context.loadColor(mId, Utils.toARGB(mOutArgbAlpha, mOutArgbRed, mOutArgbGreen, mOutArgbBlue))
            return
        }
        if (mOutTween == 0.0f) {
            if ((mMode and 1) == 1) mOutColor1 = context.getColor(mColor1)
            context.loadColor(mId, mOutColor1)
        } else {
            if ((mMode and 1) == 1) mOutColor1 = context.getColor(mColor1)
            if ((mMode and 2) == 2) mOutColor2 = context.getColor(mColor2)
            context.loadColor(mId, Utils.interpolateColor(mOutColor1, mOutColor2, mOutTween))
        }
    }

    override fun write(buffer: WireBuffer) {
        when (mMode) {
            ARGB_MODE.toInt() -> applyArgb(buffer, mId, mArgbAlpha, mArgbRed, mArgbGreen, mArgbBlue)
            HSV_MODE.toInt() -> {
                mOutValue = mValue
                mColor1 = mHue.toRawBits()
                mColor2 = mSat.toRawBits()
                val mode = mMode or (mAlpha shl 16)
                applyRaw(buffer, mId, mode, mColor1, mColor2, mTween.toRawBits())
            }
            COLOR_ID_INTERPOLATE.toInt(), ID_COLOR_INTERPOLATE.toInt(),
            ID_ID_INTERPOLATE.toInt(), COLOR_COLOR_INTERPOLATE.toInt() ->
                applyRaw(buffer, mId, mMode, mColor1, mColor2, mTween.toRawBits())
            else -> throw RuntimeException("Invalid mode")
        }
    }

    override fun toString(): String {
        if (mMode == HSV_MODE.toInt()) {
            return "ColorExpression[$mId] = hsv (${Utils.floatToString(mHue)}, ${Utils.floatToString(mSat)}, ${Utils.floatToString(mValue)})"
        }
        if (mMode == ARGB_MODE.toInt()) {
            return "ColorExpression[$mId] = rgb (${Utils.floatToString(mArgbAlpha)}, ${Utils.floatToString(mArgbRed)}, ${Utils.floatToString(mArgbGreen)}, ${Utils.floatToString(mArgbBlue)})"
        }
        val c1 = if ((mMode and 1) == 1) "[$mColor1]" else Utils.colorInt(mColor1)
        val c2 = if ((mMode and 2) == 2) "[$mColor2]" else Utils.colorInt(mColor2)
        return "ColorExpression[$mId] = tween($c1, $c2, ${Utils.floatToString(mTween)})"
    }

    override fun deepToString(indent: String): String = indent + toString()

    override fun serialize(serializer: MapSerializer) {
        serializer.addType(CLASS_NAME).add("id", mId)
        when (mMode) {
            COLOR_COLOR_INTERPOLATE.toInt(), ID_COLOR_INTERPOLATE.toInt(),
            COLOR_ID_INTERPOLATE.toInt(), ID_ID_INTERPOLATE.toInt() -> {
                serializer.add("mode", "TWEEN")
                serializer.add("startColor", mColor1.toFloat(), mOutColor1.toFloat())
                serializer.add("endColor", mColor2.toFloat(), mOutColor2.toFloat())
                serializer.add("startColor", mTween, mOutTween)
            }
            HSV_MODE.toInt() -> {
                serializer.add("mode", "HSV")
                serializer.add("hue", mHue, mOutHue)
                serializer.add("sat", mSat, mOutSat)
                serializer.add("val", mValue, mOutValue)
            }
            ARGB_MODE.toInt(), IDARGB_MODE.toInt() -> {
                serializer.add("mode", "ARGB")
                serializer.add("a", mArgbAlpha, mOutArgbAlpha)
                serializer.add("r", mArgbRed, mOutArgbRed)
                serializer.add("g", mArgbGreen, mOutArgbGreen)
                serializer.add("b", mArgbBlue, mOutArgbBlue)
            }
            else -> serializer.add("mode", "NONE")
        }
    }

    companion object {
        private val OP_CODE = Operations.COLOR_EXPRESSIONS
        private const val CLASS_NAME = "ColorExpression"

        const val COLOR_COLOR_INTERPOLATE: Byte = 0
        const val ID_COLOR_INTERPOLATE: Byte = 1
        const val COLOR_ID_INTERPOLATE: Byte = 2
        const val ID_ID_INTERPOLATE: Byte = 3
        const val HSV_MODE: Byte = 4
        const val ARGB_MODE: Byte = 5
        const val IDARGB_MODE: Byte = 6

        fun name(): String = CLASS_NAME
        fun id(): Int = OP_CODE

        fun applyRaw(buffer: WireBuffer, id: Int, param1: Int, param2: Int, param3: Int, param4: Int) {
            buffer.start(OP_CODE)
            buffer.writeInt(id)
            buffer.writeInt(param1)
            buffer.writeInt(param2)
            buffer.writeInt(param3)
            buffer.writeInt(param4)
        }

        fun apply(buffer: WireBuffer, id: Int, mode: Int, color1: Int, color2: Int, tween: Float) {
            applyRaw(buffer, id, mode, color1, color2, tween.toRawBits())
        }

        fun applyArgb(buffer: WireBuffer, id: Int, alpha: Float, red: Float, green: Float, blue: Float) {
            var param1 = if (alpha.isNaN()) IDARGB_MODE.toInt() else ARGB_MODE.toInt()
            param1 = param1 or (if (alpha.isNaN()) Utils.idFromNan(alpha) shl 16 else ((alpha * 1024).toInt()) shl 16)
            val param2 = red.toRawBits()
            val param3 = green.toRawBits()
            val param4 = blue.toRawBits()
            applyRaw(buffer, id, param1, param2, param3, param4)
        }

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val id = buffer.readInt()
            val param1 = buffer.readInt()
            val param2 = buffer.readInt()
            val param3 = buffer.readInt()
            val param4 = buffer.readInt()
            val mode = param1 and 0xFF
            when (mode) {
                IDARGB_MODE.toInt() -> {
                    val alpha = Utils.asNan(param1 shr 16)
                    operations.add(ColorExpression(id, ARGB_MODE, alpha, Float.fromBits(param2), Float.fromBits(param3), Float.fromBits(param4)))
                }
                ARGB_MODE.toInt() -> {
                    val alpha = (param1 shr 16) / 1024.0f
                    operations.add(ColorExpression(id, ARGB_MODE, alpha, Float.fromBits(param2), Float.fromBits(param3), Float.fromBits(param4)))
                }
                HSV_MODE.toInt() -> {
                    operations.add(ColorExpression(id, HSV_MODE, (param1 shr 16), Float.fromBits(param2), Float.fromBits(param3), Float.fromBits(param4)))
                }
                COLOR_ID_INTERPOLATE.toInt(), ID_COLOR_INTERPOLATE.toInt(),
                ID_ID_INTERPOLATE.toInt(), COLOR_COLOR_INTERPOLATE.toInt() -> {
                    operations.add(ColorExpression(id, mode, param2, param3, Float.fromBits(param4)))
                }
                else -> throw RuntimeException("Invalid mode $mode")
            }
        }
    }
}
