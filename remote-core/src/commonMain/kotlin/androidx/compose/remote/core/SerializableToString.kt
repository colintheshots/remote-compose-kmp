package androidx.compose.remote.core

import androidx.compose.remote.core.operations.utilities.StringSerializer

/** Interface for operations that can serialize to string format */
interface SerializableToString {
    fun serializeToString(indent: Int, serializer: StringSerializer)
}
