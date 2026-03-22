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
package androidx.compose.remote.core.operations

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.PaintOperation
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable

/** Restore previous matrix command */
class MatrixRestore : PaintOperation(), Serializable {

    override fun write(buffer: WireBuffer) {
        apply(buffer)
    }

    override fun toString(): String = "MatrixRestore"

    override fun paint(context: PaintContext) {
        context.matrixRestore()
    }

    override fun serialize(serializer: MapSerializer) {
        serializer.addType(CLASS_NAME)
    }

    companion object {
        private val OP_CODE = Operations.MATRIX_RESTORE
        private const val CLASS_NAME = "MatrixRestore"

        fun name(): String = CLASS_NAME
        fun id(): Int = OP_CODE

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            operations.add(MatrixRestore())
        }

        fun apply(buffer: WireBuffer) {
            buffer.start(OP_CODE)
        }
    }
}
