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
package androidx.compose.remote.player.core.platform

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.RemoteClock
import androidx.compose.remote.core.SystemClock
import androidx.compose.remote.core.TouchListener
import androidx.compose.remote.core.VariableSupport
import androidx.compose.remote.core.operations.BitmapData
import androidx.compose.remote.core.operations.FloatExpression
import androidx.compose.remote.core.operations.ShaderData
import androidx.compose.remote.core.operations.utilities.ArrayAccess
import androidx.compose.remote.core.operations.utilities.DataMap
import androidx.compose.remote.core.types.LongConstant

/**
 * An implementation of Context for Android.
 *
 * This is used to play the RemoteCompose operations on Android.
 */
class AndroidRemoteContext(clock: RemoteClock = SystemClock()) : RemoteContext(clock) {
    companion object {
        private const val CHECK_DATA_SIZE = true
    }

    private var a11yAnimationEnabled = true
    private var bitmapLoader: BitmapLoader = BitmapLoader.UNSUPPORTED

    init {
        setBitmapLoader(AndroidBitmapLoader())
    }

    /** Sets the BitmapLoader to be used by the RemoteContext for loading bitmaps from URLs. */
    fun setBitmapLoader(loader: BitmapLoader) {
        bitmapLoader = loader
    }

    /**
     * Sets the Canvas to be used by the RemoteContext for drawing operations.
     */
    fun useCanvas(canvas: Canvas) {
        if (mPaintContext == null) {
            mPaintContext = AndroidPaintContext(this, canvas)
        } else {
            mPaintContext!!.reset()
            (mPaintContext as AndroidPaintContext).setCanvas(canvas)
        }
        mWidth = canvas.width.toFloat()
        mHeight = canvas.height.toFloat()
    }

    // Data handling

    override fun loadPathData(instanceId: Int, winding: Int, floatPath: FloatArray) {
        mRemoteComposeState.putPathData(instanceId, floatPath)
        mRemoteComposeState.putPathWinding(instanceId, winding)
    }

    override fun getPathData(instanceId: Int): FloatArray? =
        mRemoteComposeState.getPathData(instanceId)

    internal class VarName(val name: String, val id: Int, val type: Int)

    internal val varNameHashMap = HashMap<String, VarName?>()

    override fun loadVariableName(varName: String, varId: Int, varType: Int) {
        varNameHashMap[varName] = VarName(varName, varId, varType)
    }

    override fun setNamedStringOverride(stringName: String, value: String) {
        varNameHashMap[stringName]?.let { overrideText(it.id, value) }
    }

    override fun clearNamedStringOverride(stringName: String) {
        varNameHashMap[stringName]?.let { clearDataOverride(it.id) }
        varNameHashMap[stringName] = null
    }

    override fun setNamedBooleanOverride(booleanName: String, value: Boolean) {
        setNamedIntegerOverride(booleanName, if (value) 1 else 0)
    }

    override fun clearNamedBooleanOverride(booleanName: String) {
        clearNamedIntegerOverride(booleanName)
    }

    override fun setNamedIntegerOverride(integerName: String, value: Int) {
        varNameHashMap[integerName]?.let { overrideInt(it.id, value) }
    }

    override fun clearNamedIntegerOverride(integerName: String) {
        varNameHashMap[integerName]?.let { clearIntegerOverride(it.id) }
        varNameHashMap[integerName] = null
    }

    override fun setNamedFloatOverride(floatName: String, value: Float) {
        varNameHashMap[floatName]?.let { overrideFloat(it.id, value) }
    }

    override fun clearNamedFloatOverride(floatName: String) {
        varNameHashMap[floatName]?.let { clearFloatOverride(it.id) }
        varNameHashMap[floatName] = null
    }

    override fun setNamedLong(name: String, value: Long) {
        val entry = varNameHashMap[name]
        if (entry != null) {
            val longConstant = mRemoteComposeState.getObject(entry.id) as LongConstant
            longConstant.setValue(value)
        }
    }

    override fun setNamedDataOverride(dataName: String, value: Any) {
        varNameHashMap[dataName]?.let { overrideData(it.id, value) }
    }

    override fun clearNamedDataOverride(dataName: String) {
        varNameHashMap[dataName]?.let { clearDataOverride(it.id) }
        varNameHashMap[dataName] = null
    }

