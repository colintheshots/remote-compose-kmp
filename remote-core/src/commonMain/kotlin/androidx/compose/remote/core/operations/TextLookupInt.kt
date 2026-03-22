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

class TextLookupInt(var mTextId: Int, var mDataSetId: Int, var mIndex: Int) : Operation(), VariableSupport, Serializable {
    var mOutIndex: Int = mIndex
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mTextId, mDataSetId, mIndex) }
    override fun toString(): String = "TextLookupInt[${Utils.idString(mTextId)}] = ${Utils.idString(mDataSetId)} $mIndex"
    override fun updateVariables(context: RemoteContext) { mOutIndex = context.getInteger(mIndex) }
    override fun registerListening(context: RemoteContext) { context.listensTo(mIndex, this) }
    override fun markDirty() { super<Operation>.markDirty() }
    override fun apply(context: RemoteContext) { val id = context.getCollectionsAccess()!!.getId(mDataSetId, mOutIndex); context.loadText(mTextId, context.getText(id) ?: "") }
    override fun deepToString(indent: String): String = indent + toString()
    override fun serialize(serializer: MapSerializer) { serializer.addType("TextFromINT").add("textId", mTextId).add("dataSetId", mDataSetId).add("indexId", mIndex.toFloat(), mOutIndex.toFloat()) }
    companion object {
        private val OP_CODE = Operations.TEXT_LOOKUP_INT; fun name(): String = "TextFromINT"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, textId: Int, dataSet: Int, indexId: Int) { buffer.start(OP_CODE); buffer.writeInt(textId); buffer.writeInt(dataSet); buffer.writeInt(indexId) }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { operations.add(TextLookupInt(buffer.readInt(), buffer.readInt(), buffer.readInt())) }
    }
}
