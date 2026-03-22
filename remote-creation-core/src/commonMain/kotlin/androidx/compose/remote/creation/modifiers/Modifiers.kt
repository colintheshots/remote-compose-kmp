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
package androidx.compose.remote.creation.modifiers

import androidx.compose.remote.creation.Rc
import androidx.compose.remote.creation.RemoteComposeWriter

/** align by modifier */
class AlignByModifier(private val mLine: Float = Rc.Layout.FIRST_BASELINE) :
    RecordingModifier.Element {
    override fun write(writer: RemoteComposeWriter) {
        writer.addAlignByModifier(mLine)
    }
}

/** animate spec modifier */
class AnimateSpecModifier(
    private val animationId: Int,
    private val motionDuration: Float,
    private val motionEasingType: Int,
    private val visibilityDuration: Float,
    private val visibilityEasingType: Int,
    private val enterAnimation: Int,
    private val exitAnimation: Int,
) : RecordingModifier.Element {
    override fun write(writer: RemoteComposeWriter) {
        writer.addAnimationSpecModifier(
            animationId, motionDuration, motionEasingType,
            visibilityDuration, visibilityEasingType,
            enterAnimation, exitAnimation,
        )
    }
}

/** border modifier */
class BorderModifier(
    private val width: Float,
    private val roundedCorner: Float,
    private val color: Int,
    private val shapeType: Int,
) : RecordingModifier.Element {
    override fun write(writer: RemoteComposeWriter) {
        writer.addModifierBorder(width, roundedCorner, color, shapeType)
    }
}

/** circle shape */
class CircleShape : Shape {
    override val type: Int get() = Shape.CIRCLE
}

/** click action modifier */
class ClickActionModifier(
    private val actions: Array<out androidx.compose.remote.creation.actions.Action>,
) : RecordingModifier.Element {
    override fun write(writer: RemoteComposeWriter) {
        writer.addClickModifierOperation()
        for (action in actions) {
            action.write(writer)
        }
        writer.addContainerEnd()
    }
}

/** clip modifier */
class ClipModifier private constructor(
    private val shape: Shape,
) : RecordingModifier.Element {

    companion object {
        fun rect(): ClipModifier = ClipModifier(RectShape())
        fun roundedRect(
            topStart: Float = 0f, topEnd: Float = 0f,
            bottomStart: Float = 0f, bottomEnd: Float = 0f,
        ): ClipModifier = ClipModifier(RoundedRectShape(topStart, topEnd, bottomStart, bottomEnd))
        fun circle(): ClipModifier = ClipModifier(CircleShape())
    }

    override fun write(writer: RemoteComposeWriter) {
        when (shape) {
            is RectShape -> writer.addClipRectModifier()
            is RoundedRectShape -> writer.addRoundClipRectModifier(
                shape.topStart, shape.topEnd, shape.bottomStart, shape.bottomEnd,
            )
            is CircleShape -> writer.addClipRectModifier() // circle clip via rect
        }
    }
}

/** collapsible priority modifier */
class CollapsiblePriorityModifier(
    private val orientation: Int,
    private val priority: Float,
) : RecordingModifier.Element {
    override fun write(writer: RemoteComposeWriter) {
        writer.addCollapsiblePriorityModifier(orientation, priority)
    }
}

/** component layout changes interface */
interface ComponentLayoutChanges {
    fun setX(value: Number)
    fun setY(value: Number)
    fun setWidth(value: Number)
    fun setHeight(value: Number)
    fun getX(): androidx.compose.remote.creation.RFloat
    fun getY(): androidx.compose.remote.creation.RFloat
    fun getWidth(): androidx.compose.remote.creation.RFloat
    fun getHeight(): androidx.compose.remote.creation.RFloat
    fun getParentWidth(): androidx.compose.remote.creation.RFloat
    fun getParentHeight(): androidx.compose.remote.creation.RFloat
}

/** component layout changes writer interface */
fun interface ComponentLayoutChangesWriter {
    fun run(changes: ComponentLayoutChanges)
}

/** component layout compute modifier */
class ComponentLayoutComputeModifier(
    private val type: Int,
    private val commands: ComponentLayoutChangesWriter,
) : RecordingModifier.Element {
    override fun write(writer: RemoteComposeWriter) {
        writer.addLayoutCompute(type, commands)
    }
}

/** draw with content modifier */
class DrawWithContentModifier(
    private val content: RemoteComposeWriter.() -> Unit,
) : RecordingModifier.Element {
    override fun write(writer: RemoteComposeWriter) {
        writer.addDrawContentOperation()
        content(writer)
        writer.addContainerEnd()
    }
}

/** dynamic border modifier */
class DynamicBorderModifier(
    private val width: Float,
    private val roundedCorner: Float,
    private val colorId: Int,
    private val shapeType: Int,
) : RecordingModifier.Element {
    override fun write(writer: RemoteComposeWriter) {
        writer.addModifierDynamicBorder(width, roundedCorner, colorId, shapeType)
    }
}

/** dynamic solid background modifier */
class DynamicSolidBackgroundModifier(
    private val colorId: Int,
    private val shapeType: Int,
) : RecordingModifier.Element {
    override fun write(writer: RemoteComposeWriter) {
        writer.addDynamicModifierBackground(colorId, shapeType)
    }
}

/** graphics layer modifier */
class GraphicsLayerModifier(
    private val attributes: HashMap<Int, Any>,
) : RecordingModifier.Element {
    override fun write(writer: RemoteComposeWriter) {
        writer.addModifierGraphicsLayer(attributes)
    }
}

