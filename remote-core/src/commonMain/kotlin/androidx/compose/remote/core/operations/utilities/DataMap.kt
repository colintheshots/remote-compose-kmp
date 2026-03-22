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
package androidx.compose.remote.core.operations.utilities

class DataMap(
    val names: Array<String>,
    val types: ByteArray,
    val ids: IntArray
) {

    /**
     * Return position for given string
     *
     * @param str string
     * @return position associated with the string
     */
    fun getPos(str: String): Int {
        for (i in names.indices) {
            if (str == names[i]) {
                return i
            }
        }
        return -1
    }

    /**
     * Return type for given index
     *
     * @param pos index
     * @return type at index
     */
    fun getType(pos: Int): Byte = types[pos]

    /**
     * Return id for given index
     *
     * @param pos index
     * @return id at index
     */
    fun getId(pos: Int): Int = ids[pos]
}