    override fun setNamedColorOverride(colorName: String, color: Int) {
        varNameHashMap[colorName]?.let { mRemoteComposeState.overrideColor(it.id, color) }
    }

    override fun addCollection(id: Int, collection: ArrayAccess) {
        mRemoteComposeState.addCollection(id, collection)
    }

    override fun putDataMap(id: Int, map: DataMap) {
        mRemoteComposeState.putDataMap(id, map)
    }

    override fun getDataMap(id: Int): DataMap? = mRemoteComposeState.getDataMap(id)

    override fun runAction(id: Int, metadata: String) {
        mDocument.performClick(this, id, metadata)
    }

    override fun runNamedAction(id: Int, value: Any?) {
        val text = getText(id)
        if (text != null) {
            mDocument.runNamedAction(text, value)
        }
    }

    /**
     * Decode a byte array into an image and cache it using the given imageId.
     */
    override fun loadBitmap(
        imageId: Int, encoding: Short, type: Short, width: Int, height: Int, data: ByteArray
    ) {
        if (!mRemoteComposeState.containsId(imageId)) {
            var image: Bitmap? = null
            when (encoding) {
                BitmapData.ENCODING_INLINE -> {
                    when (type) {
                        BitmapData.TYPE_PNG_8888 -> {
                            if (CHECK_DATA_SIZE) {
                                val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                                BitmapFactory.decodeByteArray(data, 0, data.size, opts)
                                if (opts.outWidth > width || opts.outHeight > height) {
                                    throw RuntimeException(
                                        "dimension don't match ${opts.outWidth}x${opts.outHeight} vs ${width}x${height}"
                                    )
                                }
                            }
                            image = BitmapFactory.decodeByteArray(data, 0, data.size)
                        }
                        BitmapData.TYPE_PNG_ALPHA_8 -> {
                            image = decodePreferringAlpha8(data)
                            if (image!!.config != Bitmap.Config.ALPHA_8) {
                                val alpha8Bitmap = Bitmap.createBitmap(
                                    image.width, image.height, Bitmap.Config.ALPHA_8
                                )
                                val canvas = Canvas(alpha8Bitmap)
                                val paint = Paint().apply {
                                    xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
                                }
                                canvas.drawBitmap(image, 0f, 0f, paint)
                                image.recycle()
                                image = alpha8Bitmap
                            }
                        }
                        BitmapData.TYPE_RAW8888 -> {
                            image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                            val idata = IntArray(data.size / 4)
                            for (i in idata.indices) {
                                val p = i * 4
                                idata[i] = (data[p].toInt() shl 24) or
                                    ((data[p + 1].toInt() and 0xFF) shl 16) or
                                    ((data[p + 2].toInt() and 0xFF) shl 8) or
                                    (data[p + 3].toInt() and 0xFF)
                            }
                            image.setPixels(idata, 0, width, 0, 0, width, height)
                        }
                        BitmapData.TYPE_RAW8 -> {
                            image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                            val bdata = IntArray(data.size / 4)
                            for (i in bdata.indices) {
                                bdata[i] = 0x1010101 * data[i].toInt()
                            }
                            image.setPixels(bdata, 0, width, 0, 0, width, height)
                        }
                    }
                }
                BitmapData.ENCODING_FILE -> {
                    image = BitmapFactory.decodeFile(String(data))
                }
                BitmapData.ENCODING_URL -> {
                    try {
                        val bytes = bitmapLoader.loadBitmap(String(data))
                        image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    } catch (e: Exception) {
                        throw RuntimeException(e)
                    }
                }
                BitmapData.ENCODING_EMPTY -> {
                    image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                }
            }
            if (image != null) {
                mRemoteComposeState.cacheData(imageId, image)
            }
        }
    }

