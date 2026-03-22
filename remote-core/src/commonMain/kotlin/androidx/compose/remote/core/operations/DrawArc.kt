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

/** Draw an arc command, scaled to fit inside the specified oval. */
class DrawArc(
    left: Float, top: Float, right: Float, bottom: Float,
    startAngle: Float, sweepAngle: Float
) : DrawBase6(left, top, right, bottom, startAngle, sweepAngle) {

    init {
        mName = "DrawArc"
    }

    override fun paint(context: PaintContext) {
        context.drawArc(mV1, mV2, mV3, mV4, mV5, mV6)
    }

    override fun write(
        buffer: WireBuffer, v1: Float, v2: Float, v3: Float, v4: Float, v5: Float, v6: Float
    ) {
        apply(buffer, v1, v2, v3, v4, v5, v6)
    }

    override fun serialize(serializer: MapSerializer) {
        serialize(serializer, "left", "top", "right", "bottom", "startAngle", "sweepAngle")
            .addType(CLASS_NAME)
    }

    companion object {
        private val OP_CODE = Operations.DRAW_ARC
        private const val CLASS_NAME = "DrawArc"

        fun name(): String = CLASS_NAME
        fun id(): Int = OP_CODE

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            read(buffer, operations, Maker(::DrawArc))
        }

        fun apply(
            buffer: WireBuffer,
            v1: Float, v2: Float, v3: Float, v4: Float, v5: Float, v6: Float
        ) {
            buffer.start(OP_CODE)
            buffer.writeFloat(v1)
            buffer.writeFloat(v2)
            buffer.writeFloat(v3)
            buffer.writeFloat(v4)
            buffer.writeFloat(v5)
            buffer.writeFloat(v6)
        }
    }
}
