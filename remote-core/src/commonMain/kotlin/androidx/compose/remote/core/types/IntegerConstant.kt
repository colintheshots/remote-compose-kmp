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

/** Represents a single integer typically used for states or named for input into the system */
class IntegerConstant(val mId: Int, private var mValue: Int) : Operation(), Serializable {

    /**
     * Updates the value of the integer constant
     *
     * @param ic the integer constant to copy
     */
    fun update(ic: IntegerConstant) {
        mValue = ic.mValue
    }

    override fun write(buffer: WireBuffer) {
        apply(buffer, mId, mValue)
    }

    override fun apply(context: RemoteContext) {
        context.loadInteger(mId, mValue)
    }

    override fun deepToString(indent: String): String = toString()

    override fun toString(): String = "IntegerConstant[$mId] = $mValue"

    override fun serialize(serializer: MapSerializer) {
        serializer.addType(CLASS_NAME).add("id", mId).add("value", mValue)
    }

    companion object {
        private const val CLASS_NAME = "IntegerConstant"

        /** The name of the class */
        fun name(): String = "IntegerConstant"

        /** The OP_CODE for this command */
        fun id(): Int = Operations.DATA_INT

        /** Writes out the operation to the buffer */
        fun apply(buffer: WireBuffer, textId: Int, value: Int) {
            buffer.start(Operations.DATA_INT)
            buffer.writeInt(textId)
            buffer.writeInt(value)
        }

        /** Read this operation and add it to the list of operations */
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val id = buffer.readInt()
            val value = buffer.readInt()
            operations.add(IntegerConstant(id, value))
        }

        /** Populate the documentation with a description of this operation */
        fun documentation(doc: DocumentationBuilder) {
            doc.operation("Expressions Operations", id(), "IntegerConstant")
                .description("A integer and its associated id")
                .field(DocumentedOperation.INT, "id", "id of Int")
                .field(DocumentedOperation.INT, "value", "32-bit int value")
        }
    }
}
