/*
 * Copyright (C) 2023 The Android Open Source Project
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
package androidx.compose.remote.core

import androidx.compose.remote.core.operations.DataDynamicListFloat
import androidx.compose.remote.core.operations.utilities.ArrayAccess
import androidx.compose.remote.core.operations.utilities.CollectionsAccess
import androidx.compose.remote.core.operations.utilities.DataMap
import androidx.compose.remote.core.operations.utilities.IntFloatMap
import androidx.compose.remote.core.operations.utilities.IntIntMap
import androidx.compose.remote.core.operations.utilities.IntMap
import androidx.compose.remote.core.operations.utilities.NanMap
import kotlin.math.min

/**
 * Represents runtime state for a RemoteCompose document. State includes things like the value of
 * variables.
 */
open class RemoteComposeState : CollectionsAccess {

    companion object {
        const val START_ID = 42
        const val BITMAP_TEXTURE_ID_OFFSET = 2000
        private const val MAX_DATA = 1000
    }

    private var sMaxColors = 200

    private val mIntDataMap = IntMap<Any>()
    private val mIntWrittenMap = IntMap<Boolean>()
    private val mDataIntMap = mutableMapOf<Any, Int>()
    private val mFloatMap = IntFloatMap()
    private val mIntegerMap = IntIntMap()
    private val mColorMap = IntIntMap()
    private val mDataMapMap = IntMap<DataMap>()
    private val mObjectMap = IntMap<Any>()

    // path information
    private val mPathMap = IntMap<Any>()
    private val mPathData = IntMap<FloatArray>()
    private val mPathWinding = IntIntMap()

    private var mColorOverride = BooleanArray(sMaxColors)
    private val mCollectionMap = IntMap<ArrayAccess>()

    private val mDataOverride = BooleanArray(MAX_DATA)
    private val mIntegerOverride = BooleanArray(MAX_DATA)
    private val mFloatOverride = BooleanArray(MAX_DATA)

    private var mNextId = START_ID
    private val mIdMaps = intArrayOf(START_ID, NanMap.START_VAR, NanMap.START_ARRAY)
    private var mRemoteContext: RemoteContext? = null

    fun getFromId(id: Int): Any? = mIntDataMap.get(id)
    fun containsId(id: Int): Boolean = mIntDataMap.get(id) != null

    fun dataGetId(data: Any): Int = mDataIntMap[data] ?: -1

    fun cacheData(item: Any): Int {
        val id = createNextAvailableId()
        mDataIntMap[item] = id; mIntDataMap.put(id, item)
        return id
    }

    fun cacheData(item: Any, type: Int): Int {
        val id = createNextAvailableId(type)
        mDataIntMap[item] = id; mIntDataMap.put(id, item)
        return id
    }

    fun cacheData(id: Int, item: Any) {
        mDataIntMap[item] = id; mIntDataMap.put(id, item)
    }

    fun updateData(id: Int, item: Any) {
        if (!mDataOverride[id]) {
            val previous = mIntDataMap.get(id)
            if (previous !== item) {
                mDataIntMap.remove(previous)
                mDataIntMap[item] = id; mIntDataMap.put(id, item)
                updateListeners(id)
            }
        }
    }

    fun getPath(id: Int): Any? = mPathMap.get(id)
    fun putPath(id: Int, path: Any) { mPathMap.put(id, path) }

    fun putPathData(id: Int, data: FloatArray) {
        mPathData.put(id, data); mPathMap.remove(id)
    }

    fun getPathData(id: Int): FloatArray? = mPathData.get(id)
    fun getPathWinding(id: Int): Int = mPathWinding.get(id)
    fun putPathWinding(id: Int, winding: Int) { mPathWinding.put(id, winding) }

    fun overrideData(id: Int, item: Any) {
        val previous = mIntDataMap.get(id)
        if (previous !== item) {
            mDataIntMap.remove(previous)
            mDataIntMap[item] = id; mIntDataMap.put(id, item)
            mDataOverride[id] = true
            updateListeners(id)
        }
    }

    fun cacheFloat(item: Float): Int {
        val id = createNextAvailableId()
        mFloatMap.put(id, item); mIntegerMap.put(id, item.toInt())
        return id
    }

    fun cacheFloat(id: Int, item: Float) { mFloatMap.put(id, item) }

    fun updateFloat(id: Int, value: Float) {
        if (!mFloatOverride[id]) {
            val previous = mFloatMap.get(id)
            if (previous != value) {
                mFloatMap.put(id, value); mIntegerMap.put(id, value.toInt())
                updateListeners(id)
            }
        }
    }

    fun overrideFloat(id: Int, value: Float) {
        val previous = mFloatMap.get(id)
        if (previous != value) {
            mFloatMap.put(id, value); mIntegerMap.put(id, value.toInt())
            mFloatOverride[id] = true
            updateListeners(id)
        }
    }

    fun cacheInteger(item: Int): Int {
        val id = createNextAvailableId()
        mIntegerMap.put(id, item); mFloatMap.put(id, item.toFloat())
        return id
    }

    fun updateInteger(id: Int, value: Int) {
        if (!mIntegerOverride[id]) {
            val previous = mIntegerMap.get(id)
            if (previous != value) {
                mFloatMap.put(id, value.toFloat()); mIntegerMap.put(id, value)
                updateListeners(id)
            }
        }
    }

    fun overrideInteger(id: Int, value: Int) {
        val previous = mIntegerMap.get(id)
        if (previous != value) {
            mIntegerMap.put(id, value); mFloatMap.put(id, value.toFloat())
            mIntegerOverride[id] = true
            updateListeners(id)
        }
    }

