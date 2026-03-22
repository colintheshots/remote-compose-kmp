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
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.VariableSupport
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.paint.PaintBundle
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable

/** Paint data operation */
class PaintData(var mPaintData: PaintBundle = PaintBundle()) :
    PaintOperation(), VariableSupport, Serializable {

    override fun updateVariables(context: RemoteContext) {
        mPaintData.updateVariables(context)
    }

    override fun registerListening(context: RemoteContext) {
        mPaintData.registerVars(context, this)
    }

    override fun write(buffer: WireBuffer) {
        apply(buffer, mPaintData)
    }

    override fun toString(): String = "PaintData \"$mPaintData\""

    override fun deepToString(indent: String): String = indent + toString()

    override fun paint(context: PaintContext) {
        context.applyPaint(mPaintData)
    }

    override fun serialize(serializer: MapSerializer) {
        serializer.addType(CLASS_NAME).add("paintBundle", mPaintData)
    }

    companion object {
        private val OP_CODE = Operations.PAINT_VALUES
        private const val CLASS_NAME = "PaintData"
        const val MAX_STRING_SIZE = 4000

        fun name(): String = CLASS_NAME
        fun id(): Int = OP_CODE

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val data = PaintData()
            data.mPaintData.readBundle(buffer)
            operations.add(data)
        }

        fun apply(buffer: WireBuffer, paintBundle: PaintBundle) {
            buffer.start(OP_CODE)
            paintBundle.writeBundle(buffer)
        }
    }
}
