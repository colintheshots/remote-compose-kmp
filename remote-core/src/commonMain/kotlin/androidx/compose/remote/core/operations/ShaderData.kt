// Copyright (C) 2024 The Android Open Source Project. Apache License 2.0.
package androidx.compose.remote.core.operations
import androidx.compose.remote.core.*
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable
class ShaderData(var mShaderID: Int, var mShaderTextId: Int, floatMap: HashMap<String, FloatArray>?, intMap: HashMap<String, IntArray>?, bitmapMap: HashMap<String, Int>?) : Operation(), VariableSupport, Serializable {
    var mUniformRawFloatMap: HashMap<String, FloatArray>? = null; var mUniformFloatMap: HashMap<String, FloatArray>? = null
    var mUniformIntMap: HashMap<String, IntArray>? = null; var mUniformBitmapMap: HashMap<String, Int>? = null; private var mShaderValid = false
    init { floatMap?.let { mUniformFloatMap = HashMap(); mUniformRawFloatMap = HashMap(); for ((k, v) in it) { mUniformRawFloatMap!![k] = v; mUniformFloatMap!![k] = v } }; intMap?.let { mUniformIntMap = HashMap(it) }; bitmapMap?.let { mUniformBitmapMap = HashMap(it) } }
    fun getShaderTextId(): Int = mShaderTextId
    fun getUniformFloatNames(): Array<String> = mUniformFloatMap?.keys?.toTypedArray() ?: emptyArray()
    fun getUniformFloats(name: String): FloatArray = mUniformFloatMap?.get(name) ?: FloatArray(0)
    fun getUniformIntegerNames(): Array<String> = mUniformIntMap?.keys?.toTypedArray() ?: emptyArray()
    fun getUniformInts(name: String): IntArray = mUniformIntMap?.get(name) ?: IntArray(0)
    fun getUniformBitmapNames(): Array<String> = mUniformBitmapMap?.keys?.toTypedArray() ?: emptyArray()
    fun getUniformBitmapId(name: String): Int = mUniformBitmapMap?.get(name) ?: -1
    override fun updateVariables(context: RemoteContext) {
        val raw = mUniformRawFloatMap ?: return; val fmap = mUniformFloatMap ?: return
        for ((name, value) in raw) { var out: FloatArray? = null; for (i in value.indices) { if (value[i].isNaN()) { if (out == null) out = value.copyOf(); out[i] = context.getFloat(Utils.idFromNan(value[i])) } }; fmap[name] = out ?: value }
    }
    override fun registerListening(context: RemoteContext) { mUniformRawFloatMap?.let { for ((_, v) in it) for (f in v) if (f.isNaN()) context.listensTo(Utils.idFromNan(f), this) } }
    override fun markDirty() { super<Operation>.markDirty() }
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mShaderID, mShaderTextId, mUniformFloatMap, mUniformIntMap, mUniformBitmapMap) }
    override fun toString(): String = "SHADER DATA $mShaderID"
    override fun apply(context: RemoteContext) { if (mShaderValid) context.loadShader(mShaderID, this) }
    override fun deepToString(indent: String): String = indent + toString()
    fun enable(shaderValid: Boolean) { mShaderValid = shaderValid }
    override fun serialize(serializer: MapSerializer) { serializer.addType("ShaderData").add("shaderTextId", mShaderTextId).add("shaderID", mShaderID) }
    companion object {
        private val OP_CODE = Operations.DATA_SHADER; private const val MAX_FLOAT_LEN = 200; fun name(): String = "ShaderData"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, shaderID: Int, shaderTextId: Int, floatMap: HashMap<String, FloatArray>?, intMap: HashMap<String, IntArray>?, bitmapMap: HashMap<String, Int>?) {
            buffer.start(OP_CODE); buffer.writeInt(shaderID); buffer.writeInt(shaderTextId)
            val fs = floatMap?.size ?: 0; val is2 = intMap?.size ?: 0; val bs = bitmapMap?.size ?: 0; buffer.writeInt(fs or (is2 shl 8) or (bs shl 16))
            floatMap?.let { for ((n, v) in it) { buffer.writeUTF8(n); buffer.writeInt(v.size); for (f in v) buffer.writeFloat(f) } }
            intMap?.let { for ((n, v) in it) { buffer.writeUTF8(n); buffer.writeInt(v.size); for (i in v) buffer.writeInt(i) } }
            bitmapMap?.let { for ((n, v) in it) { buffer.writeUTF8(n); buffer.writeInt(v) } }
        }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val shaderID = buffer.readInt(); val shaderTextId = buffer.readInt(); val sizes = buffer.readInt()
            var fm: HashMap<String, FloatArray>? = null; var im: HashMap<String, IntArray>? = null; var bm: HashMap<String, Int>? = null
            val fs = sizes and 0xFF; if (fs > 0) { fm = HashMap(); for (i in 0 until fs) { val n = buffer.readUTF8(); val len = buffer.readInt(); if (len > MAX_FLOAT_LEN) throw RuntimeException("Float array too long"); fm[n] = FloatArray(len) { buffer.readFloat() } } }
            val is2 = (sizes shr 8) and 0xFF; if (is2 > 0) { im = HashMap(); for (i in 0 until is2) { val n = buffer.readUTF8(); val len = buffer.readInt(); im[n] = IntArray(len) { buffer.readInt() } } }
            val bs = (sizes shr 16) and 0xFF; if (bs > 0) { bm = HashMap(); for (i in 0 until bs) { bm[buffer.readUTF8()] = buffer.readInt() } }
            operations.add(ShaderData(shaderID, shaderTextId, fm, im, bm))
        }
    }
}
