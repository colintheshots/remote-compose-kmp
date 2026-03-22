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
package androidx.compose.remote.core.operations

import androidx.compose.remote.core.Operation
import androidx.compose.remote.core.PaintOperation
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.VariableSupport
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable

/** Base class for commands that take 2 floats */
abstract class DrawBase2(v1: Float, v2: Float) :
    PaintOperation(), VariableSupport, Serializable {

    protected var mName: String = "DrawRectBase"
    var mV1: Float = v1
    var mV2: Float = v2
    var mValue1: Float = v1
    var mValue2: Float = v2

    override fun updateVariables(context: RemoteContext) {
        mV1 = if (mValue1.isNaN()) context.getFloat(Utils.idFromNan(mValue1)) else mValue1
        mV2 = if (mValue2.isNaN()) context.getFloat(Utils.idFromNan(mValue2)) else mValue2
    }

    override fun registerListening(context: RemoteContext) {
        if (mValue1.isNaN()) {
            context.listensTo(Utils.idFromNan(mValue1), this)
        }
        if (mValue2.isNaN()) {
            context.listensTo(Utils.idFromNan(mValue2), this)
        }
    }

    override fun write(buffer: WireBuffer) {
        write(buffer, mV1, mV2)
    }

    protected abstract fun write(buffer: WireBuffer, v1: Float, v2: Float)

    fun interface Maker {
        fun create(v1: Float, v2: Float): DrawBase2
    }

    override fun toString(): String {
        return "$mName ${Utils.floatToString(mV1)} ${Utils.floatToString(mV2)}"
    }

    open fun construct(x1: Float, y1: Float): Operation? = null

    protected fun serialize(
        serializer: MapSerializer,
        v1Name: String,
        v2Name: String
    ): MapSerializer {
        return serializer.add(v1Name, mValue1, mV1).add(v2Name, mValue2, mV2)
    }

    companion object {
        fun read(buffer: WireBuffer, operations: MutableList<Operation>, maker: Maker) {
            val v1 = buffer.readFloat()
            val v2 = buffer.readFloat()
            val op = maker.create(v1, v2)
            operations.add(op)
        }

        fun write(buffer: WireBuffer, opCode: Int, x1: Float, y1: Float) {
            buffer.start(opCode)
            buffer.writeFloat(x1)
            buffer.writeFloat(y1)
        }
    }
}
