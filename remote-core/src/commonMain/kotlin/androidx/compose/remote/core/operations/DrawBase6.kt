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

/** Base class for draw commands that take 6 floats */
abstract class DrawBase6(
    v1: Float, v2: Float, v3: Float, v4: Float, v5: Float, v6: Float
) : PaintOperation(), VariableSupport, Serializable {

    protected var mName: String = "DrawRectBase"
    var mV1: Float = v1
    var mV2: Float = v2
    var mV3: Float = v3
    var mV4: Float = v4
    var mV5: Float = v5
    var mV6: Float = v6
    var mValue1: Float = v1
    var mValue2: Float = v2
    var mValue3: Float = v3
    var mValue4: Float = v4
    var mValue5: Float = v5
    var mValue6: Float = v6

    override fun updateVariables(context: RemoteContext) {
        mV1 = if (mValue1.isNaN()) context.getFloat(Utils.idFromNan(mValue1)) else mValue1
        mV2 = if (mValue2.isNaN()) context.getFloat(Utils.idFromNan(mValue2)) else mValue2
        mV3 = if (mValue3.isNaN()) context.getFloat(Utils.idFromNan(mValue3)) else mValue3
        mV4 = if (mValue4.isNaN()) context.getFloat(Utils.idFromNan(mValue4)) else mValue4
        mV5 = if (mValue5.isNaN()) context.getFloat(Utils.idFromNan(mValue5)) else mValue5
        mV6 = if (mValue6.isNaN()) context.getFloat(Utils.idFromNan(mValue6)) else mValue6
    }

    override fun registerListening(context: RemoteContext) {
        if (mValue1.isNaN()) context.listensTo(Utils.idFromNan(mValue1), this)
        if (mValue2.isNaN()) context.listensTo(Utils.idFromNan(mValue2), this)
        if (mValue3.isNaN()) context.listensTo(Utils.idFromNan(mValue3), this)
        if (mValue4.isNaN()) context.listensTo(Utils.idFromNan(mValue4), this)
        if (mValue5.isNaN()) context.listensTo(Utils.idFromNan(mValue5), this)
        if (mValue6.isNaN()) context.listensTo(Utils.idFromNan(mValue6), this)
    }

    override fun write(buffer: WireBuffer) {
        write(buffer, mV1, mV2, mV3, mV4, mV5, mV6)
    }

    protected abstract fun write(
        buffer: WireBuffer, v1: Float, v2: Float, v3: Float, v4: Float, v5: Float, v6: Float
    )

    override fun toString(): String {
        return "$mName ${Utils.floatToString(mV1)} ${Utils.floatToString(mV2)} ${Utils.floatToString(mV3)} ${Utils.floatToString(mV4)}"
    }

    fun interface Maker {
        fun create(v1: Float, v2: Float, v3: Float, v4: Float, v5: Float, v6: Float): DrawBase6
    }

    open fun construct(
        v1: Float, v2: Float, v3: Float, v4: Float, v5: Float, v6: Float
    ): Operation? = null

    protected fun serialize(
        serializer: MapSerializer,
        v1Name: String, v2Name: String, v3Name: String,
        v4Name: String, v5Name: String, v6Name: String
    ): MapSerializer {
        return serializer
            .add(v1Name, mValue1, mV1)
            .add(v2Name, mValue2, mV2)
            .add(v3Name, mValue3, mV3)
            .add(v4Name, mValue4, mV4)
            .add(v5Name, mValue5, mV5)
            .add(v6Name, mValue6, mV6)
    }

    companion object {
        fun read(buffer: WireBuffer, operations: MutableList<Operation>, build: Maker) {
            val sv1 = buffer.readFloat()
            val sv2 = buffer.readFloat()
            val sv3 = buffer.readFloat()
            val sv4 = buffer.readFloat()
            val sv5 = buffer.readFloat()
            val sv6 = buffer.readFloat()
            val op = build.create(sv1, sv2, sv3, sv4, sv5, sv6)
            operations.add(op)
        }

        fun name(): String = "DrawBase6"
    }
}
