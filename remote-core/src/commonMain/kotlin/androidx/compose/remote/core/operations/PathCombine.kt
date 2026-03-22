// Copyright (C) 2024 The Android Open Source Project. Apache License 2.0.
package androidx.compose.remote.core.operations
import androidx.compose.remote.core.*
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable
class PathCombine(var mOutId: Int, var mPathId1: Int, var mPathId2: Int, private var mOperation: Byte) : PaintOperation(), VariableSupport, Serializable {
    override fun updateVariables(context: RemoteContext) {}; override fun registerListening(context: RemoteContext) {}
    override fun markDirty() { super<PaintOperation>.markDirty() }
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mOutId, mPathId1, mPathId2, mOperation) }
    override fun toString(): String = "PathCombine[$mOutId] = [$mPathId1] + [$mPathId2], $mOperation"
    override fun deepToString(indent: String): String = indent + toString()
    override fun paint(context: PaintContext) { context.combinePath(mOutId, mPathId1, mPathId2, mOperation) }
    override fun serialize(serializer: MapSerializer) { serializer.addType("PathCombine").add("outId", mOutId).add("pathId1", mPathId1).add("pathId2", mPathId2).add("operation", mOperation) }
    companion object {
        private val OP_CODE = Operations.PATH_COMBINE; const val OP_DIFFERENCE: Byte = 0; const val OP_INTERSECT: Byte = 1; const val OP_REVERSE_DIFFERENCE: Byte = 2; const val OP_UNION: Byte = 3; const val OP_XOR: Byte = 4
        fun name(): String = "PathCombine"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, outId: Int, pathId1: Int, pathId2: Int, op: Byte) { buffer.start(OP_CODE); buffer.writeInt(outId); buffer.writeInt(pathId1); buffer.writeInt(pathId2); buffer.writeByte(op.toInt()) }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { operations.add(PathCombine(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readByte().toByte())) }
    }
}
