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
package androidx.compose.remote.creation

import androidx.compose.remote.core.PaintOperation
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.operations.ColorAttribute.Companion.COLOR_ALPHA
import androidx.compose.remote.core.operations.ColorAttribute.Companion.COLOR_BLUE
import androidx.compose.remote.core.operations.ColorAttribute.Companion.COLOR_BRIGHTNESS
import androidx.compose.remote.core.operations.ColorAttribute.Companion.COLOR_GREEN
import androidx.compose.remote.core.operations.ColorAttribute.Companion.COLOR_HUE
import androidx.compose.remote.core.operations.ColorAttribute.Companion.COLOR_RED
import androidx.compose.remote.core.operations.ColorAttribute.Companion.COLOR_SATURATION
import androidx.compose.remote.core.operations.ConditionalOperations
import androidx.compose.remote.core.operations.DebugMessage
import androidx.compose.remote.core.operations.DrawTextAnchored
import androidx.compose.remote.core.operations.Header
import androidx.compose.remote.core.operations.PathExpression.Companion.LINEAR
import androidx.compose.remote.core.operations.PathExpression.Companion.LOOP
import androidx.compose.remote.core.operations.PathExpression.Companion.MONOTONIC
import androidx.compose.remote.core.operations.PathExpression.Companion.POLAR
import androidx.compose.remote.core.operations.TimeAttribute
import androidx.compose.remote.core.operations.TouchExpression
import androidx.compose.remote.core.operations.layout.managers.CoreText
import androidx.compose.remote.core.operations.utilities.AnimatedFloatExpression
import androidx.compose.remote.core.operations.utilities.ImageScaling
import androidx.compose.remote.core.operations.utilities.IntegerExpressionEvaluator
import androidx.compose.remote.core.operations.utilities.easing.Easing

