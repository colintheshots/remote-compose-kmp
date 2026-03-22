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

import androidx.compose.remote.core.operations.*
import androidx.compose.remote.core.operations.layout.*
import androidx.compose.remote.core.operations.layout.animation.AnimationSpec
import androidx.compose.remote.core.operations.layout.managers.*
import androidx.compose.remote.core.operations.layout.modifiers.*
import androidx.compose.remote.core.operations.matrix.MatrixConstant
import androidx.compose.remote.core.operations.matrix.MatrixExpression
import androidx.compose.remote.core.operations.matrix.MatrixVectorMath
import androidx.compose.remote.core.operations.utilities.IntMap
import androidx.compose.remote.core.semantics.CoreSemantics
import androidx.compose.remote.core.types.BooleanConstant
import androidx.compose.remote.core.types.IntegerConstant
import androidx.compose.remote.core.types.LongConstant

/** List of operations supported in a RemoteCompose document */
object Operations {

    // Protocol
    const val HEADER = 0
    const val LOAD_BITMAP = 4
    const val THEME = 63
    const val CLICK_AREA = 64
    const val ROOT_CONTENT_BEHAVIOR = 65
    const val ROOT_CONTENT_DESCRIPTION = 103
    const val ACCESSIBILITY_SEMANTICS = 250

    const val EXTENDED_OPCODE = 255
    const val EXTENSION_RANGE_RESERVED_1 = 254
    const val EXTENSION_RANGE_RESERVED_2 = 253
    const val EXTENSION_RANGE_RESERVED_3 = 252
    const val EXTENSION_RANGE_RESERVED_4 = 251

    // Draw commands
    const val DRAW_BITMAP = 44
    const val DRAW_BITMAP_INT = 66
    const val DATA_BITMAP = 101
    const val DATA_SHADER = 45
    const val DATA_TEXT = 102
    const val DATA_BITMAP_FONT = 167

