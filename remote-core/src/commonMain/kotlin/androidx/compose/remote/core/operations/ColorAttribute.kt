// Copyright (C) 2024 The Android Open Source Project. Apache License 2.0.
package androidx.compose.remote.core.operations
import androidx.compose.remote.core.*
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable
class ColorAttribute(var mId: Int, var mColorId: Int, var mType: Short) : PaintOperation(), VariableSupport, Serializable {
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mId, mColorId, mType) }
    override fun toString(): String = "ColorAttribute[$mId] = $mColorId $mType"
    override fun deepToString(indent: String): String = indent + toString()
    override fun paint(context: PaintContext) {
        val ctx = context.getContext(); val color = ctx.getColor(mColorId); val v = mType.toInt() and 255
        when (v) {
            COLOR_HUE.toInt() -> ctx.loadFloat(mId, Utils.getHue(color)); COLOR_SATURATION.toInt() -> ctx.loadFloat(mId, Utils.getSaturation(color))
            COLOR_BRIGHTNESS.toInt() -> ctx.loadFloat(mId, Utils.getBrightness(color))
            COLOR_RED.toInt() -> ctx.loadFloat(mId, ((color shr 16) and 0xFF) / 255.0f)
            COLOR_GREEN.toInt() -> ctx.loadFloat(mId, ((color shr 8) and 0xFF) / 255.0f)
            COLOR_BLUE.toInt() -> ctx.loadFloat(mId, (color and 0xFF) / 255.0f)
            COLOR_ALPHA.toInt() -> ctx.loadFloat(mId, ((color shr 24) and 0xFF) / 255.0f)
        }
    }
    override fun registerListening(context: RemoteContext) { context.listensTo(mColorId, this) }
    override fun updateVariables(context: RemoteContext) {}
    override fun markDirty() { super<PaintOperation>.markDirty() }
    override fun serialize(serializer: MapSerializer) { serializer.addType("ColorAttribute").add("id", mId).add("timeId", mColorId) }
    companion object {
        private val OP_CODE = Operations.ATTRIBUTE_COLOR
        const val COLOR_HUE: Short = 0; const val COLOR_SATURATION: Short = 1; const val COLOR_BRIGHTNESS: Short = 2
        const val COLOR_RED: Short = 3; const val COLOR_GREEN: Short = 4; const val COLOR_BLUE: Short = 5; const val COLOR_ALPHA: Short = 6
        fun name(): String = "ColorAttribute"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, id: Int, textId: Int, type: Short) { buffer.start(OP_CODE); buffer.writeInt(id); buffer.writeInt(textId); buffer.writeShort(type.toInt()) }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { operations.add(ColorAttribute(buffer.readInt(), buffer.readInt(), buffer.readShort().toShort())) }
    }
}
