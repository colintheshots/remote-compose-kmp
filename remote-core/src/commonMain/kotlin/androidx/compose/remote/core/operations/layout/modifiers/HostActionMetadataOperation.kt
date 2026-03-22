package androidx.compose.remote.core.operations.layout.modifiers

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.PaintOperation
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.utilities.StringSerializer

class HostActionMetadataOperation : PaintOperation(), ModifierOperation {

    override fun paint(context: PaintContext) {}
    override fun write(buffer: WireBuffer) { buffer.start(Operations.MODIFIER_HOST_ACTION_METADATA) }
    override fun apply(context: RemoteContext) {}
    override fun toString(): String = "HostActionMetadataOperation"
    override fun deepToString(indent: String): String = indent + toString()
    override fun serializeToString(indent: Int, serializer: StringSerializer) {
        serializer.append(indent, "HostActionMetadataOperation")
    }

    companion object {
        fun name(): String = "HostActionMetadataOperation"
        fun id(): Int = Operations.MODIFIER_HOST_ACTION_METADATA

        fun apply(buffer: WireBuffer) {
            buffer.start(Operations.MODIFIER_HOST_ACTION_METADATA)
        }

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            operations.add(HostActionMetadataOperation())
        }
    }
}