    const val CLIP_PATH = 38
    const val CLIP_RECT = 39
    const val PAINT_VALUES = 40
    const val DRAW_RECT = 42
    const val DRAW_BITMAP_FONT_TEXT_RUN = 48
    const val DRAW_BITMAP_FONT_TEXT_RUN_ON_PATH = 49
    const val DRAW_TEXT_RUN = 43
    const val DRAW_CIRCLE = 46
    const val DRAW_LINE = 47
    const val DRAW_ROUND_RECT = 51
    const val DRAW_SECTOR = 52
    const val DRAW_TEXT_ON_PATH = 53
    const val DRAW_TEXT_ON_CIRCLE = 57
    const val DRAW_OVAL = 56
    const val DATA_PATH = 123
    const val DRAW_PATH = 124
    const val DRAW_TWEEN_PATH = 125
    const val DRAW_CONTENT = 139
    const val MATRIX_SCALE = 126
    const val MATRIX_TRANSLATE = 127
    const val MATRIX_SKEW = 128
    const val MATRIX_ROTATE = 129
    const val MATRIX_SAVE = 130
    const val MATRIX_RESTORE = 131
    const val MATRIX_SET = 132
    const val DATA_FLOAT = 80
    const val ANIMATED_FLOAT = 81
    const val DRAW_TEXT_ANCHOR = 133
    const val COLOR_EXPRESSIONS = 134
    const val TEXT_FROM_FLOAT = 135
    const val TEXT_MERGE = 136
    const val NAMED_VARIABLE = 137
    const val COLOR_CONSTANT = 138
    const val DATA_INT = 140
    const val DATA_BOOLEAN = 143
    const val INTEGER_EXPRESSION = 144
    const val ID_MAP = 145
    const val ID_LIST = 146
    const val FLOAT_LIST = 147
    const val DATA_LONG = 148
    const val DYNAMIC_FLOAT_LIST = 197
    const val UPDATE_DYNAMIC_FLOAT_LIST = 198
    const val DRAW_BITMAP_SCALED = 149
    const val TEXT_LOOKUP = 151
    const val DRAW_ARC = 152
    const val TEXT_LOOKUP_INT = 153
    const val DATA_MAP_LOOKUP = 154
    const val TEXT_MEASURE = 155
    const val TEXT_LENGTH = 156
    const val TOUCH_EXPRESSION = 157
    const val PATH_TWEEN = 158
    const val PATH_CREATE = 159
    const val PATH_ADD = 160
    const val PARTICLE_DEFINE = 161
    const val PARTICLE_PROCESS = 162
    const val PARTICLE_LOOP = 163
    const val IMPULSE_START = 164
    const val IMPULSE_PROCESS = 165
    const val FUNCTION_CALL = 166
    const val FUNCTION_DEFINE = 168
    const val ATTRIBUTE_TEXT = 170
    const val ATTRIBUTE_IMAGE = 171
    const val ATTRIBUTE_TIME = 172
    const val CANVAS_OPERATIONS = 173
    const val MODIFIER_DRAW_CONTENT = 174
    const val PATH_COMBINE = 175
    const val HAPTIC_FEEDBACK = 177
    const val CONDITIONAL_OPERATIONS = 178
    const val DEBUG_MESSAGE = 179
    const val ATTRIBUTE_COLOR = 180
    const val MATRIX_FROM_PATH = 181
    const val TEXT_SUBTEXT = 182
    const val BITMAP_TEXT_MEASURE = 183
    const val DRAW_BITMAP_TEXT_ANCHORED = 184
    const val REM = 185
    const val MATRIX_CONSTANT = 186
    const val MATRIX_EXPRESSION = 187
    const val MATRIX_VECTOR_MATH = 188
    const val DATA_FONT = 189
    const val DRAW_TO_BITMAP = 190
    const val WAKE_IN = 191
    const val ID_LOOKUP = 192
    const val PATH_EXPRESSION = 193
    const val PARTICLE_COMPARE = 194

    // Layout commands
    const val LAYOUT_ROOT = 200
    const val LAYOUT_CONTENT = 201
    const val LAYOUT_BOX = 202
    const val LAYOUT_FIT_BOX = 176
    const val LAYOUT_ROW = 203
    const val LAYOUT_COLLAPSIBLE_ROW = 230
    const val LAYOUT_COLUMN = 204
    const val LAYOUT_COLLAPSIBLE_COLUMN = 233
    const val LAYOUT_CANVAS = 205
    const val LAYOUT_CANVAS_CONTENT = 207
    const val LAYOUT_TEXT = 208
    const val CORE_TEXT = 239
    const val LAYOUT_STATE = 217
    const val LAYOUT_IMAGE = 234

