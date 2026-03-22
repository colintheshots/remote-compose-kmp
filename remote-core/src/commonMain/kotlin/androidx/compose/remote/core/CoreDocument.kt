/*
 * Copyright (C) 2023 The Android Open Source Project
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

import androidx.compose.remote.core.operations.BitmapData
import androidx.compose.remote.core.operations.ComponentValue
import androidx.compose.remote.core.operations.DataListFloat
import androidx.compose.remote.core.operations.DrawContent
import androidx.compose.remote.core.operations.FloatConstant
import androidx.compose.remote.core.operations.FloatExpression
import androidx.compose.remote.core.operations.Header
import androidx.compose.remote.core.operations.IntegerExpression
import androidx.compose.remote.core.operations.NamedVariable
import androidx.compose.remote.core.operations.RootContentBehavior
import androidx.compose.remote.core.operations.ShaderData
import androidx.compose.remote.core.operations.TextData
import androidx.compose.remote.core.operations.Theme
import androidx.compose.remote.core.operations.layout.CanvasOperations
import androidx.compose.remote.core.operations.layout.Component
import androidx.compose.remote.core.operations.Container
import androidx.compose.remote.core.operations.layout.ContainerEnd
import androidx.compose.remote.core.operations.layout.LayoutComponent
import androidx.compose.remote.core.operations.layout.LoopOperation
import androidx.compose.remote.core.operations.layout.RootLayoutComponent
import androidx.compose.remote.core.operations.layout.TouchOperation
import androidx.compose.remote.core.operations.layout.modifiers.ComponentModifiers
import androidx.compose.remote.core.operations.layout.modifiers.ModifierOperation
import androidx.compose.remote.core.operations.utilities.IntMap
import androidx.compose.remote.core.operations.utilities.StringSerializer
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable
import androidx.compose.remote.core.types.IntegerConstant
import androidx.compose.remote.core.types.LongConstant
import kotlin.math.max
import kotlin.math.min

/**
 * Represents a platform independent RemoteCompose document, containing RemoteCompose operations +
 * state
 */
