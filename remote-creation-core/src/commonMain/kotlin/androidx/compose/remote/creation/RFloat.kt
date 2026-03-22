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

import androidx.compose.remote.core.RemoteContext.Companion.FLOAT_ANIMATION_DELTA_TIME
import androidx.compose.remote.core.RemoteContext.Companion.FLOAT_ANIMATION_TIME
import androidx.compose.remote.core.RemoteContext.Companion.FLOAT_CALENDAR_MONTH
import androidx.compose.remote.core.RemoteContext.Companion.FLOAT_CONTINUOUS_SEC
import androidx.compose.remote.core.RemoteContext.Companion.FLOAT_DAY_OF_MONTH
import androidx.compose.remote.core.RemoteContext.Companion.FLOAT_OFFSET_TO_UTC
import androidx.compose.remote.core.RemoteContext.Companion.FLOAT_TIME_IN_HR
import androidx.compose.remote.core.RemoteContext.Companion.FLOAT_TIME_IN_MIN
import androidx.compose.remote.core.RemoteContext.Companion.FLOAT_TIME_IN_SEC
import androidx.compose.remote.core.RemoteContext.Companion.FLOAT_WEEK_DAY
import androidx.compose.remote.core.operations.utilities.AnimatedFloatExpression

/**
 * Collection of utilities for the RFloat class and allows kotlin float expressions to
 * be converted to remote compose RPM expressions.
 */
fun RemoteComposeWriter.rf(vararg elements: Float): RFloat = RFloat(this, elements)

fun RemoteComposeWriter.rf(v: Number): RFloat {
    if (v is RFloat) return v
    return RFloat(this, v.toFloat())
}

operator fun Float.times(v: RFloat): RFloat =
    RFloat(v.writer, floatArrayOf(this, *v.array, Rc.FloatExpression.MUL))

operator fun Float.plus(v: RFloat): RFloat =
    RFloat(v.writer, floatArrayOf(this, *v.array, Rc.FloatExpression.ADD))

operator fun Float.minus(v: RFloat): RFloat =
    RFloat(v.writer, floatArrayOf(this, *v.array, Rc.FloatExpression.SUB))

operator fun Float.div(v: RFloat): RFloat =
    RFloat(v.writer, floatArrayOf(this, *v.array, Rc.FloatExpression.DIV))

operator fun Float.rem(v: RFloat): RFloat =
    RFloat(v.writer, floatArrayOf(this, *v.array, Rc.FloatExpression.MOD))

class RFloat : Number {
    var array: FloatArray = floatArrayOf()
    var id: Float = 0f
    var writer: RemoteComposeWriter? = null
    var animation: FloatArray? = null

    constructor(writer: RemoteComposeWriter?, array: FloatArray) {
        this.array = array
        this.writer = writer
    }

    constructor(writer: RemoteComposeWriter?, a: Float) {
        if (a.isNaN()) id = a
        this.array = floatArrayOf(a)
        this.writer = writer
    }

    // writer property setter is used directly via writer = w

    override fun toByte(): Byte = TODO("Not yet implemented")
    override fun toDouble(): Double = TODO("Not yet implemented")
    fun toArray(): FloatArray = array

    override fun toFloat(): Float {
        if (!id.isNaN()) {
            id = if (animation != null) {
                writer?.floatExpression(array, animation)!!
            } else {
                writer?.floatExpression(*array)!!
            }
        }
        return id
    }

    fun flush(): RFloat { toFloat(); return this }
    override fun toInt(): Int = TODO("Not yet implemented")
    override fun toLong(): Long = TODO("Not yet implemented")
    override fun toShort(): Short = TODO("Not yet implemented")

    operator fun unaryPlus(): RFloat =
        RFloat(writer, floatArrayOf(*toArray(this), -1f, Rc.FloatExpression.MUL))

    operator fun rem(v: Float): RFloat =
        RFloat(writer, floatArrayOf(*toArray(this), v, Rc.FloatExpression.MOD))

