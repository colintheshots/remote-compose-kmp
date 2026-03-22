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
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable

/** this evaluates a matrix * vector and outputs a vector */
class MatrixVectorMath(
    private val mType: Short,
    private val mOutputs: IntArray,
    var mMatrixId: Int,
    private val mInputs: FloatArray
) : Operation(), VariableSupport, Serializable {

    private val mOutInputs = FloatArray(mOutputs.size)
    private val mTempOut = FloatArray(mOutputs.size)
    var mMatrix = Matrix()

    override fun updateVariables(context: RemoteContext) {
        for (i in mInputs.indices) {
            val v = mInputs[i]
            if (v.isNaN()) {
                mOutInputs[i] = context.getFloat(Utils.idFromNan(v))
            } else {
                mOutInputs[i] = mInputs[i]
            }
        }
    }

    override fun registerListening(context: RemoteContext) {
        context.listensTo(mMatrixId, this)
        for (i in mInputs.indices) {
            val v = mInputs[i]
            if (v.isNaN()) {
                context.listensTo(Utils.idFromNan(v), this)
            }
        }
    }

    override fun write(buffer: WireBuffer) {
        apply(buffer, mType, mOutputs, mMatrixId, mInputs)
    }

    override fun toString(): String {
        var str = ""
        for (i in mInputs.indices) {
            str += " ${Utils.floatToString(mInputs[i], mOutInputs[i])}"
        }
        return "MatrixVectorMath ${mOutputs.contentToString()} $mMatrixId *$str"
    }

    override fun apply(context: RemoteContext) {
        val m = context.getObject(mMatrixId) as MatrixAccess
        mMatrix.copyFrom(m.get())
        if (mType.toInt() == 0) {
            mMatrix.multiply(mOutInputs, mTempOut)
        } else {
            mMatrix.evalPerspective(mOutInputs, mTempOut)
        }
        for (i in mOutputs.indices) {
            context.loadFloat(mOutputs[i], mTempOut[i])
        }
    }

    override fun deepToString(indent: String): String = indent + toString()

    override fun serialize(serializer: MapSerializer) {
        serializer
            .addType(CLASS_NAME)
            .add("matrix", mMatrixId)
            .addFloatExpressionSrc("input", mInputs)
            .add("output", mOutputs.contentToString())
    }

    companion object {
        private val OP_CODE = Operations.MATRIX_VECTOR_MATH
        private const val CLASS_NAME = "MatrixVectorMath"

        /** The name of the class */
        fun name(): String = CLASS_NAME

        /** The OP_CODE for this command */
        fun id(): Int = OP_CODE

        /** Writes out the operation to the buffer */
        fun apply(
            buffer: WireBuffer,
            type: Short,
            outputs: IntArray,
            matrixId: Int,
            inputs: FloatArray
        ) {
            buffer.start(OP_CODE)
            buffer.writeShort(type.toInt())
            buffer.writeInt(matrixId)
            buffer.writeInt(outputs.size)
            for (o in outputs) {
                buffer.writeInt(o)
            }
            buffer.writeInt(inputs.size)
            for (inp in inputs) {
                buffer.writeFloat(inp)
            }
        }

        /** Read this operation and add it to the list of operations */
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val type = buffer.readShort().toShort()
            val matrixId = buffer.readInt()
            val lenOut = buffer.readInt()
            if (lenOut > 4 || lenOut < 1) {
                throw IllegalArgumentException("Invalid Length $lenOut corrupt buffer")
            }
            val out = IntArray(lenOut)
            for (i in out.indices) {
                out[i] = buffer.readInt()
            }

            val lenIn = buffer.readInt()
            if (lenIn > 4 || lenIn < 1) {
                throw IllegalArgumentException("Invalid Length $lenOut corrupt buffer")
            }
            val inp = FloatArray(lenIn)
            for (i in inp.indices) {
                inp[i] = buffer.readFloat()
            }
            operations.add(MatrixVectorMath(type, out, matrixId, inp))
        }

        /** Populate the documentation with a description of this operation */
        fun documentation(doc: DocumentationBuilder) {
            doc.operation("Expressions Operations", OP_CODE, CLASS_NAME)
                .description("A float and its associated id")
                .field(DocumentedOperation.INT, "matrixId", "id of Matrix")
                .field(DocumentedOperation.SHORT, "opType", "The type of op 0=multiply")
                .field(DocumentedOperation.INT, "outLength", "The length of the output vector")
                .field(DocumentedOperation.FLOAT, "value", "outLength", "32-bit float value")
                .field(DocumentedOperation.INT, "inLength", "The length of the input vector")
                .field(DocumentedOperation.FLOAT, "value", "inLength", "32-bit float value")
        }
    }
}
