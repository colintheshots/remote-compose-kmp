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

class TextSubtext(private var mTextId: Int, private var mSrcId1: Int, private var mStart: Float, private var mLen: Float) : Operation(), VariableSupport, Serializable {
    private var mOutStart: Float = mStart; private var mOutLen: Float = mLen
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mTextId, mSrcId1, mStart, mLen) }
    override fun toString(): String = "TextSubrange[$mTextId] = [$mSrcId1] + $mStart - $mLen"
    override fun apply(context: RemoteContext) {
        val str = context.getText(mSrcId1) ?: ""
        val out = if (mOutLen == -1f) str.substring(mOutStart.toInt()) else str.substring(mOutStart.toInt(), (mOutStart + mOutLen).toInt())
        context.loadText(mTextId, out)
    }
    override fun updateVariables(context: RemoteContext) {
        if (mStart.isNaN()) mOutStart = context.getFloat(Utils.idFromNan(mStart))
        if (mLen.isNaN()) mOutLen = context.getFloat(Utils.idFromNan(mLen))
    }
    override fun registerListening(context: RemoteContext) {
        context.listensTo(mSrcId1, this)
        if (mStart.isNaN()) context.listensTo(Utils.idFromNan(mStart), this)
        if (mLen.isNaN()) context.listensTo(Utils.idFromNan(mLen), this)
    }
    override fun markDirty() { super<Operation>.markDirty() }
    override fun deepToString(indent: String): String = indent + toString()
    override fun serialize(serializer: MapSerializer) { serializer.addType("TextSubtext").add("id", mTextId).add("source", mSrcId1).add("start", mStart).add("end", mLen) }
    companion object {
        private val OP_CODE = Operations.TEXT_SUBTEXT; fun name(): String = "TextSubtext"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, textId: Int, srcId1: Int, start: Float, len: Float) { buffer.start(OP_CODE); buffer.writeInt(textId); buffer.writeInt(srcId1); buffer.writeFloat(start); buffer.writeFloat(len) }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { operations.add(TextSubtext(buffer.readInt(), buffer.readInt(), buffer.readFloat(), buffer.readFloat())) }
    }
}
