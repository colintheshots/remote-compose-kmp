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
package androidx.compose.remote.core.operations.utilities

import androidx.compose.remote.core.operations.Utils

/**
 * This defines the major id maps and ranges used by remote compose. Generally ids ranging from
 * 1 ... 7FFFFF (4095) are for ids. The data range is divided into bits:
 * 0xxxxx are allocated for Predefined Global System Variables
 * 1xxxxx are allocated to normal variables
 * 2xxxxx are allocated to List&MAPS (Arrays of stuff)
 * 3xxxxx are allocated to path & float operations
 * 4xxxxx,5xxxxx,7xxxxx are reserved for future use
 */
object NanMap {

    const val MOVE = 0x300_000
    const val LINE = 0x300_001
    const val QUADRATIC = 0x300_002
    const val CONIC = 0x300_003
    const val CUBIC = 0x300_004
    const val CLOSE = 0x300_005
    const val DONE = 0x300_006
    val MOVE_NAN = Utils.asNan(MOVE)
    val LINE_NAN = Utils.asNan(LINE)
    val QUADRATIC_NAN = Utils.asNan(QUADRATIC)
    val CONIC_NAN = Utils.asNan(CONIC)
    val CUBIC_NAN = Utils.asNan(CUBIC)
    val CLOSE_NAN = Utils.asNan(CLOSE)
    val DONE_NAN = Utils.asNan(DONE)

    /**
     * Returns true if the float id is a system variable
     *
     * @param value the id encoded as float NaN
     * @return true if system variable
     */
    fun isSystemVariable(value: Float): Boolean {
        return (fromNaN(value) shr 20) == 0
    }

    /**
     * Returns true if the float id is a normal variable
     *
     * @param value the id encoded as float NaN
     * @return true if normal variable
     */
    fun isNormalVariable(value: Float): Boolean {
        return (fromNaN(value) shr 20) == 1
    }

    /**
     * Returns true if the float id is a data variable
     *
     * @param value the id encoded as float NaN
     * @return true if data variable
     */
    fun isDataVariable(value: Float): Boolean {
        return (fromNaN(value) shr 20) == 2
    }

    /**
     * Returns true if the float id is a var1
     *
     * @param value the id encoded as float NaN
     * @return true if the float id is a var1
     */
    fun isVar1(value: Float): Boolean {
        return value.toRawBits() == AnimatedFloatExpression.VAR1.toRawBits()
    }

    /**
     * Returns true if the float id is an operation variable
     *
     * @param value the id encoded as float NaN
     * @return true if operation variable
     */
    fun isOperationVariable(value: Float): Boolean {
        return (fromNaN(value) shr 20) == 3
    }

    const val START_VAR = (1 shl 20) + 42
    const val START_ARRAY = (2 shl 20) + 42
    const val TYPE_SYSTEM = 0
    const val TYPE_VARIABLE = 1
    const val TYPE_ARRAY = 2
    const val TYPE_OPERATION = 3
    const val ID_REGION_MASK = 0x700000
    const val ID_REGION_ARRAY = 0x200000

    /**
     * Get ID from NaN float
     *
     * @param v the NaN float
     * @return the id
     */
    fun fromNaN(v: Float): Int {
        val b = v.toRawBits()
        return b and 0x7FFFFF
    }

    /**
     * Given id return as a NaN float
     *
     * @param v the id
     * @return the NaN float
     */
    fun asNan(v: Int): Float {
        return Float.fromBits(v or 0xFF800000.toInt())
    }
}
