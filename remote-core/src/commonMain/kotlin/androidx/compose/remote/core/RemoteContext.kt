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
package androidx.compose.remote.core

import androidx.compose.remote.core.operations.FloatExpression
import androidx.compose.remote.core.operations.ShaderData
import androidx.compose.remote.core.operations.Theme
import androidx.compose.remote.core.operations.Utils
import androidx.compose.remote.core.operations.layout.Component
import androidx.compose.remote.core.operations.utilities.ArrayAccess
import androidx.compose.remote.core.operations.utilities.CollectionsAccess
import androidx.compose.remote.core.operations.utilities.DataMap
import androidx.compose.remote.core.operations.utilities.IntMap

/**
 * Specify an abstract context used to playback RemoteCompose documents
 *
 * This allows us to intercept the different operations in a document and react to them.
 *
 * We also contain a PaintContext, so that any operation can draw as needed.
 */
abstract class RemoteContext(clock: RemoteClock = SystemClock()) {

    private var mClock: RemoteClock = clock
    var mDocument: CoreDocument = CoreDocument()
    var mRemoteComposeState: RemoteComposeState = object : RemoteComposeState() {
        override fun getDynamicFloats(id: Int): FloatArray? = null
        override fun markVariableDirty(id: Int) {}
    }
    private var mDocLoadTime: Long = 0L
    protected var mPaintContext: PaintContext? = null
    protected var mDensity: Float = Float.NaN

    var mMode: ContextMode = ContextMode.UNSET

    var mDebug: Int = 0

    private var mOpCount: Int = 0
    private var mTheme: Int = Theme.UNSPECIFIED

    var mWidth: Float = 0f
    var mHeight: Float = 0f
    private var mAnimationTime: Float = 0f

    private var mAnimate: Boolean = true

    var mLastComponent: Component? = null
    var currentTime: Long = 0L

    private var mUseChoreographer: Boolean = true

    init {
        setDocLoadTime()
    }

    /**
     * Returns true if the document has been encoded for at least the given version MAJOR.MINOR
     *
     * @param major major version number
     * @param minor minor version number
     * @param patch patch version number
     * @return true if the document was written at least with the given version
     */
    fun supportsVersion(major: Int, minor: Int, patch: Int): Boolean {
        // CoreDocument is currently a stub; version checking can be added later
        return true
    }

    fun getDensity(): Float = mDensity

    /**
     * Set the density of the document
     *
     * @param density density value
     */
    fun setDensity(density: Float) {
        if (!density.isNaN() && density > 0) {
            mDensity = density
        }
    }

    /**
     * Get the time the document was loaded
     *
     * @return time in ms since the document was loaded
     */
    fun getDocLoadTime(): Long = mDocLoadTime

    /** Set the time the document was loaded */
    fun setDocLoadTime() {
        mDocLoadTime = getClock().millis()
    }

    open fun isAnimationEnabled(): Boolean = mAnimate

    fun setAnimationEnabled(value: Boolean) {
        mAnimate = value
    }

    /**
     * Provide access to the table of collections
     *
     * @return the CollectionsAccess implementation
     */
    fun getCollectionsAccess(): CollectionsAccess? = mRemoteComposeState

    /**
     * Load a path under an id. Paths can be used in clip drawPath and drawTweenPath
     *
     * @param instanceId the id to save this path under
     * @param winding the winding rule
     * @param floatPath the path as a float array
     */
    abstract fun loadPathData(instanceId: Int, winding: Int, floatPath: FloatArray)

    /**
     * Load a path under an id. Paths can be used in clip drawPath and drawTweenPath
     *
     * @param instanceId the id
     * @return the path data
     */
    abstract fun getPathData(instanceId: Int): FloatArray?

    /**
     * Associate a name with a given id.
     *
     * @param varName the name
     * @param varId the id (color, integer, float etc.)
     * @param varType the type
     */
    abstract fun loadVariableName(varName: String, varId: Int, varType: Int)

    /**
     * Save a color under a given id
     *
     * @param id the id of the color
     * @param color the color to set
     */
    abstract fun loadColor(id: Int, color: Int)

    /**
     * Set the animation time allowing the creator to control animation rates
     *
     * @param time the animation time in seconds
     */
    fun setAnimationTime(time: Float) {
        mAnimationTime = time
    }

