/*
 * Copyright 2025 The Android Open Source Project
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
package androidx.compose.remote.core.operations.utilities.touch

import androidx.compose.remote.core.WireBuffer

/**
 * Utility to read an arbitrary parameter set in a command in a compact format
 */
class CommandParameters(vararg params: Param) {

    val mParams = arrayOfNulls<Param>(256)
    var mParamsCount = 0

    init {
        for (param in params) {
            mParams[0xFF and param.mId.toInt()] = param
        }
        mParamsCount = params.size
    }

    /** Returns the number of known parameters */
    fun getParamsCount(): Int = mParamsCount

    fun isDefault(id: Byte, value: Int): Boolean {
        val param = mParams[0xFF and id.toInt()] ?: return false
        return param.mType == P_INT && value == param.mDefaultIntValue
    }

    fun isDefault(id: Byte, value: Float): Boolean {
        val param = mParams[0xFF and id.toInt()] ?: return false
        return param.mType == P_FLOAT && value == param.mDefaultFloatValue
    }

    fun isDefault(id: Byte, value: Boolean): Boolean {
        val param = mParams[0xFF and id.toInt()] ?: return false
        return param.mType == P_BOOLEAN && value == param.mDefaultBooleanValue
    }

    fun countIfNotDefault(id: Byte, value: Int): Int =
        if (isDefault(id, value)) 0 else 1

    fun countIfNotDefault(id: Byte, value: Float): Int =
        if (isDefault(id, value)) 0 else 1

    fun countIfNotDefault(id: Byte, value: Boolean): Int =
        if (isDefault(id, value)) 0 else 1

    /** A parameter */
    class Param {
        var mName: String
        var mId: Byte
        var mType: Byte

        var mDefaultIntValue: Int = 0
        var mDefaultFloatValue: Float = 0f
        var mDefaultBooleanValue: Boolean = false

        constructor(name: String, id: Byte, type: Byte) {
            mName = name
            mId = id
            mType = type
        }

        constructor(name: String, id: Byte, defaultValue: Int) {
            mName = name
            mId = id
            mType = P_INT
            mDefaultIntValue = defaultValue
        }

        constructor(name: String, id: Byte, defaultValue: Float) {
            mName = name
            mId = id
            mType = P_FLOAT
            mDefaultFloatValue = defaultValue
        }

        constructor(name: String, id: Byte, defaultValue: Boolean) {
            mName = name
            mId = id
            mType = P_BOOLEAN
            mDefaultBooleanValue = defaultValue
        }
    }

    fun write(buffer: WireBuffer, id: Byte, value: Int): Boolean {
        val param = mParams[0xFF and id.toInt()]!!
        if (param.mType == P_INT && value == param.mDefaultIntValue) return false
        buffer.writeByte(param.mId.toInt())
        when (param.mType) {
            P_INT -> buffer.writeInt(value)
            P_FLOAT -> buffer.writeFloat(Float.fromBits(value))
            P_SHORT -> buffer.writeShort(value)
            P_BYTE -> buffer.writeByte(value)
            else -> throw IllegalArgumentException("Unknown parameter type ${param.mType}")
        }
        return true
    }

    fun write(buffer: WireBuffer, id: Byte, value: Float): Boolean {
        val param = mParams[0xFF and id.toInt()]!!
        if (param.mType != P_FLOAT) {
            throw IllegalArgumentException("Unknown parameter type ${param.mType}")
        }
        if (value == param.mDefaultFloatValue) return false
        buffer.writeByte(param.mId.toInt())
        buffer.writeFloat(value)
        return true
    }

    fun write(buffer: WireBuffer, id: Byte, value: FloatArray) {
        val param = mParams[0xFF and id.toInt()]!!
        if (param.mType != PA_FLOAT) {
            throw IllegalArgumentException("Unknown parameter type ${param.mType}")
        }
        buffer.writeByte(param.mId.toInt())
        buffer.writeShort(value.size)
        for (v in value) {
            buffer.writeFloat(v)
        }
    }

    fun write(buffer: WireBuffer, id: Byte, value: IntArray) {
        val param = mParams[0xFF and id.toInt()]!!
        if (param.mType != PA_INT) {
            throw IllegalArgumentException("Unknown parameter type ${param.mType}")
        }
        buffer.writeByte(param.mId.toInt())
        buffer.writeShort(value.size)
        for (v in value) {
            buffer.writeInt(v)
        }
    }

