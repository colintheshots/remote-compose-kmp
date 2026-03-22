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

/**
 * Support a standardized interface to commands that contain arrays. All commands that implement
 * array access will be collected in a map in the state.
 */
interface ArrayAccess {
    /**
     * Get a value as a float for an index
     *
     * @param index position in the collection
     * @return float value
     */
    fun getFloatValue(index: Int): Float

    /**
     * If the objects have ids return the id
     *
     * @param index index of the object
     * @return id or -1 if no id is available
     */
    fun getId(index: Int): Int = -1

    /**
     * Get the backing array of float if available for float arrays
     *
     * @return array of floats or null
     */
    fun getFloats(): FloatArray?

    /**
     * Get the length of the collection
     *
     * @return length of the collection
     */
    fun getLength(): Int

    /**
     * Get the value as an integer if available
     *
     * @param index the position in the collection
     * @return the value as an integer
     */
    fun getIntValue(index: Int): Int = getFloatValue(index).toInt()
}