    /**
     * Gets the time animation clock as float in seconds
     *
     * @return a monotonic time in seconds (arbitrary zero point)
     */
    fun getAnimationTime(): Float = mAnimationTime

    /**
     * Set the value of a named Color. This overrides the color in the document
     *
     * @param colorName the name of the color to override
     * @param color Override the default color
     */
    abstract fun setNamedColorOverride(colorName: String, color: Int)

    /**
     * Set the value of a named String. This overrides the string in the document
     *
     * @param stringName the name of the string to override
     * @param value Override the default string
     */
    abstract fun setNamedStringOverride(stringName: String, value: String)

    /**
     * Allows to clear a named String.
     *
     * If an override exists, we revert back to the default value in the document.
     *
     * @param stringName the name of the string to override
     */
    abstract fun clearNamedStringOverride(stringName: String)

    /**
     * Set the value of a named Boolean. This overrides the boolean in the document
     *
     * @param booleanName the name of the boolean to override
     * @param value Override the default boolean
     */
    abstract fun setNamedBooleanOverride(booleanName: String, value: Boolean)

    /**
     * Allows to clear a named Boolean.
     *
     * If an override exists, we revert back to the default value in the document.
     *
     * @param booleanName the name of the boolean to override
     */
    abstract fun clearNamedBooleanOverride(booleanName: String)

    /**
     * Set the value of a named Integer. This overrides the integer in the document
     *
     * @param integerName the name of the integer to override
     * @param value Override the default integer
     */
    abstract fun setNamedIntegerOverride(integerName: String, value: Int)

    /**
     * Allows to clear a named Integer.
     *
     * If an override exists, we revert back to the default value in the document.
     *
     * @param integerName the name of the integer to override
     */
    abstract fun clearNamedIntegerOverride(integerName: String)

    /**
     * Set the value of a named float. This overrides the float in the document
     *
     * @param floatName the name of the float to override
     * @param value Override the default float
     */
    abstract fun setNamedFloatOverride(floatName: String, value: Float)

    /**
     * Allows to clear a named Float.
     *
     * If an override exists, we revert back to the default value in the document.
     *
     * @param floatName the name of the float to override
     */
    abstract fun clearNamedFloatOverride(floatName: String)

    /**
     * Set the value of a named long. This modifies the content of a LongConstant
     *
     * @param name the name of the float to override
     * @param value Override the default float
     */
    abstract fun setNamedLong(name: String, value: Long)

    /**
     * Set the value of a named Object. This overrides the Object in the document
     *
     * @param dataName the name of the Object to override
     * @param value Override the default float
     */
    abstract fun setNamedDataOverride(dataName: String, value: Any)

    /**
     * Allows to clear a named Object.
     *
     * If an override exists, we revert back to the default value in the document.
     *
     * @param dataName the name of the Object to override
     */
    abstract fun clearNamedDataOverride(dataName: String)

    /**
     * Support Collections by registering this collection
     *
     * @param id id of the collection
     * @param collection the collection under this id
     */
    abstract fun addCollection(id: Int, collection: ArrayAccess)

    /**
     * Put DataMap under an id
     *
     * @param id the id of the DataMap
     * @param map the DataMap
     */
    abstract fun putDataMap(id: Int, map: DataMap)

    /**
     * Get a DataMap given an id
     *
     * @param id the id of the DataMap
     * @return the DataMap
     */
    abstract fun getDataMap(id: Int): DataMap?

    /**
     * Run an action
     *
     * @param id the id of the action
     * @param metadata the metadata of the action
     */
    abstract fun runAction(id: Int, metadata: String)

    /**
     * Run an action with a named parameter
     *
     * @param id the text id of the action
     * @param value the value of the parameter
     */
    abstract fun runNamedAction(id: Int, value: Any?)

    /**
     * Put an object under an id
     *
     * @param id the id of the object
     * @param value the object
     */
    abstract fun putObject(id: Int, value: Any)

    /**
     * Get an object given an id
     *
     * @param id the id of the object
     * @return the object
     */
    abstract fun getObject(id: Int): Any?

    /**
     * Add a touch listener to the context
     *
     * @param touchExpression the touch expression
     */
    open fun addTouchListener(touchExpression: TouchListener) {}

    /**
     * Vibrate the device
     *
     * @param type 0 = none, 1-21, see HapticFeedbackConstants
     */
    abstract fun hapticEffect(type: Int)

