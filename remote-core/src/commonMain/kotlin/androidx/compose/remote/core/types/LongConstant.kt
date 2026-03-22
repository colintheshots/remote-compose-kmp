/*
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.compose.remote.core.types

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.documentation.DocumentationBuilder
import androidx.compose.remote.core.documentation.DocumentedOperation
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable

/** Used to represent a long */
class LongConstant(val mId: Int, private var mValue: Long) : Operation(), Serializable {

    /**
     * Copy the value from another longConstant
     *
     * @param from the constant to copy from
     */
    fun update(from: LongConstant) {
        mValue = from.mValue
    }

    /** Get the value of the long constant */
    fun getValue(): Long = mValue

    /** Set the value of the long constant */
    fun setValue(value: Long) {
        mValue = value
    }

    override fun write(buffer: WireBuffer) {
        apply(buffer, mId, mValue)
    }

    override fun apply(context: RemoteContext) {
        context.putObject(mId, this)
    }

    override fun deepToString(indent: String): String = toString()

    override fun toString(): String = "LongConstant[$mId] = $mValue"

    override fun serialize(serializer: MapSerializer) {
        serializer.addType(CLASS_NAME).add("id", mId).add("value", mValue)
    }

    companion object {
        private const val CLASS_NAME = "LongConstant"
        private val OP_CODE = Operations.DATA_LONG

        /** Writes out the operation to the buffer */
        fun apply(buffer: WireBuffer, id: Int, value: Long) {
            buffer.start(OP_CODE)
            buffer.writeInt(id)
            buffer.writeLong(value)
        }

        /** Read this operation and add it to the list of operations */
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val id = buffer.readInt()
            val value = buffer.readLong()
            operations.add(LongConstant(id, value))
        }

        /** Populate the documentation with a description of this operation */
        fun documentation(doc: DocumentationBuilder) {
            doc.operation("Expressions Operations", OP_CODE, "LongConstant")
                .description("A boolean and its associated id")
                .field(DocumentedOperation.INT, "id", "id of Int")
                .field(DocumentedOperation.LONG, "value", "The long Value")
        }
    }
}
