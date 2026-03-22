// Copyright (C) 2024 The Android Open Source Project. Apache License 2.0.
package androidx.compose.remote.core.operations
import androidx.compose.remote.core.*
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable
class WakeIn(var mWake: Float) : PaintOperation(), VariableSupport, Serializable {
    var mWakeOut: Float = mWake
    override fun updateVariables(context: RemoteContext) { mWakeOut = if (mWake.isNaN()) context.getFloat(Utils.idFromNan(mWake)) else mWake }
    override fun registerListening(context: RemoteContext) { if (mWake.isNaN()) context.listensTo(Utils.idFromNan(mWake), this) }
    override fun markDirty() { super<PaintOperation>.markDirty() }
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mWake) }; override fun toString(): String = "WakeIn ${Utils.floatToString(mWake, mWakeOut)}"
    override fun deepToString(indent: String): String = indent + toString(); override fun paint(context: PaintContext) { context.wakeIn(mWakeOut) }
    override fun serialize(serializer: MapSerializer) { serializer.addType("WakeIn").add("wake", mWake) }
    companion object { private val OP_CODE = Operations.WAKE_IN; fun name(): String = "WakeIn"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, wake: Float) { buffer.start(OP_CODE); buffer.writeFloat(wake) }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { operations.add(WakeIn(buffer.readFloat())) } }
}