    fun write(buffer: WireBuffer, id: Byte, value: String) {
        val param = mParams[0xFF and id.toInt()]!!
        if (param.mType != PA_STRING) {
            throw IllegalArgumentException("Unknown parameter type ${param.mType}")
        }
        buffer.writeByte(param.mId.toInt())
        buffer.writeUTF8(value)
    }

    fun writeByte(buffer: WireBuffer, id: Byte, value: Byte) {
        val param = mParams[0xFF and id.toInt()]!!
        buffer.writeByte(param.mId.toInt())
        if (param.mType == P_BYTE) {
            buffer.writeByte(value.toInt())
        } else {
            throw IllegalArgumentException("Unknown parameter type ${param.mType}")
        }
    }

    fun write(buffer: WireBuffer, id: Byte, value: Boolean): Boolean {
        val param = mParams[0xFF and id.toInt()]!!
        if (param.mType == P_BOOLEAN && value == param.mDefaultBooleanValue) return false
        buffer.writeByte(param.mId.toInt())
        if (param.mType == P_BOOLEAN) {
            buffer.writeBoolean(value)
        } else {
            throw IllegalArgumentException("Unknown parameter type ${param.mType}")
        }
        return true
    }

    /** Callback for reading parameters from a buffer */
    interface Callback {
        fun value(id: Int, value: Int)
        fun value(id: Int, value: Float)
        fun value(id: Int, value: Short)
        fun value(id: Int, value: Byte)
        fun value(id: Int, value: Boolean)
        fun value(id: Int, value: String)
        fun value(id: Int, value: IntArray)
        fun value(id: Int, value: FloatArray)
    }

    /** Abstract callback for reading parameters from a buffer */
    abstract class AbstractCallback : Callback {
        override fun value(id: Int, value: Int) {}
        override fun value(id: Int, value: Float) {}
        override fun value(id: Int, value: Short) {}
        override fun value(id: Int, value: Byte) {}
        override fun value(id: Int, value: Boolean) {}
        override fun value(id: Int, value: String) {}
        override fun value(id: Int, value: IntArray) {}
        override fun value(id: Int, value: FloatArray) {}
    }

    fun read(buffer: WireBuffer, callback: Callback) {
        val id = buffer.readByte()
        val param = mParams[0xFF and id]!!
        when (param.mType) {
            P_INT -> callback.value(id, buffer.readInt())
            P_FLOAT -> callback.value(id, Float.fromBits(buffer.readInt()))
            P_SHORT -> callback.value(id, buffer.readShort().toShort())
            P_BYTE -> callback.value(id, buffer.readByte().toByte())
            P_BOOLEAN -> callback.value(id, buffer.readBoolean())
            PA_INT -> {
                val count = buffer.readShort()
                val values = IntArray(count)
                for (i in 0 until count) {
                    values[i] = buffer.readInt()
                }
                callback.value(id, values)
            }
            PA_FLOAT -> {
                val count = buffer.readShort()
                val floats = FloatArray(count)
                for (i in 0 until count) {
                    floats[i] = buffer.readFloat()
                }
                callback.value(id, floats)
            }
            PA_STRING -> {
                val str = buffer.readUTF8()
                callback.value(id, str)
            }
            else -> throw IllegalArgumentException("Unknown parameter type ${param.mType}")
        }
    }

    companion object {
        const val P_INT: Byte = 1
        const val P_FLOAT: Byte = 2
        const val P_SHORT: Byte = 3
        const val P_BYTE: Byte = 4
        const val P_BOOLEAN: Byte = 5
        const val PA_INT: Byte = 6
        const val PA_FLOAT: Byte = 7
        const val PA_STRING: Byte = 8

        fun param(name: String, id: Byte, type: Byte): Param = Param(name, id, type)
        fun param(name: String, id: Byte, defaultValue: Int): Param = Param(name, id, defaultValue)
        fun param(name: String, id: Byte, defaultValue: Float): Param = Param(name, id, defaultValue)
        fun param(name: String, id: Byte, defaultValue: Boolean): Param = Param(name, id, defaultValue)
    }
}
