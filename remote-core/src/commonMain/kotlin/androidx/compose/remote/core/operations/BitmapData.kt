// Copyright (C) 2024 The Android Open Source Project. Apache License 2.0.
package androidx.compose.remote.core.operations
import androidx.compose.remote.core.*
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable
class BitmapData : Operation, Serializable {
    val mImageId: Int; var mImageWidth: Int; var mImageHeight: Int; var mType: Short; var mEncoding: Short; var mBitmap: ByteArray
    constructor(imageId: Int, width: Int, height: Int, bitmap: ByteArray) : super() { mImageId = imageId; mImageWidth = width; mImageHeight = height; mBitmap = bitmap; mType = TYPE_PNG_8888; mEncoding = ENCODING_INLINE }
    constructor(imageId: Int, type: Short, width: Short, encoding: Short, height: Short, bitmap: ByteArray) : super() { mImageId = imageId; mType = type; mImageWidth = width.toInt(); mEncoding = encoding; mImageHeight = height.toInt(); mBitmap = bitmap }
    fun update(from: BitmapData) { mImageWidth = from.mImageWidth; mImageHeight = from.mImageHeight; mBitmap = from.mBitmap; mType = from.mType; mEncoding = from.mEncoding }
    fun getWidth(): Int = mImageWidth; fun getHeight(): Int = mImageHeight; fun getType(): Int = mType.toInt()
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mImageId, mType, mImageWidth.toShort(), mEncoding, mImageHeight.toShort(), mBitmap) }
    override fun toString(): String = "BITMAP DATA $mImageId"
    override fun apply(context: RemoteContext) { context.putObject(mImageId, this); context.loadBitmap(mImageId, mEncoding, mType, mImageWidth, mImageHeight, mBitmap) }
    override fun deepToString(indent: String): String = indent + toString()
    override fun serialize(serializer: MapSerializer) { serializer.addType("BitmapData").add("imageId", mImageId).add("imageWidth", mImageWidth).add("imageHeight", mImageHeight) }
    companion object {
        private val OP_CODE = Operations.DATA_BITMAP; const val MAX_IMAGE_DIMENSION = 8000
        const val ENCODING_INLINE: Short = 0; const val ENCODING_URL: Short = 1; const val ENCODING_FILE: Short = 2; const val ENCODING_EMPTY: Short = 3
        const val TYPE_PNG_8888: Short = 0; const val TYPE_PNG: Short = 1; const val TYPE_RAW8: Short = 2; const val TYPE_RAW8888: Short = 3; const val TYPE_PNG_ALPHA_8: Short = 4
        fun name(): String = "BitmapData"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, imageId: Int, width: Int, height: Int, bitmap: ByteArray) { buffer.start(OP_CODE); buffer.writeInt(imageId); buffer.writeInt(width); buffer.writeInt(height); buffer.writeBuffer(bitmap) }
        fun apply(buffer: WireBuffer, imageId: Int, type: Short, width: Short, encoding: Short, height: Short, bitmap: ByteArray) {
            buffer.start(OP_CODE); buffer.writeInt(imageId)
            buffer.writeInt((type.toInt() shl 16) or width.toInt()); buffer.writeInt((encoding.toInt() shl 16) or height.toInt()); buffer.writeBuffer(bitmap)
        }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val imageId = buffer.readInt(); var width = buffer.readInt(); var height = buffer.readInt()
            var type: Int; var encoding: Int
            if (width > 0xffff) { type = width shr 16; width = width and 0xffff } else type = TYPE_PNG_8888.toInt()
            if (height > 0xffff) { encoding = height shr 16; height = height and 0xffff } else encoding = ENCODING_INLINE.toInt()
            if (width < 1 || height < 1 || height > MAX_IMAGE_DIMENSION || width > MAX_IMAGE_DIMENSION) throw RuntimeException("Invalid dimension ${width}x$height")
            val bitmap = buffer.readBuffer(); val bd = BitmapData(imageId, width, height, bitmap); bd.mType = type.toShort(); bd.mEncoding = encoding.toShort(); operations.add(bd)
        }
    }
}
