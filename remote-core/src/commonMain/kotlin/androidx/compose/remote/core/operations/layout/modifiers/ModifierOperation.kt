package androidx.compose.remote.core.operations.layout.modifiers

import androidx.compose.remote.core.OperationInterface
import androidx.compose.remote.core.operations.utilities.StringSerializer

interface ModifierOperation : OperationInterface {
    fun serializeToString(indent: Int, serializer: StringSerializer)
}