    operator fun rem(v: RFloat): RFloat =
        RFloat(writer, floatArrayOf(*toArray(this), *toArray(v), Rc.FloatExpression.MOD))

    fun min(v: RFloat): RFloat =
        RFloat(writer, floatArrayOf(*toArray(this), *toArray(v), Rc.FloatExpression.MIN))

    fun min(v: Float): RFloat =
        RFloat(writer, floatArrayOf(*toArray(this), v, Rc.FloatExpression.MIN))

    operator fun plus(v: Float): RFloat =
        RFloat(writer, floatArrayOf(*toArray(this), v, Rc.FloatExpression.ADD))

    operator fun plus(v: RFloat): RFloat =
        RFloat(writer, floatArrayOf(*toArray(this), *toArray(v), Rc.FloatExpression.ADD))

    operator fun plus(v: Number): RFloat =
        if (v is RFloat) plus(v) else plus(v.toFloat())

    operator fun minus(v: Float): RFloat =
        RFloat(writer, floatArrayOf(*toArray(this), v, Rc.FloatExpression.SUB))

    operator fun minus(v: RFloat): RFloat =
        RFloat(writer, floatArrayOf(*toArray(this), *toArray(v), Rc.FloatExpression.SUB))

    operator fun minus(v: Number): RFloat =
        if (v is RFloat) minus(v) else minus(v.toFloat())

    operator fun times(v: Float): RFloat =
        RFloat(writer, floatArrayOf(*toArray(this), v, Rc.FloatExpression.MUL))

    operator fun times(v: RFloat): RFloat =
        RFloat(writer, floatArrayOf(*toArray(this), *toArray(v), Rc.FloatExpression.MUL))

    operator fun div(v: Float): RFloat =
        RFloat(writer, floatArrayOf(*toArray(this), v, Rc.FloatExpression.DIV))

    operator fun div(v: RFloat): RFloat =
        RFloat(writer, floatArrayOf(*toArray(this), *toArray(v), Rc.FloatExpression.DIV))

    operator fun get(v: RFloat): RFloat =
        RFloat(writer, floatArrayOf(*toArray(v), *toArray(this), Rc.FloatExpression.A_DEREF))

    operator fun get(v: Int): RFloat =
        RFloat(writer, floatArrayOf(v.toFloat(), *toArray(this), Rc.FloatExpression.A_DEREF))

    companion object {
        operator fun invoke(float: Float, writer: RemoteComposeWriter? = null): RFloat =
            RFloat(writer, floatArrayOf(float))
    }

    fun anim(
        duration: Float, type: Int = Rc.Animate.CUBIC_STANDARD,
        spec: FloatArray? = null, initialValue: Float = Float.NaN, wrap: Float = Float.NaN,
    ): RFloat {
        animation = writer?.anim(duration, type, spec, initialValue, wrap)
        this.flush()
        return this
    }

    fun genTextId(
        before: Int = 2, after: Int = 1, flags: Int = Rc.TextFromFloat.PAD_AFTER_ZERO,
    ): Int {
        val w = writer ?: throw IllegalStateException("writer is null")
        return w.createTextFromFloat(this.toFloat(), after, after, flags)
    }
}

fun toFloat(a: Number): Float = when (a) { is RFloat -> a.id; else -> a.toFloat() }

fun arrayValue(array: Float, b: RFloat): RFloat =
    RFloat(b.writer, floatArrayOf(array, *b.array, Rc.FloatExpression.A_DEREF))

fun max(a: RFloat, b: Float): RFloat = RFloat(a.writer, floatArrayOf(*a.array, b, Rc.FloatExpression.MAX))
fun max(a: Float, b: RFloat): RFloat = RFloat(b.writer, floatArrayOf(a, *b.array, Rc.FloatExpression.MAX))
fun max(a: RFloat, b: RFloat): RFloat = RFloat(a.writer, floatArrayOf(*a.array, *b.array, Rc.FloatExpression.MAX))