/** Constants used in RemoteCompose */
class Rc {
    /** Used in FloatExpressions */
    object FloatExpression {
        val ADD: Float = AnimatedFloatExpression.ADD
        val SUB: Float = AnimatedFloatExpression.SUB
        val MUL: Float = AnimatedFloatExpression.MUL
        val DIV: Float = AnimatedFloatExpression.DIV
        val MOD: Float = AnimatedFloatExpression.MOD
        val MIN: Float = AnimatedFloatExpression.MIN
        val MAX: Float = AnimatedFloatExpression.MAX
        val POW: Float = AnimatedFloatExpression.POW
        val SQRT: Float = AnimatedFloatExpression.SQRT
        val ABS: Float = AnimatedFloatExpression.ABS
        val SIGN: Float = AnimatedFloatExpression.SIGN
        val COPY_SIGN: Float = AnimatedFloatExpression.COPY_SIGN
        val EXP: Float = AnimatedFloatExpression.EXP
        val FLOOR: Float = AnimatedFloatExpression.FLOOR
        val LOG: Float = AnimatedFloatExpression.LOG
        val LN: Float = AnimatedFloatExpression.LN
        val ROUND: Float = AnimatedFloatExpression.ROUND
        val SIN: Float = AnimatedFloatExpression.SIN
        val COS: Float = AnimatedFloatExpression.COS
        val TAN: Float = AnimatedFloatExpression.TAN
        val ASIN: Float = AnimatedFloatExpression.ASIN
        val ACOS: Float = AnimatedFloatExpression.ACOS
        val ATAN: Float = AnimatedFloatExpression.ATAN
        val ATAN2: Float = AnimatedFloatExpression.ATAN2
        val MAD: Float = AnimatedFloatExpression.MAD
        val IFELSE: Float = AnimatedFloatExpression.IFELSE
        val CLAMP: Float = AnimatedFloatExpression.CLAMP
        val CBRT: Float = AnimatedFloatExpression.CBRT
        val DEG: Float = AnimatedFloatExpression.DEG
        val RAD: Float = AnimatedFloatExpression.RAD
        val CEIL: Float = AnimatedFloatExpression.CEIL
        val A_DEREF: Float = AnimatedFloatExpression.A_DEREF
        val A_MAX: Float = AnimatedFloatExpression.A_MAX
        val A_MIN: Float = AnimatedFloatExpression.A_MIN
        val A_SUM: Float = AnimatedFloatExpression.A_SUM
        val A_AVG: Float = AnimatedFloatExpression.A_AVG
        val A_LEN: Float = AnimatedFloatExpression.A_LEN
        val A_SPLINE: Float = AnimatedFloatExpression.A_SPLINE
        val RAND: Float = AnimatedFloatExpression.RAND
        val RAND_SEED: Float = AnimatedFloatExpression.RAND_SEED
        val NOISE_FROM: Float = AnimatedFloatExpression.NOISE_FROM
        val RAND_IN_RANGE: Float = AnimatedFloatExpression.RAND_IN_RANGE
        val SQUARE_SUM: Float = AnimatedFloatExpression.SQUARE_SUM
        val STEP: Float = AnimatedFloatExpression.STEP
        val SQUARE: Float = AnimatedFloatExpression.SQUARE
        val DUP: Float = AnimatedFloatExpression.DUP
        val HYPOT: Float = AnimatedFloatExpression.HYPOT
        val SWAP: Float = AnimatedFloatExpression.SWAP
        val LERP: Float = AnimatedFloatExpression.LERP
        val SMOOTH_STEP: Float = AnimatedFloatExpression.SMOOTH_STEP
        val LOG2: Float = AnimatedFloatExpression.LOG2
        val INV: Float = AnimatedFloatExpression.INV
        val FRACT: Float = AnimatedFloatExpression.FRACT
        val PINGPONG: Float = AnimatedFloatExpression.PINGPONG
        val VAR1: Float = AnimatedFloatExpression.VAR1
        val VAR2: Float = AnimatedFloatExpression.VAR2
        val VAR3: Float = AnimatedFloatExpression.VAR3
        val CUBIC: Float = AnimatedFloatExpression.CUBIC
        val A_SPLINE_LOOP: Float = AnimatedFloatExpression.A_SPLINE_LOOP
        val CHANGE_SIGN: Float = AnimatedFloatExpression.CHANGE_SIGN
    }

    /** Used in IntegerExpressions */
    object IntegerExpression {
        val L_ADD: Long = 0x100000000L + IntegerExpressionEvaluator.I_ADD
        val L_SUB: Long = 0x100000000L + IntegerExpressionEvaluator.I_SUB
        val L_MUL: Long = 0x100000000L + IntegerExpressionEvaluator.I_MUL
        val L_DIV: Long = 0x100000000L + IntegerExpressionEvaluator.I_DIV
        val L_MOD: Long = 0x100000000L + IntegerExpressionEvaluator.I_MOD
        val L_SHL: Long = 0x100000000L + IntegerExpressionEvaluator.I_SHL
        val L_SHR: Long = 0x100000000L + IntegerExpressionEvaluator.I_SHR
        val L_USHR: Long = 0x100000000L + IntegerExpressionEvaluator.I_USHR
        val L_OR: Long = 0x100000000L + IntegerExpressionEvaluator.I_OR
        val L_AND: Long = 0x100000000L + IntegerExpressionEvaluator.I_AND
        val L_XOR: Long = 0x100000000L + IntegerExpressionEvaluator.I_XOR
        val L_COPY_SIGN: Long = 0x100000000L + IntegerExpressionEvaluator.I_COPY_SIGN
        val L_MIN: Long = 0x100000000L + IntegerExpressionEvaluator.I_MIN
        val L_MAX: Long = 0x100000000L + IntegerExpressionEvaluator.I_MAX
        val L_NEG: Long = 0x100000000L + IntegerExpressionEvaluator.I_NEG
        val L_ABS: Long = 0x100000000L + IntegerExpressionEvaluator.I_ABS
        val L_INCR: Long = 0x100000000L + IntegerExpressionEvaluator.I_INCR
        val L_DECR: Long = 0x100000000L + IntegerExpressionEvaluator.I_DECR
        val L_NOT: Long = 0x100000000L + IntegerExpressionEvaluator.I_NOT
        val L_SIGN: Long = 0x100000000L + IntegerExpressionEvaluator.I_SIGN
        val L_CLAMP: Long = 0x100000000L + IntegerExpressionEvaluator.I_CLAMP
        val L_IFELSE: Long = 0x100000000L + IntegerExpressionEvaluator.I_IFELSE
        val L_MAD: Long = 0x100000000L + IntegerExpressionEvaluator.I_MAD
        val L_VAR1: Long = 0x100000000L + IntegerExpressionEvaluator.I_VAR1
        val L_VAR2: Long = 0x100000000L + IntegerExpressionEvaluator.I_VAR2
    }

