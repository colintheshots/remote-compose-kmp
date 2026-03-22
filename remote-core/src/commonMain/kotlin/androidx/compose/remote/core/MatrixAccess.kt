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
package androidx.compose.remote.core

/** Support access to floats in matrix */
interface MatrixAccess {
    /** Get the matrix */
    fun get(): FloatArray

    companion object {
        /** Convert a 4x4 matrix to a 3x3 matrix */
        fun to3x3(matrix: FloatArray): FloatArray? {
            if (matrix.size == 16) {
                val m3 = FloatArray(9)
                m3[0] = matrix[0]; m3[1] = matrix[1]; m3[2] = matrix[3]
                m3[3] = matrix[4]; m3[4] = matrix[5]; m3[5] = matrix[7]
                m3[6] = matrix[8]; m3[7] = matrix[9]; m3[8] = matrix[15]
                return m3
            } else if (matrix.size == 9) {
                return matrix
            }
            return null
        }

        /** Dump a matrix to the console */
        fun dump(m: FloatArray) {
            val step = if (m.size == 16) 4 else 3
            val sb = StringBuilder()
            for (i in m.indices) {
                if (i % step == 0) sb.append("\n")
                sb.append("${m[i]}  ")
            }
            println(sb.toString())
        }
    }
}
