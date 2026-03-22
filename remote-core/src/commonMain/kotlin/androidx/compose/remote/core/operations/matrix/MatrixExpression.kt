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
import androidx.compose.remote.core.VariableSupport
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.documentation.DocumentationBuilder
import androidx.compose.remote.core.documentation.DocumentedOperation
import androidx.compose.remote.core.operations.Utils
import androidx.compose.remote.core.operations.utilities.Matrix
import androidx.compose.remote.core.operations.utilities.MatrixOperations
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable

/** This is a matrix that is formed by an expression */
class MatrixExpression(
    private val mMatrixId: Int,
    private val mType: Int,
    private val mExpression: FloatArray
) : Operation(), VariableSupport, MatrixAccess, Serializable {

    private var mValues: FloatArray = FloatArray(16)
    private var mOutExpression: FloatArray? = null
    var mMatrixOperations = MatrixOperations()

    override fun updateVariables(context: RemoteContext) {
        val outExp = mOutExpression
        if (outExp == null || outExp.size != mExpression.size) {
            mOutExpression = FloatArray(mExpression.size)
        }
        for (i in mExpression.indices) {
            val v = mExpression[i]
            if (v.isNaN() && !MatrixOperations.isOperator(v)) {
                mOutExpression!![i] = context.getFloat(Utils.idFromNan(v))
            } else {
                mOutExpression!![i] = mExpression[i]
            }
        }
    }

    override fun registerListening(context: RemoteContext) {
        for (i in mExpression.indices) {
            val v = mExpression[i]
            if (v.isNaN() && !MatrixOperations.isOperator(v)) {
                context.listensTo(Utils.idFromNan(v), this)
            }
        }
    }

    /**
     * Copy the value from another operation
     *
     * @param from value to copy from
     */
    fun update(from: MatrixExpression) {
        mValues = from.mValues
    }

    override fun write(buffer: WireBuffer) {
        apply(buffer, mMatrixId, mType, mValues)
    }

    override fun toString(): String =
        "FloatConstant[$mMatrixId] = ${mValues.contentToString()}"

    override fun apply(context: RemoteContext) {
        val m = mMatrixOperations.eval(mOutExpression!!)
        m.putValues(mValues)
        context.putObject(mMatrixId, this)
        // Note: System.nanoTime() replaced with platform-independent approach
        context.loadFloat(mMatrixId, 0f)
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
        private val OP_CODE = Operations.MATRIX_EXPRESSION
        private const val CLASS_NAME = "MatrixExpression"

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
            if (len > 32 || len < 0) {
                throw IllegalArgumentException("Invalid Length $len corrupt buffer")
            }
            val exp = FloatArray(len)
            for (i in exp.indices) {
                exp[i] = buffer.readFloat()
            }
            operations.add(MatrixExpression(id, type, exp))
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