    /** Used in Easing */
    object Animate {
        val CUBIC_STANDARD: Int = Easing.CUBIC_STANDARD
        val CUBIC_ACCELERATE: Int = Easing.CUBIC_ACCELERATE
        val CUBIC_DECELERATE: Int = Easing.CUBIC_DECELERATE
        val CUBIC_LINEAR: Int = Easing.CUBIC_LINEAR
        val CUBIC_ANTICIPATE: Int = Easing.CUBIC_ANTICIPATE
        val CUBIC_OVERSHOOT: Int = Easing.CUBIC_OVERSHOOT
        val CUBIC_CUSTOM: Int = Easing.CUBIC_CUSTOM
        val SPLINE_CUSTOM: Int = Easing.SPLINE_CUSTOM
        val EASE_OUT_BOUNCE: Int = Easing.EASE_OUT_BOUNCE
        val EASE_OUT_ELASTIC: Int = Easing.EASE_OUT_ELASTIC
    }

    /** Used in ColorExpression */
    object ColorExpression {
        val COLOR_COLOR_INTERPOLATE: Byte =
            androidx.compose.remote.core.operations.ColorExpression.COLOR_COLOR_INTERPOLATE
        val ID_COLOR_INTERPOLATE: Byte =
            androidx.compose.remote.core.operations.ColorExpression.ID_COLOR_INTERPOLATE
        val COLOR_ID_INTERPOLATE: Byte =
            androidx.compose.remote.core.operations.ColorExpression.COLOR_ID_INTERPOLATE
        val ID_ID_INTERPOLATE: Byte =
            androidx.compose.remote.core.operations.ColorExpression.ID_ID_INTERPOLATE
        val HSV_MODE: Byte =
            androidx.compose.remote.core.operations.ColorExpression.HSV_MODE
        val ARGB_MODE: Byte =
            androidx.compose.remote.core.operations.ColorExpression.ARGB_MODE
        val IDARGB_MODE: Byte =
            androidx.compose.remote.core.operations.ColorExpression.IDARGB_MODE
    }

    /** Used in ImageScale */
    object ImageScale {
        val NONE: Int = ImageScaling.SCALE_NONE
        val INSIDE: Int = ImageScaling.SCALE_INSIDE
        val FILL_WIDTH: Int = ImageScaling.SCALE_FILL_WIDTH
        val FILL_HEIGHT: Int = ImageScaling.SCALE_FILL_HEIGHT
        val FIT: Int = ImageScaling.SCALE_FIT
        val CROP: Int = ImageScaling.SCALE_CROP
        val FILL_BOUNDS: Int = ImageScaling.SCALE_FILL_BOUNDS
        val FIXED_SCALE: Int = ImageScaling.SCALE_FIXED_SCALE
    }

    /** Used in TextAnchorMask */
    object TextAnchorMask {
        val TEXT_RTL: Int = DrawTextAnchored.ANCHOR_TEXT_RTL
        val MONOSPACE_MEASURE: Int = DrawTextAnchored.ANCHOR_MONOSPACE_MEASURE
        val MEASURE_EVERY_TIME: Int = DrawTextAnchored.MEASURE_EVERY_TIME
        val BASELINE_RELATIVE: Int = DrawTextAnchored.BASELINE_RELATIVE
    }