    const val COMPONENT_START = 2
    const val MODIFIER_WIDTH = 16
    const val MODIFIER_HEIGHT = 67
    const val MODIFIER_WIDTH_IN = 231
    const val MODIFIER_HEIGHT_IN = 232
    const val MODIFIER_COLLAPSIBLE_PRIORITY = 235
    const val MODIFIER_BACKGROUND = 55
    const val MODIFIER_BORDER = 107
    const val MODIFIER_PADDING = 58
    const val MODIFIER_CLIP_RECT = 108
    const val MODIFIER_ROUNDED_CLIP_RECT = 54
    const val MODIFIER_CLICK = 59
    const val MODIFIER_TOUCH_DOWN = 219
    const val MODIFIER_TOUCH_UP = 220
    const val MODIFIER_TOUCH_CANCEL = 225
    const val CONTAINER_END = 214
    const val MODIFIER_OFFSET = 221
    const val MODIFIER_ZINDEX = 223
    const val MODIFIER_GRAPHICS_LAYER = 224
    const val MODIFIER_SCROLL = 226
    const val MODIFIER_MARQUEE = 228
    const val MODIFIER_RIPPLE = 229
    const val MODIFIER_ALIGN_BY = 237
    const val MODIFIER_CLIP_ROUNDED_RECT = MODIFIER_ROUNDED_CLIP_RECT
    const val MODIFIER_Z_INDEX = MODIFIER_ZINDEX
    const val MODIFIER_VALUE_STRING_CHANGE = 238
    const val MODIFIER_VALUE_INTEGER_CHANGE = 239
    const val MODIFIER_VALUE_FLOAT_CHANGE = 240
    const val MODIFIER_VALUE_INTEGER_EXPRESSION_CHANGE = 241
    const val MODIFIER_VALUE_FLOAT_EXPRESSION_CHANGE = 242
    const val MODIFIER_RUN_ACTION = 243
    const val MODIFIER_HOST_ACTION = 244
    const val MODIFIER_HOST_ACTION_METADATA = 245
    const val MODIFIER_HOST_NAMED_ACTION = 246
    const val MODIFIER_LAYOUT_COMPUTE = 247
    const val LOOP_START = 215
    const val MODIFIER_VISIBILITY = 211
    const val HOST_ACTION = 209
    const val HOST_METADATA_ACTION = 216
    const val HOST_NAMED_ACTION = 210
    const val RUN_ACTION = 236
    const val LAYOUT_COMPUTE = 238
    const val VALUE_INTEGER_CHANGE_ACTION = 212
    const val VALUE_STRING_CHANGE_ACTION = 213
    const val VALUE_INTEGER_EXPRESSION_CHANGE_ACTION = 218
    const val VALUE_FLOAT_CHANGE_ACTION = 222
    const val VALUE_FLOAT_EXPRESSION_CHANGE_ACTION = 227
    const val ANIMATION_SPEC = 14
    const val COMPONENT_VALUE = 150

    // Profiles management
    private var sMapV6: UniqueIntMap<CompanionOperation>? = null
    private var sMapV7: MutableMap<Int, UniqueIntMap<CompanionOperation>>? = null

    fun valid(opId: Int, apiLevel: Int, profiles: Int): Boolean {
        val map = getOperations(apiLevel, profiles) ?: return false
        return map.get(opId) != null
    }

    fun getOperations(apiLevel: Int, profiles: Int): UniqueIntMap<CompanionOperation>? {
        return when (apiLevel) {
            7 -> {
                if (sMapV7 == null || !sMapV7!!.containsKey(profiles)) {
                    sMapV7 = createMapV7(sMapV7, profiles)
                }
                sMapV7!![profiles]
            }
            6 -> {
                if (sMapV6 == null) sMapV6 = createMapV6()
                sMapV6
            }
            else -> null
        }
    }

    private fun createMapV6(): UniqueIntMap<CompanionOperation> {
        val map = UniqueIntMap<CompanionOperation>()
        fillDefaultVersionMap(map)
        map.put(DATA_SHADER, ShaderData::read)
        map.put(ROOT_CONTENT_BEHAVIOR, RootContentBehavior::read)
        return map
    }

