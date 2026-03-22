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

/** Base class for commands that take 3 floats */
abstract class DrawBase3(v1: Float, v2: Float, v3: Float) :
    PaintOperation(), VariableSupport, Serializable {

    protected var mName: String = "DrawRectBase"
    var mV1: Float = v1
    var mV2: Float = v2
    var mV3: Float = v3
    var mValue1: Float = v1
    var mValue2: Float = v2
    var mValue3: Float = v3

    override fun updateVariables(context: RemoteContext) {
        mV1 = if (Utils.isVariable(mValue1)) context.getFloat(Utils.idFromNan(mValue1)) else mValue1
        mV2 = if (Utils.isVariable(mValue2)) context.getFloat(Utils.idFromNan(mValue2)) else mValue2
        mV3 = if (Utils.isVariable(mValue3)) context.getFloat(Utils.idFromNan(mValue3)) else mValue3
    }

    override fun registerListening(context: RemoteContext) {
        if (Utils.isVariable(mValue1)) context.listensTo(Utils.idFromNan(mValue1), this)
        if (Utils.isVariable(mValue2)) context.listensTo(Utils.idFromNan(mValue2), this)
        if (Utils.isVariable(mValue3)) context.listensTo(Utils.idFromNan(mValue3), this)
    }

    override fun write(buffer: WireBuffer) {
        write(buffer, mV1, mV2, mV3)
    }

    protected abstract fun write(buffer: WireBuffer, v1: Float, v2: Float, v3: Float)

    fun interface Maker {
        fun create(v1: Float, v2: Float, v3: Float): DrawBase3
    }

    override fun toString(): String {
        return "$mName ${Utils.floatToString(mV1)} ${Utils.floatToString(mV2)} ${Utils.floatToString(mV3)}"
    }

    open fun construct(x1: Float, y1: Float, x2: Float): Operation? = null

    protected fun serialize(
        serializer: MapSerializer,
        v1Name: String,
        v2Name: String,
        v3Name: String
    ): MapSerializer {
        return serializer
            .add(v1Name, mValue1, mV1)
            .add(v2Name, mValue2, mV2)
            .add(v3Name, mValue3, mV3)
    }

    companion object {
        fun read(buffer: WireBuffer, operations: MutableList<Operation>, maker: Maker) {
            val v1 = buffer.readFloat()
            val v2 = buffer.readFloat()
            val v3 = buffer.readFloat()
            val op = maker.create(v1, v2, v3)
            operations.add(op)
        }
    }
}