    /** Used in Haptic */
    object Haptic {
        const val NO_HAPTICS: Int = 0
        const val LONG_PRESS: Int = 1
        const val VIRTUAL_KEY: Int = 2
        const val KEYBOARD_TAP: Int = 3
        const val CLOCK_TICK: Int = 4
        const val CONTEXT_CLICK: Int = 5
        const val KEYBOARD_PRESS: Int = 6
        const val KEYBOARD_RELEASE: Int = 7
        const val VIRTUAL_KEY_RELEASE: Int = 8
        const val TEXT_HANDLE_MOVE: Int = 9
        const val GESTURE_START: Int = 10
        const val GESTURE_END: Int = 11
        const val CONFIRM: Int = 12
        const val REJECT: Int = 13
        const val TOGGLE_ON: Int = 14
        const val TOGGLE_OFF: Int = 15
        const val GESTURE_THRESHOLD_ACTIVATE: Int = 16
        const val GESTURE_THRESHOLD_DEACTIVATE: Int = 17
        const val DRAG_START: Int = 18
        const val SEGMENT_TICK: Int = 19
        const val SEGMENT_FREQUENT_TICK: Int = 20
    }

    /** Used for Time variables */
    object Time {
        val CONTINUOUS_SEC: Float = RemoteContext.FLOAT_CONTINUOUS_SEC
        val TIME_IN_SEC: Float = RemoteContext.FLOAT_TIME_IN_SEC
        val TIME_IN_MIN: Float = RemoteContext.FLOAT_TIME_IN_MIN
        val TIME_IN_HR: Float = RemoteContext.FLOAT_TIME_IN_HR
        val CALENDAR_MONTH: Float = RemoteContext.FLOAT_CALENDAR_MONTH
        val WEEK_DAY: Float = RemoteContext.FLOAT_WEEK_DAY
        val DAY_OF_MONTH: Float = RemoteContext.FLOAT_DAY_OF_MONTH
        val OFFSET_TO_UTC: Float = RemoteContext.FLOAT_OFFSET_TO_UTC
        val ANIMATION_TIME: Float = RemoteContext.FLOAT_ANIMATION_TIME
        val ANIMATION_DELTA_TIME: Float = RemoteContext.FLOAT_ANIMATION_DELTA_TIME
        val INT_EPOCH_SECOND: Long = RemoteContext.INT_EPOCH_SECOND
        val DAY_OF_YEAR: Float = RemoteContext.FLOAT_DAY_OF_YEAR
        val YEAR: Float = RemoteContext.FLOAT_YEAR
    }

    /** Used for System wide variables */
    object System {
        val FONT_SIZE: Float = RemoteContext.FLOAT_FONT_SIZE
        val WINDOW_WIDTH: Float = RemoteContext.FLOAT_WINDOW_WIDTH
        val WINDOW_HEIGHT: Float = RemoteContext.FLOAT_WINDOW_HEIGHT
        val API_LEVEL: Float = RemoteContext.FLOAT_API_LEVEL
        val DENSITY: Float = RemoteContext.FLOAT_DENSITY
        val ID_DEREF: Int = PaintOperation.PTR_DEREFERENCE
        var sLightMode: Float = 0f
    }