open class CoreDocument(
    internal val mClock: RemoteClock = SystemClock()
) : Serializable {

    companion object {
        private const val DEBUG = false

        const val MAJOR_VERSION = 1
        const val MINOR_VERSION = 1
        const val PATCH_VERSION = 0
        const val DOCUMENT_API_LEVEL = 7
        internal const val BUILD = 0.0f

        private const val UPDATE_VARIABLES_BEFORE_LAYOUT = false

        fun getDocumentApiLevel(): Int = DOCUMENT_API_LEVEL
    }

    internal var mOperations: MutableList<Operation> = mutableListOf()
    internal var mRootLayoutComponent: RootLayoutComponent? = null
    var mRemoteComposeState: RemoteComposeState = RemoteComposeState()
    var mTimeVariables: TimeVariables = TimeVariables(mClock)
    internal var mVersion: Version = Version(MAJOR_VERSION, MINOR_VERSION, PATCH_VERSION)

    var mContentDescription: String? = null
    var mRequiredCapabilities: Long = 0L
    var mWidth: Int = 0
    var mHeight: Int = 0
    var mContentScroll: Int = RootContentBehavior.NONE
    var mContentSizing: Int = RootContentBehavior.NONE
    var mContentMode: Int = RootContentBehavior.NONE
    var mContentAlignment: Int = RootContentBehavior.ALIGNMENT_CENTER

    var mBuffer: RemoteComposeBuffer = RemoteComposeBuffer()

    private val mIntegerExpressions: MutableMap<Long, IntegerExpression> = mutableMapOf()
    private val mFloatExpressions: MutableMap<Int, FloatExpression> = mutableMapOf()
    private val mAppliedTouchOperations: MutableSet<Component> = mutableSetOf()

    private var mLastId: Int = 1
    private var mDocProperties: IntMap<Any>? = null
    internal var mFirstPaint: Boolean = true
    private var mIsUpdateDoc: Boolean = false
    private var mHostExceptionID: Int = 0
    private var mBitmapMemory: Int = 0

    fun getClock(): RemoteClock = mClock

    var contentDescription: String?
        get() = mContentDescription
        set(value) { mContentDescription = value }

    var requiredCapabilities: Long
        get() = mRequiredCapabilities
        set(value) { mRequiredCapabilities = value }

    var width: Int
        get() = mWidth
        set(value) {
            mWidth = value
            mRemoteComposeState.setWindowWidth(value.toFloat())
        }

    var height: Int
        get() = mHeight
        set(value) {
            mHeight = value
            mRemoteComposeState.setWindowHeight(value.toFloat())
        }

    var buffer: RemoteComposeBuffer
        get() = mBuffer
        set(value) { mBuffer = value }

    var remoteComposeState: RemoteComposeState
        get() = mRemoteComposeState
        set(value) { mRemoteComposeState = value }

    val contentScroll: Int get() = mContentScroll
    val contentSizing: Int get() = mContentSizing
    val contentMode: Int get() = mContentMode

    fun setRootContentBehavior(scroll: Int, alignment: Int, sizing: Int, mode: Int) {
        mContentScroll = scroll
        mContentAlignment = alignment
        mContentSizing = sizing
        mContentMode = mode
    }

    fun computeScale(w: Float, h: Float, scaleOutput: FloatArray) {
        var contentScaleX = 1f
        var contentScaleY = 1f
        if (mContentSizing == RootContentBehavior.SIZING_SCALE) {
            var scaleX: Float
            var scaleY: Float
            var scale: Float
            when (mContentMode) {
                RootContentBehavior.SCALE_INSIDE -> {
                    scaleX = w / mWidth; scaleY = h / mHeight
                    scale = min(1f, min(scaleX, scaleY))
                    contentScaleX = scale; contentScaleY = scale
                }
                RootContentBehavior.SCALE_FIT -> {
                    scaleX = w / mWidth; scaleY = h / mHeight
                    scale = min(scaleX, scaleY)
                    contentScaleX = scale; contentScaleY = scale
                }
                RootContentBehavior.SCALE_FILL_WIDTH -> {
                    scale = w / mWidth; contentScaleX = scale; contentScaleY = scale
                }
                RootContentBehavior.SCALE_FILL_HEIGHT -> {
                    scale = h / mHeight; contentScaleX = scale; contentScaleY = scale
                }
                RootContentBehavior.SCALE_CROP -> {
                    scaleX = w / mWidth; scaleY = h / mHeight
                    scale = max(scaleX, scaleY)
                    contentScaleX = scale; contentScaleY = scale
                }
                RootContentBehavior.SCALE_FILL_BOUNDS -> {
                    contentScaleX = w / mWidth; contentScaleY = h / mHeight
                }
            }
        }
        scaleOutput[0] = contentScaleX
        scaleOutput[1] = contentScaleY
    }

    private fun computeTranslate(
        w: Float, h: Float, contentScaleX: Float, contentScaleY: Float, translateOutput: FloatArray
    ) {
        val horizontalContentAlignment = mContentAlignment and 0xF0
        val verticalContentAlignment = mContentAlignment and 0xF
        var translateX = 0f
        var translateY = 0f
        val contentWidth = mWidth * contentScaleX
        val contentHeight = mHeight * contentScaleY

        when (horizontalContentAlignment) {
            RootContentBehavior.ALIGNMENT_HORIZONTAL_CENTER -> translateX = (w - contentWidth) / 2f
            RootContentBehavior.ALIGNMENT_END -> translateX = w - contentWidth
        }
        when (verticalContentAlignment) {
            RootContentBehavior.ALIGNMENT_VERTICAL_CENTER -> translateY = (h - contentHeight) / 2f
            RootContentBehavior.ALIGNMENT_BOTTOM -> translateY = h - contentHeight
        }
        translateOutput[0] = translateX
        translateOutput[1] = translateY
    }

    val clickAreas: MutableSet<ClickAreaRepresentation> get() = mClickAreas

    fun getRootLayoutComponent(): RootLayoutComponent? = mRootLayoutComponent

    fun invalidateMeasure() { mRootLayoutComponent?.invalidateMeasure() }

    fun getComponent(id: Int): Component? = mRootLayoutComponent?.getComponent(id)

    fun displayHierarchy(): String {
        val serializer = StringSerializer()
        for (op in mOperations) {
            if (op is RootLayoutComponent) {
                op.displayHierarchy(op as Component, 0, serializer)
            } else if (op is SerializableToString) {
                op.serializeToString(0, serializer)
            }
        }
        return serializer.toString()
    }

    fun evaluateIntExpression(expressionId: Long, targetId: Int, context: RemoteContext) {
        mIntegerExpressions[expressionId]?.let {
            context.overrideInteger(targetId, it.evaluate(context))
        }
    }

    fun evaluateFloatExpression(expressionId: Int, targetId: Int, context: RemoteContext) {
        mFloatExpressions[expressionId]?.let {
            context.overrideFloat(targetId, it.evaluate(context))
        }
    }

    override fun serialize(serializer: MapSerializer) {
        serializer.addType("CoreDocument").add("width", mWidth).add("height", mHeight)
            .add("operations", mOperations)
    }

    fun setProperties(properties: IntMap<Any>?) { mDocProperties = properties }

    fun getProperty(key: Short): Any? = mDocProperties?.get(key.toInt())

    fun applyUpdate(delta: CoreDocument) {
        val txtData = mutableMapOf<Int, TextData>()
        val imgData = mutableMapOf<Int, BitmapData>()
        val fltData = mutableMapOf<Int, FloatConstant>()
        val intData = mutableMapOf<Int, IntegerConstant>()
        val longData = mutableMapOf<Int, LongConstant>()
        val floatListData = mutableMapOf<Int, DataListFloat>()

        recursiveTraverse(mOperations) { op ->
            when (op) {
                is TextData -> txtData[op.mTextId] = op
                is BitmapData -> imgData[op.mImageId] = op
                is FloatConstant -> fltData[op.mId] = op
                is IntegerConstant -> intData[op.mId] = op
                is LongConstant -> longData[op.mId] = op
                is DataListFloat -> floatListData[op.mId] = op
            }
        }

        recursiveTraverse(delta.mOperations) { op ->
            when (op) {
                is TextData -> txtData[op.mTextId]?.let { it.update(op); it.markDirty() }
                is BitmapData -> imgData[op.mImageId]?.let { it.update(op); it.markDirty() }
                is FloatConstant -> fltData[op.mId]?.let { it.update(op); it.markDirty() }
                is IntegerConstant -> intData[op.mId]?.let { it.update(op); it.markDirty() }
                is LongConstant -> longData[op.mId]?.let { it.update(op); it.markDirty() }
                is DataListFloat -> floatListData[op.mId]?.let { it.update(op); it.markDirty() }
            }
        }
    }

    fun setHostExceptionID(exceptionID: Int) { mHostExceptionID = exceptionID }
    fun getHostExceptionID(): Int = mHostExceptionID
    fun bitmapMemory(): Int = mBitmapMemory

    private fun interface Visitor { fun visit(op: Operation) }

    private fun recursiveTraverse(operations: MutableList<Operation>, visitor: Visitor) {
        for (op in operations) {
            if (op is Container) recursiveTraverse(op.getList(), visitor)
            visitor.visit(op)
        }
    }

    // Haptic support
    interface HapticEngine { fun haptic(type: Int) }
    internal var mHapticEngine: HapticEngine? = null
    fun setHapticEngine(engine: HapticEngine) { mHapticEngine = engine }
    fun haptic(type: Int) { mHapticEngine?.haptic(type) }

    fun appliedTouchOperation(component: Component) { mAppliedTouchOperations.add(component) }

    // Action callbacks
    interface ActionCallback { fun onAction(name: String, value: Any?) }
    internal var mActionListeners: MutableSet<ActionCallback> = mutableSetOf()
    fun runNamedAction(name: String, value: Any?) {
        for (cb in mActionListeners) cb.onAction(name, value)
    }
    fun addActionCallback(callback: ActionCallback) { mActionListeners.add(callback) }
    fun clearActionCallbacks() { mActionListeners.clear() }

    // Id Action callbacks
    interface IdActionCallback { fun onAction(id: Int, metadata: String?) }
    internal var mIdActionListeners: MutableSet<IdActionCallback> = mutableSetOf()
    internal var mTouchListeners: MutableSet<TouchListener> = mutableSetOf()
    internal var mClickAreas: MutableSet<ClickAreaRepresentation> = mutableSetOf()

    internal class Version(val major: Int, val minor: Int, val patchLevel: Int) {
        fun supportsVersion(major: Int, minor: Int, patch: Int): Boolean {
            if (major > this.major) return false
            if (major < this.major) return true
            if (minor > this.minor) return false
            if (minor < this.minor) return true
            return patch <= this.patchLevel
        }
    }

    class ClickAreaRepresentation(
        var mId: Int, val mContentDescription: String?,
        var mLeft: Float, var mTop: Float, var mRight: Float, var mBottom: Float,
        val mMetadata: String?
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ClickAreaRepresentation) return false
            return mId == other.mId && mContentDescription == other.mContentDescription
                && mMetadata == other.mMetadata
        }
        override fun hashCode(): Int {
            var result = mId
            result = 31 * result + (mContentDescription?.hashCode() ?: 0)
            result = 31 * result + (mMetadata?.hashCode() ?: 0)
            return result
        }
        fun contains(x: Float, y: Float): Boolean = x >= mLeft && x < mRight && y >= mTop && y < mBottom
        val left: Float get() = mLeft
        val top: Float get() = mTop
        fun width(): Float = max(0f, mRight - mLeft)
        fun height(): Float = max(0f, mBottom - mTop)
        val id: Int get() = mId
        val contentDescription: String? get() = mContentDescription
        val metadata: String? get() = mMetadata
    }

    @Suppress("UNCHECKED_CAST")
    fun initFromBuffer(buffer: RemoteComposeBuffer) {
        mOperations = mutableListOf()
        buffer.inflateFromBuffer(mOperations as ArrayList<Operation>)
        var hasTouchOperations = false
        for (op in mOperations) {
            if (op is Header) setVersion(op.mMajorVersion, op.mMinorVersion, op.mPatchVersion)
            if (op is IntegerExpression) mIntegerExpressions[op.mId.toLong()] = op
            if (op is FloatExpression) mFloatExpressions[op.mId] = op
            if (op is TouchOperation) hasTouchOperations = true
        }
        mBitmapMemory = 0
        mOperations = inflateComponents(mOperations as ArrayList<Operation>)
        mBuffer = buffer
        for (op in mOperations) {
            if (op is RootLayoutComponent) { mRootLayoutComponent = op; break }
        }
        mRootLayoutComponent?.let {
            it.setHasTouchListeners(hasTouchOperations)
            it.assignIds(mLastId)
        }
    }

    private fun inflateComponents(operations: ArrayList<Operation>): MutableList<Operation> {
        val finalOperationsList = ArrayList<Operation>()
        var ops: MutableList<Operation> = finalOperationsList
        val containers = ArrayList<Container>()
        var lastLayoutComponent: LayoutComponent? = null
        mLastId = -1
        for (o in operations) {
            if (o is BitmapData) mBitmapMemory += o.getHeight() * o.getWidth() * 4
            if (o is Container) {
                if (o is Component) {
                    if (containers.isNotEmpty()) {
                        val parent = containers[containers.size - 1]
                        if (parent is Component) o.setParent(parent)
                    }
                    if (o.componentId < mLastId) mLastId = o.componentId
                    if (o is LayoutComponent) lastLayoutComponent = o
                }
                containers.add(o)
                ops = o.getList()
            } else if (o is ContainerEnd) {
                var container: Container? = null
                if (containers.isNotEmpty()) container = containers.removeAt(containers.size - 1)
                val parentContainer: Container? = if (containers.isNotEmpty()) containers[containers.size - 1] else null
                ops = parentContainer?.getList() ?: finalOperationsList
                if (container != null) {
                    if (container is Component) container.inflate()
                    ops.add(container as Operation)
                }
                if (container is CanvasOperations) container.setComponent(lastLayoutComponent)
            } else {
                if (o is DrawContent) o.setComponent(lastLayoutComponent)
                ops.add(o)
            }
        }
        return ops
    }

    private val mComponentMap: MutableMap<Int, Component> = mutableMapOf()
    private val mLayoutComputeOperations: MutableSet<LayoutCompute> = mutableSetOf()

    private fun registerVariables(context: RemoteContext, list: MutableList<Operation>) {
        for (op in list) {
            if (op is LayoutCompute) mLayoutComputeOperations.add(op)
            if (op is VariableSupport) op.registerListening(context)
            if (op is Component) {
                mComponentMap[op.componentId] = op
                op.registerVariables(context)
            }
            if (op is Container) registerVariables(context, op.getList())
            if (op is ComponentValue) {
                val component = mComponentMap[op.getComponentId()]
                if (component != null) component.addComponentValue(op)
                else println("=> Component not found for id ${op.getComponentId()}")
            }
            if (op is ComponentModifiers) {
                for (modifier in op.getList()) {
                    if (modifier is VariableSupport) modifier.registerListening(context)
                    if (modifier is LayoutCompute) mLayoutComputeOperations.add(modifier)
                }
            }
        }
    }

    private fun applyOperations(context: RemoteContext, list: MutableList<Operation>) {
        for (op in list) {
            if (op is VariableSupport) op.updateVariables(context)
            if (op is Component) op.updateVariables(context)
            op.markNotDirty()
            op.apply(context)
            context.incrementOpCount()
            if (op is Container) applyOperations(context, op.getList())
        }
    }

    fun initializeContext(context: RemoteContext) { initializeContext(context, null) }

    fun initializeContext(context: RemoteContext, bitmapMap: Map<Int, Any>?) {
        mRemoteComposeState.reset()
        mRemoteComposeState.setContext(context)
        mClickAreas.clear()
        mRemoteComposeState.setNextId(RemoteComposeState.START_ID)
        bitmapMap?.forEach { (i, v) -> mRemoteComposeState.cacheData(i, v) }
        context.mDocument = this
        context.mRemoteComposeState = mRemoteComposeState
        context.mMode = RemoteContext.ContextMode.DATA
        mTimeVariables.updateTime(context)
        registerVariables(context, mOperations)
        applyOperations(context, mOperations)
        context.mMode = RemoteContext.ContextMode.UNSET
        if (UPDATE_VARIABLES_BEFORE_LAYOUT) mFirstPaint = true
    }

    fun canBeDisplayed(playerMajorVersion: Int, playerMinorVersion: Int, capabilities: Long): Boolean {
        if (mVersion.major < playerMajorVersion) return true
        if (mVersion.major > playerMajorVersion) return false
        return mVersion.minor <= playerMinorVersion
    }

    fun setVersion(majorVersion: Int, minorVersion: Int, patch: Int) {
        mVersion = Version(majorVersion, minorVersion, patch)
    }

    fun addClickArea(
        id: Int, contentDescription: String?, left: Float, top: Float,
        right: Float, bottom: Float, metadata: String?
    ) {
        val car = ClickAreaRepresentation(id, contentDescription, left, top, right, bottom, metadata)
        mClickAreas.remove(car)
        mClickAreas.add(car)
    }

    fun addTouchListener(listener: TouchListener) { mTouchListeners.add(listener) }
    fun addIdActionListener(callback: IdActionCallback) { mIdActionListeners.add(callback) }
    fun getIdActionListeners(): MutableSet<IdActionCallback> = mIdActionListeners

    fun onClick(context: RemoteContext, x: Float, y: Float) {
        for (ca in mClickAreas) if (ca.contains(x, y)) warnClickListeners(ca)
        mRootLayoutComponent?.onClick(context, this, x, y)
    }

    fun performClick(context: RemoteContext, id: Int, metadata: String) {
        for (ca in mClickAreas) {
            if (ca.mId == id) { warnClickListeners(ca); return }
        }
        notifyOfException(id, metadata)
        getComponent(id)?.onClick(context, this, -1f, -1f)
    }

    fun notifyOfException(id: Int, metadata: String?) {
        for (listener in mIdActionListeners) listener.onAction(id, metadata)
    }

    private fun warnClickListeners(clickArea: ClickAreaRepresentation) {
        notifyOfException(clickArea.mId, clickArea.mMetadata)
    }

    fun hasTouchListener(): Boolean {
        val hasComponentsTouchListeners = mRootLayoutComponent?.getHasTouchListeners() == true
        return hasComponentsTouchListeners || mTouchListeners.isNotEmpty()
    }

    fun touchDrag(context: RemoteContext, x: Float, y: Float): Boolean {
        context.loadFloat(RemoteContext.ID_TOUCH_POS_X, x)
        context.loadFloat(RemoteContext.ID_TOUCH_POS_Y, y)
        for (tl in mTouchListeners) tl.touchDrag(context, x, y)
        if (mRootLayoutComponent != null) {
            for (c in mAppliedTouchOperations) c.onTouchDrag(context, this, x, y, true)
            if (mAppliedTouchOperations.isNotEmpty()) return true
        }
        return mTouchListeners.isNotEmpty()
    }

    fun touchDown(context: RemoteContext, x: Float, y: Float) {
        context.loadFloat(RemoteContext.ID_TOUCH_POS_X, x)
        context.loadFloat(RemoteContext.ID_TOUCH_POS_Y, y)
        for (tl in mTouchListeners) tl.touchDown(context, x, y)
        mRootLayoutComponent?.onTouchDown(context, this, x, y)
        mRepaintNext = 1
    }

    fun touchUp(context: RemoteContext, x: Float, y: Float, dx: Float, dy: Float) {
        context.loadFloat(RemoteContext.ID_TOUCH_POS_X, x)
        context.loadFloat(RemoteContext.ID_TOUCH_POS_Y, y)
        for (tl in mTouchListeners) tl.touchUp(context, x, y, dx, dy)
        if (mRootLayoutComponent != null) {
            for (c in mAppliedTouchOperations) c.onTouchUp(context, this, x, y, dx, dy, true)
            mAppliedTouchOperations.clear()
        }
        mRepaintNext = 1
    }

    fun touchCancel(context: RemoteContext, x: Float, y: Float, dx: Float, dy: Float) {
        if (mRootLayoutComponent != null) {
            for (c in mAppliedTouchOperations) c.onTouchCancel(context, this, x, y, true)
            mAppliedTouchOperations.clear()
        }
        mRepaintNext = 1
    }

    override fun toString(): String = buildString {
        for (op in mOperations) { append(op.toString()); append("\n") }
    }

    fun getNamedColors(): Array<String>? = getNamedVariables(NamedVariable.COLOR_TYPE)

    fun getNamedVariables(type: Int): Array<String> {
        val ret = mutableListOf<String>()
        getNamedVars(type, mOperations, ret)
        return ret.toTypedArray()
    }

    private fun getNamedVars(type: Int, ops: MutableList<Operation>, list: MutableList<String>) {
        for (op in ops) {
            if (op is NamedVariable && op.mVarType == type) list.add(op.mVarName)
            if (op is Container) getNamedVars(type, op.getList(), list)
        }
    }

    // Painting
    private val mScaleOutput = FloatArray(2)
    private val mTranslateOutput = FloatArray(2)
    private var mRepaintNext = -1
    private var mLastOpCount = 0

    fun getOpsPerFrame(): Int = mLastOpCount
    fun needsRepaint(): Int = mRepaintNext

    private fun updateVariables(context: RemoteContext, theme: Int, operations: List<Operation>) {
        for (op in operations) {
            if (op.isDirty() && op is VariableSupport) {
                op.updateVariables(context); op.apply(context); op.markNotDirty()
            }
            if (op is Container) updateVariables(context, theme, op.getList())
        }
    }

    fun paint(context: RemoteContext, theme: Int) {
        context.clearLastOpCount()
        val paintContext = context.getPaintContext()!!
        paintContext.clearNeedsRepaint()
        context.mMode = RemoteContext.ContextMode.UNSET
        context.setTheme(Theme.UNSPECIFIED)
        context.mRemoteComposeState = mRemoteComposeState
        context.mRemoteComposeState.setContext(context)
        context.loadFloat(RemoteContext.ID_DENSITY, context.getDensity())

        if (UPDATE_VARIABLES_BEFORE_LAYOUT) {
            if (mFirstPaint) mFirstPaint = false
            else updateVariables(context, theme, mOperations)
        }

        if (mContentSizing == RootContentBehavior.SIZING_SCALE) {
            computeScale(context.mWidth, context.mHeight, mScaleOutput)
            val sw = mScaleOutput[0]; val sh = mScaleOutput[1]
            computeTranslate(context.mWidth, context.mHeight, sw, sh, mTranslateOutput)
            paintContext.translate(mTranslateOutput[0], mTranslateOutput[1])
            paintContext.scale(sw, sh)
        } else {
            width = context.mWidth.toInt(); height = context.mHeight.toInt()
        }
        mTimeVariables.updateTime(context)
        mRepaintNext = context.updateOps()

        val rl = mRootLayoutComponent
        if (rl != null) {
            if (context.mWidth != rl.width || context.mHeight != rl.height) rl.invalidateMeasure()
            if (mLayoutComputeOperations.isNotEmpty()) {
                var nbEvals = 0; var needsEval = true
                while (needsEval && nbEvals < 2) {
                    needsEval = false
                    for (op in mLayoutComputeOperations) if (op.evaluateInLayout(context)) needsEval = true
                    nbEvals++
                }
            }
            if (rl.needsMeasure()) rl.layout(context)
            if (rl.needsBoundsAnimation()) {
                mRepaintNext = 1; rl.clearNeedsBoundsAnimation(); rl.animatingBounds(context)
            }
            if (DEBUG) println(rl.displayHierarchy())
            if (rl.doesNeedsRepaint()) mRepaintNext = 1
        }

        context.mMode = RemoteContext.ContextMode.PAINT
        for (i in mOperations.indices) {
            val op = mOperations[i]
            var apply = true
            if (theme != Theme.UNSPECIFIED) {
                val ct = context.getTheme()
                apply = ct == theme || ct == Theme.UNSPECIFIED || op is Theme
            }
            if (apply) {
                val dirty = op.isDirty()
                if (dirty || op is PaintOperation) {
                    if (dirty && op is VariableSupport) {
                        op.markNotDirty(); (op as VariableSupport).updateVariables(context)
                    }
                    context.incrementOpCount(); op.apply(context)
                }
            }
        }
        if (paintContext.doesNeedsRepaint() || (rl != null && rl.doesNeedsRepaint())) mRepaintNext = 1
        context.mMode = RemoteContext.ContextMode.UNSET
        if (DEBUG && rl != null) println(rl.displayHierarchy())
        mLastOpCount = context.getLastOpCount()
    }

    fun getNumberOfOps(): Int {
        var count = mOperations.size
        for (op in mOperations) if (op is Container) count += getChildOps(op)
        return count
    }

    private fun getChildOps(base: Container): Int {
        var count = base.getList().size
        for (op in base.getList()) {
            if (op is Container) {
                val mult = if (op is LoopOperation) op.estimateIterations() else 1
                count += mult * getChildOps(op)
            }
        }
        return count
    }

    class DocInfo {
        var mNumberOfOps: Int = 0; var mNumberOfImages: Int = 0; var mSizeOfImages: Int = 0
        val numberOfOps: Int get() = mNumberOfOps
        val numberOfImages: Int get() = mNumberOfImages
        val sizeOfImages: Int get() = mSizeOfImages
    }

    fun getBitmapDataSet(): Array<BitmapData> {
        val ret = mutableListOf<BitmapData>()
        getBitmapDataSet(mOperations, ret)
        return ret.toTypedArray()
    }

    private fun getBitmapDataSet(operations: List<Operation>, ret: MutableList<BitmapData>) {
        for (op in operations) {
            if (op is BitmapData) ret.add(op)
            if (op is Container) getBitmapDataSet(op.getList(), ret)
        }
    }

    fun getDocInfo(): DocInfo {
        val info = DocInfo(); getDocInfo(mOperations, info); return info
    }

    private fun getDocInfo(operations: List<Operation>, info: DocInfo) {
        info.mNumberOfOps += operations.size
        for (op in operations) {
            if (op is Container) getDocInfo(op.getList(), info)
            if (op is BitmapData) { info.mNumberOfImages++; info.mSizeOfImages += op.getWidth() * op.getHeight() }
        }
    }

    fun getStats(): Array<String> {
        val ret = mutableListOf<String>()
        val tmp = WireBuffer()
        var count = mOperations.size
        val map = mutableMapOf<String, IntArray>()
        for (op in mOperations) {
            val name = op::class.simpleName ?: "Unknown"
            val values = map.getOrPut(name) { IntArray(2) }
            values[0]++; values[1] += sizeOfComponent(op, tmp)
            if (op is Container) count += addChildren(op, map, tmp)
        }
        ret.add(0, "number of operations : $count")
        for ((s, v) in map) ret.add("$s : ${v[0]}:${v[1]}")
        return ret.toTypedArray()
    }

    private fun sizeOfComponent(com: Operation, tmp: WireBuffer): Int {
        tmp.reset(100); com.write(tmp); val size = tmp.size; tmp.reset(100); return size
    }

    private fun addChildren(base: Container, map: MutableMap<String, IntArray>, tmp: WireBuffer): Int {
        var count = base.getList().size
        for (op in base.getList()) {
            val name = op::class.simpleName ?: "Unknown"
            val values = map.getOrPut(name) { IntArray(2) }
            values[0]++; values[1] += sizeOfComponent(op, tmp)
            if (op is Container) count += addChildren(op, map, tmp)
        }
        return count
    }

    fun toNestedString(): String = buildString {
        for (op in mOperations) {
            append(op.toString()); append("\n")
            if (op is Container) toNestedString(op, this, "  ")
        }
    }

    private fun toNestedString(base: Container, ret: StringBuilder, indent: String) {
        for (op in base.getList()) {
            for (line in op.toString().split("\n")) { ret.append(indent); ret.append(line); ret.append("\n") }
            if (op is Container) toNestedString(op, ret, indent + "  ")
        }
    }

    fun getOperations(): List<Operation> = mOperations

    interface ShaderControl { fun isShaderValid(shader: String): Boolean }

    fun checkShaders(context: RemoteContext, ctl: ShaderControl) { checkShaders(context, ctl, mOperations) }

    private fun checkShaders(context: RemoteContext, ctl: ShaderControl, operations: List<Operation>) {
        for (op in operations) {
            if (op is TextData) op.apply(context)
            if (op is Container) checkShaders(context, ctl, op.getList())
            if (op is ShaderData) {
                val str = context.getText(op.getShaderTextId())
                if (str != null) op.enable(ctl.isShaderValid(str))
            }
        }
    }

    fun setUpdateDoc(isUpdateDoc: Boolean) { mIsUpdateDoc = isUpdateDoc }
    fun isUpdateDoc(): Boolean = mIsUpdateDoc
}
