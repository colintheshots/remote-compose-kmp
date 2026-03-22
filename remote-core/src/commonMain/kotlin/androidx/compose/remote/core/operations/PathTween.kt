// Copyright (C) 2024 The Android Open Source Project. Apache License 2.0.
package androidx.compose.remote.core.operations
import androidx.compose.remote.core.*
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable
class PathTween(var mOutId: Int, var mPathId1: Int, var mPathId2: Int, var mTween: Float) : PaintOperation(), VariableSupport, Serializable {
    var mTweenOut: Float = mTween
    override fun updateVariables(context: RemoteContext) { mTweenOut = if (mTween.isNaN()) context.getFloat(Utils.idFromNan(mTween)) else mTween }
    override fun registerListening(context: RemoteContext) { if (mTween.isNaN()) context.listensTo(Utils.idFromNan(mTween), this) }
    override fun markDirty() { super<PaintOperation>.markDirty() }
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mOutId, mPathId1, mPathId2, mTween) }
    override fun toString(): String = "PathTween[$mOutId] = [$mPathId1] + [$mPathId2], ${Utils.floatToString(mTween, mTweenOut)}"
    override fun deepToString(indent: String): String = indent + toString()
    override fun paint(context: PaintContext) { context.tweenPath(mOutId, mPathId1, mPathId2, mTweenOut) }
    override fun serialize(serializer: MapSerializer) { serializer.addType("PathTween").add("outId", mOutId).add("pathId1", mPathId1).add("pathId2", mPathId2).add("tween", mTween, mTweenOut) }
    companion object {
        private val OP_CODE = Operations.PATH_TWEEN; fun name(): String = "PathTween"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, outId: Int, pathId1: Int, pathId2: Int, tween: Float) { buffer.start(OP_CODE); buffer.writeInt(outId); buffer.writeInt(pathId1); buffer.writeInt(pathId2); buffer.writeFloat(tween) }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { operations.add(PathTween(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readFloat())) }
    }
}