fun min(a: RFloat, b: Float): RFloat = RFloat(a.writer, floatArrayOf(*a.array, b, Rc.FloatExpression.MIN))
fun min(a: Float, b: RFloat): RFloat = RFloat(b.writer, floatArrayOf(a, *b.array, Rc.FloatExpression.MIN))
fun min(a: RFloat, b: RFloat): RFloat = RFloat(a.writer, floatArrayOf(*a.array, *b.array, Rc.FloatExpression.MIN))

fun pow(a: RFloat, b: Float): RFloat = RFloat(a.writer, floatArrayOf(*a.array, b, Rc.FloatExpression.POW))
fun pow(a: Float, b: RFloat): RFloat = RFloat(b.writer, floatArrayOf(a, *b.array, Rc.FloatExpression.POW))
fun pow(a: RFloat, b: RFloat): RFloat = RFloat(a.writer, floatArrayOf(*a.array, *b.array, Rc.FloatExpression.POW))

fun sqrt(a: RFloat): RFloat = RFloat(a.writer, floatArrayOf(*a.array, Rc.FloatExpression.SQRT))
fun abs(a: RFloat): RFloat = RFloat(a.writer, floatArrayOf(*a.array, Rc.FloatExpression.ABS))
fun sign(a: RFloat): RFloat = RFloat(a.writer, floatArrayOf(*a.array, Rc.FloatExpression.SIGN))
fun copySign(a: RFloat, b: Float): RFloat = RFloat(a.writer, floatArrayOf(*a.array, b, Rc.FloatExpression.COPY_SIGN))
fun copySign(a: Float, b: RFloat): RFloat = RFloat(b.writer, floatArrayOf(a, *b.array, Rc.FloatExpression.COPY_SIGN))
fun copySign(a: RFloat, b: RFloat): RFloat = RFloat(a.writer, floatArrayOf(*a.array, *b.array, Rc.FloatExpression.COPY_SIGN))
fun exp(a: RFloat): RFloat = RFloat(a.writer, floatArrayOf(*a.array, Rc.FloatExpression.EXP))
fun ceil(a: RFloat): RFloat = RFloat(a.writer, floatArrayOf(*a.array, Rc.FloatExpression.CEIL))
fun floor(a: RFloat): RFloat = RFloat(a.writer, floatArrayOf(*a.array, Rc.FloatExpression.FLOOR))
fun log(a: RFloat): RFloat = RFloat(a.writer, floatArrayOf(*a.array, Rc.FloatExpression.LOG))
fun ln(a: RFloat): RFloat = RFloat(a.writer, floatArrayOf(*a.array, Rc.FloatExpression.LN))
fun round(a: RFloat): RFloat = RFloat(a.writer, floatArrayOf(*a.array, Rc.FloatExpression.ROUND))
fun sin(a: RFloat): RFloat = RFloat(a.writer, floatArrayOf(*a.array, Rc.FloatExpression.SIN))
fun cos(a: RFloat): RFloat = RFloat(a.writer, floatArrayOf(*a.array, Rc.FloatExpression.COS))
fun tan(a: RFloat): RFloat = RFloat(a.writer, floatArrayOf(*a.array, Rc.FloatExpression.TAN))
fun asin(a: RFloat): RFloat = RFloat(a.writer, floatArrayOf(*a.array, Rc.FloatExpression.ASIN))
fun acos(a: RFloat): RFloat = RFloat(a.writer, floatArrayOf(*a.array, Rc.FloatExpression.ACOS))
fun atan(a: RFloat): RFloat = RFloat(a.writer, floatArrayOf(*a.array, Rc.FloatExpression.ATAN))
fun atan2(a: RFloat, b: Float): RFloat = RFloat(a.writer, floatArrayOf(*a.array, b, Rc.FloatExpression.ATAN2))
fun atan2(a: Float, b: RFloat): RFloat = RFloat(b.writer, floatArrayOf(a, *b.array, Rc.FloatExpression.ATAN2))
fun atan2(a: RFloat, b: RFloat): RFloat = RFloat(a.writer, floatArrayOf(*a.array, *b.array, Rc.FloatExpression.ATAN2))
fun cbrt(a: RFloat): RFloat = RFloat(a.writer, floatArrayOf(*a.array, Rc.FloatExpression.CBRT))