    private fun createMapV7(
        currentMapV7: MutableMap<Int, UniqueIntMap<CompanionOperation>>?,
        profiles: Int
    ): MutableMap<Int, UniqueIntMap<CompanionOperation>> {
        val mapV7 = UniqueIntMap<CompanionOperation>()
        fillDefaultVersionMap(mapV7)

        if (profiles != 0) {
            val listProfiles = mutableListOf<UniqueIntMap<CompanionOperation>>()

            if ((profiles and RcProfiles.PROFILE_ANDROIDX) != 0) {
                val androidx = UniqueIntMap<CompanionOperation>()
                androidx.putAll(createMapV7AndroidX())
                if ((profiles and RcProfiles.PROFILE_EXPERIMENTAL) != 0) {
                    androidx.putAll(createMapV7AndroidXExperimental())
                }
                if ((profiles and RcProfiles.PROFILE_DEPRECATED) != 0) {
                    androidx.putAll(createMapV7AndroidXDeprecated())
                }
                listProfiles.add(androidx)
            }
            if ((profiles and RcProfiles.PROFILE_WIDGETS) != 0) {
                val widgets = UniqueIntMap<CompanionOperation>()
                widgets.putAll(createMapV7Widgets())
                if ((profiles and RcProfiles.PROFILE_EXPERIMENTAL) != 0) {
                    widgets.putAll(createMapV7WidgetsExperimental())
                }
                if ((profiles and RcProfiles.PROFILE_DEPRECATED) != 0) {
                    widgets.putAll(createMapV7WidgetsDeprecated())
                }
                listProfiles.add(widgets)
            }
            if ((profiles and RcProfiles.PROFILE_ANDROID_NATIVE) != 0) {
                throw UnsupportedOperationException("Android native profiles are defined externally")
            }

            if (listProfiles.size == 1) {
                mapV7.putAll(listProfiles[0])
            } else if (listProfiles.size > 1) {
                val intersection = mutableMapOf<Int, Int>()
                for (profile in listProfiles) {
                    for (i in 0 until 255) {
                        if (profile.get(i) != null) {
                            intersection[i] = (intersection[i] ?: 0) + 1
                        }
                    }
                }
                if (intersection.isNotEmpty()) {
                    val max = listProfiles.size
                    val profile = listProfiles[0]
                    for ((key, count) in intersection) {
                        if (count == max) {
                            val op = profile.get(key)
                            if (op != null) mapV7.put(key, op)
                        }
                    }
                }
            }
        }

        // Baseline v7 operations
        mapV7.put(REM, Rem::read)
        mapV7.put(MATRIX_CONSTANT, MatrixConstant::read)
        mapV7.put(MATRIX_EXPRESSION, MatrixExpression::read)
        mapV7.put(MATRIX_VECTOR_MATH, MatrixVectorMath::read)

        val result = currentMapV7?.toMutableMap() ?: mutableMapOf()
        result[profiles] = mapV7
        return result
    }

    private fun createMapV7AndroidX(): UniqueIntMap<CompanionOperation> {
        val map = UniqueIntMap<CompanionOperation>()
        map.put(MATRIX_FROM_PATH, MatrixFromPath::read)
        map.put(TEXT_SUBTEXT, TextSubtext::read)
        map.put(BITMAP_TEXT_MEASURE, BitmapTextMeasure::read)
        map.put(DRAW_BITMAP_FONT_TEXT_RUN_ON_PATH, DrawBitmapFontTextOnPath::read)
        map.put(DRAW_BITMAP_TEXT_ANCHORED, DrawBitmapTextAnchored::read)
        map.put(DATA_SHADER, ShaderData::read)
        map.put(DATA_FONT, FontData::read)
        map.put(DRAW_TO_BITMAP, DrawToBitmap::read)
        map.put(WAKE_IN, WakeIn::read)
        map.put(ID_LOOKUP, IdLookup::read)
        map.put(PATH_EXPRESSION, PathExpression::read)
        map.put(PARTICLE_COMPARE, ParticlesCompare::read)
        map.put(DYNAMIC_FLOAT_LIST, DataDynamicListFloat::read)
        map.put(UPDATE_DYNAMIC_FLOAT_LIST, UpdateDynamicFloatList::read)
        return map
    }

    private fun createMapV7AndroidXExperimental(): UniqueIntMap<CompanionOperation> {
        val map = UniqueIntMap<CompanionOperation>()
        map.put(MODIFIER_ALIGN_BY, AlignByModifierOperation::read)
        map.put(LAYOUT_COMPUTE, LayoutComputeOperation::read)
        map.put(CORE_TEXT, CoreText::read)
        return map
    }