    /** Used for Touch variables */
    object Touch {
        val POSITION_X: Float = RemoteContext.FLOAT_TOUCH_POS_X
        val POSITION_Y: Float = RemoteContext.FLOAT_TOUCH_POS_Y
        val VELOCITY_X: Float = RemoteContext.FLOAT_TOUCH_VEL_X
        val VELOCITY_Y: Float = RemoteContext.FLOAT_TOUCH_VEL_Y
        val TOUCH_EVENT_TIME: Float = RemoteContext.FLOAT_TOUCH_EVENT_TIME
        val STOP_INSTANTLY: Float = TouchExpression.STOP_INSTANTLY.toFloat()
        val STOP_ABSOLUTE_POS: Float = TouchExpression.STOP_ABSOLUTE_POS.toFloat()
        val STOP_ENDS: Float = TouchExpression.STOP_ENDS.toFloat()
        val STOP_NOTCHES_PERCENTS: Float = TouchExpression.STOP_NOTCHES_PERCENTS.toFloat()
        val STOP_GENTLY: Float = TouchExpression.STOP_GENTLY.toFloat()
        val STOP_NOTCHES_ABSOLUTE: Float = TouchExpression.STOP_NOTCHES_ABSOLUTE.toFloat()
        val STOP_NOTCHES_EVEN: Float = TouchExpression.STOP_NOTCHES_EVEN.toFloat()
        val STOP_NOTCHES_SINGLE_EVEN: Float = TouchExpression.STOP_NOTCHES_SINGLE_EVEN.toFloat()
    }

    /** Used for Sensor variables */
    object Sensor {
        val ACCELERATION_X: Float = RemoteContext.FLOAT_ACCELERATION_X
        val ACCELERATION_Y: Float = RemoteContext.FLOAT_ACCELERATION_Y
        val ACCELERATION_Z: Float = RemoteContext.FLOAT_ACCELERATION_Z
        val GYRO_ROT_X: Float = RemoteContext.FLOAT_GYRO_ROT_X
        val GYRO_ROT_Y: Float = RemoteContext.FLOAT_GYRO_ROT_Y
        val GYRO_ROT_Z: Float = RemoteContext.FLOAT_GYRO_ROT_Z
        val MAGNETIC_X: Float = RemoteContext.FLOAT_MAGNETIC_X
        val MAGNETIC_Y: Float = RemoteContext.FLOAT_MAGNETIC_Y
        val MAGNETIC_Z: Float = RemoteContext.FLOAT_MAGNETIC_Z
        val LIGHT: Float = RemoteContext.FLOAT_LIGHT
    }

    /** Use in configuration of RC doc headers */
    object DocHeader {
        val DOC_WIDTH: Short = Header.DOC_WIDTH
        val DOC_HEIGHT: Short = Header.DOC_HEIGHT
        val DOC_DENSITY_AT_GENERATION: Short = Header.DOC_DENSITY_AT_GENERATION
        val DOC_DESIRED_FPS: Short = Header.DOC_DESIRED_FPS
        val DOC_CONTENT_DESCRIPTION: Short = Header.DOC_CONTENT_DESCRIPTION
        val DOC_SOURCE: Short = Header.DOC_SOURCE
        val DOC_DATA_UPDATE: Short = Header.DOC_DATA_UPDATE
        val HOST_EXCEPTION_HANDLER: Short = Header.HOST_EXCEPTION_HANDLER
        val DOC_PROFILES: Short = Header.DOC_PROFILES
    }

    /** Used in accessing attributes of time */
    object TimeAttributes {
        val TIME_FROM_NOW_SEC: Short = TimeAttribute.TIME_FROM_NOW_SEC
        val TIME_FROM_NOW_MIN: Short = TimeAttribute.TIME_FROM_NOW_MIN
        val TIME_FROM_NOW_HR: Short = TimeAttribute.TIME_FROM_NOW_HR
        val TIME_FROM_ARG_SEC: Short = TimeAttribute.TIME_FROM_ARG_SEC
        val TIME_FROM_ARG_MIN: Short = TimeAttribute.TIME_FROM_ARG_MIN
        val TIME_FROM_ARG_HR: Short = TimeAttribute.TIME_FROM_ARG_HR
        val TIME_IN_SEC: Short = TimeAttribute.TIME_IN_SEC
        val TIME_IN_MIN: Short = TimeAttribute.TIME_IN_MIN
        val TIME_IN_HR: Short = TimeAttribute.TIME_IN_HR
        val TIME_DAY_OF_MONTH: Short = TimeAttribute.TIME_DAY_OF_MONTH
        val TIME_MONTH_VALUE: Short = TimeAttribute.TIME_MONTH_VALUE
        val TIME_DAY_OF_WEEK: Short = TimeAttribute.TIME_DAY_OF_WEEK
        val TIME_YEAR: Short = TimeAttribute.TIME_YEAR
        val TIME_FROM_LOAD_SEC: Short = TimeAttribute.TIME_FROM_LOAD_SEC
    }

