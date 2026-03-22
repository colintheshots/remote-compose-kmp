// Copyright (C) 2024 The Android Open Source Project. Apache License 2.0.
package androidx.compose.remote.core.operations
import androidx.compose.remote.core.*
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable
class PathAppend(var mInstanceId: Int, var mFloatPath: FloatArray) : PaintOperation(), VariableSupport, Serializable {
    var mOutputPath: FloatArray = mFloatPath.copyOf(); private val RESET_NAN = Utils.asNan(17)
    override fun updateVariables(context: RemoteContext) { for (i in mFloatPath.indices) { val v = mFloatPath[i]; if (Utils.isVariable(v)) mOutputPath[i] = if (v.isNaN()) context.getFloat(Utils.idFromNan(v)) else v; else mOutputPath[i] = v } }
    override fun registerListening(context: RemoteContext) { for (v in mFloatPath) if (v.isNaN()) context.listensTo(Utils.idFromNan(v), this) }
    override fun markDirty() { super<PaintOperation>.markDirty() }
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mInstanceId, mOutputPath) }
    override fun toString(): String = "PathAppend[$mInstanceId]"
    override fun deepToString(indent: String): String = PathData.pathString(mFloatPath)
    override fun paint(context: PaintContext) { apply(context.getContext()) }
    override fun apply(context: RemoteContext) {
        if (mOutputPath[0].toRawBits() == RESET_NAN.toRawBits()) { context.loadPathData(mInstanceId, 0, FloatArray(0)); return }
        val data = context.getPathData(mInstanceId)
        val out = if (data != null) { val r = FloatArray(data.size + mOutputPath.size); data.copyInto(r); mOutputPath.copyInto(r, data.size); r } else mOutputPath
        context.loadPathData(mInstanceId, 0, out)
    }
    override fun serialize(serializer: MapSerializer) { serializer.addType("PathAppend").add("id", mInstanceId).addPath("path", mFloatPath) }
    companion object {
        private val OP_CODE = Operations.PATH_ADD; private const val MAX_PATH_BUFFER = 2000; fun name(): String = "PathAppend"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, id: Int, data: FloatArray) { buffer.start(OP_CODE); buffer.writeInt(id); buffer.writeInt(data.size); for (d in data) buffer.writeFloat(d) }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { val id = buffer.readInt(); val len = buffer.readInt(); if (len > MAX_PATH_BUFFER) throw RuntimeException("path too long"); operations.add(PathAppend(id, FloatArray(len) { buffer.readFloat() })) }
    }
}