fun ifThenElse(a: RFloat, b: RFloat, c: RFloat): RFloat =
    RFloat(a.writer, floatArrayOf(*a.array, *b.array, *c.array, Rc.FloatExpression.IFELSE))

fun ifElse(a: RFloat, b: RFloat, c: RFloat): RFloat =
    RFloat(a.writer, floatArrayOf(*c.array, *b.array, *a.array, Rc.FloatExpression.IFELSE))

fun toDeg(a: RFloat): RFloat = RFloat(a.writer, floatArrayOf(*a.array, Rc.FloatExpression.DEG))
fun toRad(a: RFloat): RFloat = RFloat(a.writer, floatArrayOf(*a.array, Rc.FloatExpression.RAD))
fun second(a: RFloat): RFloat = RFloat(a.writer, floatArrayOf(*a.array, AnimatedFloatExpression.CMD2))
fun first(a: RFloat): RFloat = RFloat(a.writer, floatArrayOf(*a.array, AnimatedFloatExpression.CMD1))
fun noiseFrom(a: RFloat): RFloat = RFloat(a.writer, floatArrayOf(*a.array, Rc.FloatExpression.NOISE_FROM))

fun sqrSum(a: RFloat, b: RFloat): RFloat =
    RFloat(a.writer, floatArrayOf(*a.array, *b.array, Rc.FloatExpression.SQUARE_SUM))

fun step(a: RFloat, b: RFloat): RFloat =
    RFloat(a.writer, floatArrayOf(*a.array, *b.array, Rc.FloatExpression.STEP))

fun smoothStep(value: RFloat, min: RFloat, max: RFloat): RFloat =
    RFloat(value.writer, floatArrayOf(*value.array, *max.array, *min.array, Rc.FloatExpression.SMOOTH_STEP))

fun smoothStep(value: Number, min: Number, max: Number): RFloat {
    val valuer = value as? RFloat ?: RFloat(null, value.toFloat())
    val minr = min as? RFloat ?: RFloat(null, min.toFloat())
    val maxr = max as? RFloat ?: RFloat(null, max.toFloat())
    val w = valuer.writer ?: minr.writer ?: maxr.writer
        ?: throw IllegalStateException("one of the inputs must have a writer")
    return RFloat(w, floatArrayOf(*valuer.array, *maxr.array, *minr.array, Rc.FloatExpression.SMOOTH_STEP))
}

fun pingPong(max: Number, x: Number): RFloat {
    val xr = x as? RFloat ?: RFloat(null, x.toFloat())
    val maxr = max as? RFloat ?: RFloat(null, max.toFloat())
    val w = xr.writer ?: maxr.writer ?: throw IllegalStateException("one of the inputs must have a writer")
    return RFloat(w, floatArrayOf(*xr.array, *maxr.array, Rc.FloatExpression.PINGPONG))
}

fun lerp(x: RFloat, y: RFloat, t: RFloat): RFloat =
    RFloat(x.writer, floatArrayOf(*x.array, *y.array, *t.array, Rc.FloatExpression.LERP))

fun lerp(x: Number, y: Number, t: Number): RFloat {
    val xr = x as? RFloat ?: RFloat(null, x.toFloat())
    val yr = y as? RFloat ?: RFloat(null, y.toFloat())
    val tr = t as? RFloat ?: RFloat(null, t.toFloat())
    val w = xr.writer ?: yr.writer ?: tr.writer
        ?: throw IllegalStateException("one of the inputs must have a writer")
    return RFloat(w, floatArrayOf(*xr.array, *yr.array, *tr.array, Rc.FloatExpression.LERP))
}

fun hypot(a: RFloat, b: RFloat): RFloat =
    RFloat(a.writer, floatArrayOf(*a.array, *b.array, Rc.FloatExpression.HYPOT))

