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
import androidx.compose.remote.core.PaintContext
import androidx.compose.remote.core.PaintOperation
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.VariableSupport
import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.serialize.MapSerializer

/** Draw a tween path (interpolation between two paths) */
class DrawTweenPath(
    private val mPath1Id: Int,
    private val mPath2Id: Int,
    private val mTween: Float,
    private val mStart: Float,
    private val mStop: Float
) : PaintOperation(), VariableSupport {

    private var mOutTween: Float = mTween
    private var mOutStart: Float = mStart
    private var mOutStop: Float = mStop

    override fun updateVariables(context: RemoteContext) {
        mOutTween = if (mTween.isNaN()) context.getFloat(Utils.idFromNan(mTween)) else mTween
        mOutStart = if (mStart.isNaN()) context.getFloat(Utils.idFromNan(mStart)) else mStart
        mOutStop = if (mStop.isNaN()) context.getFloat(Utils.idFromNan(mStop)) else mStop
    }

    override fun registerListening(context: RemoteContext) {
        if (mTween.isNaN()) context.listensTo(Utils.idFromNan(mTween), this)
        if (mStart.isNaN()) context.listensTo(Utils.idFromNan(mStart), this)
        if (mStop.isNaN()) context.listensTo(Utils.idFromNan(mStop), this)
    }

    override fun write(buffer: WireBuffer) {
        apply(buffer, mPath1Id, mPath2Id, mTween, mStart, mStop)
    }

    override fun toString(): String =
        "DrawTweenPath $mPath1Id $mPath2Id " +
            "${Utils.floatToString(mTween, mOutTween)} " +
            "${Utils.floatToString(mStart, mOutStart)} " +
            "- ${Utils.floatToString(mStop, mOutStop)}"

    override fun paint(context: PaintContext) {
        context.drawTweenPath(
            getId(mPath1Id, context), getId(mPath2Id, context),
            mOutTween, mOutStart, mOutStop
        )
    }

    override fun serialize(serializer: MapSerializer) {
        serializer
            .addType(CLASS_NAME)
            .add("path1Id", mPath1Id)
            .add("path2Id", mPath2Id)
            .add("tween", mTween, mOutTween)
            .add("start", mStart, mOutStart)
            .add("stop", mStop, mOutStop)
    }

    companion object {
        private val OP_CODE = Operations.DRAW_TWEEN_PATH
        private const val CLASS_NAME = "DrawTweenPath"

        fun name(): String = CLASS_NAME
        fun id(): Int = OP_CODE

        fun read(buffer: WireBuffer, operations: MutableList<Operation>) {
            val path1Id = buffer.readInt()
            val path2Id = buffer.readInt()
            val tween = buffer.readFloat()
            val start = buffer.readFloat()
            val stop = buffer.readFloat()
            operations.add(DrawTweenPath(path1Id, path2Id, tween, start, stop))
        }

        fun apply(
            buffer: WireBuffer,
            path1Id: Int, path2Id: Int, tween: Float, start: Float, stop: Float
        ) {
            buffer.start(OP_CODE)
            buffer.writeInt(path1Id)
            buffer.writeInt(path2Id)
            buffer.writeFloat(tween)
            buffer.writeFloat(start)
            buffer.writeFloat(stop)
        }
    }
}
