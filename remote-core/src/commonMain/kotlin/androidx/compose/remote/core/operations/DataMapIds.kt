// Copyright (C) 2024 The Android Open Source Project. Apache License 2.0.
package androidx.compose.remote.core.operations
import androidx.compose.remote.core.*
import androidx.compose.remote.core.operations.utilities.DataMap
class DataMapIds(var mId: Int, names: Array<String>, types: ByteArray, ids: IntArray) : Operation() {
    val mDataMap = DataMap(names, types, ids)
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mId, mDataMap.names, mDataMap.types, mDataMap.ids) }
    override fun toString(): String { val sb = StringBuilder("DataMapIds[${Utils.idString(mId)}] "); for (i in mDataMap.names.indices) { if (i != 0) sb.append(" "); sb.append("[${mDataMap.names[i]}]=${mDataMap.ids[i]}") }; return sb.toString() }
    override fun apply(context: RemoteContext) { context.putDataMap(mId, mDataMap) }
    override fun deepToString(indent: String): String = indent + toString()
    companion object {
        private val OP_CODE = Operations.ID_MAP; private const val MAX_MAP = 2000; const val TYPE_STRING: Byte = 0; const val TYPE_INT: Byte = 1; const val TYPE_FLOAT: Byte = 2; const val TYPE_LONG: Byte = 3; const val TYPE_BOOLEAN: Byte = 4
        fun name(): String = "DataMapIds"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, id: Int, names: Array<String>, type: ByteArray?, ids: IntArray) { buffer.start(OP_CODE); buffer.writeInt(id); buffer.writeInt(names.size); for (i in names.indices) { buffer.writeUTF8(names[i]); buffer.writeByte(type?.get(i)?.toInt() ?: 2); buffer.writeInt(ids[i]) } }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { val id = buffer.readInt(); val len = buffer.readInt(); if (len > MAX_MAP) throw RuntimeException("$len > max"); val names = Array(len) { "" }; val ids = IntArray(len); val types = ByteArray(len); for (i in 0 until len) { names[i] = buffer.readUTF8(); types[i] = buffer.readByte().toByte(); ids[i] = buffer.readInt() }; operations.add(DataMapIds(id, names, types, ids)) }
    }
}
