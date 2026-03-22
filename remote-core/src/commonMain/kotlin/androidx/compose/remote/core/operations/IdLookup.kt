// Copyright (C) 2024 The Android Open Source Project. Apache License 2.0.
package androidx.compose.remote.core.operations
import androidx.compose.remote.core.*
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable
class IdLookup(var mTextId: Int, var mDataSetId: Int, var mIndex: Float) : Operation(), VariableSupport, Serializable {
    var mOutIndex: Float = mIndex
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mTextId, mDataSetId, mIndex) }; override fun toString(): String = "IdLookup[${Utils.idString(mTextId)}]"
    override fun updateVariables(context: RemoteContext) { if (mIndex.isNaN()) mOutIndex = context.getFloat(Utils.idFromNan(mIndex)) }
    override fun registerListening(context: RemoteContext) { if (mIndex.isNaN()) context.listensTo(Utils.idFromNan(mIndex), this) }
    override fun markDirty() { super<Operation>.markDirty() }
    override fun apply(context: RemoteContext) { val id = context.getCollectionsAccess()!!.getId(mDataSetId, mOutIndex.toInt()); context.loadInteger(mTextId, id) }
    override fun deepToString(indent: String): String = indent + toString()
    override fun serialize(serializer: MapSerializer) { serializer.addType("IdLookup").add("textId", mTextId).add("dataSetId", mDataSetId).add("indexId", mIndex, mOutIndex) }
    companion object { private val OP_CODE = Operations.ID_LOOKUP; fun name(): String = "IdLookup"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, textId: Int, dataSet: Int, index: Float) { buffer.start(OP_CODE); buffer.writeInt(textId); buffer.writeInt(dataSet); buffer.writeFloat(index) }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { operations.add(IdLookup(buffer.readInt(), buffer.readInt(), buffer.readFloat())) } }
}
