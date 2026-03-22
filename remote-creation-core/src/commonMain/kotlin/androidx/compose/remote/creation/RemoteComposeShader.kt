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
package androidx.compose.remote.creation

import androidx.compose.remote.core.operations.ShaderData

/** Provides an API to create Shaders, setUniforms which is inserted into doc */
class RemoteComposeShader(
    val shader: String,
    internal val mWriter: RemoteComposeWriter,
) {
    var shaderID: Int = 0
    val shaderTextID: Int = mWriter.addText(shader)
    private var mUniformFloatMap: HashMap<String, FloatArray>? = null
    private var mUniformIntMap: HashMap<String, IntArray>? = null
    private var mUniformBitmapMap: HashMap<String, Int>? = null

    fun setIntUniform(name: String, v1: Int, v2: Int, v3: Int, v4: Int): RemoteComposeShader =
        mySetIntUniform(name, intArrayOf(v1, v2, v3, v4))

    fun setIntUniform(name: String, v1: Int, v2: Int, v3: Int): RemoteComposeShader =
        mySetIntUniform(name, intArrayOf(v1, v2, v3))

    fun setIntUniform(name: String, v1: Int, v2: Int): RemoteComposeShader =
        mySetIntUniform(name, intArrayOf(v1, v2))

    fun setIntUniform(name: String, v1: Int): RemoteComposeShader =
        mySetIntUniform(name, intArrayOf(v1))

    private fun mySetIntUniform(name: String, value: IntArray): RemoteComposeShader {
        val map = mUniformIntMap ?: HashMap<String, IntArray>().also { mUniformIntMap = it }
        map[name] = value
        return this
    }

    fun setFloatUniform(name: String, v1: Float, v2: Float, v3: Float, v4: Float): RemoteComposeShader =
        mySetFloatUniform(name, floatArrayOf(v1, v2, v3, v4))

    fun setFloatUniform(name: String, v1: Float, v2: Float, v3: Float): RemoteComposeShader =
        mySetFloatUniform(name, floatArrayOf(v1, v2, v3))

    fun setFloatUniform(name: String, v1: Float, v2: Float): RemoteComposeShader =
        mySetFloatUniform(name, floatArrayOf(v1, v2))

    fun setFloatUniform(name: String, v1: Float): RemoteComposeShader =
        mySetFloatUniform(name, floatArrayOf(v1))

    fun setFloatUniform(name: String, values: FloatArray): RemoteComposeShader =
        mySetFloatUniform(name, values)

    private fun mySetFloatUniform(name: String, value: FloatArray): RemoteComposeShader {
        val map = mUniformFloatMap ?: HashMap<String, FloatArray>().also { mUniformFloatMap = it }
        map[name] = value
        return this
    }

    fun setBitmapUniform(name: String, id: Int): RemoteComposeShader {
        val map = mUniformBitmapMap ?: HashMap<String, Int>().also { mUniformBitmapMap = it }
        map[name] = id
        return this
    }

    fun commit(): Int {
        shaderID = mWriter.mState.dataGetId(this)
        if (shaderID == -1) {
            shaderID = mWriter.mState.cacheData(this)
        }
        ShaderData.apply(
            mWriter.getBuffer().getBuffer(),
            shaderID,
            shaderTextID,
            mUniformFloatMap,
            mUniformIntMap,
            mUniformBitmapMap,
        )
        return shaderID
    }
}