    /** Set the repaint flag. This will trigger a repaint of the current document. */
    fun needsRepaint() {
        mPaintContext?.needsRepaint()
    }

    /**
     * Returns true if we should use the choreographer
     *
     * @return true if we use the choreographer
     */
    fun getUseChoreographer(): Boolean = mUseChoreographer

    /**
     * Set to true to use the android choreographer
     *
     * @param value true to use the choreographer
     */
    fun setUseChoreographer(value: Boolean) {
        mUseChoreographer = value
    }

    fun getClock(): RemoteClock = mClock

    fun setClock(clock: RemoteClock) {
        mClock = clock
    }

    /**
     * Load a font under an id
     *
     * @param fontId the id of the font
     * @param fontData the font data
     */
    fun loadFont(fontId: Int, fontData: ByteArray) {
        val info = getObject(fontId)
        if (info != null) {
            val fi = info as FontInfo
            if (fi.mFontData === fontData) {
                return
            }
        }
        putObject(fontId, FontInfo(fontId, fontData))
    }

    /** The font information */
    class FontInfo(
        /** the id of the font */
        val mFontId: Int,
        /** the byte array of the font data */
        val mFontData: ByteArray
    ) {
        /** opaque cache of a font builder */
        var fontBuilder: Any? = null
    }

    /**
     * The context can be used in a few different modes, allowing operations to skip being executed:
     * - UNSET : all operations will get executed
     * - DATA : only operations dealing with DATA (eg loading a bitmap) should execute
     * - PAINT : only operations painting should execute
     */
    enum class ContextMode {
        UNSET,
        DATA,
        PAINT
    }

    fun getTheme(): Int = mTheme

    fun setTheme(theme: Int) {
        mTheme = theme
    }

    fun getMode(): ContextMode = mMode

    fun setMode(mode: ContextMode) {
        mMode = mode
    }

    fun getPaintContext(): PaintContext? = mPaintContext

    fun setPaintContext(paintContext: PaintContext) {
        mPaintContext = paintContext
    }

    fun getDocument(): CoreDocument? = mDocument

    fun isBasicDebug(): Boolean = mDebug == 1

    fun isVisualDebug(): Boolean = mDebug == 2

    fun setDebug(debug: Int) {
        mDebug = debug
    }

    /**
     * Set the document on the context
     *
     * @param document document used
     */
    fun setDocument(document: CoreDocument) {
        mDocument = document
    }

    // =============================================================================================
    // Operations
    // =============================================================================================

    /**
     * Set the main information about a document
     *
     * @param majorVersion major version of the document protocol used
     * @param minorVersion minor version of the document protocol used
     * @param patchVersion patch version of the document protocol used
     * @param width original width of the document when created
     * @param height original height of the document when created
     * @param capabilities bitmask of capabilities used in the document (TBD)
     * @param properties properties of the document (TBD)
     */
    fun header(
        majorVersion: Int,
        minorVersion: Int,
        patchVersion: Int,
        width: Int,
        height: Int,
        capabilities: Long,
        properties: IntMap<Any>?
    ) {
        mDocument.setVersion(majorVersion, minorVersion, patchVersion)
    }

    /**
     * Sets the way the player handles the content
     *
     * @param scroll set the horizontal behavior (NONE|SCROLL_HORIZONTAL|SCROLL_VERTICAL)
     * @param alignment set the alignment of the content (TOP|CENTER|BOTTOM|START|END)
     * @param sizing set the type of sizing for the content (NONE|SIZING_LAYOUT|SIZING_SCALE)
     * @param mode set the mode of sizing
     */
    fun setRootContentBehavior(scroll: Int, alignment: Int, sizing: Int, mode: Int) {
        // CoreDocument stub: behavior can be forwarded when CoreDocument is fleshed out
    }

    /**
     * Set a content description for the document
     *
     * @param contentDescriptionId the text id pointing at the description
     */
    fun setDocumentContentDescription(contentDescriptionId: Int) {
        // CoreDocument stub: content description can be set when CoreDocument is fleshed out
    }

    // =============================================================================================
    // Data handling
    // =============================================================================================

    /**
     * Mark the variable as dirty
     *
     * @param id the variable id
     */
    open fun markVariableDirty(id: Int) {
        // empty
    }