    /** Constants for use in ConditionalOperations */
    object Condition {
        val EQ: Byte = ConditionalOperations.TYPE_EQ
        val NEQ: Byte = ConditionalOperations.TYPE_NEQ
        val LT: Byte = ConditionalOperations.TYPE_LT
        val LTE: Byte = ConditionalOperations.TYPE_LTE
        val GT: Byte = ConditionalOperations.TYPE_GT
        val GTE: Byte = ConditionalOperations.TYPE_GTE
    }

    object ColorAttribute {
        val HUE: Short = COLOR_HUE
        val SATURATION: Short = COLOR_SATURATION
        val BRIGHTNESS: Short = COLOR_BRIGHTNESS
        val RED: Short = COLOR_RED
        val GREEN: Short = COLOR_GREEN
        val BLUE: Short = COLOR_BLUE
        val ALPHA: Short = COLOR_ALPHA
    }

    /** Used in TextLayout operations */
    object Text {
        val ALIGN_LEFT: Int = CoreText.TEXT_ALIGN_LEFT
        val ALIGN_RIGHT: Int = CoreText.TEXT_ALIGN_RIGHT
        val ALIGN_CENTER: Int = CoreText.TEXT_ALIGN_CENTER
        val ALIGN_JUSTIFY: Int = CoreText.TEXT_ALIGN_JUSTIFY
        val ALIGN_START: Int = CoreText.TEXT_ALIGN_START
        val ALIGN_END: Int = CoreText.TEXT_ALIGN_END
        val OVERFLOW_CLIP: Int = CoreText.OVERFLOW_CLIP
        val OVERFLOW_VISIBLE: Int = CoreText.OVERFLOW_VISIBLE
        val OVERFLOW_ELLIPSIS: Int = CoreText.OVERFLOW_ELLIPSIS
        val OVERFLOW_START_ELLIPSIS: Int = CoreText.OVERFLOW_START_ELLIPSIS
        val OVERFLOW_MIDDLE_ELLIPSIS: Int = CoreText.OVERFLOW_MIDDLE_ELLIPSIS
    }

