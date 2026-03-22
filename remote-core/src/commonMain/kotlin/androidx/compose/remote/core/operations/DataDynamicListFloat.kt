// Copyright (C) 2024 The Android Open Source Project. Apache License 2.0.
package androidx.compose.remote.core.operations
import androidx.compose.remote.core.*
import androidx.compose.remote.core.operations.utilities.ArrayAccess
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable
class DataDynamicListFloat(val mId: Int, private val mArrayLength: Float) : Operation(), VariableSupport, ArrayAccess, Serializable {
    private var mArrayLengthOut: Float = mArrayLength; private var mValues: FloatArray = FloatArray(mArrayLength.toInt().coerceAtMost(2000))
    override fun updateVariables(context: RemoteContext) { mArrayLengthOut = if (mArrayLength.isNaN()) context.getFloat(Utils.idFromNan(mArrayLength)) else mArrayLength; if (mArrayLengthOut.toInt() != mValues.size) mValues = FloatArray(mArrayLengthOut.toInt()) }
    override fun registerListening(context: RemoteContext) { context.addCollection(mId, this); if (mArrayLength.isNaN()) context.listensTo(Utils.idFromNan(mArrayLength), this) }
    override fun markDirty() { super<Operation>.markDirty() }
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mId, mValues.size.toFloat()) }
    override fun toString(): String = "DynamicDataListFloat[${Utils.idString(mId)}]"
    override fun apply(context: RemoteContext) { context.addCollection(mId, this) }
    override fun deepToString(indent: String): String = indent + toString()
    override fun getFloatValue(index: Int): Float = mValues[index]
    override fun getFloats(): FloatArray = mValues
    override fun getLength(): Int = mValues.size
    fun updateValues(values: FloatArray) { mValues = values.copyOf() }
    fun update(lc: DataDynamicListFloat) { mValues = lc.mValues }
    override fun serialize(serializer: MapSerializer) { serializer.addType("DataDynamicListFloat").add("id", mId) }
    companion object {
        private val OP_CODE = Operations.DYNAMIC_FLOAT_LIST; private const val MAX_FLOAT_ARRAY = 2000; fun name(): String = "DataDynamicListFloat"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, id: Int, nbValues: Float) { buffer.start(OP_CODE); buffer.writeInt(id); buffer.writeFloat(nbValues) }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { val id = buffer.readInt(); val len = buffer.readFloat(); if (len > MAX_FLOAT_ARRAY) throw RuntimeException("$len > max"); operations.add(DataDynamicListFloat(id, len)) }
    }
}