    /**
     * Save a bitmap under an imageId
     *
     * @param imageId the id of the image
     * @param encoding how the data is encoded 0 = png, 1 = raw, 2 = url
     * @param type the type of the data 0 = RGBA 8888, 1 = 888, 2 = 8 gray
     * @param width the width of the image
     * @param height the height of the image
     * @param bitmap the bytes that represent the image
     */
    abstract fun loadBitmap(
        imageId: Int,
        encoding: Short,
        type: Short,
        width: Int,
        height: Int,
        bitmap: ByteArray
    )

    /**
     * Save a string under a given id
     *
     * @param id the id of the string
     * @param text the value to set
     */
    abstract fun loadText(id: Int, text: String)

    /**
     * Get a string given an id
     *
     * @param id the id of the string
     * @return a string if found, null otherwise
     */
    abstract fun getText(id: Int): String?

    /**
     * Load a float
     *
     * @param id id of the float
     * @param value the value to set
     */
    abstract fun loadFloat(id: Int, value: Float)

    /**
     * Override an existing float value
     *
     * @param id the value id
     * @param value the new value
     */
    abstract fun overrideFloat(id: Int, value: Float)

    /**
     * Load a integer
     *
     * @param id id of the integer
     * @param value the value to set
     */
    abstract fun loadInteger(id: Int, value: Int)

    /**
     * Override an existing int value
     *
     * @param id the value id
     * @param value the new value
     */
    abstract fun overrideInteger(id: Int, value: Int)

    /**
     * Override an existing text value
     *
     * @param id the value id
     * @param valueId the new value
     */
    abstract fun overrideText(id: Int, valueId: Int)

    /**
     * Load an animated float associated with an id
     *
     * @param id the id of the float
     * @param animatedFloat The animated float
     */
    abstract fun loadAnimatedFloat(id: Int, animatedFloat: FloatExpression)

    /**
     * Save a shader under an ID
     *
     * @param id the id of the Shader
     * @param value the shader
     */
    abstract fun loadShader(id: Int, value: ShaderData)

    /**
     * Get a float given an id
     *
     * @param id the id of the float
     * @return the value of the float
     */
    abstract fun getFloat(id: Int): Float

    /**
     * Get a Integer given an id
     *
     * @param id of the integer
     * @return the value
     */
    abstract fun getInteger(id: Int): Int

    /**
     * Get a Long given an id
     *
     * @param id of the long
     * @return the value
     */
    abstract fun getLong(id: Int): Long

    /**
     * Get the color given an ID
     *
     * @param id of the color
     * @return the color
     */
    abstract fun getColor(id: Int): Int

    /**
     * Called to notify system that a command is interested in a variable
     *
     * @param id track when this id changes value
     * @param variableSupport call back when value changes
     */
    abstract fun listensTo(id: Int, variableSupport: VariableSupport)

    /**
     * Get the listeners for a given id
     *
     * @param id the variable id
     * @return the list of listeners, or null
     */
    open fun getListeners(id: Int): MutableList<VariableSupport>? = null

    /**
     * Notify commands with variables have changed
     *
     * @return the number of ms to next update
     */
    abstract fun updateOps(): Int

    /**
     * Get a shader given the id
     *
     * @param id get a shader given the id
     * @return The shader
     */
    abstract fun getShader(id: Int): ShaderData?

    // =============================================================================================
    // Click handling
    // =============================================================================================

    /**
     * Add a click area to the doc
     *
     * @param id the id of the click area
     * @param contentDescriptionId the content description of the click area
     * @param left the left bounds of the click area
     * @param top the top bounds of the click area
     * @param right the right bounds of the click area
     * @param bottom the bottom bounds of the click area
     * @param metadataId the id of the metadata string
     */
    abstract fun addClickArea(
        id: Int,
        contentDescriptionId: Int,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        metadataId: Int
    )

    /** Increments the count of operations executed in a pass */
    fun incrementOpCount() {
        mOpCount++
        if (mOpCount > MAX_OP_COUNT) {
            throw RuntimeException("Too many operations executed")
        }
    }

    /**
     * Get the last Op Count and clear the count.
     *
     * @return the number of ops executed.
     */
    fun getLastOpCount(): Int {
        val count = mOpCount
        mOpCount = 0
        return count
    }

    /** Explicitly clear the operation counter */
    fun clearLastOpCount() {
        mOpCount = 0
    }

