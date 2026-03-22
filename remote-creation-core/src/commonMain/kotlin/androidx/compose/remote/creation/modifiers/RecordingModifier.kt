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
package androidx.compose.remote.creation.modifiers

import androidx.compose.remote.creation.RemoteComposeWriter

/** Recording modifier that records modifiers for later playback */
open class RecordingModifier {
    private val mList: MutableList<Element> = mutableListOf()
    private var mComponentId: Int = -1
    private var mSpacedBy: Float = Float.NaN

    fun interface Element {
        fun write(writer: RemoteComposeWriter)
    }

    fun getList(): List<Element> = mList

    fun getComponentId(): Int = mComponentId

    fun setComponentId(id: Int) {
        mComponentId = id
    }

    fun getSpacedBy(): Float = mSpacedBy

    fun then(modifier: Element): RecordingModifier {
        mList.add(modifier)
        return this
    }

    fun componentId(id: Int): RecordingModifier {
        mComponentId = id
        return this
    }

    fun spacedBy(spacedBy: Float): RecordingModifier {
        mSpacedBy = spacedBy
        return this
    }
}
