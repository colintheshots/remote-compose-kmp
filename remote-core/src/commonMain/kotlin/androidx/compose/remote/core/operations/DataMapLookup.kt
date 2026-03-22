// Copyright (C) 2024 The Android Open Source Project. Apache License 2.0.
package androidx.compose.remote.core.operations
import androidx.compose.remote.core.*
import androidx.compose.remote.core.types.LongConstant
import androidx.compose.remote.core.types.BooleanConstant
class DataMapLookup(var mId: Int, var mDataMapId: Int, var mStringId: Int) : Operation() {
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mId, mDataMapId, mStringId) }
    override fun toString(): String = "DataMapLookup[$mId] = ${Utils.idString(mDataMapId)} $mStringId"
    override fun apply(context: RemoteContext) {
        val str = context.getText(mStringId) ?: ""; val data = context.getDataMap(mDataMapId) ?: return; val pos = data.getPos(str); val type = data.getType(pos); val dataId = data.getId(pos)
        when (type) { DataMapIds.TYPE_STRING -> context.loadText(mId, context.getText(dataId) ?: ""); DataMapIds.TYPE_INT -> context.loadInteger(mId, context.getInteger(dataId)); DataMapIds.TYPE_FLOAT -> context.loadFloat(mId, context.getFloat(dataId))
            DataMapIds.TYPE_LONG -> { val lc = context.getObject(dataId) as LongConstant; context.loadInteger(mId, lc.getValue().toInt()) }; DataMapIds.TYPE_BOOLEAN -> { val bc = context.getObject(dataId) as BooleanConstant; context.loadInteger(mId, if (bc.getValue()) 1 else 0) } }
    }
    override fun deepToString(indent: String): String = indent + toString()
    companion object {
        private val OP_CODE = Operations.DATA_MAP_LOOKUP; fun name(): String = "DataMapLookup"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, id: Int, dataMapId: Int, keyStringId: Int) { buffer.start(OP_CODE); buffer.writeInt(id); buffer.writeInt(dataMapId); buffer.writeInt(keyStringId) }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { operations.add(DataMapLookup(buffer.readInt(), buffer.readInt(), buffer.readInt())) }
    }
}