    private fun decodePreferringAlpha8(data: ByteArray): Bitmap {
        val options = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.ALPHA_8
        }
        return BitmapFactory.decodeByteArray(data, 0, data.size, options)
    }

    override fun loadText(id: Int, text: String) {
        if (!mRemoteComposeState.containsId(id)) {
            mRemoteComposeState.cacheData(id, text)
        } else {
            mRemoteComposeState.updateData(id, text)
        }
    }

    /** Overrides the text associated with a given ID. */
    fun overrideText(id: Int, text: String) {
        mRemoteComposeState.overrideData(id, text)
    }

    /** Overrides the integer value associated with a given ID. */
    fun overrideInt(id: Int, value: Int) {
        mRemoteComposeState.overrideInteger(id, value)
    }

    /** Overrides the data associated with a given ID. */
    fun overrideData(id: Int, value: Any) {
        mRemoteComposeState.overrideData(id, value)
    }

    /** Clears any data override for the given ID. */
    fun clearDataOverride(id: Int) {
        mRemoteComposeState.clearDataOverride(id)
    }

    /** Clears any integer override for the given ID. */
    fun clearIntegerOverride(id: Int) {
        mRemoteComposeState.clearIntegerOverride(id)
    }

    /** Clears any float override for the given ID. */
    fun clearFloatOverride(id: Int) {
        mRemoteComposeState.clearFloatOverride(id)
    }

    override fun getText(id: Int): String? = mRemoteComposeState.getFromId(id) as? String

    override fun loadFloat(id: Int, value: Float) {
        mRemoteComposeState.updateFloat(id, value)
    }

    override fun overrideFloat(id: Int, value: Float) {
        mRemoteComposeState.overrideFloat(id, value)
    }

    override fun loadInteger(id: Int, value: Int) {
        mRemoteComposeState.updateInteger(id, value)
    }

    override fun markVariableDirty(id: Int) {
        mRemoteComposeState.markVariableDirty(id)
    }

    override fun overrideInteger(id: Int, value: Int) {
        mRemoteComposeState.overrideInteger(id, value)
    }

    override fun overrideText(id: Int, valueId: Int) {
        val text = getText(valueId)
        if (text != null) overrideText(id, text)
    }

    override fun loadColor(id: Int, color: Int) {
        mRemoteComposeState.updateColor(id, color)
    }

    override fun loadAnimatedFloat(id: Int, animatedFloat: FloatExpression) {
        mRemoteComposeState.cacheData(id, animatedFloat)
    }

    override fun loadShader(id: Int, value: ShaderData) {
        mRemoteComposeState.cacheData(id, value)
    }

    override fun getFloat(id: Int): Float = mRemoteComposeState.getFloat(id)

    override fun putObject(id: Int, value: Any) {
        mRemoteComposeState.updateObject(id, value)
    }

    override fun getObject(id: Int): Any? = mRemoteComposeState.getObject(id)

    override fun getInteger(id: Int): Int = mRemoteComposeState.getInteger(id)

    override fun getLong(id: Int): Long =
        (mRemoteComposeState.getObject(id) as LongConstant).getValue()

    override fun getColor(id: Int): Int = mRemoteComposeState.getColor(id)

    override fun listensTo(id: Int, variableSupport: VariableSupport) {
        mRemoteComposeState.listenToVar(id, variableSupport)
    }

    override fun getListeners(id: Int): MutableList<VariableSupport>? =
        mRemoteComposeState.getListeners(id)

    override fun updateOps(): Int = mRemoteComposeState.getOpsToUpdate(this, currentTime)

    override fun getShader(id: Int): ShaderData? =
        mRemoteComposeState.getFromId(id) as? ShaderData

    override fun addTouchListener(touchExpression: TouchListener) {
        mDocument.addTouchListener(touchExpression)
    }

    // Click handling

    override fun addClickArea(
        id: Int,
        contentDescriptionId: Int,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        metadataId: Int
    ) {
        val contentDescription = mRemoteComposeState.getFromId(contentDescriptionId) as? String
        val metadata = mRemoteComposeState.getFromId(metadataId) as? String
        mDocument.addClickArea(id, contentDescription, left, top, right, bottom, metadata)
    }

    /** Vibrate the device. */
    override fun hapticEffect(type: Int) {
        mDocument.haptic(type)
    }

    /** Enable or disable animations for accessibility. */
    fun setAccessibilityAnimationEnabled(animationEnabled: Boolean) {
        a11yAnimationEnabled = animationEnabled
    }

    override fun isAnimationEnabled(): Boolean {
        return if (a11yAnimationEnabled) super.isAnimationEnabled() else false
    }
}
