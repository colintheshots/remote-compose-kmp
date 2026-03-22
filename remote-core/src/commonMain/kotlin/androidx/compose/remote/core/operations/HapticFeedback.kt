// Copyright (C) 2024 The Android Open Source Project. Apache License 2.0.
package androidx.compose.remote.core.operations
import androidx.compose.remote.core.*
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable
class HapticFeedback(private var mHapticFeedbackType: Int) : Operation(), Serializable {
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mHapticFeedbackType) }; override fun toString(): String = "HapticFeedback($mHapticFeedbackType)"
    override fun apply(context: RemoteContext) { context.hapticEffect(mHapticFeedbackType) }; override fun deepToString(indent: String): String = indent + toString()
    override fun serialize(serializer: MapSerializer) { serializer.addType("HapticFeedback").add("hapticFeedbackType", mHapticFeedbackType) }
    companion object { private val OP_CODE = Operations.HAPTIC_FEEDBACK; fun name(): String = "HapticFeedback"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, hapticFeedbackType: Int) { buffer.start(OP_CODE); buffer.writeInt(hapticFeedbackType) }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { operations.add(HapticFeedback(buffer.readInt())) } }
}
