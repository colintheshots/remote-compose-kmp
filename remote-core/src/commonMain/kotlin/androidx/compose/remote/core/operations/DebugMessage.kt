// Copyright (C) 2024 The Android Open Source Project. Apache License 2.0.
package androidx.compose.remote.core.operations
import androidx.compose.remote.core.*
class DebugMessage(var mTextID: Int, var mFloatValue: Float, var mFlags: Int = 0) : Operation(), VariableSupport {
    var mOutFloatValue: Float = 0f; companion object { private val OP_CODE = Operations.DEBUG_MESSAGE; const val SHOW_USAGE = 1; fun name(): String = "DebugMessage"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, textId: Int, value: Float, flags: Int) { buffer.start(OP_CODE); buffer.writeInt(textId); buffer.writeFloat(value); buffer.writeInt(flags) }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { operations.add(DebugMessage(buffer.readInt(), buffer.readFloat(), buffer.readInt())) } }
    override fun updateVariables(context: RemoteContext) { mOutFloatValue = if (mFloatValue.isNaN()) context.getFloat(Utils.idFromNan(mFloatValue)) else mFloatValue }
    override fun registerListening(context: RemoteContext) { context.listensTo(mTextID, this); if (mFloatValue.isNaN()) context.listensTo(Utils.idFromNan(mFloatValue), this) }
    override fun markDirty() { super<Operation>.markDirty() }
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mTextID, mFloatValue, mFlags) }
    override fun toString(): String = "DebugMessage $mTextID, $mOutFloatValue, $mFlags"
    override fun apply(context: RemoteContext) { println("Debug message : ${context.getText(mTextID)} $mOutFloatValue") }
    override fun deepToString(indent: String): String = indent + toString()
}
