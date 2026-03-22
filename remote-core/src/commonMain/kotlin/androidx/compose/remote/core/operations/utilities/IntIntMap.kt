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

class IntIntMap {

    private var keys: IntArray
    private var values: IntArray
    var size: Int = 0
        private set

    init {
        keys = IntArray(DEFAULT_CAPACITY) { NOT_PRESENT }
        values = IntArray(DEFAULT_CAPACITY)
    }

    /** Clear the map */
    fun clear() {
        keys.fill(NOT_PRESENT)
        values.fill(0)
        size = 0
    }

    /**
     * Is the key contained in map
     *
     * @param key the key to check
     * @return true if the map contains the key
     */
    fun contains(key: Int): Boolean = findKey(key) != -1

    /**
     * Put an item in the map
     *
     * @param key item's key
     * @param value item's value
     * @return old value if exists
     */
    fun put(key: Int, value: Int): Int {
        require(key != NOT_PRESENT) { "Key cannot be NOT_PRESENT" }
        if (size > keys.size * LOAD_FACTOR) {
            resize()
        }
        return insert(key, value)
    }

    /**
     * Get an element given the key
     *
     * @param key the key to fetch
     * @return the value
     */
    fun get(key: Int): Int {
        val index = findKey(key)
        return if (index == -1) 0 else values[index]
    }

    private fun insert(key: Int, value: Int): Int {
        var index = hash(key) % keys.size
        while (keys[index] != NOT_PRESENT && keys[index] != key) {
            index = (index + 1) % keys.size
        }
        val oldValue: Int
        if (keys[index] == NOT_PRESENT) {
            size++
            oldValue = 0
        } else {
            oldValue = values[index]
        }
        keys[index] = key
        values[index] = value
        return oldValue
    }

    private fun findKey(key: Int): Int {
        var index = hash(key) % keys.size
        while (keys[index] != NOT_PRESENT) {
            if (keys[index] == key) return index
            index = (index + 1) % keys.size
        }
        return -1
    }

    private fun hash(key: Int): Int = key

    private fun resize() {
        val oldKeys = keys
        val oldValues = values
        val newSize = oldKeys.size * 2
        keys = IntArray(newSize) { NOT_PRESENT }
        values = IntArray(newSize)
        size = 0
        for (i in oldKeys.indices) {
            if (oldKeys[i] != NOT_PRESENT) {
                put(oldKeys[i], oldValues[i])
            }
        }
    }

    companion object {
        private const val DEFAULT_CAPACITY = 16
        private const val LOAD_FACTOR = 0.75f
        private const val NOT_PRESENT = Int.MIN_VALUE
    }
}
