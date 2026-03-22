// Copyright (C) 2024 The Android Open Source Project. Apache License 2.0.
package androidx.compose.remote.core.operations
import androidx.compose.remote.core.*
import androidx.compose.remote.core.operations.utilities.ArrayAccess
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable
class DataListFloat(val mId: Int, private var mValues: FloatArray) : Operation(), VariableSupport, ArrayAccess, Serializable {
    override fun updateVariables(context: RemoteContext) {}
    override fun registerListening(context: RemoteContext) { context.addCollection(mId, this); for (v in mValues) if (Utils.isVariable(v)) context.listensTo(Utils.idFromNan(v), this) }
    override fun markDirty() { super<Operation>.markDirty() }
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mId, mValues) }
    override fun toString(): String = "DataListFloat[${Utils.idString(mId)}] ${mValues.contentToString()}"
    override fun apply(context: RemoteContext) { context.addCollection(mId, this) }
    override fun deepToString(indent: String): String = indent + toString()
    override fun getFloatValue(index: Int): Float = mValues[index]
    override fun getFloats(): FloatArray = mValues
    override fun getLength(): Int = mValues.size
    fun update(lc: DataListFloat) { mValues = lc.mValues }
    override fun serialize(serializer: MapSerializer) { serializer.addType("IdListData").add("id", mId) }
    companion object {
        private val OP_CODE = Operations.FLOAT_LIST; private const val MAX_FLOAT_ARRAY = 2000; fun name(): String = "IdListData"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, id: Int, values: FloatArray) { buffer.start(OP_CODE); buffer.writeInt(id); buffer.writeInt(values.size); for (v in values) buffer.writeFloat(v) }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { val id = buffer.readInt(); val len = buffer.readInt(); if (len > MAX_FLOAT_ARRAY) throw RuntimeException("$len > max"); operations.add(DataListFloat(id, FloatArray(len) { buffer.readFloat() })) }
    }
}