    private fun createMapV7AndroidXDeprecated(): UniqueIntMap<CompanionOperation> {
        val map = UniqueIntMap<CompanionOperation>()
        map.put(ROOT_CONTENT_BEHAVIOR, RootContentBehavior::read)
        return map
    }

    private fun createMapV7Widgets(): UniqueIntMap<CompanionOperation> {
        val map = UniqueIntMap<CompanionOperation>()
        map.put(MATRIX_FROM_PATH, MatrixFromPath::read)
        map.put(TEXT_SUBTEXT, TextSubtext::read)
        map.put(BITMAP_TEXT_MEASURE, BitmapTextMeasure::read)
        map.put(DRAW_BITMAP_FONT_TEXT_RUN_ON_PATH, DrawBitmapFontTextOnPath::read)
        map.put(DRAW_BITMAP_TEXT_ANCHORED, DrawBitmapTextAnchored::read)
        map.put(DRAW_TO_BITMAP, DrawToBitmap::read)
        map.put(WAKE_IN, WakeIn::read)
        map.put(ID_LOOKUP, IdLookup::read)
        map.put(PATH_EXPRESSION, PathExpression::read)
        map.put(PARTICLE_COMPARE, ParticlesCompare::read)
        map.put(DYNAMIC_FLOAT_LIST, DataDynamicListFloat::read)
        map.put(UPDATE_DYNAMIC_FLOAT_LIST, UpdateDynamicFloatList::read)
        return map
    }

    private fun createMapV7WidgetsExperimental(): UniqueIntMap<CompanionOperation> {
        val map = UniqueIntMap<CompanionOperation>()
        map.put(MODIFIER_ALIGN_BY, AlignByModifierOperation::read)
        map.put(LAYOUT_COMPUTE, LayoutComputeOperation::read)
        map.put(CORE_TEXT, CoreText::read)
        return map
    }

    private fun createMapV7WidgetsDeprecated(): UniqueIntMap<CompanionOperation> {
        val map = UniqueIntMap<CompanionOperation>()
        map.put(ROOT_CONTENT_BEHAVIOR, RootContentBehavior::read)
        return map
    }

    class UniqueIntMap<T> : IntMap<T>() {
        override fun put(key: Int, value: T): T? {
            check(get(key) == null) { "Opcode $key already used in Operations!" }
            return super.put(key, value)
        }
    }