    companion object {
        private const val MAX_OP_COUNT = 20_000 // Maximum cmds per frame

        // ID constants
        const val ID_CONTINUOUS_SEC = 1
        const val ID_TIME_IN_SEC = 2
        const val ID_TIME_IN_MIN = 3
        const val ID_TIME_IN_HR = 4
        const val ID_WINDOW_WIDTH = 5
        const val ID_WINDOW_HEIGHT = 6
        const val ID_COMPONENT_WIDTH = 7
        const val ID_COMPONENT_HEIGHT = 8
        const val ID_CALENDAR_MONTH = 9
        const val ID_OFFSET_TO_UTC = 10
        const val ID_WEEK_DAY = 11
        const val ID_DAY_OF_MONTH = 12
        const val ID_TOUCH_POS_X = 13
        const val ID_TOUCH_POS_Y = 14

        const val ID_TOUCH_VEL_X = 15
        const val ID_TOUCH_VEL_Y = 16

        const val ID_ACCELERATION_X = 17
        const val ID_ACCELERATION_Y = 18
        const val ID_ACCELERATION_Z = 19

        const val ID_GYRO_ROT_X = 20
        const val ID_GYRO_ROT_Y = 21
        const val ID_GYRO_ROT_Z = 22

        const val ID_MAGNETIC_X = 23
        const val ID_MAGNETIC_Y = 24
        const val ID_MAGNETIC_Z = 25

        const val ID_LIGHT = 26

        const val ID_DENSITY = 27

        /** Defines when the last build was made */
        const val ID_API_LEVEL = 28

        /** Defines when the TOUCH EVENT HAPPENED */
        const val ID_TOUCH_EVENT_TIME = 29

        /** Animation time in seconds */
        const val ID_ANIMATION_TIME = 30

        /** The delta between current and last Frame */
        const val ID_ANIMATION_DELTA_TIME = 31

        const val ID_EPOCH_SECOND = 32

        const val ID_FONT_SIZE = 33

        /** DAY OF THE YEAR 1-366 */
        const val ID_DAY_OF_YEAR = 34

        /** The YEAR e.g. 2026 */
        const val ID_YEAR = 35

        /** First baseline (for alignment) */
        const val ID_FIRST_BASELINE = 36

        /** Last baseline (for alignment) */
        const val ID_LAST_BASELINE = 37

        val FLOAT_DENSITY: Float = Utils.asNan(ID_DENSITY)

        /** CONTINUOUS_SEC is seconds from midnight looping every hour 0-3600 */
        val FLOAT_CONTINUOUS_SEC: Float = Utils.asNan(ID_CONTINUOUS_SEC)

        /** seconds run from Midnight=0 quantized to seconds hour 0..3599 */
        val FLOAT_TIME_IN_SEC: Float = Utils.asNan(ID_TIME_IN_SEC)

        /** minutes run from Midnight=0 quantized to minutes 0..1439 */
        val FLOAT_TIME_IN_MIN: Float = Utils.asNan(ID_TIME_IN_MIN)

        /** hours run from Midnight=0 quantized to Hours 0-23 */
        val FLOAT_TIME_IN_HR: Float = Utils.asNan(ID_TIME_IN_HR)

        /** Month of Year quantized to MONTHS 1-12. 1 = January */
        val FLOAT_CALENDAR_MONTH: Float = Utils.asNan(ID_CALENDAR_MONTH)

        /** DAY OF THE WEEK 1-7. 1 = Monday */
        val FLOAT_WEEK_DAY: Float = Utils.asNan(ID_WEEK_DAY)

        /** DAY OF THE MONTH 1-31 */
        val FLOAT_DAY_OF_MONTH: Float = Utils.asNan(ID_DAY_OF_MONTH)

        /** DAY OF THE YEAR 1-366 */
        val FLOAT_DAY_OF_YEAR: Float = Utils.asNan(ID_DAY_OF_YEAR)

        /** The YEAR e.g. 2026 */
        val FLOAT_YEAR: Float = Utils.asNan(ID_YEAR)

        val FLOAT_WINDOW_WIDTH: Float = Utils.asNan(ID_WINDOW_WIDTH)
        val FLOAT_WINDOW_HEIGHT: Float = Utils.asNan(ID_WINDOW_HEIGHT)
        val FLOAT_COMPONENT_WIDTH: Float = Utils.asNan(ID_COMPONENT_WIDTH)
        val FLOAT_COMPONENT_HEIGHT: Float = Utils.asNan(ID_COMPONENT_HEIGHT)

        /** ID_OFFSET_TO_UTC is the offset from UTC in sec (typically / 3600f) */
        val FLOAT_OFFSET_TO_UTC: Float = Utils.asNan(ID_OFFSET_TO_UTC)

        /** TOUCH_POS_X is the x position of the touch */
        val FLOAT_TOUCH_POS_X: Float = Utils.asNan(ID_TOUCH_POS_X)

        /** TOUCH_POS_Y is the y position of the touch */
        val FLOAT_TOUCH_POS_Y: Float = Utils.asNan(ID_TOUCH_POS_Y)

        /** TOUCH_VEL_X is the x velocity of the touch */
        val FLOAT_TOUCH_VEL_X: Float = Utils.asNan(ID_TOUCH_VEL_X)

        /** TOUCH_VEL_Y is the y velocity of the touch */
        val FLOAT_TOUCH_VEL_Y: Float = Utils.asNan(ID_TOUCH_VEL_Y)

        /** TOUCH_EVENT_TIME the time of the touch */
        val FLOAT_TOUCH_EVENT_TIME: Float = Utils.asNan(ID_TOUCH_EVENT_TIME)

        /** Animation time in seconds */
        val FLOAT_ANIMATION_TIME: Float = Utils.asNan(ID_ANIMATION_TIME)

        /** Animation delta time in seconds */
        val FLOAT_ANIMATION_DELTA_TIME: Float = Utils.asNan(ID_ANIMATION_DELTA_TIME)

        /** X acceleration sensor value in M/s^2 */
        val FLOAT_ACCELERATION_X: Float = Utils.asNan(ID_ACCELERATION_X)

        /** Y acceleration sensor value in M/s^2 */
        val FLOAT_ACCELERATION_Y: Float = Utils.asNan(ID_ACCELERATION_Y)

        /** Z acceleration sensor value in M/s^2 */
        val FLOAT_ACCELERATION_Z: Float = Utils.asNan(ID_ACCELERATION_Z)

        /** X Gyroscope rotation rate sensor value in radians/second */
        val FLOAT_GYRO_ROT_X: Float = Utils.asNan(ID_GYRO_ROT_X)

        /** Y Gyroscope rotation rate sensor value in radians/second */
        val FLOAT_GYRO_ROT_Y: Float = Utils.asNan(ID_GYRO_ROT_Y)

        /** Z Gyroscope rotation rate sensor value in radians/second */
        val FLOAT_GYRO_ROT_Z: Float = Utils.asNan(ID_GYRO_ROT_Z)

        /** Ambient magnetic field in X. sensor value in micro-Tesla (uT) */
        val FLOAT_MAGNETIC_X: Float = Utils.asNan(ID_MAGNETIC_X)

        /** Ambient magnetic field in Y. sensor value in micro-Tesla (uT) */
        val FLOAT_MAGNETIC_Y: Float = Utils.asNan(ID_MAGNETIC_Y)

        /** Ambient magnetic field in Z. sensor value in micro-Tesla (uT) */
        val FLOAT_MAGNETIC_Z: Float = Utils.asNan(ID_MAGNETIC_Z)

        /** Ambient light level in SI lux */
        val FLOAT_LIGHT: Float = Utils.asNan(ID_LIGHT)

        /** When was this player built */
        val FLOAT_API_LEVEL: Float = Utils.asNan(ID_API_LEVEL)

        /** The default font size */
        val FLOAT_FONT_SIZE: Float = Utils.asNan(ID_FONT_SIZE)

        /** The time in seconds since the epoch. */
        val INT_EPOCH_SECOND: Long = ID_EPOCH_SECOND.toLong() + 0x100000000L

        /** First Baseline */
        val FIRST_BASELINE: Float = Utils.asNan(ID_FIRST_BASELINE)

        /** Last Baseline */
        val LAST_BASELINE: Float = Utils.asNan(ID_LAST_BASELINE)

        /**
         * Is this a time id float
         *
         * @param fl the floatId to test
         * @return true if it is a time id
         */
        fun isTime(fl: Float): Boolean {
            val value = Utils.idFromNan(fl)
            return value in ID_CONTINUOUS_SEC..ID_DAY_OF_MONTH
        }

        /**
         * Get the time from a float id that indicates a type of time.
         *
         * Uses Long timestamp arithmetic instead of java.time classes.
         *
         * @param fl id of the type of time information requested
         * @return various time information such as seconds or min
         */
        fun getTime(fl: Float): Float {
            val value = Utils.idFromNan(fl)
            val nowMillis = currentTimeMillis()

            // Compute UTC offset in seconds for the local timezone via expect/actual
            val utcOffsetSeconds = localUtcOffsetSeconds()

            // Local time in milliseconds since epoch adjusted for local timezone
            val localMillis = nowMillis + utcOffsetSeconds * 1000L

            // Seconds since midnight in local time
            val localDayMillis = ((localMillis % 86_400_000L) + 86_400_000L) % 86_400_000L
            val totalSecondsInDay = localDayMillis / 1000L
            val hour = (totalSecondsInDay / 3600).toInt()
            val minute = ((totalSecondsInDay % 3600) / 60).toInt()
            val second = (totalSecondsInDay % 60).toInt()
            val currentMinute = hour * 60 + minute
            val currentSeconds = minute * 60 + second
            val nanoFraction = (localDayMillis % 1000L) * 1_000_000L
            val sec = currentSeconds + nanoFraction * 1E-9f

            // Compute calendar fields from epoch millis using simple arithmetic
            // Days since Unix epoch (1970-01-01)
            val localDays = floorDiv(localMillis, 86_400_000L)
            val dateFields = daysToDate(localDays)
            val year = dateFields[0]
            val month = dateFields[1]     // 1-12
            val dayOfMonth = dateFields[2] // 1-31

            // Day of week: 1970-01-01 was Thursday (4). ISO: Monday=1 .. Sunday=7
            val dayOfWeek = (((localDays % 7) + 3 + 7) % 7 + 1).toInt()

            // Day of year
            val dayOfYear = dayOfYearFromDate(year, month, dayOfMonth)

            return when (value) {
                ID_OFFSET_TO_UTC -> utcOffsetSeconds.toFloat()
                ID_CONTINUOUS_SEC -> sec
                ID_TIME_IN_SEC -> currentSeconds.toFloat()
                ID_TIME_IN_MIN -> currentMinute.toFloat()
                ID_TIME_IN_HR -> hour.toFloat()
                ID_CALENDAR_MONTH -> month.toFloat()
                ID_DAY_OF_MONTH -> dayOfMonth.toFloat()
                ID_WEEK_DAY -> dayOfWeek.toFloat()
                ID_DAY_OF_YEAR -> dayOfYear.toFloat()
                ID_YEAR -> year.toFloat()
                else -> fl
            }
        }

        // -- Date arithmetic helpers (no java.time) --

        private fun floorDiv(a: Long, b: Long): Long {
            val q = a / b
            return if (a xor b < 0 && q * b != a) q - 1 else q
        }

        private fun isLeapYear(year: Int): Boolean {
            return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
        }

        /**
         * Convert days since Unix epoch (1970-01-01) to [year, month, dayOfMonth].
         */
        private fun daysToDate(daysSinceEpoch: Long): IntArray {
            // Algorithm based on civil_from_days (Howard Hinnant)
            var z = daysSinceEpoch + 719468
            val era = (if (z >= 0) z else z - 146096) / 146097
            val doe = (z - era * 146097).toInt()          // day of era [0, 146096]
            val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
            val y = yoe + era.toInt() * 400
            val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
            val mp = (5 * doy + 2) / 153
            val d = doy - (153 * mp + 2) / 5 + 1
            val m = if (mp < 10) mp + 3 else mp - 9
            val year = if (m <= 2) y + 1 else y
            return intArrayOf(year, m, d)
        }

        private fun dayOfYearFromDate(year: Int, month: Int, day: Int): Int {
            val daysInMonth = intArrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
            if (isLeapYear(year)) daysInMonth[2] = 29
            var result = 0
            for (i in 1 until month) {
                result += daysInMonth[i]
            }
            return result + day
        }
    }
}

/**
 * Expect function to get the local timezone's UTC offset in seconds.
 * Implementations should return the current offset including DST if applicable.
 */
expect fun localUtcOffsetSeconds(): Int
