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
import androidx.compose.remote.core.operations.utilities.StringUtils
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable

class TextFromFloat(
    var mTextId: Int, var mValue: Float, var mDigitsBefore: Short, var mDigitsAfter: Short, var mFlags: Int
) : Operation(), VariableSupport, Serializable, ComponentData {
    var mOutValue: Float = mValue
    var mLegacy: Boolean = (mFlags and LEGACY_MODE) != 0
    private var mPre: Char = ' '; private var mAfter: Char = ' '
    private var mGroup: Byte = GROUPING_NONE.toByte(); private var mSeparator: Byte = (SEPARATOR_PERIOD_COMMA shr 6).toByte()
    private var mOptions: Int = 0

    init {
        when (mFlags and 3) { PAD_AFTER_SPACE -> mAfter = ' '; PAD_AFTER_NONE -> mAfter = 0.toChar(); PAD_AFTER_ZERO -> mAfter = '0' }
        when (mFlags and (3 shl 2)) { PAD_PRE_SPACE -> mPre = ' '; PAD_PRE_NONE -> mPre = 0.toChar(); PAD_PRE_ZERO -> mPre = '0' }
        when (mFlags and (3 shl 4)) { GROUPING_BY3 -> mGroup = (GROUPING_BY3 shr 4).toByte(); GROUPING_BY4 -> mGroup = (GROUPING_BY4 shr 4).toByte(); GROUPING_BY32 -> mGroup = (GROUPING_BY32 shr 4).toByte() }
        when (mFlags and (3 shl 6)) { SEPARATOR_PERIOD_COMMA -> mSeparator = (SEPARATOR_PERIOD_COMMA shr 6).toByte(); SEPARATOR_COMMA_PERIOD -> mSeparator = 0.toByte(); SEPARATOR_SPACE_COMMA -> mSeparator = (SEPARATOR_SPACE_COMMA shr 6).toByte(); SEPARATOR_UNDER_PERIOD -> mSeparator = (SEPARATOR_UNDER_PERIOD shr 6).toByte() }
        if ((mFlags and OPTIONS_ROUNDING) != 0) mOptions = mOptions or (OPTIONS_ROUNDING shr 8)
        if ((mFlags and OPTIONS_NEGATIVE_PARENTHESES) != 0) mOptions = mOptions or (OPTIONS_NEGATIVE_PARENTHESES shr 8)
    }

    override fun updateVariables(context: RemoteContext) { if (mValue.isNaN()) mOutValue = context.getFloat(Utils.idFromNan(mValue)) }
    override fun registerListening(context: RemoteContext) { if (mValue.isNaN()) context.listensTo(Utils.idFromNan(mValue), this) }
    override fun markDirty() { super<Operation>.markDirty() }
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mTextId, mValue, mDigitsBefore, mDigitsAfter, mFlags) }
    override fun toString(): String = "TextFromFloat[$mTextId] = ${Utils.floatToString(mValue)} $mDigitsBefore.$mDigitsAfter $mFlags"
    override fun apply(context: RemoteContext) {
        val s = if (mLegacy) StringUtils.floatToString(mOutValue, mDigitsBefore.toInt(), mDigitsAfter.toInt(), mPre, mAfter)
        else StringUtils.floatToString(mOutValue, mDigitsBefore.toInt(), mDigitsAfter.toInt(), mPre, mAfter, mSeparator, mGroup, mOptions)
        context.loadText(mTextId, s)
    }
    override fun deepToString(indent: String): String = indent + toString()
    override fun serialize(serializer: MapSerializer) {
        serializer.addType(CLASS_NAME).add("textId", mTextId).add("value", mValue, mOutValue).add("digitsBefore", mDigitsBefore).add("digitsAfter", mDigitsAfter).add("flags", mFlags)
    }
    companion object {
        private val OP_CODE = Operations.TEXT_FROM_FLOAT; private const val CLASS_NAME = "TextFromFloat"
        const val PAD_AFTER_SPACE = 0; const val PAD_AFTER_NONE = 1; const val PAD_AFTER_ZERO = 3
        const val PAD_PRE_SPACE = 0; const val PAD_PRE_NONE = 4; const val PAD_PRE_ZERO = 12
        const val GROUPING_NONE = 0; const val GROUPING_BY3 = 1 shl 4; const val GROUPING_BY4 = 2 shl 4; const val GROUPING_BY32 = 3 shl 4
        const val SEPARATOR_COMMA_PERIOD = 0; const val SEPARATOR_PERIOD_COMMA = 1 shl 6; const val SEPARATOR_SPACE_COMMA = 2 shl 6; const val SEPARATOR_UNDER_PERIOD = 3 shl 6
        const val OPTIONS_NONE = 0; const val OPTIONS_NEGATIVE_PARENTHESES = 1 shl 8; const val OPTIONS_ROUNDING = 2 shl 8; const val LEGACY_MODE = 1 shl 10
        fun name(): String = CLASS_NAME; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, textId: Int, value: Float, digitsBefore: Short, digitsAfter: Short, flags: Int) {
            buffer.start(OP_CODE); buffer.writeInt(textId); buffer.writeFloat(value); buffer.writeInt((digitsBefore.toInt() shl 16) or digitsAfter.toInt()); buffer.writeInt(flags)
        }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val textId = buffer.readInt(); val value = buffer.readFloat(); val tmp = buffer.readInt()
            val post = (tmp and 0xFFFF).toShort(); val pre = ((tmp shr 16) and 0xFFFF).toShort()
            val flags = buffer.readInt(); operations.add(TextFromFloat(textId, value, pre, post, flags))
        }
    }
}
