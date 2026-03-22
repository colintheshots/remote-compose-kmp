/*
 * Copyright (C) 2023 The Android Open Source Project
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

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Tests for the Operations registry */
class OperationsTest {

    @Test
    fun validReturnsTrueForRegisteredV6Opcodes() {
        val registeredOpcodes = listOf(
            Operations.HEADER,
            Operations.DRAW_RECT,
            Operations.DRAW_CIRCLE,
            Operations.DRAW_LINE,
            Operations.PAINT_VALUES,
            Operations.DRAW_TEXT_RUN,
            Operations.CLIP_RECT,
            Operations.MATRIX_SAVE,
            Operations.MATRIX_RESTORE,
            Operations.LAYOUT_ROOT,
            Operations.LAYOUT_BOX,
            Operations.LAYOUT_ROW,
            Operations.LAYOUT_COLUMN,
        )
        for (opcode in registeredOpcodes) {
            assertTrue(
                Operations.valid(opcode, 6, 0),
                "Opcode $opcode should be valid for V6"
            )
        }
    }

    @Test
    fun validReturnsTrueForV6SpecificOpcodes() {
        // DATA_SHADER and ROOT_CONTENT_BEHAVIOR are V6-specific additions
        assertTrue(Operations.valid(Operations.DATA_SHADER, 6, 0))
        assertTrue(Operations.valid(Operations.ROOT_CONTENT_BEHAVIOR, 6, 0))
    }

    @Test
    fun validReturnsFalseForUnregisteredOpcodes() {
        // Opcode 255 (EXTENDED_OPCODE) is not registered in V6
        assertFalse(Operations.valid(Operations.EXTENDED_OPCODE, 6, 0))
    }

    @Test
    fun validReturnsFalseForUnsupportedApiLevel() {
        // API level 5 is not supported
        assertFalse(Operations.valid(Operations.HEADER, 5, 0))
    }

    @Test
    fun getOperationsReturnsNullForUnsupportedApiLevel() {
        assertNull(Operations.getOperations(5, 0))
        assertNull(Operations.getOperations(8, 0))
    }

    @Test
    fun getOperationsReturnsMapForV6() {
        val map = Operations.getOperations(6, 0)
        assertNotNull(map, "V6 operations map should not be null")
        assertNotNull(map.get(Operations.HEADER))
        assertNotNull(map.get(Operations.DRAW_RECT))
    }

    @Test
    fun getOperationsReturnsMapForV7Baseline() {
        val map = Operations.getOperations(7, 0)
        assertNotNull(map, "V7 baseline operations map should not be null")
        // V7 baseline includes REM, MATRIX_CONSTANT, etc.
        assertNotNull(map.get(Operations.REM))
        assertNotNull(map.get(Operations.MATRIX_CONSTANT))
        assertNotNull(map.get(Operations.MATRIX_EXPRESSION))
        assertNotNull(map.get(Operations.MATRIX_VECTOR_MATH))
    }

    @Test
    fun getOperationsV7WithAndroidXProfile() {
        val map = Operations.getOperations(7, RcProfiles.PROFILE_ANDROIDX)
        assertNotNull(map)
        // AndroidX profile adds DATA_SHADER, WAKE_IN, etc.
        assertNotNull(map.get(Operations.DATA_SHADER))
        assertNotNull(map.get(Operations.WAKE_IN))
        assertNotNull(map.get(Operations.DRAW_TO_BITMAP))
    }

    @Test
    fun getOperationsV7WithWidgetsProfile() {
        val map = Operations.getOperations(7, RcProfiles.PROFILE_WIDGETS)
        assertNotNull(map)
        assertNotNull(map.get(Operations.WAKE_IN))
        assertNotNull(map.get(Operations.DRAW_TO_BITMAP))
    }

    @Test
    fun allDefaultOpcodesRegistered() {
        val map = Operations.getOperations(6, 0)
        assertNotNull(map)
        // Check a comprehensive set of default opcodes
        val defaultOpcodes = listOf(
            Operations.HEADER,
            Operations.DRAW_RECT,
            Operations.DRAW_CIRCLE,
            Operations.DRAW_LINE,
            Operations.DRAW_OVAL,
            Operations.DRAW_ROUND_RECT,
            Operations.DRAW_SECTOR,
            Operations.PAINT_VALUES,
            Operations.CLIP_PATH,
            Operations.CLIP_RECT,
            Operations.MATRIX_SAVE,
            Operations.MATRIX_RESTORE,
            Operations.MATRIX_ROTATE,
            Operations.MATRIX_SCALE,
            Operations.MATRIX_SKEW,
            Operations.MATRIX_TRANSLATE,
            Operations.DATA_FLOAT,
            Operations.ANIMATED_FLOAT,
            Operations.LAYOUT_ROOT,
            Operations.LAYOUT_BOX,
            Operations.LAYOUT_ROW,
            Operations.LAYOUT_COLUMN,
            Operations.LAYOUT_CANVAS,
            Operations.LAYOUT_TEXT,
            Operations.CONTAINER_END,
            Operations.COMPONENT_START,
        )
        for (opcode in defaultOpcodes) {
            assertNotNull(
                map.get(opcode),
                "Default opcode $opcode should be registered"
            )
        }
    }
}
