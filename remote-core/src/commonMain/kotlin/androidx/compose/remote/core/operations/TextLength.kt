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
import androidx.compose.remote.core.WireBuffer

class TextLength(var mLengthId: Int, var mTextId: Int) : Operation() {
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mLengthId, mTextId) }
    override fun toString(): String = "TextLength[$mLengthId] = $mTextId"
    override fun apply(context: RemoteContext) { context.loadFloat(mLengthId, context.getText(mTextId)!!.length.toFloat()) }
    override fun deepToString(indent: String): String = indent + toString()
    companion object {
        private val OP_CODE = Operations.TEXT_LENGTH; fun name(): String = "TextLength"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, lengthId: Int, textId: Int) { buffer.start(OP_CODE); buffer.writeInt(lengthId); buffer.writeInt(textId) }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { operations.add(TextLength(buffer.readInt(), buffer.readInt())) }
    }
}