fun random(min: RFloat, max: RFloat): RFloat =
    RFloat(min.writer, floatArrayOf(*min.array, *max.array, Rc.FloatExpression.RAND_IN_RANGE))

fun mad(a: Number, b: Number, c: Number): RFloat =
    RFloat(null, floatArrayOf(*toArray(a), *toArray(b), *toArray(c), Rc.FloatExpression.MAD))

fun RemoteComposeWriter.ifElse(a: Number, b: Number, c: Number): RFloat =
    RFloat(this, floatArrayOf(*toArray(c), *toArray(b), *toArray(a), Rc.FloatExpression.IFELSE))

fun toArray(a: RFloat): FloatArray = if (a.id.isNaN()) floatArrayOf(a.id) else a.array

fun toArray(a: Number): FloatArray {
    if (a is RFloat) return if (a.id.isNaN()) floatArrayOf(a.id) else a.array
    return floatArrayOf(a.toFloat())
}

fun clamp(min: Number, max: Number, value: RFloat): RFloat =
    RFloat(value.writer, floatArrayOf(*toArray(min), *toArray(max), *toArray(value), Rc.FloatExpression.CLAMP))

fun cubic(x1: Number, x2: Number, y1: Number, y2: Number, value: Number): RFloat {
    val w = (value as? RFloat)?.writer ?: (x1 as? RFloat)?.writer
        ?: (x2 as? RFloat)?.writer ?: (y1 as? RFloat)?.writer ?: (y2 as? RFloat)?.writer
        ?: throw IllegalStateException("one of the inputs must be an RFloat")
    return RFloat(w, floatArrayOf(
        *toArray(x1), *toArray(y1), *toArray(x2), *toArray(y2), *toArray(value),
        Rc.FloatExpression.CUBIC,
    ))
}

fun RemoteComposeWriter.Hour(): RFloat = RFloat(this, FLOAT_TIME_IN_HR)
fun RemoteComposeWriter.Minutes(): RFloat = RFloat(this, FLOAT_TIME_IN_MIN)
fun RemoteComposeWriter.Seconds(): RFloat = RFloat(this, FLOAT_TIME_IN_SEC)
fun RemoteComposeWriter.ContinuousSec(): RFloat = RFloat(this, FLOAT_CONTINUOUS_SEC)
fun RemoteComposeWriter.UtcOffset(): RFloat = RFloat(this, FLOAT_OFFSET_TO_UTC)
fun RemoteComposeWriter.DayOfWeek(): RFloat = RFloat(this, FLOAT_WEEK_DAY)
fun RemoteComposeWriter.Month(): RFloat = RFloat(this, FLOAT_CALENDAR_MONTH)
fun RemoteComposeWriter.DayOfMonth(): RFloat = RFloat(this, FLOAT_DAY_OF_MONTH)
fun RemoteComposeWriter.ComponentWidth(): RFloat = RFloat(this, addComponentWidthValue())
fun RemoteComposeWriter.ComponentHeight(): RFloat = RFloat(this, addComponentHeightValue())
fun RemoteComposeWriter.rand(): RFloat = RFloat(this, Rc.FloatExpression.RAND)
fun RemoteComposeWriter.index(): RFloat = RFloat(this, Rc.FloatExpression.VAR1)
fun RemoteComposeWriter.animationTime(): RFloat = RFloat(this, FLOAT_ANIMATION_TIME)
fun RemoteComposeWriter.deltaTime(): RFloat = RFloat(this, FLOAT_ANIMATION_DELTA_TIME)
fun RemoteComposeWriter.windowWidth(): RFloat = RFloat(this, Rc.System.WINDOW_WIDTH)
fun RemoteComposeWriter.windowHeight(): RFloat = RFloat(this, Rc.System.WINDOW_HEIGHT)

val RemoteComposeWriter.var1: RFloat
    get() = RFloat(this, floatArrayOf(Rc.FloatExpression.VAR1))
