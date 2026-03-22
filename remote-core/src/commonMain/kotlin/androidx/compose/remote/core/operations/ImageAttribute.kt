// Copyright (C) 2024 The Android Open Source Project. Apache License 2.0.
package androidx.compose.remote.core.operations
import androidx.compose.remote.core.*
import androidx.compose.remote.core.serialize.MapSerializer
class ImageAttribute(var mId: Int, var mImageId: Int, var mType: Short, private val mArgs: IntArray?) : PaintOperation() {
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mId, mImageId, mType, mArgs) }
    override fun toString(): String = "ImageAttribute[$mId] = $mImageId $mType"
    override fun deepToString(indent: String): String = indent + toString()
    override fun paint(context: PaintContext) {
        val bitmapData = context.getContext().getObject(mImageId) as BitmapData
        when (mType) { IMAGE_WIDTH -> context.getContext().loadFloat(mId, bitmapData.getWidth().toFloat()); IMAGE_HEIGHT -> context.getContext().loadFloat(mId, bitmapData.getHeight().toFloat()) }
    }
    override fun serialize(serializer: MapSerializer) { serializer.addType("ImageAttribute").add("id", mId).add("imageId", mImageId) }
    companion object {
        private val OP_CODE = Operations.ATTRIBUTE_IMAGE; const val IMAGE_WIDTH: Short = 0; const val IMAGE_HEIGHT: Short = 1
        fun name(): String = "ImageAttribute"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, id: Int, imageId: Int, type: Short, args: IntArray?) {
            buffer.start(OP_CODE); buffer.writeInt(id); buffer.writeInt(imageId); buffer.writeShort(type.toInt())
            if (args == null) buffer.writeShort(0) else { buffer.writeShort(args.size); for (a in args) buffer.writeInt(a) }
        }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val id = buffer.readInt(); val imageId = buffer.readInt(); val type = buffer.readShort().toShort(); val len = buffer.readShort()
            val args = IntArray(len) { buffer.readInt() }; operations.add(ImageAttribute(id, imageId, type, args))
        }
    }
}
