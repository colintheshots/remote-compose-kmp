// Copyright (C) 2024 The Android Open Source Project. Apache License 2.0.
package androidx.compose.remote.core.operations
import androidx.compose.remote.core.*
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable
class NamedVariable(val mVarId: Int, val mVarType: Int, val mVarName: String) : Operation(), Serializable {
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mVarId, mVarType, mVarName) }
    override fun toString(): String = "VariableName[$mVarId] = \"${Utils.trimString(mVarName, 10)}\" type=$mVarType"
    override fun apply(context: RemoteContext) { context.loadVariableName(mVarName, mVarId, mVarType) }
    override fun deepToString(indent: String): String = indent + toString()
    override fun serialize(serializer: MapSerializer) { serializer.addType("NamedVariable").add("varId", mVarId).add("varName", mVarName) }
    companion object {
        private val OP_CODE = Operations.NAMED_VARIABLE; const val MAX_STRING_SIZE = 4000
        const val COLOR_TYPE = 2; const val FLOAT_TYPE = 1; const val STRING_TYPE = 0; const val IMAGE_TYPE = 3; const val INT_TYPE = 4; const val LONG_TYPE = 5; const val FLOAT_ARRAY_TYPE = 6
        fun name(): String = "NamedVariable"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, varId: Int, varType: Int, text: String) { buffer.start(OP_CODE); buffer.writeInt(varId); buffer.writeInt(varType); buffer.writeUTF8(text) }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { operations.add(NamedVariable(buffer.readInt(), buffer.readInt(), buffer.readUTF8(MAX_STRING_SIZE))) }
    }
}