/** height in modifier */
class HeightInModifier(
    private val min: Float,
    private val max: Float,
) : RecordingModifier.Element {
    override fun write(writer: RemoteComposeWriter) {
        writer.addHeightInModifierOperation(min, max)
    }
}

/** height modifier */
class HeightModifier(
    private val type: Int,
    private val value: Float,
) : RecordingModifier.Element {
    override fun write(writer: RemoteComposeWriter) {
        writer.addHeightModifierOperation(type, value)
    }
}

/** marquee modifier */
class MarqueeModifier(
    private val iterations: Int,
    private val animationMode: Int,
    private val repeatDelayMillis: Float,
    private val initialDelayMillis: Float,
    private val spacing: Float,
    private val velocity: Float,
) : RecordingModifier.Element {
    override fun write(writer: RemoteComposeWriter) {
        writer.addModifierMarquee(
            iterations, animationMode,
            repeatDelayMillis, initialDelayMillis, spacing, velocity,
        )
    }
}

/** offset modifier */
class OffsetModifier(
    private val x: Float,
    private val y: Float,
) : RecordingModifier.Element {
    override fun write(writer: RemoteComposeWriter) {
        writer.addModifierOffset(x, y)
    }
}

/** padding modifier */
class PaddingModifier(
    private val left: Float,
    private val top: Float,
    private val right: Float,
    private val bottom: Float,
) : RecordingModifier.Element {
    constructor(all: Float) : this(all, all, all, all)
    constructor(horizontal: Float, vertical: Float) : this(
        horizontal, vertical, horizontal, vertical,
    )

    override fun write(writer: RemoteComposeWriter) {
        writer.addModifierPadding(left, top, right, bottom)
    }
}

/** rect shape */
class RectShape : Shape {
    override val type: Int get() = Shape.RECT
}

/** ripple modifier */
class RippleModifier : RecordingModifier.Element {
    override fun write(writer: RemoteComposeWriter) {
        writer.addModifierRipple()
    }
}

/** rounded rect shape */
class RoundedRectShape(
    val topStart: Float = 0f,
    val topEnd: Float = 0f,
    val bottomStart: Float = 0f,
    val bottomEnd: Float = 0f,
) : Shape {
    constructor(radius: Float) : this(radius, radius, radius, radius)

    override val type: Int get() = Shape.ROUNDED_RECT
}

/** scroll modifier */
class ScrollModifier(
    private val direction: Int,
    private val positionId: Float = Float.NaN,
    private val notches: Int = -1,
) : RecordingModifier.Element {
    override fun write(writer: RemoteComposeWriter) {
        if (positionId.isNaN()) {
            writer.addModifierScroll(direction)
        } else if (notches > 0) {
            writer.addModifierScroll(direction, positionId, notches)
        } else {
            writer.addModifierScroll(direction, positionId)
        }
    }
}

/** semantics modifier */
class SemanticsModifier(
    private val contentDescriptionId: Int = 0,
    private val role: Byte = 0,
    private val textId: Int = 0,
    private val stateDescriptionId: Int = 0,
    private val mode: Int = 0,
    private val enabled: Boolean = true,
    private val clickable: Boolean = false,
) : RecordingModifier.Element {
    override fun write(writer: RemoteComposeWriter) {
        writer.addSemanticsModifier(
            contentDescriptionId, role, textId,
            stateDescriptionId, mode, enabled, clickable,
        )
    }
}

/** shape interface */
interface Shape {
    val type: Int

    companion object {
        const val RECT: Int = 0
        const val ROUNDED_RECT: Int = 1
        const val CIRCLE: Int = 2
    }
}

/** solid background modifier */
class SolidBackgroundModifier(
    private val color: Int,
    private val shape: Int,
) : RecordingModifier.Element {
    override fun write(writer: RemoteComposeWriter) {
        writer.addModifierBackground(color, shape)
    }
}

/** touch action modifier */
class TouchActionModifier(
    private val actions: Array<out androidx.compose.remote.creation.actions.Action>,
) : RecordingModifier.Element {
    override fun write(writer: RemoteComposeWriter) {
        writer.addTouchDownModifierOperation()
        for (action in actions) {
            action.write(writer)
        }
        writer.addContainerEnd()
    }
}

/** unsupported modifier (placeholder) */
class UnsupportedModifier(private val name: String) : RecordingModifier.Element {
    override fun write(writer: RemoteComposeWriter) {
        // No-op: unsupported modifier
    }
}

/** visibility modifier */
class VisibilityModifier(
    private val valueId: Int,
) : RecordingModifier.Element {
    override fun write(writer: RemoteComposeWriter) {
        writer.addComponentVisibilityOperation(valueId)
    }
}

/** width in modifier */
class WidthInModifier(
    private val min: Float,
    private val max: Float,
) : RecordingModifier.Element {
    override fun write(writer: RemoteComposeWriter) {
        writer.addWidthInModifierOperation(min, max)
    }
}

/** width modifier */
class WidthModifier(
    private val type: Int,
    private val value: Float,
) : RecordingModifier.Element {
    override fun write(writer: RemoteComposeWriter) {
        writer.addWidthModifierOperation(type, value)
    }
}

/** z-index modifier */
class ZIndexModifier(
    private val value: Float,
) : RecordingModifier.Element {
    override fun write(writer: RemoteComposeWriter) {
        writer.addModifierZIndex(value)
    }
}
