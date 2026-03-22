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

/** Methods to update the state of a RemoteContext. */
interface StateUpdater {

    fun setNamedLong(name: String, value: Long?)

    fun setUserLocalFloat(floatName: String, value: Float?)

    fun setUserLocalInt(integerName: String, value: Int?)

    fun setUserLocalColor(name: String, value: Int?)

    fun setUserLocalBitmap(name: String, content: Any?)

    fun setUserLocalString(stringName: String, value: String?)

    companion object {
        /** Returns the user domain string for the given parameter name. */
        fun getUserDomainString(name: String): String =
            "${RemoteDomains.USER}:$name"
    }
}
