// Copyright (C) 2024 The Android Open Source Project. Apache License 2.0.
package androidx.compose.remote.core.operations
import androidx.compose.remote.core.*
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable
class PathData(var mInstanceId: Int, var mFloatPath: FloatArray, var mWinding: Int) : Operation(), VariableSupport, Serializable {
    var mOutputPath: FloatArray = mFloatPath.copyOf(); private var mPathChanged = true
    override fun updateVariables(context: RemoteContext) { for (i in mFloatPath.indices) { val v = mFloatPath[i]; if (Utils.isVariable(v)) { val tmp = mOutputPath[i]; mOutputPath[i] = if (v.isNaN()) context.getFloat(Utils.idFromNan(v)) else v; if (tmp != mOutputPath[i]) mPathChanged = true } else mOutputPath[i] = v } }
    override fun registerListening(context: RemoteContext) { for (v in mFloatPath) if (v.isNaN()) context.listensTo(Utils.idFromNan(v), this) }
    override fun markDirty() { super<Operation>.markDirty() }
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mInstanceId, mOutputPath) }
    override fun toString(): String = "PathData[$mInstanceId] = \"${pathString(mFloatPath)}\""
    override fun deepToString(indent: String): String = pathString(mFloatPath)
    override fun apply(context: RemoteContext) { if (mPathChanged) context.loadPathData(mInstanceId, mWinding, mOutputPath); mPathChanged = false }
    override fun serialize(serializer: MapSerializer) { serializer.addType("PathData").add("id", mInstanceId).addPath("path", mFloatPath) }
    companion object {
        private val OP_CODE = Operations.DATA_PATH; private const val MAX_PATH_LENGTH = 20000
        const val MOVE = 10; const val LINE = 11; const val QUADRATIC = 12; const val CONIC = 13; const val CUBIC = 14; const val CLOSE = 15; const val DONE = 16
        val MOVE_NAN = Utils.asNan(MOVE); val LINE_NAN = Utils.asNan(LINE); val CLOSE_NAN = Utils.asNan(CLOSE); val DONE_NAN = Utils.asNan(DONE)
        fun name(): String = "PathData"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, id: Int, data: FloatArray) { buffer.start(OP_CODE); buffer.writeInt(id); buffer.writeInt(data.size); for (d in data) buffer.writeFloat(d) }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { var imageId = buffer.readInt(); val winding = imageId shr 24; imageId = imageId and 0xffffff; val len = buffer.readInt(); if (len > MAX_PATH_LENGTH) throw RuntimeException("Path too long"); val data = FloatArray(len) { buffer.readFloat() }; operations.add(PathData(imageId, data, winding)) }
        fun pathString(path: FloatArray?): String { if (path == null) return "null"; val sb = StringBuilder(); for (i in path.indices) { if (i != 0) sb.append(" "); if (path[i].isNaN()) { val id = Utils.idFromNan(path[i]); when { id <= DONE -> sb.append(when(id) { MOVE->"M"; LINE->"L"; QUADRATIC->"Q"; CONIC->"R"; CUBIC->"C"; CLOSE->"Z"; DONE->"."; else->"[$id]" }); else -> sb.append("($id)") } } else sb.append(path[i]) }; return sb.toString() }
    }
}
