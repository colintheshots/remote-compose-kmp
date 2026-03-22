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

open class IntMap<T> {

    private var keys: IntArray
    private var values: MutableList<T?>
    var size: Int = 0
        private set

    init {
        keys = IntArray(DEFAULT_CAPACITY) { NOT_PRESENT }
        values = MutableList(DEFAULT_CAPACITY) { null }
    }

    /** Clear the map */
    fun clear() {
        keys.fill(NOT_PRESENT)
        values.clear()
        size = 0
    }

    /**
     * Insert the value into the map with the given key
     *
     * @param key the key
     * @param value the value
     * @return the previous value, or null
     */
    open fun put(key: Int, value: T): T? {
        require(key != NOT_PRESENT) { "Key cannot be NOT_PRESENT" }
        if (size > keys.size * LOAD_FACTOR) {
            resize()
        }
        return insert(key, value)
    }

    /**
     * Insert all the key values from the given map
     *
     * @param map the source map
     */
    fun putAll(map: IntMap<T>) {
        for (i in map.values.indices) {
            val key = map.keys[i]
            if (key != NOT_PRESENT) {
                val value = map.get(key)
                if (value != null) {
                    put(key, value)
                }
            }
        }
    }

    /**
     * Return the value associated with the given key
     *
     * @param key the key
     * @return the value or null
     */
    fun get(key: Int): T? {
        val index = findKey(key)
        return if (index == -1) null else values[index]
    }

    /**
     * Returns a [Set] view of the keys contained in this map.
     * The set is a copy; changes to the map are not reflected in the set, and vice-versa.
     *
     * @return a set view of the keys contained in this map
     */
    fun keySet(): Set<Int> {
        val result = mutableSetOf<Int>()
        for (value in keys) {
            if (value != NOT_PRESENT) {
                result.add(value)
            }
        }
        return result
    }

    private fun insert(key: Int, value: T): T? {
        var index = hash(key) % keys.size
        while (keys[index] != NOT_PRESENT && keys[index] != key) {
            index = (index + 1) % keys.size
        }
        val oldValue: T?
        if (keys[index] == NOT_PRESENT) {
            size++
            oldValue = null
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
            if (keys[index] == key) {
                return index
            }
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
        values = MutableList(newSize) { null }
        size = 0
        for (i in oldKeys.indices) {
            if (oldKeys[i] != NOT_PRESENT) {
                val v = oldValues[i]
                if (v != null) {
                    put(oldKeys[i], v)
                }
            }
        }
    }

    /**
     * Remove the key from the map
     *
     * @param key the key to remove
     * @return the removed value or null
     */
    fun remove(key: Int): T? {
        var index = hash(key) % keys.size
        val initialIndex = index

        while (keys[index] != NOT_PRESENT) {
            if (keys[index] == key) {
                val oldValue = values[index]
                keys[index] = NOT_PRESENT
                values[index] = null
                size--
                rehashFrom((index + 1) % keys.size)
                return oldValue
            }
            index = (index + 1) % keys.size
            if (index == initialIndex) {
                break
            }
        }
        return null
    }

    private fun rehashFrom(startIndex: Int) {
        var index = startIndex
        while (keys[index] != NOT_PRESENT) {
            val keyToRehash = keys[index]
            val valueToRehash = values[index]
            keys[index] = NOT_PRESENT
            values[index] = null
            size--
            if (valueToRehash != null) {
                insert(keyToRehash, valueToRehash)
            }
            index = (index + 1) % keys.size
        }
    }

    companion object {
        private const val DEFAULT_CAPACITY = 16
        private const val LOAD_FACTOR = 0.75f
        private const val NOT_PRESENT = Int.MIN_VALUE
    }
}
