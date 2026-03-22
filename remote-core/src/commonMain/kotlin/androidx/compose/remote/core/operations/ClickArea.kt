// Copyright (C) 2024 The Android Open Source Project. Apache License 2.0.
package androidx.compose.remote.core.operations
import androidx.compose.remote.core.*
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable
class ClickArea(var mId: Int, var mContentDescription: Int, var mLeft: Float, var mTop: Float, var mRight: Float, var mBottom: Float, var mMetadata: Int) : Operation(), RemoteComposeOperation, VariableSupport, Serializable {
    var mOutLeft = mLeft; var mOutTop = mTop; var mOutRight = mRight; var mOutBottom = mBottom
    override fun registerListening(context: RemoteContext) { if (mLeft.isNaN()) context.listensTo(Utils.idFromNan(mLeft), this); if (mTop.isNaN()) context.listensTo(Utils.idFromNan(mTop), this); if (mRight.isNaN()) context.listensTo(Utils.idFromNan(mRight), this); if (mBottom.isNaN()) context.listensTo(Utils.idFromNan(mBottom), this) }
    override fun updateVariables(context: RemoteContext) { mOutLeft = if (mLeft.isNaN()) context.getFloat(Utils.idFromNan(mLeft)) else mLeft; mOutTop = if (mTop.isNaN()) context.getFloat(Utils.idFromNan(mTop)) else mTop; mOutRight = if (mRight.isNaN()) context.getFloat(Utils.idFromNan(mRight)) else mRight; mOutBottom = if (mBottom.isNaN()) context.getFloat(Utils.idFromNan(mBottom)) else mBottom }
    override fun markDirty() { super<Operation>.markDirty() }
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mId, mContentDescription, mLeft, mTop, mRight, mBottom, mMetadata) }
    override fun toString(): String = "CLICK_AREA <$mId $mLeft $mTop $mRight $mBottom>"
    override fun apply(context: RemoteContext) { context.addClickArea(mId, mContentDescription, mOutLeft, mOutTop, mOutRight, mOutBottom, mMetadata) }
    override fun deepToString(indent: String): String = indent + toString()
    override fun serialize(serializer: MapSerializer) { serializer.addType("ClickArea").add("id", mId).add("left", mLeft, mOutLeft).add("top", mTop, mOutTop).add("right", mRight, mOutRight).add("bottom", mBottom, mOutBottom).add("metadata", mMetadata) }
    companion object {
        private val OP_CODE = Operations.CLICK_AREA; fun name(): String = "ClickArea"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, id: Int, contentDescription: Int, left: Float, top: Float, right: Float, bottom: Float, metadata: Int) { buffer.start(OP_CODE); buffer.writeInt(id); buffer.writeInt(contentDescription); buffer.writeFloat(left); buffer.writeFloat(top); buffer.writeFloat(right); buffer.writeFloat(bottom); buffer.writeInt(metadata) }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { operations.add(ClickArea(buffer.readInt(), buffer.readInt(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readInt())) }
    }
}
