// Copyright (C) 2024 The Android Open Source Project. Apache License 2.0.
package androidx.compose.remote.core.operations
import androidx.compose.remote.core.*
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable
class BitmapFontData : Operation, Serializable {
    var mVersion: Short; var mId: Int; var mFontGlyphs: Array<Glyph>; var mKerningTable: MutableMap<String, Short>
    class Glyph(var mChars: String? = null, var mBitmapId: Int = 0, var mMarginLeft: Short = 0, var mMarginTop: Short = 0, var mMarginRight: Short = 0, var mMarginBottom: Short = 0, var mBitmapWidth: Short = 0, var mBitmapHeight: Short = 0)
    constructor(id: Int, fontGlyphs: Array<Glyph>) : super() { mId = id; mFontGlyphs = fontGlyphs; mVersion = VERSION_1; mKerningTable = mutableMapOf()
        require(fontGlyphs.size < MAX_GLYPHS); mFontGlyphs.sortByDescending { it.mChars?.length ?: 0 } }
    constructor(id: Int, fontGlyphs: Array<Glyph>, version: Short, kerningTable: MutableMap<String, Short>) : super() { mId = id; mFontGlyphs = fontGlyphs; mVersion = version; mKerningTable = kerningTable
        require(fontGlyphs.size < MAX_GLYPHS); mFontGlyphs.sortByDescending { it.mChars?.length ?: 0 } }
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mId, mFontGlyphs, mKerningTable) }
    override fun toString(): String = "BITMAP FONT DATA $mId"
    override fun apply(context: RemoteContext) { context.putObject(mId, this) }
    override fun deepToString(indent: String): String = indent + toString()
    fun lookupGlyph(string: String, offset: Int): Glyph? { for (g in mFontGlyphs) { if (g.mChars != null && string.startsWith(g.mChars!!, offset)) return g }; return null }
    override fun serialize(serializer: MapSerializer) { serializer.addType("BitmapFontData").add("id", mId) }
    companion object {
        private val OP_CODE = Operations.DATA_BITMAP_FONT; const val VERSION_1: Short = 0; const val VERSION_2: Short = 1; private const val MAX_GLYPHS = 0xffff
        fun name(): String = "BitmapFontData"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, id: Int, glyphs: Array<Glyph>, kerningTable: Map<String, Short>?) {
            buffer.start(OP_CODE); buffer.writeInt(id)
            if (kerningTable != null && kerningTable.isNotEmpty()) buffer.writeInt(glyphs.size + (VERSION_2.toInt() shl 16)) else buffer.writeInt(glyphs.size)
            for (g in glyphs) { buffer.writeUTF8(g.mChars ?: ""); buffer.writeInt(g.mBitmapId); buffer.writeShort(g.mMarginLeft.toInt()); buffer.writeShort(g.mMarginTop.toInt()); buffer.writeShort(g.mMarginRight.toInt()); buffer.writeShort(g.mMarginBottom.toInt()); buffer.writeShort(g.mBitmapWidth.toInt()); buffer.writeShort(g.mBitmapHeight.toInt()) }
            if (kerningTable != null && kerningTable.isNotEmpty()) { buffer.writeShort(kerningTable.size); for ((k, v) in kerningTable) { buffer.writeUTF8(k); buffer.writeShort(v.toInt()) } }
        }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val id = buffer.readInt(); val vAndN = buffer.readInt(); val version = (vAndN ushr 16).toShort(); val numGlyphs = vAndN and 0xffff
            val glyphs = Array(numGlyphs) { Glyph().also { g -> g.mChars = buffer.readUTF8(); g.mBitmapId = buffer.readInt(); g.mMarginLeft = buffer.readShort().toShort(); g.mMarginTop = buffer.readShort().toShort(); g.mMarginRight = buffer.readShort().toShort(); g.mMarginBottom = buffer.readShort().toShort(); g.mBitmapWidth = buffer.readShort().toShort(); g.mBitmapHeight = buffer.readShort().toShort() } }
            val kt = mutableMapOf<String, Short>()
            if (version >= VERSION_2) { val n = buffer.readShort(); for (i in 0 until n) { kt[buffer.readUTF8()] = buffer.readShort().toShort() } }
            operations.add(BitmapFontData(id, glyphs, version, kt))
        }
    }
}
