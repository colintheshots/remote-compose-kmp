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

import androidx.compose.remote.core.operations.paint.PaintBundle
import kotlin.test.Test
import kotlin.test.assertEquals

/** Tests for PaintBundle serialization round-trips */
class PaintBundleTest {

    @Test
    fun colorRoundTrip() {
        val paint = PaintBundle()
        val color = 0xFFFF0000.toInt() // opaque red
        paint.setColor(color)

        val buffer = WireBuffer(256)
        paint.writeBundle(buffer)

        buffer.index = 0
        val restored = PaintBundle()
        restored.readBundle(buffer)

        assertEquals(paint.mPos, restored.mPos, "Position count should match")
        for (i in 0 until paint.mPos) {
            assertEquals(paint.mArray[i], restored.mArray[i], "Array element $i should match")
        }
    }

    @Test
    fun textSizeRoundTrip() {
        val paint = PaintBundle()
        paint.setTextSize(24.0f)

        val buffer = WireBuffer(256)
        paint.writeBundle(buffer)

        buffer.index = 0
        val restored = PaintBundle()
        restored.readBundle(buffer)

        assertEquals(paint.mPos, restored.mPos)
        for (i in 0 until paint.mPos) {
            assertEquals(paint.mArray[i], restored.mArray[i])
        }
    }

    @Test
    fun strokeWidthRoundTrip() {
        val paint = PaintBundle()
        paint.setStrokeWidth(3.5f)

        val buffer = WireBuffer(256)
        paint.writeBundle(buffer)

        buffer.index = 0
        val restored = PaintBundle()
        restored.readBundle(buffer)

        assertEquals(paint.mPos, restored.mPos)
        for (i in 0 until paint.mPos) {
            assertEquals(paint.mArray[i], restored.mArray[i])
        }
    }

    @Test
    fun styleRoundTrip() {
        val paint = PaintBundle()
        paint.setStyle(PaintBundle.STYLE_STROKE)

        val buffer = WireBuffer(256)
        paint.writeBundle(buffer)

        buffer.index = 0
        val restored = PaintBundle()
        restored.readBundle(buffer)

        assertEquals(paint.mPos, restored.mPos)
        for (i in 0 until paint.mPos) {
            assertEquals(paint.mArray[i], restored.mArray[i])
        }
    }

    @Test
    fun multiplePropertiesRoundTrip() {
        val paint = PaintBundle()
        paint.setColor(0xFF00FF00.toInt()) // green
        paint.setTextSize(16.0f)
        paint.setStrokeWidth(2.0f)
        paint.setStyle(PaintBundle.STYLE_FILL_AND_STROKE)
        paint.setStrokeCap(1) // round cap

        val buffer = WireBuffer(512)
        paint.writeBundle(buffer)

        buffer.index = 0
        val restored = PaintBundle()
        restored.readBundle(buffer)

        assertEquals(paint.mPos, restored.mPos, "Position count should match for combined paint")
        for (i in 0 until paint.mPos) {
            assertEquals(
                paint.mArray[i], restored.mArray[i],
                "Array element $i should match for combined paint"
            )
        }
    }

    @Test
    fun strokeMiterRoundTrip() {
        val paint = PaintBundle()
        paint.setStrokeMiter(4.0f)

        val buffer = WireBuffer(256)
        paint.writeBundle(buffer)

        buffer.index = 0
        val restored = PaintBundle()
        restored.readBundle(buffer)

        assertEquals(paint.mPos, restored.mPos)
        for (i in 0 until paint.mPos) {
            assertEquals(paint.mArray[i], restored.mArray[i])
        }
    }

    @Test
    fun strokeJoinRoundTrip() {
        val paint = PaintBundle()
        paint.setStrokeJoin(2) // bevel

        val buffer = WireBuffer(256)
        paint.writeBundle(buffer)

        buffer.index = 0
        val restored = PaintBundle()
        restored.readBundle(buffer)

        assertEquals(paint.mPos, restored.mPos)
        for (i in 0 until paint.mPos) {
            assertEquals(paint.mArray[i], restored.mArray[i])
        }
    }

    @Test
    fun paintDataOperationRoundTrip() {
        val buffer = WireBuffer(512)
        buffer.setVersion(6, 0)

        val paint = PaintBundle()
        paint.setColor(0xFFABCDEF.toInt())
        paint.setTextSize(32.0f)

        val operations = mutableListOf<Operation>()
        androidx.compose.remote.core.operations.PaintData.apply(buffer, paint)

        buffer.index = 0
        val opType = buffer.readOperationType()
        assertEquals(Operations.PAINT_VALUES, opType)

        androidx.compose.remote.core.operations.PaintData.read(buffer, operations)
        assertEquals(1, operations.size)

        val restored = (operations[0] as androidx.compose.remote.core.operations.PaintData).mPaintData
        assertEquals(paint.mPos, restored.mPos)
        for (i in 0 until paint.mPos) {
            assertEquals(paint.mArray[i], restored.mArray[i])
        }
    }
}
