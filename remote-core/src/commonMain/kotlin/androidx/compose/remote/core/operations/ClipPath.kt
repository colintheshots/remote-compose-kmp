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

/**
 * Defines a path that clips subsequent drawing commands.
 * Use MatrixSave and MatrixRestore commands to remove clip.
 */
class ClipPath(
    private val mId: Int,
    private val mRegionOp: Int
) : PaintOperation(), Serializable {

    override fun write(buffer: WireBuffer) {
        apply(buffer, mId)
    }

    override fun toString(): String = "ClipPath $mId;"

    override fun paint(context: PaintContext) {
        context.clipPath(mId, mRegionOp)
    }

    override fun serialize(serializer: MapSerializer) {
        serializer.addType(CLASS_NAME).add("id", mId).add("regionOp", regionOpToString())
    }

    private fun regionOpToString(): String = when (mRegionOp) {
        REPLACE -> "REPLACE"
        DIFFERENCE -> "DIFFERENCE"
        INTERSECT -> "INTERSECT"
        XOR -> "XOR"
        REVERSE_DIFFERENCE -> "REVERSE_DIFFERENCE"
        else -> "UNDEFINED"
    }

    companion object {
        private val OP_CODE = Operations.CLIP_PATH
        private const val CLASS_NAME = "ClipPath"

        const val PATH_CLIP_REPLACE = 0
        const val PATH_CLIP_DIFFERENCE = 1
        const val PATH_CLIP_INTERSECT = 2
        const val PATH_CLIP_UNION = 3
        const val PATH_CLIP_XOR = 4
        const val PATH_CLIP_REVERSE_DIFFERENCE = 5
        const val PATH_CLIP_UNDEFINED = 6

        const val REPLACE = PATH_CLIP_REPLACE
        const val DIFFERENCE = PATH_CLIP_DIFFERENCE
        const val INTERSECT = PATH_CLIP_INTERSECT
        const val UNION = PATH_CLIP_UNION
        const val XOR = PATH_CLIP_XOR
        const val REVERSE_DIFFERENCE = PATH_CLIP_REVERSE_DIFFERENCE
        const val UNDEFINED = PATH_CLIP_UNDEFINED

        fun name(): String = CLASS_NAME
        fun id(): Int = OP_CODE

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val pack = buffer.readInt()
            val id = pack and 0xFFFFF
            val regionOp = pack shr 24
            operations.add(ClipPath(id, regionOp))
        }

        fun apply(buffer: WireBuffer, id: Int) {
            buffer.start(OP_CODE)
            buffer.writeInt(id)
        }
    }
}
