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
package androidx.compose.remote.creation.actions

import androidx.compose.remote.core.operations.layout.modifiers.HostActionMetadataOperation
import androidx.compose.remote.core.operations.layout.modifiers.HostActionOperation
import androidx.compose.remote.core.operations.layout.modifiers.HostNamedActionOperation
import androidx.compose.remote.creation.RemoteComposeWriter

/** Base interface for actions */
interface Action {
    fun write(writer: RemoteComposeWriter)
}

/** Host action */
class HostAction : Action {
    private var mActionId: Int = -1
    private var mValueId: Int = -1
    private var mType: Int = -1
    private var mActionName: String? = null

    constructor(id: Int) { mActionId = id }
    constructor(id: Int, metadataId: Int) { mActionId = id; mValueId = metadataId }
    constructor(name: String, type: Int, valueId: Int) { mActionName = name; mType = type; mValueId = valueId }
    constructor(name: String) { mActionName = name; mValueId = -1 }

    override fun write(writer: RemoteComposeWriter) {
        if (mActionName == null) {
            if (mValueId != -1) {
                HostActionMetadataOperation.apply(writer.getBuffer().getBuffer())
            } else {
                HostActionOperation.apply(writer.getBuffer().getBuffer())
            }
        } else {
            writer.addText(mActionName!!)
            HostNamedActionOperation.apply(writer.getBuffer().getBuffer())
        }
    }
}

/** Value float change action */
class ValueFloatChange(private val valueId: Int, private val value: Float) : Action {
    override fun write(writer: RemoteComposeWriter) {
        writer.addValueFloatChangeActionOperation(valueId, value)
    }
}

/** Value float expression change action */
class ValueFloatExpressionChange(private val valueId: Int, private val value: Int) : Action {
    override fun write(writer: RemoteComposeWriter) {
        writer.addValueFloatExpressionChangeActionOperation(valueId, value)
    }
}

/** Value integer change action */
class ValueIntegerChange(private val valueId: Int, private val value: Int) : Action {
    override fun write(writer: RemoteComposeWriter) {
        writer.addValueIntegerChangeActionOperation(valueId, value)
    }
}

/** Value integer expression change action */
class ValueIntegerExpressionChange(
    private val destIntegerId: Long,
    private val srcIntegerId: Long,
) : Action {
    override fun write(writer: RemoteComposeWriter) {
        writer.addValueIntegerExpressionChangeActionOperation(destIntegerId, srcIntegerId)
    }
}

/** Value string change action */
class ValueStringChange(private val destTextId: Int, private val srcTextId: Int) : Action {
    override fun write(writer: RemoteComposeWriter) {
        writer.addValueStringChangeActionOperation(destTextId, srcTextId)
    }
}
