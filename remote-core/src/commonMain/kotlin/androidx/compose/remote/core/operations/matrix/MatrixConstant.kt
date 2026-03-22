/*
 * Copyright (C) 2025 The Android Open Source Project
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
package androidx.compose.remote.core.operations.matrix

import androidx.compose.remote.core.MatrixAccess
import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.documentation.DocumentationBuilder
import androidx.compose.remote.core.documentation.DocumentedOperation
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable

/** This is for a constant matrix */
class MatrixConstant(
    private val mMatrixId: Int,
    private val mType: Int,
    private var mValues: FloatArray
) : Operation(), Serializable, MatrixAccess {

    /**
     * Copy the value from another operation
     *
     * @param from value to copy from
     */
    fun update(from: MatrixConstant) {
        mValues = from.mValues
    }

    override fun write(buffer: WireBuffer) {
        apply(buffer, mMatrixId, mType, mValues)
    }

    override fun toString(): String =
        "FloatConstant[$mMatrixId] = ${mValues.contentToString()}"

    override fun apply(context: RemoteContext) {
        context.putObject(mMatrixId, this)
    }

    override fun deepToString(indent: String): String = indent + toString()

    override fun serialize(serializer: MapSerializer) {
        serializer
            .addType(CLASS_NAME)
            .add("matrix", mMatrixId)
            .add("type", mType)
            .addFloatExpressionSrc("value", mValues)
    }

    override fun get(): FloatArray = mValues

    companion object {
        private val OP_CODE = Operations.MATRIX_CONSTANT
        private const val CLASS_NAME = "MatrixConstant"

        /** The name of the class */
        fun name(): String = CLASS_NAME

        /** The OP_CODE for this command */
        fun id(): Int = OP_CODE

        /** Writes out the operation to the buffer */
        fun apply(buffer: WireBuffer, matrixId: Int, type: Int, values: FloatArray) {
            buffer.start(OP_CODE)
            buffer.writeInt(matrixId)
            buffer.writeInt(type)
            buffer.writeInt(values.size)
            for (v in values) {
                buffer.writeFloat(v)
            }
        }

        /** Read this operation and add it to the list of operations */
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val id = buffer.readInt()
            val type = buffer.readInt()
            val len = buffer.readInt()
            if (len > 16 || len < 0) {
                throw RuntimeException("Invalid matrix length: $len corrupt buffer")
            }
            val matrix = FloatArray(len)
            for (i in 0 until len) {
                matrix[i] = buffer.readFloat()
            }
            operations.add(MatrixConstant(id, type, matrix))
        }

        /** Populate the documentation with a description of this operation */
        fun documentation(doc: DocumentationBuilder) {
            doc.operation("Expressions Operations", OP_CODE, CLASS_NAME)
                .description("A float and its associated id")
                .field(DocumentedOperation.INT, "id", "id of float")
                .field(DocumentedOperation.FLOAT, "value", "32-bit float value")
        }
    }
}