    /** Used in TextFromFloat */
    object TextFromFloat {
        val PAD_AFTER_SPACE: Int =
            androidx.compose.remote.core.operations.TextFromFloat.PAD_AFTER_SPACE
        val PAD_AFTER_NONE: Int =
            androidx.compose.remote.core.operations.TextFromFloat.PAD_AFTER_NONE
        val PAD_AFTER_ZERO: Int =
            androidx.compose.remote.core.operations.TextFromFloat.PAD_AFTER_ZERO
        val PAD_PRE_SPACE: Int =
            androidx.compose.remote.core.operations.TextFromFloat.PAD_PRE_SPACE
        val PAD_PRE_NONE: Int =
            androidx.compose.remote.core.operations.TextFromFloat.PAD_PRE_NONE
        val PAD_PRE_ZERO: Int =
            androidx.compose.remote.core.operations.TextFromFloat.PAD_PRE_ZERO
        val GROUPING_NONE: Int =
            androidx.compose.remote.core.operations.TextFromFloat.GROUPING_NONE
        val GROUPING_BY3: Int =
            androidx.compose.remote.core.operations.TextFromFloat.GROUPING_BY3
        val GROUPING_BY4: Int =
            androidx.compose.remote.core.operations.TextFromFloat.GROUPING_BY4
        val GROUPING_BY32: Int =
            androidx.compose.remote.core.operations.TextFromFloat.GROUPING_BY32
        val SEPARATOR_PERIOD_COMMA: Int =
            androidx.compose.remote.core.operations.TextFromFloat.SEPARATOR_PERIOD_COMMA
        val SEPARATOR_COMMA_PERIOD: Int =
            androidx.compose.remote.core.operations.TextFromFloat.SEPARATOR_COMMA_PERIOD
        val SEPARATOR_SPACE_COMMA: Int =
            androidx.compose.remote.core.operations.TextFromFloat.SEPARATOR_SPACE_COMMA
        val SEPARATOR_UNDER_PERIOD: Int =
            androidx.compose.remote.core.operations.TextFromFloat.SEPARATOR_UNDER_PERIOD
        val OPTIONS_NONE: Int =
            androidx.compose.remote.core.operations.TextFromFloat.OPTIONS_NONE
        val OPTIONS_NEGATIVE_PARENTHESES: Int =
            androidx.compose.remote.core.operations.TextFromFloat.OPTIONS_NEGATIVE_PARENTHESES
        val OPTIONS_ROUNDING: Int =
            androidx.compose.remote.core.operations.TextFromFloat.OPTIONS_ROUNDING
        val LEGACY_MODE: Int =
            androidx.compose.remote.core.operations.TextFromFloat.LEGACY_MODE
    }

    /** Used in Texture */
    object Texture {
        const val TILE_CLAMP: Short = 0
        const val TILE_MIRROR: Short = 1
        const val TILE_REPEAT: Short = 2
        const val TILE_DECAL: Short = 3
        const val FILTER_DEFAULT: Short = 0
        const val FILTER_NEAREST: Short = 1
        const val FILTER_LINEAR: Short = 2
    }

    object Debug {
        val SHOW_USAGE: Int = DebugMessage.SHOW_USAGE
    }

    object PathExpression {
        const val SPLINE_PATH: Int = 0
        val LOOP_PATH: Int = LOOP
        val MONOTONIC_PATH: Int = MONOTONIC
        val LINEAR_PATH: Int = LINEAR
        val POLAR_PATH: Int = POLAR
    }

    object Layout {
        val FIRST_BASELINE: Float = RemoteContext.FIRST_BASELINE
        val LAST_BASELINE: Float = RemoteContext.LAST_BASELINE
    }

    object PathEffect {
        const val PATH_DASH_TRANSLATE: Int = 0
        const val PATH_DASH_ROTATE: Int = 1
        const val PATH_DASH_MORPH: Int = 2
    }

    object Theme {
        val DARK: Int = androidx.compose.remote.core.operations.Theme.DARK
        val LIGHT: Int = androidx.compose.remote.core.operations.Theme.LIGHT
        val UNSPECIFIED: Int = androidx.compose.remote.core.operations.Theme.UNSPECIFIED
    }

    object TextAttribute {
        val MEASURE_WIDTH: Short =
            androidx.compose.remote.core.operations.TextAttribute.MEASURE_WIDTH
        val MEASURE_HEIGHT: Short =
            androidx.compose.remote.core.operations.TextAttribute.MEASURE_HEIGHT
        val MEASURE_LEFT: Short =
            androidx.compose.remote.core.operations.TextAttribute.MEASURE_LEFT
        val MEASURE_RIGHT: Short =
            androidx.compose.remote.core.operations.TextAttribute.MEASURE_RIGHT
        val MEASURE_TOP: Short =
            androidx.compose.remote.core.operations.TextAttribute.MEASURE_TOP
        val MEASURE_BOTTOM: Short =
            androidx.compose.remote.core.operations.TextAttribute.MEASURE_BOTTOM
        val TEXT_LENGTH: Short =
            androidx.compose.remote.core.operations.TextAttribute.TEXT_LENGTH
    }
}
