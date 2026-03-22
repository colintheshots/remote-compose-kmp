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
package androidx.compose.remote.core

import androidx.compose.remote.core.operations.DrawCircle
import androidx.compose.remote.core.operations.DrawLine
import androidx.compose.remote.core.operations.DrawOval
import androidx.compose.remote.core.operations.DrawRect
import androidx.compose.remote.core.operations.DrawRoundRect
import androidx.compose.remote.core.operations.Utils
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Tests for draw operations encode/decode round-trips */
class DrawOperationTest {

    /** Helper to create a buffer with all operations valid */
    private fun createBuffer(): WireBuffer {
        val buffer = WireBuffer(1024)
        buffer.setVersion(6, 0)
        return buffer
    }

    @Test
    fun drawRectRoundTrip() {
        val buffer = createBuffer()
        val left = 10.0f
        val top = 20.0f
        val right = 100.0f
        val bottom = 200.0f

        DrawRect.apply(buffer, left, top, right, bottom)

        buffer.index = 0
        val opType = buffer.readOperationType()
        assertEquals(Operations.DRAW_RECT, opType)

        val operations = mutableListOf<Operation>()
        DrawRect.read(buffer, operations)
        assertEquals(1, operations.size)

        val drawRect = operations[0] as DrawRect
        assertEquals(left, drawRect.mX1Value)
        assertEquals(top, drawRect.mY1Value)
        assertEquals(right, drawRect.mX2Value)
        assertEquals(bottom, drawRect.mY2Value)
    }

    @Test
    fun drawCircleRoundTrip() {
        val buffer = createBuffer()
        val cx = 50.0f
        val cy = 60.0f
        val radius = 30.0f

        DrawCircle.apply(buffer, cx, cy, radius)

        buffer.index = 0
        val opType = buffer.readOperationType()
        assertEquals(Operations.DRAW_CIRCLE, opType)

        val operations = mutableListOf<Operation>()
        DrawCircle.read(buffer, operations)
        assertEquals(1, operations.size)

        val drawCircle = operations[0] as DrawCircle
        assertEquals(cx, drawCircle.mV1)
        assertEquals(cy, drawCircle.mV2)
        assertEquals(radius, drawCircle.mV3)
    }

    @Test
    fun drawLineRoundTrip() {
        val buffer = createBuffer()
        val startX = 0.0f
        val startY = 0.0f
        val endX = 100.0f
        val endY = 100.0f

        DrawLine.apply(buffer, startX, startY, endX, endY)

        buffer.index = 0
        val opType = buffer.readOperationType()
        assertEquals(Operations.DRAW_LINE, opType)

        val operations = mutableListOf<Operation>()
        DrawLine.read(buffer, operations)
        assertEquals(1, operations.size)

        val drawLine = operations[0] as DrawLine
        assertEquals(startX, drawLine.mX1Value)
        assertEquals(startY, drawLine.mY1Value)
        assertEquals(endX, drawLine.mX2Value)
        assertEquals(endY, drawLine.mY2Value)
    }

    @Test
    fun drawOvalRoundTrip() {
        val buffer = createBuffer()
        val left = 5.0f
        val top = 10.0f
        val right = 200.0f
        val bottom = 150.0f

        DrawOval.apply(buffer, left, top, right, bottom)

        buffer.index = 0
        val opType = buffer.readOperationType()
        assertEquals(Operations.DRAW_OVAL, opType)

        val operations = mutableListOf<Operation>()
        DrawOval.read(buffer, operations)
        assertEquals(1, operations.size)

        val drawOval = operations[0] as DrawOval
        assertEquals(left, drawOval.mX1Value)
        assertEquals(top, drawOval.mY1Value)
        assertEquals(right, drawOval.mX2Value)
        assertEquals(bottom, drawOval.mY2Value)
    }

    @Test
    fun drawRoundRectRoundTrip() {
        val buffer = createBuffer()

        DrawRoundRect.apply(buffer, 10f, 20f, 100f, 200f, 5f, 5f)

        buffer.index = 0
        val opType = buffer.readOperationType()
        assertEquals(Operations.DRAW_ROUND_RECT, opType)

        val operations = mutableListOf<Operation>()
        DrawRoundRect.read(buffer, operations)
        assertEquals(1, operations.size)

        val op = operations[0] as DrawRoundRect
        assertEquals(10f, op.mV1)
        assertEquals(20f, op.mV2)
        assertEquals(100f, op.mV3)
        assertEquals(200f, op.mV4)
        assertEquals(5f, op.mV5)
        assertEquals(5f, op.mV6)
    }

    @Test
    fun variableEncodedAsNan() {
        // Test that Utils.asNan encodes an ID as a NaN float
        val varId = 42
        val nanFloat = Utils.asNan(varId)
        assertTrue(nanFloat.isNaN(), "asNan should produce a NaN float")
        assertEquals(varId, Utils.idFromNan(nanFloat), "idFromNan should recover the original ID")
    }

    @Test
    fun drawRectWithVariableReferences() {
        val buffer = createBuffer()
        val varId = 42
        val nanFloat = Utils.asNan(varId)

        // Use NaN-encoded variable for left, literal for others
        DrawRect.apply(buffer, nanFloat, 20.0f, 100.0f, 200.0f)

        buffer.index = 0
        buffer.readOperationType() // consume opType
        val operations = mutableListOf<Operation>()
        DrawRect.read(buffer, operations)

        val drawRect = operations[0] as DrawRect
        assertTrue(drawRect.mX1Value.isNaN(), "Variable-encoded float should be NaN")
        assertEquals(varId, Utils.idFromNan(drawRect.mX1Value))
        assertEquals(20.0f, drawRect.mY1Value)
    }

    @Test
    fun multipleDrawOperationsInSequence() {
        val buffer = createBuffer()

        DrawRect.apply(buffer, 0f, 0f, 100f, 100f)
        DrawCircle.apply(buffer, 50f, 50f, 25f)
        DrawLine.apply(buffer, 0f, 0f, 100f, 100f)

        buffer.index = 0
        val allOps = mutableListOf<Operation>()

        // Read DrawRect
        var opType = buffer.readOperationType()
        assertEquals(Operations.DRAW_RECT, opType)
        DrawRect.read(buffer, allOps)

        // Read DrawCircle
        opType = buffer.readOperationType()
        assertEquals(Operations.DRAW_CIRCLE, opType)
        DrawCircle.read(buffer, allOps)

        // Read DrawLine
        opType = buffer.readOperationType()
        assertEquals(Operations.DRAW_LINE, opType)
        DrawLine.read(buffer, allOps)

        assertEquals(3, allOps.size)
        assertTrue(allOps[0] is DrawRect)
        assertTrue(allOps[1] is DrawCircle)
        assertTrue(allOps[2] is DrawLine)
    }
}
