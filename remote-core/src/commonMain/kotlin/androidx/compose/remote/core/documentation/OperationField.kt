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
package androidx.compose.remote.core.documentation

class OperationField(
    val mType: Int,
    val mName: String,
    val mDescription: String,
    var mVarSize: String? = null
) {
    val mPossibleValues: MutableList<StringPair> = mutableListOf()

    companion object {
        fun withVarSize(type: Int, name: String, varSize: String?, description: String): OperationField {
            return OperationField(type, name, description, varSize)
        }
    }

    fun getType(): Int = mType
    fun getName(): String = mName
    fun getDescription(): String = mDescription
    fun getPossibleValues(): MutableList<StringPair> = mPossibleValues

    fun possibleValue(name: String, value: String) { mPossibleValues.add(StringPair(name, value)) }
    fun hasEnumeratedValues(): Boolean = mPossibleValues.isNotEmpty()
    fun getVarSize(): String? = mVarSize

    fun getSize(): Int = when (mType) {
        DocumentedOperation.BYTE -> 1
        DocumentedOperation.INT, DocumentedOperation.FLOAT -> 4
        DocumentedOperation.LONG -> 8
        DocumentedOperation.SHORT -> 2
        DocumentedOperation.INT_ARRAY, DocumentedOperation.FLOAT_ARRAY -> -1
        else -> 0
    }
}
