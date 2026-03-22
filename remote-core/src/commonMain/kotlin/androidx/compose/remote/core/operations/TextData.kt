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
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable

class TextData(val mTextId: Int, var mText: String) : Operation(), Serializable {
    fun update(from: TextData) { mText = from.mText }
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mTextId, mText) }
    override fun toString(): String = "TextData[$mTextId] = \"${Utils.trimString(mText, 10)}\""
    override fun apply(context: RemoteContext) { context.loadText(mTextId, mText) }
    override fun deepToString(indent: String): String = indent + toString()
    override fun serialize(serializer: MapSerializer) { serializer.addType(CLASS_NAME).add("textId", mTextId).add("text", mText) }
    companion object {
        private val OP_CODE = Operations.DATA_TEXT
        private const val CLASS_NAME = "TextData"
        const val MAX_STRING_SIZE = 4000
        fun name(): String = CLASS_NAME; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, textId: Int, text: String) { buffer.start(OP_CODE); buffer.writeInt(textId); buffer.writeUTF8(text) }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { val textId = buffer.readInt(); val text = buffer.readUTF8(MAX_STRING_SIZE); operations.add(TextData(textId, text)) }
    }
}
