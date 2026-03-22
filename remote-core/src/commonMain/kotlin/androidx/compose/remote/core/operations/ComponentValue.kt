// Copyright (C) 2024 The Android Open Source Project. Apache License 2.0.
package androidx.compose.remote.core.operations
import androidx.compose.remote.core.*
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable
class ComponentValue(private var mType: Int = WIDTH, private var mComponentID: Int = -1, private var mValueId: Int = -1) : Operation(), Serializable, ComponentData {
    fun getType(): Int = mType; fun getComponentId(): Int = mComponentID; fun getValueId(): Int = mValueId; fun setComponentId(componentId: Int) { mComponentID = componentId }
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mType, mComponentID, mValueId) }
    override fun toString(): String = "ComponentValue($mType, $mComponentID, $mValueId)"
    override fun apply(context: RemoteContext) {}
    override fun deepToString(indent: String): String = indent + toString()
    override fun serialize(serializer: MapSerializer) { serializer.addType("ComponentValue").add("valueId", mValueId).add("componentId", mComponentID) }
    companion object {
        private val OP_CODE = Operations.COMPONENT_VALUE; const val WIDTH = 0; const val HEIGHT = 1; fun name(): String = "ComponentValue"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, type: Int, componentId: Int, valueId: Int) { buffer.start(OP_CODE); buffer.writeInt(type); buffer.writeInt(componentId); buffer.writeInt(valueId) }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { operations.add(ComponentValue(buffer.readInt(), buffer.readInt(), buffer.readInt())) }
    }
}
