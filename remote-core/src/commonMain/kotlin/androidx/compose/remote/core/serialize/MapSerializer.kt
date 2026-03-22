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
package androidx.compose.remote.core.serialize

/** Represents a serializer for a map */
interface MapSerializer {

    /**
     * Adds a "type" field with this value
     *
     * @param type The name of the type
     */
    fun addType(type: String): MapSerializer

    /**
     * Add a float expression
     *
     * @param key The key
     * @param value The float src
     */
    fun addFloatExpressionSrc(key: String, value: FloatArray): MapSerializer

    /**
     * Add an int expression
     *
     * @param key The key
     * @param value The int src
     * @param mask For determining ID from int
     */
    fun addIntExpressionSrc(key: String, value: IntArray, mask: Int): MapSerializer

    /**
     * Add a path
     *
     * @param key The key
     * @param path The path
     */
    fun addPath(key: String, path: FloatArray): MapSerializer

    /**
     * Add metadata to this map for filtering by the data format generator.
     *
     * @param value A set of tags to add
     */
    fun addTags(vararg value: SerializeTags): MapSerializer

    /**
     * Add a list entry to this map. The List values can be any primitive, List, Map, or
     * Serializable
     *
     * @param key The key
     * @param value The list
     */
    fun <T> add(key: String, value: List<T>?): MapSerializer

    /**
     * Add a map entry to this map. The map values can be any primitive, List, Map, or Serializable
     *
     * @param key The key
     * @param value The map
     */
    fun <T> add(key: String, value: Map<String, T>?): MapSerializer

    /**
     * Adds any Serializable type to this map
     *
     * @param key The key
     * @param value The Serializable
     */
    fun add(key: String, value: Serializable?): MapSerializer

    /**
     * Adds a String entry
     *
     * @param key The key
     * @param value The String
     */
    fun add(key: String, value: String?): MapSerializer

    /**
     * Adds a color entry
     *
     * @param key The key
     * @param a Alpha value [0, 1]
     * @param r Red value [0, 1]
     * @param g Green value [0, 1]
     * @param b Blue value [0, 1]
     */
    fun addColor(key: String, a: Float, r: Float, g: Float, b: Float): MapSerializer

    /**
     * Adds an ID and Value pair. This can be either a value or variable.
     *
     * @param key The key
     * @param id Maybe float NaN ID
     * @param value Maybe value
     */
    fun addIdValue(key: String, id: Float, value: Float): MapSerializer

    /**
     * Convenience alias for addIdValue
     */
    fun add(key: String, id: Float, value: Float): MapSerializer = addIdValue(key, id, value)

    /**
     * Adds a Byte entry
     *
     * @param key The key
     * @param value The Byte
     */
    fun add(key: String, value: Byte?): MapSerializer

    /**
     * Adds a Short entry
     *
     * @param key The key
     * @param value The Short
     */
    fun add(key: String, value: Short?): MapSerializer

    /**
     * Adds an Integer entry
     *
     * @param key The key
     * @param value The Integer
     */
    fun add(key: String, value: Int?): MapSerializer

    /**
     * Adds a Long entry
     *
     * @param key The key
     * @param value The Long
     */
    fun add(key: String, value: Long?): MapSerializer

    /**
     * Adds a Float entry
     *
     * @param key The key
     * @param value The Float
     */
    fun add(key: String, value: Float?): MapSerializer

    /**
     * Adds a Double entry
     *
     * @param key The key
     * @param value The Double
     */
    fun add(key: String, value: Double?): MapSerializer

    /**
     * Adds a Boolean entry
     *
     * @param key The key
     * @param value The Boolean
     */
    fun add(key: String, value: Boolean?): MapSerializer

    /**
     * Adds an Enum entry
     *
     * @param key The key
     * @param value The Enum
     */
    fun <T : Enum<T>> add(key: String, value: Enum<T>?): MapSerializer

    companion object {
        /**
         * Similar to mapOf, but creates a LinkedHashMap preserving insertion order for predictable
         * serialization.
         *
         * @param keysAndValues an even number of items, repeating String key and Any value.
         * @return A LinkedHashMap.
         */
        fun orderedOf(vararg keysAndValues: Any): LinkedHashMap<String, Any> {
            val map = LinkedHashMap<String, Any>()
            var i = 0
            while (i < keysAndValues.size) {
                map[keysAndValues[i] as String] = keysAndValues[i + 1]
                i += 2
            }
            return map
        }
    }
}
