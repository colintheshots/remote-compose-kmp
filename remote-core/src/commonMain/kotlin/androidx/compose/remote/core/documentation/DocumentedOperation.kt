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

import kotlin.math.max

open class DocumentedOperation(
    val mCategory: String,
    var mId: Int,
    val mName: String,
    var mWIP: Boolean = false
) {
    companion object {
        const val LAYOUT = 0
        const val INT = 0
        const val FLOAT = 1
        const val BOOLEAN = 2
        const val BUFFER = 4
        const val UTF8 = 5
        const val BYTE = 6
        const val VALUE = 7
        const val LONG = 8
        const val SHORT = 9
        const val FLOAT_ARRAY = 10
        const val INT_ARRAY = 11
        const val BYTE_ARRAY = 12

        fun getType(type: Int): String = when (type) {
            INT -> "INT"; FLOAT -> "FLOAT"; BOOLEAN -> "BOOLEAN"; BUFFER -> "BUFFER"
            UTF8 -> "UTF8"; BYTE -> "BYTE"; VALUE -> "VALUE"; LONG -> "LONG"; SHORT -> "SHORT"
            FLOAT_ARRAY -> "FLOAT[]"; INT_ARRAY -> "INT[]"; BYTE_ARRAY -> "byte[]"
            else -> "UNKNOWN"
        }
    }

    var mDescription: String = ""
    var mTextExamples: String? = null
    val mExamples: MutableList<StringPair> = mutableListOf()
    val mFields: MutableList<OperationField> = mutableListOf()
    var mVarSize: String = ""
    var mExamplesWidth: Int = 100
    var mExamplesHeight: Int = 100

    fun getFields(): MutableList<OperationField> = mFields
    fun getCategory(): String = mCategory
    fun getId(): Int = mId
    fun getName(): String = mName
    fun isWIP(): Boolean = mWIP
    fun getVarSize(): String = mVarSize

    fun getSizeFields(): Int {
        var size = 0; mVarSize = ""
        for (field in mFields) {
            size += max(0, field.getSize())
            if (field.getSize() < 0) mVarSize += " + ${field.getVarSize()} x 4"
        }
        return size
    }

    fun getDescription(): String? = mDescription
    fun getTextExamples(): String? = mTextExamples
    fun getExamples(): MutableList<StringPair> = mExamples
    fun getExamplesWidth(): Int = mExamplesWidth
    fun getExamplesHeight(): Int = mExamplesHeight

    fun field(type: Int, name: String, description: String): DocumentedOperation {
        mFields.add(OperationField(type, name, description)); return this
    }
    fun field(type: Int, name: String, varSize: String, description: String): DocumentedOperation {
        val field = OperationField(type, name, description); field.mVarSize = varSize; mFields.add(field); return this
    }
    fun possibleValues(name: String, value: Int): DocumentedOperation {
        if (mFields.isNotEmpty()) mFields.last().possibleValue(name, "$value")
        return this
    }
    fun description(description: String): DocumentedOperation { mDescription = description; return this }
    fun examples(examples: String): DocumentedOperation { mTextExamples = examples; return this }
    fun exampleImage(name: String, imagePath: String): DocumentedOperation {
        mExamples.add(StringPair(name, imagePath)); return this
    }
    fun examplesDimension(width: Int, height: Int): DocumentedOperation {
        mExamplesWidth = width; mExamplesHeight = height; return this
    }
}
