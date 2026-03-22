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
import androidx.compose.remote.core.Operations
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.VariableSupport
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.utilities.IntegerExpressionEvaluator
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable
import androidx.compose.remote.core.serialize.SerializeTags

class IntegerExpression(
    var mId: Int,
    private var mMask: Int,
    val mSrcValue: IntArray
) : Operation(), VariableSupport, Serializable {

    private var mPreMask: Int = 0
    var mPreCalcValue: IntArray? = null
    private var mLastChange: Float = Float.NaN
    val mExp = IntegerExpressionEvaluator()

    override fun updateVariables(context: RemoteContext) {
        if (mPreCalcValue == null || mPreCalcValue!!.size != mSrcValue.size)
            mPreCalcValue = IntArray(mSrcValue.size)
        mPreMask = mMask
        for (i in mSrcValue.indices) {
            if (isId(mMask, i, mSrcValue[i])) {
                mPreMask = mPreMask and (0x1 shl i).inv()
                mPreCalcValue!![i] = context.getInteger(mSrcValue[i])
            } else {
                mPreCalcValue!![i] = mSrcValue[i]
            }
        }
    }

    override fun registerListening(context: RemoteContext) {
        for (i in mSrcValue.indices) {
            if (isId(mMask, i, mSrcValue[i])) context.listensTo(mSrcValue[i], this)
        }
    }

    override fun markDirty() { super<Operation>.markDirty() }

    override fun apply(context: RemoteContext) {
        updateVariables(context)
        val t = context.getAnimationTime()
        if (mLastChange.isNaN()) mLastChange = t
        val v = mExp.eval(mPreMask, mPreCalcValue!!.copyOf())
        context.loadInteger(mId, v)
    }

    fun evaluate(context: RemoteContext): Int {
        updateVariables(context)
        val t = context.getAnimationTime()
        if (mLastChange.isNaN()) mLastChange = t
        return mExp.eval(mPreMask, mPreCalcValue!!.copyOf())
    }

    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mId, mMask, mSrcValue) }

    override fun toString(): String {
        val pre = mPreCalcValue ?: return ""
        val s = StringBuilder()
        for (i in pre.indices) {
            if (i != 0) s.append(" ")
            if (IntegerExpressionEvaluator.isOperation(mMask, i)) {
                if (isId(mMask, i, mSrcValue[i])) s.append("[${mSrcValue[i]}]")
                else s.append(IntegerExpressionEvaluator.toMathName(pre[i]))
            } else s.append(mSrcValue[i])
        }
        return "IntegerExpression[$mId] = ($s)"
    }

    override fun deepToString(indent: String): String = indent + toString()

    override fun serialize(serializer: MapSerializer) {
        serializer.addTags(SerializeTags.EXPRESSION)
            .addType(CLASS_NAME).add("id", mId)
            .add("mask", mId)
            .addIntExpressionSrc("srcValues", mSrcValue, mMask)
    }

    companion object {
        private val OP_CODE = Operations.INTEGER_EXPRESSION
        private const val CLASS_NAME = "IntegerExpression"
        const val MAX_SIZE = 320

        fun name(): String = CLASS_NAME
        fun id(): Int = OP_CODE

        fun apply(buffer: WireBuffer, id: Int, mask: Int, value: IntArray) {
            buffer.start(OP_CODE)
            buffer.writeInt(id)
            buffer.writeInt(mask)
            buffer.writeInt(value.size)
            for (v in value) buffer.writeInt(v)
        }

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val id = buffer.readInt()
            val mask = buffer.readInt()
            val len = buffer.readInt()
            if (len > MAX_SIZE) throw RuntimeException("buffer corrupt integer expression $len")
            val values = IntArray(len) { buffer.readInt() }
            operations.add(IntegerExpression(id, mask, values))
        }

        fun isId(mask: Int, i: Int, value: Int): Boolean =
            ((1 shl i) and mask) != 0 && value < IntegerExpressionEvaluator.OFFSET
    }
}