    fun getFloat(id: Int): Float = mFloatMap.get(id)
    fun getInteger(id: Int): Int = mIntegerMap.get(id)
    fun getColor(id: Int): Int = mColorMap.get(id)

    fun updateColor(id: Int, color: Int) {
        if (id < sMaxColors && mColorOverride[id]) return
        mColorMap.put(id, color)
        updateListeners(id)
    }

    private fun updateListeners(id: Int) {
        val v = mVarListeners.get(id)
        if (v != null && mRemoteContext != null) {
            for (i in v.indices) v[i].markDirty()
        }
    }

    fun overrideColor(id: Int, color: Int) {
        if (id >= sMaxColors) {
            sMaxColors *= 2
            mColorOverride = mColorOverride.copyOf(sMaxColors)
        }
        mColorOverride[id] = true
        mColorMap.put(id, color)
        updateListeners(id)
    }

    fun clearColorOverride() { mColorOverride.fill(false) }
    fun clearDataOverride(id: Int) { mDataOverride[id] = false; updateListeners(id) }
    fun clearIntegerOverride(id: Int) { mIntegerOverride[id] = false; updateListeners(id) }
    fun clearFloatOverride(id: Int) { mFloatOverride[id] = false; updateListeners(id) }

    fun wasNotWritten(id: Int): Boolean = mIntWrittenMap.get(id) != true
    fun markWritten(id: Int) { mIntWrittenMap.put(id, true) }

    fun reset() { mIntWrittenMap.clear(); mDataIntMap.clear() }

    fun createNextAvailableId(): Int = mNextId++

    fun createNextAvailableId(type: Int): Int {
        if (type == 0) return mNextId++
        return mIdMaps[type]++
    }

    fun setNextId(id: Int) { mNextId = id }

    internal var mVarListeners = IntMap<MutableList<VariableSupport>>()
    internal var mAllVarListeners = mutableListOf<VariableSupport>()

    private fun add(id: Int, variableSupport: VariableSupport) {
        var v = mVarListeners.get(id)
        if (v == null) { v = mutableListOf(); mVarListeners.put(id, v) }
        if (!v.contains(variableSupport)) { v.add(variableSupport); mAllVarListeners.add(variableSupport) }
    }

    fun listenToVar(id: Int, variableSupport: VariableSupport) { add(id, variableSupport) }
    fun getListeners(id: Int): MutableList<VariableSupport>? = mVarListeners.get(id)
    fun hasListener(id: Int): Boolean = mVarListeners.get(id) != null

    internal var mLastRepaint = Float.NaN

    fun getOpsToUpdate(context: RemoteContext, currentTime: Long): Int {
        if (mVarListeners.get(RemoteContext.ID_CONTINUOUS_SEC) != null) return 1
        var repaintMs = Int.MAX_VALUE
        if (!mRepaintSeconds.isNaN()) {
            repaintMs = (mRepaintSeconds * 1000).toInt()
            mLastRepaint = mRepaintSeconds
        }
        if (mVarListeners.get(RemoteContext.ID_TIME_IN_SEC) != null) {
            val sub = (currentTime % 1000).toInt()
            return min(repaintMs, 2 + 1000 - sub)
        }
        if (mVarListeners.get(RemoteContext.ID_TIME_IN_MIN) != null) {
            val sub = (currentTime % 60000).toInt()
            return min(repaintMs, 2 + 1000 * 60 - sub)
        }
        return -1
    }

    internal var mRepaintSeconds = Float.NaN

    fun wakeIn(seconds: Float) {
        if (seconds.isNaN() || mLastRepaint.isNaN() || mRepaintSeconds > seconds) {
            mRepaintSeconds = seconds
        }
    }

    fun setWindowWidth(width: Float) { updateFloat(RemoteContext.ID_WINDOW_WIDTH, width) }
    fun setWindowHeight(height: Float) { updateFloat(RemoteContext.ID_WINDOW_HEIGHT, height) }

    fun addCollection(id: Int, collection: ArrayAccess) {
        mCollectionMap.put(id and 0xFFFFF, collection)
    }

    override fun getFloatValue(id: Int, index: Int): Float {
        val array = mCollectionMap.get(id and 0xFFFFF)
        return array?.getFloatValue(index) ?: 0f
    }

    override fun getFloats(id: Int): FloatArray? {
        return mCollectionMap.get(id and 0xFFFFF)?.getFloats()
    }

    override fun getDynamicFloats(id: Int): FloatArray? {
        val array = mCollectionMap.get(id and 0xFFFFF)
        return if (array is DataDynamicListFloat) array.getFloats() else null
    }

    override fun getArray(id: Int): ArrayAccess? = mCollectionMap.get(id and 0xFFFFF)

    override fun getId(listId: Int, index: Int): Int {
        return mCollectionMap.get(listId and 0xFFFFF)?.getId(index) ?: -1
    }

    fun putDataMap(id: Int, map: DataMap) { mDataMapMap.put(id, map) }
    fun getDataMap(id: Int): DataMap? = mDataMapMap.get(id)

    override fun getListLength(id: Int): Int {
        return mCollectionMap.get(id and 0xFFFFF)?.getLength() ?: 0
    }

    fun setContext(context: RemoteContext) {
        mRemoteContext = context
        mRemoteContext!!.clearLastOpCount()
    }

    fun updateObject(id: Int, value: Any) { mObjectMap.put(id, value) }
    fun getObject(id: Int): Any? = mObjectMap.get(id)
    open fun markVariableDirty(id: Int) { updateListeners(id) }
}
