package androidx.compose.remote.core.operations.layout

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.WireBuffer

class ContainerEnd : Operation() {
    override fun write(buffer: WireBuffer) { Companion.apply(buffer) }
    override fun toString(): String = "LIST_END"
    override fun apply(context: RemoteContext) {}
    override fun deepToString(indent: String): String = indent + toString()

    companion object {
        fun name(): String = "ListEnd"
        fun id(): Int = Operations.CONTAINER_END
        fun apply(buffer: WireBuffer) { buffer.start(id()) }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { operations.add(ContainerEnd()) }
    }
}
