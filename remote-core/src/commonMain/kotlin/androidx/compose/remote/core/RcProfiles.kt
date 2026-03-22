/*
 * Copyright 2025 The Android Open Source Project
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

/** List of supported Profiles */
object RcProfiles {
    const val PROFILE_BASELINE = 0x0
    const val PROFILE_EXPERIMENTAL = 0x1
    const val PROFILE_DEPRECATED = 0x2
    const val PROFILE_OEM = 0x4
    const val PROFILE_LOW_POWER = 0x8
    const val PROFILE_WIDGETS = 0x100
    const val PROFILE_ANDROIDX = 0x200
    const val PROFILE_ANDROID_NATIVE = 0x400
    const val PROFILE_WEAR_WIDGETS = 0x800
}
