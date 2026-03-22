// Copyright (C) 2024 The Android Open Source Project. Apache License 2.0.
package androidx.compose.remote.core.operations
import androidx.compose.remote.core.*
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.types.LongConstant
class TimeAttribute(var mId: Int, var mTimeId: Int, var mType: Short, private val mArgs: IntArray?) : PaintOperation() {
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mId, mTimeId, mType, mArgs) }
    override fun toString(): String = "TimeAttribute[$mId] = $mTimeId $mType"
    override fun deepToString(indent: String): String = indent + toString()
    override fun paint(context: PaintContext) {
        val ctx = context.getContext()
        val lc = ctx.getObject(mTimeId) as? LongConstant
        val value = lc?.getValue() ?: context.getCurrentTimeMillis()
        val v = mType.toInt() and 255
        when (v) {
            TIME_FROM_NOW_SEC.toInt() -> { ctx.loadFloat(mId, (value - context.getCurrentTimeMillis()) * 1E-3f); ctx.needsRepaint() }
            TIME_FROM_NOW_MIN.toInt() -> { ctx.loadFloat(mId, ((value - context.getCurrentTimeMillis()) * 1E-3 / 60).toFloat()); ctx.needsRepaint() }
            TIME_FROM_NOW_HR.toInt() -> ctx.loadFloat(mId, ((value - context.getCurrentTimeMillis()) * 1E-3 / 3600).toFloat())
            TIME_FROM_LOAD_SEC.toInt() -> { ctx.loadFloat(mId, (value - ctx.getDocLoadTime()) * 1E-3f); ctx.needsRepaint() }
        }
    }
    override fun serialize(serializer: MapSerializer) { serializer.addType("TimeAttribute").add("id", mId).add("timeId", mTimeId) }
    companion object {
        private val OP_CODE = Operations.ATTRIBUTE_TIME; private const val MAX_ARG_LEN: Short = 32
        const val TIME_FROM_NOW_SEC: Short = 0; const val TIME_FROM_NOW_MIN: Short = 1; const val TIME_FROM_NOW_HR: Short = 2
        const val TIME_FROM_ARG_SEC: Short = 3; const val TIME_FROM_ARG_MIN: Short = 4; const val TIME_FROM_ARG_HR: Short = 5
        const val TIME_IN_SEC: Short = 6; const val TIME_IN_MIN: Short = 7; const val TIME_IN_HR: Short = 8
        const val TIME_DAY_OF_MONTH: Short = 9; const val TIME_MONTH_VALUE: Short = 10; const val TIME_DAY_OF_WEEK: Short = 11
        const val TIME_YEAR: Short = 12; const val TIME_FROM_LOAD_SEC: Short = 14; const val TIME_DAY_OF_YEAR: Short = 15
        fun name(): String = "TimeAttribute"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, id: Int, textId: Int, type: Short, args: IntArray? = null) {
            buffer.start(OP_CODE); buffer.writeInt(id); buffer.writeInt(textId); buffer.writeShort(type.toInt())
            if (args == null) buffer.writeShort(0) else { buffer.writeShort(args.size); for (a in args) buffer.writeInt(a) }
        }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val id = buffer.readInt(); val textId = buffer.readInt(); val type = buffer.readShort().toShort(); val len = buffer.readShort()
            if (len > MAX_ARG_LEN) throw RuntimeException("Too many args")
            val args = if (len != 0) IntArray(len) { buffer.readInt() } else null
            operations.add(TimeAttribute(id, textId, type, args))
        }
    }
}
