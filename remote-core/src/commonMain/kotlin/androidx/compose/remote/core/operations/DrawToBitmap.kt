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
package androidx.compose.remote.core.operations

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.PaintOperation
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable

/** Draw to a bitmap. This command redirects drawing to a bitmap. */
class DrawToBitmap(
    private val mBitmapId: Int,
    private val mMode: Int,
    private val mColor: Int
) : PaintOperation(), Serializable {

    override fun write(buffer: WireBuffer) {
        apply(buffer, mBitmapId, mMode, mColor)
    }

    override fun toString(): String = "DrawToBitmap"

    override fun paint(context: PaintContext) {
        context.drawToBitmap(getId(mBitmapId, context), mMode, mColor)
    }

    override fun serialize(serializer: MapSerializer) {
        serializer.addType(CLASS_NAME).add("bitmapId", mBitmapId)
    }

    companion object {
        private val OP_CODE = Operations.DRAW_TO_BITMAP
        private const val CLASS_NAME = "DrawToBitmap"

        const val MODE_NO_INITIALIZE = 1

        fun name(): String = CLASS_NAME
        fun id(): Int = OP_CODE

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val bitmapId = buffer.readInt()
            val mode = buffer.readInt()
            val color = buffer.readInt()
            operations.add(DrawToBitmap(bitmapId, mode, color))
        }

        fun apply(buffer: WireBuffer, bitmapId: Int, mode: Int, color: Int) {
            buffer.start(OP_CODE)
            buffer.writeInt(bitmapId)
            buffer.writeInt(mode)
            buffer.writeInt(color)
        }
    }
}
