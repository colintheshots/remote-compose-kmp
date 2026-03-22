// Copyright (C) 2024 The Android Open Source Project. Apache License 2.0.
package androidx.compose.remote.core.operations
import androidx.compose.remote.core.*
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable
class PathCreate(var mInstanceId: Int, startX: Float, startY: Float) : PaintOperation(), VariableSupport, Serializable {
    var mFloatPath: FloatArray = floatArrayOf(PathData.MOVE_NAN, startX, startY); var mOutputPath: FloatArray = mFloatPath.copyOf()
    override fun updateVariables(context: RemoteContext) { for (i in mFloatPath.indices) { val v = mFloatPath[i]; if (Utils.isVariable(v)) mOutputPath[i] = if (v.isNaN()) context.getFloat(Utils.idFromNan(v)) else v; else mOutputPath[i] = v } }
    override fun registerListening(context: RemoteContext) { for (v in mFloatPath) if (v.isNaN()) context.listensTo(Utils.idFromNan(v), this) }
    override fun markDirty() { super<PaintOperation>.markDirty() }
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mInstanceId, mFloatPath[1], mFloatPath[2]) }
    override fun toString(): String = "PathCreate[$mInstanceId]"
    override fun deepToString(indent: String): String = PathData.pathString(mFloatPath)
    override fun paint(context: PaintContext) { apply(context.getContext()) }
    override fun apply(context: RemoteContext) { context.loadPathData(mInstanceId, 0, mOutputPath) }
    override fun serialize(serializer: MapSerializer) { serializer.addType("PathCreate").add("id", mInstanceId).addPath("path", mFloatPath) }
    companion object {
        private val OP_CODE = Operations.PATH_CREATE; fun name(): String = "PathCreate"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, id: Int, startX: Float, startY: Float) { buffer.start(OP_CODE); buffer.writeInt(id); buffer.writeFloat(startX); buffer.writeFloat(startY) }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { operations.add(PathCreate(buffer.readInt(), buffer.readFloat(), buffer.readFloat())) }
    }
}
