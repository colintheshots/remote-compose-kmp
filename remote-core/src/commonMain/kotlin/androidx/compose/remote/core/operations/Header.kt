// Copyright (C) 2024 The Android Open Source Project. Apache License 2.0.
package androidx.compose.remote.core.operations
import androidx.compose.remote.core.*
import androidx.compose.remote.core.operations.utilities.IntMap
class Header : Operation, RemoteComposeOperation {
    var mMajorVersion: Int; var mMinorVersion: Int; var mPatchVersion: Int; var mWidth: Int = 256; var mHeight: Int = 256; var mDensity: Float = 3f; var mCapabilities: Long = 0; var mProfiles: Int = 0; private var mProperties: IntMap<Any>? = null
    constructor(majorVersion: Int, minorVersion: Int, patchVersion: Int, width: Int, height: Int, density: Float, capabilities: Long) : super() { mMajorVersion = majorVersion; mMinorVersion = minorVersion; mPatchVersion = patchVersion; mWidth = width; mHeight = height; mDensity = density; mCapabilities = capabilities }
    constructor(majorVersion: Int, minorVersion: Int, patchVersion: Int, properties: IntMap<Any>?) : super() { mMajorVersion = majorVersion; mMinorVersion = minorVersion; mPatchVersion = patchVersion; mProperties = properties; if (properties != null) { mWidth = (properties.get(DOC_WIDTH.toInt()) as? Int) ?: 256; mHeight = (properties.get(DOC_HEIGHT.toInt()) as? Int) ?: 256; mDensity = (properties.get(DOC_DENSITY_AT_GENERATION.toInt()) as? Float) ?: 0f; mProfiles = (properties.get(DOC_PROFILES.toInt()) as? Int) ?: 0 } }
    fun get(property: Short): Any? = mProperties?.get(property.toInt()); fun getProfiles(): Int = mProfiles
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mWidth, mHeight, mDensity, mCapabilities) }
    override fun toString(): String = "HEADER v$mMajorVersion.$mMinorVersion.$mPatchVersion, $mWidth x $mHeight"
    override fun apply(context: RemoteContext) { context.header(mMajorVersion, mMinorVersion, mPatchVersion, mWidth, mHeight, mCapabilities, mProperties) }
    override fun deepToString(indent: String): String = toString()
    companion object {
        private val OP_CODE = Operations.HEADER; private const val MAGIC_NUMBER = 0x048C0000; private const val MAX_TABLE_SIZE = 1000
        const val DOC_WIDTH: Short = 5; const val DOC_HEIGHT: Short = 6; const val DOC_DENSITY_AT_GENERATION: Short = 7; const val DOC_DESIRED_FPS: Short = 8; const val DOC_CONTENT_DESCRIPTION: Short = 9; const val DOC_SOURCE: Short = 11; const val DOC_DATA_UPDATE: Short = 12; const val HOST_EXCEPTION_HANDLER: Short = 13; const val DOC_PROFILES: Short = 14
        private const val DATA_TYPE_INT: Short = 0; private const val DATA_TYPE_FLOAT: Short = 1; private const val DATA_TYPE_LONG: Short = 2; private const val DATA_TYPE_STRING: Short = 3
        fun name(): String = "Header"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, width: Int, height: Int, @Suppress("UNUSED_PARAMETER") density: Float, capabilities: Long) { buffer.start(OP_CODE); buffer.writeInt(CoreDocument.MAJOR_VERSION); buffer.writeInt(CoreDocument.MINOR_VERSION); buffer.writeInt(CoreDocument.PATCH_VERSION); buffer.writeInt(width); buffer.writeInt(height); buffer.writeLong(capabilities) }
        fun apply(buffer: WireBuffer, apiLevel: Int, tags: ShortArray, values: Array<Any>) {
            buffer.start(OP_CODE); buffer.writeInt(apiLevel or MAGIC_NUMBER); buffer.writeInt(CoreDocument.MINOR_VERSION); buffer.writeInt(CoreDocument.PATCH_VERSION)
            buffer.writeInt(tags.size); for (i in tags.indices) { var tag = tags[i]
                when (val v = values[i]) {
                    is String -> { tag = (tag.toInt() or (DATA_TYPE_STRING.toInt() shl 10)).toShort(); buffer.writeShort(tag.toInt()); val data = v.encodeToByteArray(); buffer.writeShort(data.size + 4); buffer.writeBuffer(data) }
                    is Int -> { tag = (tag.toInt() or (DATA_TYPE_INT.toInt() shl 10)).toShort(); buffer.writeShort(tag.toInt()); buffer.writeShort(4); buffer.writeInt(v) }
                    is Float -> { tag = (tag.toInt() or (DATA_TYPE_FLOAT.toInt() shl 10)).toShort(); buffer.writeShort(tag.toInt()); buffer.writeShort(4); buffer.writeFloat(v) }
                    is Long -> { tag = (tag.toInt() or (DATA_TYPE_LONG.toInt() shl 10)).toShort(); buffer.writeShort(tag.toInt()); buffer.writeShort(8); buffer.writeLong(v) }
                } } }
        fun readApiLevel(buffer: WireBuffer): Int {
            if (!buffer.available()) return -1; val savedPos = buffer.index
            try { val opCode = buffer.readByte(); if (opCode != OP_CODE) return -1; val majorVersion = buffer.readInt(); return majorVersion and 0xFFFF } finally { buffer.index = savedPos } }
        fun readDirect(buffer: WireBuffer): Header {
            val opCode = buffer.readByte(); val majorVersion = buffer.readInt(); val minorVersion = buffer.readInt(); val patchVersion = buffer.readInt()
            if (majorVersion < 0x10000) { val width = buffer.readInt(); val height = buffer.readInt(); val capabilities = buffer.readLong(); return Header(majorVersion, minorVersion, patchVersion, width, height, 1f, capabilities) }
            val mv = majorVersion and 0xFFFF; val length = buffer.readInt(); val types = ShortArray(length); val values = arrayOfNulls<Any>(length)
            for (i in 0 until length) { val tag = buffer.readShort().toShort(); buffer.readShort(); val dt = tag.toInt() shr 10; types[i] = (tag.toInt() and 0x3F).toShort()
                when (dt) { DATA_TYPE_INT.toInt() -> values[i] = buffer.readInt(); DATA_TYPE_FLOAT.toInt() -> values[i] = buffer.readFloat(); DATA_TYPE_LONG.toInt() -> values[i] = buffer.readLong(); DATA_TYPE_STRING.toInt() -> values[i] = buffer.readUTF8() } }
            val map = IntMap<Any>(); for (i in 0 until length) { values[i]?.let { map.put(types[i].toInt(), it) } }; return Header(mv, minorVersion, patchVersion, map) }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val majorVersion = buffer.readInt(); val minorVersion = buffer.readInt(); val patchVersion = buffer.readInt()
            if (majorVersion < 0x10000) { val width = buffer.readInt(); val height = buffer.readInt(); val capabilities = buffer.readLong(); operations.add(Header(majorVersion, minorVersion, patchVersion, width, height, 1f, capabilities)) }
            else { val mv = majorVersion and 0xFFFF; val length = buffer.readInt(); if (length > MAX_TABLE_SIZE) throw RuntimeException("Invalid table size"); val types = ShortArray(length); val values = arrayOfNulls<Any>(length)
                for (i in 0 until length) { val tag = buffer.readShort().toShort(); buffer.readShort(); val dt = tag.toInt() shr 10; types[i] = (tag.toInt() and 0x3F).toShort()
                    when (dt) { DATA_TYPE_INT.toInt() -> values[i] = buffer.readInt(); DATA_TYPE_FLOAT.toInt() -> values[i] = buffer.readFloat(); DATA_TYPE_LONG.toInt() -> values[i] = buffer.readLong(); DATA_TYPE_STRING.toInt() -> values[i] = buffer.readUTF8() } }
                val map = IntMap<Any>(); for (i in 0 until length) { values[i]?.let { map.put(types[i].toInt(), it) } }; operations.add(Header(mv, minorVersion, patchVersion, map)) }
        }
    }
}
