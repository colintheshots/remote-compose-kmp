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
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.serialize.MapSerializer

/** Skew the matrix command */
class MatrixSkew(skewX: Float, skewY: Float) : DrawBase2(skewX, skewY) {

    init {
        mName = CLASS_NAME
    }

    override fun paint(context: PaintContext) {
        context.matrixSkew(mV1, mV2)
    }

    override fun write(buffer: WireBuffer, v1: Float, v2: Float) {
        apply(buffer, v1, v2)
    }

    override fun serialize(serializer: MapSerializer) {
        serialize(serializer, "skewX", "skewY").addType(CLASS_NAME)
    }

    companion object {
        private val OP_CODE = Operations.MATRIX_SKEW
        private const val CLASS_NAME = "MatrixSkew"

        fun name(): String = CLASS_NAME
        fun id(): Int = OP_CODE

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            read(buffer, operations, Maker(::MatrixSkew))
        }

        fun apply(buffer: WireBuffer, x1: Float, y1: Float) {
            write(buffer, OP_CODE, x1, y1)
        }
    }
}
