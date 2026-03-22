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
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable

class TextMerge(var mTextId: Int, var mSrcId1: Int, var mSrcId2: Int) : Operation(), VariableSupport, Serializable {
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mTextId, mSrcId1, mSrcId2) }
    override fun toString(): String = "TextMerge[$mTextId] = [$mSrcId1] + [$mSrcId2]"
    override fun apply(context: RemoteContext) { context.loadText(mTextId, (context.getText(mSrcId1) ?: "") + (context.getText(mSrcId2) ?: "")) }
    override fun updateVariables(context: RemoteContext) { apply(context) }
    override fun registerListening(context: RemoteContext) { context.listensTo(mSrcId1, this); context.listensTo(mSrcId2, this) }
    override fun markDirty() { super<Operation>.markDirty() }
    override fun deepToString(indent: String): String = indent + toString()
    override fun serialize(serializer: MapSerializer) { serializer.addType(CLASS_NAME).add("id", mTextId).add("leftId", mSrcId1).add("rightId", mSrcId2) }
    companion object {
        private val OP_CODE = Operations.TEXT_MERGE; private const val CLASS_NAME = "TextMerge"
        fun name(): String = CLASS_NAME; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, textId: Int, srcId1: Int, srcId2: Int) { buffer.start(OP_CODE); buffer.writeInt(textId); buffer.writeInt(srcId1); buffer.writeInt(srcId2) }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { operations.add(TextMerge(buffer.readInt(), buffer.readInt(), buffer.readInt())) }
    }
}
