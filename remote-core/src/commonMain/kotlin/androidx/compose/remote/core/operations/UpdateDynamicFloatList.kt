// Copyright (C) 2024 The Android Open Source Project. Apache License 2.0.
package androidx.compose.remote.core.operations
import androidx.compose.remote.core.*
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable
class UpdateDynamicFloatList(var mArrayId: Int, var mIndex: Float, var mValue: Float) : Operation(), VariableSupport, Serializable {
    var mIndexOut: Float = mIndex; var mValueOut: Float = mValue
    override fun updateVariables(context: RemoteContext) { mIndexOut = if (mIndex.isNaN()) context.getFloat(Utils.idFromNan(mIndex)) else mIndex; mValueOut = if (mValue.isNaN()) context.getFloat(Utils.idFromNan(mValue)) else mValue }
    override fun registerListening(context: RemoteContext) { if (mIndex.isNaN()) context.listensTo(Utils.idFromNan(mIndex), this); if (mValue.isNaN()) context.listensTo(Utils.idFromNan(mValue), this) }
    override fun markDirty() { super<Operation>.markDirty() }
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mArrayId, mIndexOut, mValueOut) }
    override fun apply(context: RemoteContext) { val values = context.mRemoteComposeState?.getDynamicFloats(mArrayId); if (values != null) { val index = mIndexOut.toInt(); if (index < values.size) values[index] = mValueOut; context.mRemoteComposeState?.markVariableDirty(mArrayId) } }
    override fun toString(): String = "UpdateDynamicFloatList array: ${Utils.idString(Utils.idFromNan(mArrayId.toFloat()))} index: ${Utils.floatToString(mIndexOut)} value: ${Utils.floatToString(mValueOut)}"
    override fun deepToString(indent: String): String = toString()
    override fun serialize(serializer: MapSerializer) { serializer.add("arrayId", mArrayId).add("index", mIndex, mIndexOut).add("value", mValue, mValueOut) }
    companion object {
        private val OP_CODE = Operations.UPDATE_DYNAMIC_FLOAT_LIST; fun name(): String = "UpdateDynamicFloatList"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, id: Int, index: Float, value: Float) { buffer.start(OP_CODE); buffer.writeInt(id); buffer.writeFloat(index); buffer.writeFloat(value) }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { operations.add(UpdateDynamicFloatList(buffer.readInt(), buffer.readFloat(), buffer.readFloat())) }
    }
}
