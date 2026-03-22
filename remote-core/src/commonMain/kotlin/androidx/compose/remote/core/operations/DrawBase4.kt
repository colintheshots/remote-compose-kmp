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

/** Base class for draw commands that take 4 floats */
abstract class DrawBase4(x1: Float, y1: Float, x2: Float, y2: Float) :
    PaintOperation(), VariableSupport, Serializable {

    protected var mName: String = "DrawRectBase"
    protected var mX1: Float = x1
    protected var mY1: Float = y1
    protected var mX2: Float = x2
    protected var mY2: Float = y2
    var mX1Value: Float = x1
    var mY1Value: Float = y1
    var mX2Value: Float = x2
    var mY2Value: Float = y2

    override fun updateVariables(context: RemoteContext) {
        mX1 = if (mX1Value.isNaN()) context.getFloat(Utils.idFromNan(mX1Value)) else mX1Value
        mY1 = if (mY1Value.isNaN()) context.getFloat(Utils.idFromNan(mY1Value)) else mY1Value
        mX2 = if (mX2Value.isNaN()) context.getFloat(Utils.idFromNan(mX2Value)) else mX2Value
        mY2 = if (mY2Value.isNaN()) context.getFloat(Utils.idFromNan(mY2Value)) else mY2Value
    }

    override fun registerListening(context: RemoteContext) {
        if (mX1Value.isNaN()) context.listensTo(Utils.idFromNan(mX1Value), this)
        if (mY1Value.isNaN()) context.listensTo(Utils.idFromNan(mY1Value), this)
        if (mX2Value.isNaN()) context.listensTo(Utils.idFromNan(mX2Value), this)
        if (mY2Value.isNaN()) context.listensTo(Utils.idFromNan(mY2Value), this)
    }

    override fun write(buffer: WireBuffer) {
        write(buffer, mX1, mY1, mX2, mY2)
    }

    protected abstract fun write(buffer: WireBuffer, v1: Float, v2: Float, v3: Float, v4: Float)

    fun interface Maker {
        fun create(v1: Float, v2: Float, v3: Float, v4: Float): DrawBase4
    }

    override fun toString(): String {
        return "$mName ${Utils.floatToString(mX1Value, mX1)} ${Utils.floatToString(mY1Value, mY1)} ${Utils.floatToString(mX2Value, mX2)} ${Utils.floatToString(mY2Value, mY2)}"
    }

    open fun construct(x1: Float, y1: Float, x2: Float, y2: Float): Operation? = null

    protected fun serialize(
        serializer: MapSerializer,
        x1Name: String,
        y1Name: String,
        x2Name: String,
        y2Name: String
    ): MapSerializer {
        return serializer
            .add(x1Name, mX1Value, mX1)
            .add(y1Name, mY1Value, mY1)
            .add(x2Name, mX2Value, mX2)
            .add(y2Name, mY2Value, mY2)
    }

    companion object {
        fun read(buffer: WireBuffer, operations: MutableList<Operation>, maker: Maker) {
            val v1 = buffer.readFloat()
            val v2 = buffer.readFloat()
            val v3 = buffer.readFloat()
            val v4 = buffer.readFloat()
            val op = maker.create(v1, v2, v3, v4)
            operations.add(op)
        }

        fun write(
            buffer: WireBuffer, opCode: Int, x1: Float, y1: Float, x2: Float, y2: Float
        ) {
            buffer.start(opCode)
            buffer.writeFloat(x1)
            buffer.writeFloat(y1)
            buffer.writeFloat(x2)
            buffer.writeFloat(y2)
        }
    }
}
