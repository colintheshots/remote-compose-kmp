/*
 * Copyright (C) 2025 The Android Open Source Project
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
package androidx.compose.remote.player.core.state

import androidx.compose.remote.core.RemoteContext

/** Default implementation of [StateUpdater]. */
class StateUpdaterImpl(private val remoteContext: RemoteContext) : StateUpdater {

    override fun setNamedLong(name: String, value: Long?) {
        if (value != null) {
            remoteContext.setNamedLong(name, value)
        }
    }

    override fun setUserLocalFloat(floatName: String, value: Float?) {
        val domainName = StateUpdater.getUserDomainString(floatName)
        if (value != null) {
            remoteContext.setNamedFloatOverride(domainName, value)
        } else {
            remoteContext.clearNamedFloatOverride(domainName)
        }
    }

    override fun setUserLocalInt(integerName: String, value: Int?) {
        val domainName = StateUpdater.getUserDomainString(integerName)
        if (value != null) {
            remoteContext.setNamedIntegerOverride(domainName, value)
        } else {
            remoteContext.clearNamedIntegerOverride(domainName)
        }
    }

    override fun setUserLocalColor(name: String, value: Int?) {
        val domainName = StateUpdater.getUserDomainString(name)
        if (value != null) {
            remoteContext.setNamedColorOverride(domainName, value)
        }
    }

    override fun setUserLocalBitmap(name: String, content: Any?) {
        val domainName = StateUpdater.getUserDomainString(name)
        if (content != null) {
            remoteContext.setNamedDataOverride(domainName, content)
        } else {
            remoteContext.clearNamedDataOverride(domainName)
        }
    }

    override fun setUserLocalString(stringName: String, value: String?) {
        val domainName = StateUpdater.getUserDomainString(stringName)
        if (value != null) {
            remoteContext.setNamedStringOverride(domainName, value)
        } else {
            remoteContext.clearNamedStringOverride(domainName)
        }
    }
}
