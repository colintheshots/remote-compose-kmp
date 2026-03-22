// Copyright (C) 2024 The Android Open Source Project. Apache License 2.0.
package androidx.compose.remote.core.operations
import androidx.compose.remote.core.*
import androidx.compose.remote.core.operations.utilities.ArrayAccess
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable
class DataListIds(private val mId: Int, private val mIds: IntArray) : Operation(), VariableSupport, ArrayAccess, Serializable {
    override fun updateVariables(context: RemoteContext) {}; override fun registerListening(context: RemoteContext) {}; override fun markDirty() { super<Operation>.markDirty() }
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mId, mIds) }
    override fun toString(): String = "map[${Utils.idString(mId)}] \"${mIds.contentToString()}\""
    override fun apply(context: RemoteContext) { context.addCollection(mId, this) }
    override fun deepToString(indent: String): String = indent + toString()
    override fun getFloatValue(index: Int): Float = Float.NaN; override fun getId(index: Int): Int = mIds[index]; override fun getFloats(): FloatArray? = null; override fun getLength(): Int = mIds.size; override fun getIntValue(index: Int): Int = 0
    override fun serialize(serializer: MapSerializer) { serializer.addType("IdListData").add("id", mId) }
    companion object {
        private val OP_CODE = Operations.ID_LIST; private const val MAX_LIST = 2000; fun name(): String = "IdListData"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, id: Int, ids: IntArray) { buffer.start(OP_CODE); buffer.writeInt(id); buffer.writeInt(ids.size); for (i in ids) buffer.writeInt(i) }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { val id = buffer.readInt(); val len = buffer.readInt(); if (len > MAX_LIST) throw RuntimeException("$len > max"); operations.add(DataListIds(id, IntArray(len) { buffer.readInt() })) }
    }
}
