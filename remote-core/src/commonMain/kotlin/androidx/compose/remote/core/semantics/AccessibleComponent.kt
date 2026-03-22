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
package androidx.compose.remote.core.semantics

/**
 * Interface representing an accessible component in the UI. This interface defines properties and
 * methods related to accessibility semantics for a component.
 */
interface AccessibleComponent : AccessibilitySemantics {

    fun getContentDescriptionId(): Int? = null
    fun getTextId(): Int? = null
    fun getRole(): Role? = null
    fun isClickable(): Boolean = false
    fun getMode(): CoreSemantics.Mode = CoreSemantics.Mode.SET

    enum class Role(val description: String?) {
        BUTTON("Button"),
        CHECKBOX("Checkbox"),
        SWITCH("Switch"),
        RADIO_BUTTON("RadioButton"),
        TAB("Tab"),
        IMAGE("Image"),
        DROPDOWN_LIST("DropdownList"),
        PICKER("Picker"),
        CAROUSEL("Carousel"),
        UNKNOWN(null);

        companion object {
            fun fromInt(i: Int): Role {
                return if (i < UNKNOWN.ordinal) Role.values()[i] else UNKNOWN
            }
        }
    }

    enum class Mode {
        SET, CLEAR_AND_SET, MERGE
    }
}