    private fun fillDefaultVersionMap(map: UniqueIntMap<CompanionOperation>) {
        map.put(HEADER, Header::read)
        map.put(DRAW_BITMAP_INT, DrawBitmapInt::read)
        map.put(DATA_BITMAP, BitmapData::read)
        map.put(DATA_BITMAP_FONT, BitmapFontData::read)
        map.put(DATA_TEXT, TextData::read)
        map.put(THEME, Theme::read)
        map.put(CLICK_AREA, ClickArea::read)
        map.put(ROOT_CONTENT_DESCRIPTION, RootContentDescription::read)
        map.put(DRAW_SECTOR, DrawSector::read)
        map.put(DRAW_BITMAP, DrawBitmap::read)
        map.put(DRAW_CIRCLE, DrawCircle::read)
        map.put(DRAW_LINE, DrawLine::read)
        map.put(DRAW_OVAL, DrawOval::read)
        map.put(DRAW_PATH, DrawPath::read)
        map.put(DRAW_RECT, DrawRect::read)
        map.put(DRAW_ROUND_RECT, DrawRoundRect::read)
        map.put(DRAW_TEXT_ON_PATH, DrawTextOnPath::read)
        map.put(DRAW_TEXT_RUN, DrawText::read)
        map.put(DRAW_BITMAP_FONT_TEXT_RUN, DrawBitmapFontText::read)
        map.put(DRAW_TWEEN_PATH, DrawTweenPath::read)
        map.put(DATA_PATH, PathData::read)
        map.put(PAINT_VALUES, PaintData::read)
        map.put(MATRIX_RESTORE, MatrixRestore::read)
        map.put(MATRIX_ROTATE, MatrixRotate::read)
        map.put(MATRIX_SAVE, MatrixSave::read)
        map.put(MATRIX_SCALE, MatrixScale::read)
        map.put(MATRIX_SKEW, MatrixSkew::read)
        map.put(MATRIX_TRANSLATE, MatrixTranslate::read)
        map.put(CLIP_PATH, ClipPath::read)
        map.put(CLIP_RECT, ClipRect::read)
        map.put(DATA_FLOAT, FloatConstant::read)
        map.put(ANIMATED_FLOAT, FloatExpression::read)
        map.put(DRAW_TEXT_ANCHOR, DrawTextAnchored::read)
        map.put(COLOR_EXPRESSIONS, ColorExpression::read)
        map.put(TEXT_FROM_FLOAT, TextFromFloat::read)
        map.put(TEXT_MERGE, TextMerge::read)
        map.put(NAMED_VARIABLE, NamedVariable::read)
        map.put(COLOR_CONSTANT, ColorConstant::read)
        map.put(DATA_INT, IntegerConstant::read)
        map.put(INTEGER_EXPRESSION, IntegerExpression::read)
        map.put(DATA_BOOLEAN, BooleanConstant::read)
        map.put(ID_MAP, DataMapIds::read)
        map.put(ID_LIST, DataListIds::read)
        map.put(FLOAT_LIST, DataListFloat::read)
        map.put(DATA_LONG, LongConstant::read)
        map.put(DRAW_BITMAP_SCALED, DrawBitmapScaled::read)
        map.put(TEXT_LOOKUP, TextLookup::read)
        map.put(TEXT_LOOKUP_INT, TextLookupInt::read)
        map.put(LOOP_START, LoopOperation::read)
        map.put(COMPONENT_START, ComponentStart::read)
        map.put(ANIMATION_SPEC, AnimationSpec::read)
        map.put(MODIFIER_WIDTH, WidthModifierOperation::read)
        map.put(MODIFIER_HEIGHT, HeightModifierOperation::read)
        map.put(MODIFIER_WIDTH_IN, WidthInModifierOperation::read)
        map.put(MODIFIER_HEIGHT_IN, HeightInModifierOperation::read)
        map.put(MODIFIER_COLLAPSIBLE_PRIORITY, CollapsiblePriorityModifierOperation::read)
        map.put(MODIFIER_PADDING, PaddingModifierOperation::read)
        map.put(MODIFIER_BACKGROUND, BackgroundModifierOperation::read)
        map.put(MODIFIER_BORDER, BorderModifierOperation::read)
        map.put(MODIFIER_ROUNDED_CLIP_RECT, RoundedClipRectModifierOperation::read)
        map.put(MODIFIER_CLIP_RECT, ClipRectModifierOperation::read)
        map.put(MODIFIER_CLICK, ClickModifierOperation::read)
        map.put(MODIFIER_TOUCH_DOWN, TouchDownModifierOperation::read)
        map.put(MODIFIER_TOUCH_UP, TouchUpModifierOperation::read)
        map.put(MODIFIER_TOUCH_CANCEL, TouchCancelModifierOperation::read)
        map.put(MODIFIER_VISIBILITY, ComponentVisibilityOperation::read)
        map.put(MODIFIER_OFFSET, OffsetModifierOperation::read)
        map.put(MODIFIER_ZINDEX, ZIndexModifierOperation::read)
        map.put(MODIFIER_GRAPHICS_LAYER, GraphicsLayerModifierOperation::read)
        map.put(MODIFIER_SCROLL, ScrollModifierOperation::read)
        map.put(MODIFIER_MARQUEE, MarqueeModifierOperation::read)
        map.put(MODIFIER_RIPPLE, RippleModifierOperation::read)
        map.put(MODIFIER_DRAW_CONTENT, DrawContentOperation::read)
        map.put(CONTAINER_END, ContainerEnd::read)
        map.put(RUN_ACTION, RunActionOperation::read)
        map.put(HOST_ACTION, HostActionOperation::read)
        map.put(HOST_METADATA_ACTION, HostActionMetadataOperation::read)
        map.put(HOST_NAMED_ACTION, HostNamedActionOperation::read)
        map.put(VALUE_INTEGER_CHANGE_ACTION, ValueIntegerChangeActionOperation::read)
        map.put(VALUE_INTEGER_EXPRESSION_CHANGE_ACTION, ValueIntegerExpressionChangeActionOperation::read)
        map.put(VALUE_STRING_CHANGE_ACTION, ValueStringChangeActionOperation::read)
        map.put(VALUE_FLOAT_CHANGE_ACTION, ValueFloatChangeActionOperation::read)
        map.put(VALUE_FLOAT_EXPRESSION_CHANGE_ACTION, ValueFloatExpressionChangeActionOperation::read)
        map.put(LAYOUT_ROOT, RootLayoutComponent::read)
        map.put(LAYOUT_CONTENT, LayoutComponentContent::read)
        map.put(LAYOUT_BOX, BoxLayout::read)
        map.put(LAYOUT_FIT_BOX, FitBoxLayout::read)
        map.put(LAYOUT_COLUMN, ColumnLayout::read)
        map.put(LAYOUT_COLLAPSIBLE_COLUMN, CollapsibleColumnLayout::read)
        map.put(LAYOUT_ROW, RowLayout::read)
        map.put(LAYOUT_COLLAPSIBLE_ROW, CollapsibleRowLayout::read)
        map.put(LAYOUT_CANVAS, CanvasLayout::read)
        map.put(LAYOUT_CANVAS_CONTENT, CanvasContent::read)
        map.put(LAYOUT_TEXT, TextLayout::read)
        map.put(LAYOUT_IMAGE, ImageLayout::read)
        map.put(LAYOUT_STATE, StateLayout::read)
        map.put(DRAW_CONTENT, DrawContent::read)
        map.put(COMPONENT_VALUE, ComponentValue::read)
        map.put(DRAW_ARC, DrawArc::read)
        map.put(DATA_MAP_LOOKUP, DataMapLookup::read)
        map.put(TEXT_MEASURE, TextMeasure::read)
        map.put(TEXT_LENGTH, TextLength::read)
        map.put(TOUCH_EXPRESSION, TouchExpression::read)
        map.put(PATH_TWEEN, PathTween::read)
        map.put(PATH_CREATE, PathCreate::read)
        map.put(PATH_ADD, PathAppend::read)
        map.put(IMPULSE_START, ImpulseOperation::read)
        map.put(IMPULSE_PROCESS, ImpulseProcess::read)
        map.put(PARTICLE_DEFINE, ParticlesCreate::read)
        map.put(PARTICLE_LOOP, ParticlesLoop::read)
        map.put(FUNCTION_CALL, FloatFunctionCall::read)
        map.put(FUNCTION_DEFINE, FloatFunctionDefine::read)
        map.put(CANVAS_OPERATIONS, CanvasOperations::read)
        map.put(ACCESSIBILITY_SEMANTICS, CoreSemantics::read)
        map.put(ATTRIBUTE_IMAGE, ImageAttribute::read)
        map.put(ATTRIBUTE_TEXT, TextAttribute::read)
        map.put(ATTRIBUTE_TIME, TimeAttribute::read)
        map.put(PATH_COMBINE, PathCombine::read)
        map.put(HAPTIC_FEEDBACK, HapticFeedback::read)
        map.put(CONDITIONAL_OPERATIONS, ConditionalOperations::read)
        map.put(DEBUG_MESSAGE, DebugMessage::read)
        map.put(ATTRIBUTE_COLOR, ColorAttribute::read)
    }
}
