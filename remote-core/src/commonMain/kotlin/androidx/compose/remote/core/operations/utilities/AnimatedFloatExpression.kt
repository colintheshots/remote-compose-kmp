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
package androidx.compose.remote.core.operations.utilities

import androidx.compose.remote.core.operations.utilities.easing.CubicEasing
import androidx.compose.remote.core.operations.utilities.easing.MonotonicSpline
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.random.Random

/** High performance floating point expression evaluator used in animation */
class AnimatedFloatExpression {
    private var mR0 = 0f
    private var mR1 = 0f
    private var mR2 = 0f
    private var mR3 = 0f

    private var mStack = FloatArray(0)
    private var mLocalStack = FloatArray(128)
    private var mVar = FloatArray(0)
    private var mCollectionsAccess: CollectionsAccess? = null
    private var mEasing: CubicEasing? = null
    private val mSplineMap = IntMap<MonotonicSpline>()

    /**
     * Get the max op for a given API level
     */
    fun getMaxOpForLevel(level: Int): Int {
        return if (level == 7) LAST_OP else API_LEVEL6_MAX
    }

    private fun getSplineValue(arrayId: Int, pos: Float): Float {
        var fit = mSplineMap.get(arrayId)
        val ca = mCollectionsAccess!!
        val f = ca.getFloats(arrayId)
        if (fit != null) {
            if (fit.getArray() === f) {
                return fit.getPos(pos)
            }
        }
        fit = MonotonicSpline(null, f!!)
        mSplineMap.put(arrayId, fit)
        return fit.getPos(pos)
    }

    /**
     * Evaluate a float expression
     */
    fun eval(exp: FloatArray, vararg `var`: Float): Float {
        mStack = exp
        mVar = `var`
        var sp = -1
        for (i in mStack.indices) {
            val v = mStack[i]
            if (v.isNaN()) {
                sp = opEval(sp, fromNaN(v))
            } else {
                mStack[++sp] = v
            }
        }
        return mStack[sp]
    }

    /**
     * Evaluate a float expression with CollectionsAccess, length and vars
     */
    fun eval(ca: CollectionsAccess, exp: FloatArray, len: Int, vararg `var`: Float): Float {
        exp.copyInto(mLocalStack, endIndex = len)
        mStack = mLocalStack
        mVar = `var`
        mCollectionsAccess = ca
        var sp = -1
        for (i in 0 until len) {
            val v = mStack[i]
            if (v.isNaN()) {
                val id = fromNaN(v)
                if ((id and NanMap.ID_REGION_MASK) != NanMap.ID_REGION_ARRAY) {
                    sp = opEval(sp, id)
                } else {
                    mStack[++sp] = v
                }
            } else {
                mStack[++sp] = v
            }
        }
        return mStack[sp]
    }

    /**
     * Evaluate a float expression with CollectionsAccess and length
     */
    fun eval(ca: CollectionsAccess, exp: FloatArray, len: Int): Float {
        exp.copyInto(mLocalStack, endIndex = len)
        mStack = mLocalStack
        mCollectionsAccess = ca
        var sp = -1
        for (i in 0 until len) {
            val v = mStack[i]
            if (v.isNaN()) {
                val id = fromNaN(v)
                if ((id and NanMap.ID_REGION_MASK) != NanMap.ID_REGION_ARRAY) {
                    sp = opEval(sp, id)
                } else {
                    mStack[++sp] = v
                }
            } else {
                mStack[++sp] = v
            }
        }
        return mStack[sp]
    }

    /**
     * Evaluate a float expression with length and vars
     */
    fun eval(exp: FloatArray, len: Int, vararg `var`: Float): Float {
        exp.copyInto(mLocalStack, endIndex = len)
        mStack = mLocalStack
        mVar = `var`
        var sp = -1
        for (i in 0 until len) {
            val v = mStack[i]
            if (v.isNaN()) {
                sp = opEval(sp, fromNaN(v))
            } else {
                mStack[++sp] = v
            }
        }
        return mStack[sp]
    }

    /**
     * Evaluate a float expression (debug variant)
     */
    fun evalDB(exp: FloatArray, vararg `var`: Float): Float {
        mStack = exp
        mVar = `var`
        var sp = -1
        for (v in exp) {
            if (v.isNaN()) {
                sp = opEval(sp, fromNaN(v))
            } else {
                mStack[++sp] = v
            }
        }
        return mStack[sp]
    }

    @Suppress("LongMethod")
    private fun opEval(sp: Int, id: Int): Int {
        var arrayId: Int
        var array: FloatArray?

        when (id) {
            OP_ADD -> { mStack[sp - 1] += mStack[sp]; return sp - 1 }
            OP_SUB -> { mStack[sp - 1] -= mStack[sp]; return sp - 1 }
            OP_MUL -> { mStack[sp - 1] *= mStack[sp]; return sp - 1 }
            OP_DIV -> { mStack[sp - 1] /= mStack[sp]; return sp - 1 }
            OP_MOD -> { mStack[sp - 1] %= mStack[sp]; return sp - 1 }
            OP_MIN -> { mStack[sp - 1] = min(mStack[sp - 1], mStack[sp]); return sp - 1 }
            OP_MAX -> { mStack[sp - 1] = max(mStack[sp - 1], mStack[sp]); return sp - 1 }
            OP_POW -> { mStack[sp - 1] = mStack[sp - 1].pow(mStack[sp]); return sp - 1 }
            OP_SQRT -> { mStack[sp] = sqrt(mStack[sp]); return sp }
            OP_ABS -> { mStack[sp] = abs(mStack[sp]); return sp }
            OP_SIGN -> { mStack[sp] = mStack[sp].sign; return sp }
            OP_COPY_SIGN -> {
                mStack[sp - 1] = if (mStack[sp] >= 0) abs(mStack[sp - 1]) else -abs(mStack[sp - 1])
                return sp - 1
            }
            OP_EXP -> { mStack[sp] = exp(mStack[sp]); return sp }
            OP_FLOOR -> { mStack[sp] = floor(mStack[sp]); return sp }
            OP_LOG -> { mStack[sp] = log10(mStack[sp]); return sp }
            OP_LN -> { mStack[sp] = ln(mStack[sp]); return sp }
            OP_ROUND -> { mStack[sp] = mStack[sp].roundToInt().toFloat(); return sp }
            OP_SIN -> { mStack[sp] = sin(mStack[sp]); return sp }
            OP_COS -> { mStack[sp] = cos(mStack[sp]); return sp }
            OP_TAN -> { mStack[sp] = tan(mStack[sp]); return sp }
            OP_ASIN -> { mStack[sp] = asin(mStack[sp]); return sp }
            OP_ACOS -> { mStack[sp] = acos(mStack[sp]); return sp }
            OP_ATAN -> { mStack[sp] = atan(mStack[sp]); return sp }
            OP_ATAN2 -> { mStack[sp - 1] = atan2(mStack[sp - 1], mStack[sp]); return sp - 1 }
            OP_MAD -> {
                mStack[sp - 2] = mStack[sp] + mStack[sp - 1] * mStack[sp - 2]
                return sp - 2
            }
            OP_TERNARY_CONDITIONAL -> {
                mStack[sp - 2] = if (mStack[sp] > 0) mStack[sp - 1] else mStack[sp - 2]
                return sp - 2
            }
            OP_CLAMP -> {
                mStack[sp - 2] = min(max(mStack[sp - 2], mStack[sp]), mStack[sp - 1])
                return sp - 2
            }
            OP_CBRT -> { mStack[sp] = mStack[sp].pow(1f / 3f); return sp }
            OP_DEG -> { mStack[sp] = mStack[sp] * FP_TO_RAD; return sp }
            OP_RAD -> { mStack[sp] = mStack[sp] * FP_TO_DEG; return sp }
            OP_CEIL -> { mStack[sp] = ceil(mStack[sp]); return sp }
            OP_A_DEREF -> {
                arrayId = fromNaN(mStack[sp - 1])
                mStack[sp - 1] = mCollectionsAccess!!.getFloatValue(arrayId, mStack[sp].toInt())
                return sp - 1
            }
            OP_A_MAX -> {
                arrayId = fromNaN(mStack[sp])
                array = mCollectionsAccess!!.getFloats(arrayId)!!
                var maxVal = array[0]
                for (i in 1 until array.size) {
                    maxVal = max(maxVal, array[i])
                }
                mStack[sp] = maxVal
                return sp
            }
            OP_A_MIN -> {
                arrayId = fromNaN(mStack[sp])
                array = mCollectionsAccess!!.getFloats(arrayId)
                if (array == null || array.isEmpty()) return sp
                var minVal = array[0]
                for (i in 1 until array.size) {
                    minVal = min(minVal, array[i])
                }
                mStack[sp] = minVal
                return sp
            }
            OP_A_SUM -> {
                arrayId = fromNaN(mStack[sp])
                array = mCollectionsAccess!!.getFloats(arrayId)!!
                var sum = 0f
                for (i in array.indices) sum += array[i]
                mStack[sp] = sum
                return sp
            }
            OP_A_AVG -> {
                arrayId = fromNaN(mStack[sp])
                array = mCollectionsAccess!!.getFloats(arrayId)!!
                var sum = 0f
                for (i in array.indices) sum += array[i]
                mStack[sp] = sum / array.size
                return sp
            }
            OP_A_LEN -> {
                arrayId = fromNaN(mStack[sp])
                mStack[sp] = mCollectionsAccess!!.getListLength(arrayId).toFloat()
                return sp
            }
            OP_A_SPLINE -> {
                arrayId = fromNaN(mStack[sp - 1])
                mStack[sp - 1] = getSplineValue(arrayId, mStack[sp])
                return sp - 1
            }
            OP_RAND -> {
                mStack[sp + 1] = sRandom.nextFloat()
                return sp + 1
            }
            OP_RAND_SEED -> {
                val seed = mStack[sp]
                sRandom = if (seed == 0f) Random else Random(seed.toRawBits())
                return sp - 1
            }
            OP_NOISE_FROM -> {
                var x = mStack[sp].toRawBits()
                x = (x shl 13) xor x
                mStack[sp] = (1.0f -
                    ((x * (x * x * 15731 + 789221) + 1376312589) and 0x7fffffff) /
                    1.0737418E+9f)
                return sp
            }
            OP_RAND_IN_RANGE -> {
                mStack[sp] = sRandom.nextFloat() * (mStack[sp] - mStack[sp - 1]) + mStack[sp - 1]
                return sp
            }
            OP_SQUARE_SUM -> {
                mStack[sp - 1] = mStack[sp - 1] * mStack[sp - 1] + mStack[sp] * mStack[sp]
                return sp - 1
            }
            OP_STEP -> {
                mStack[sp - 1] = if (mStack[sp - 1] > mStack[sp]) 1f else 0f
                return sp - 1
            }
            OP_SQUARE -> { mStack[sp] = mStack[sp] * mStack[sp]; return sp }
            OP_DUP -> { mStack[sp + 1] = mStack[sp]; return sp + 1 }
            OP_HYPOT -> {
                mStack[sp - 1] = sqrt(mStack[sp - 1] * mStack[sp - 1] + mStack[sp] * mStack[sp])
                return sp - 1
            }
            OP_SWAP -> {
                val swap = mStack[sp - 1]
                mStack[sp - 1] = mStack[sp]
                mStack[sp] = swap
                return sp
            }
            OP_LERP -> {
                val tmp1 = mStack[sp - 2]
                val tmp2 = mStack[sp - 1]
                val tmp3 = mStack[sp]
                mStack[sp - 2] = tmp1 + (tmp2 - tmp1) * tmp3
                return sp - 2
            }
            OP_SMOOTH_STEP -> {
                val val3 = mStack[sp - 2]
                val max2 = mStack[sp - 1]
                val min1 = mStack[sp]
                if (val3 < min1) {
                    mStack[sp - 2] = 0f
                } else if (val3 > max2) {
                    mStack[sp - 2] = 1f
                } else {
                    val v = (val3 - min1) / (max2 - min1)
                    mStack[sp - 2] = v * v * (3 - 2 * v)
                }
                return sp - 2
            }
            OP_LOG2 -> { mStack[sp] = ln(mStack[sp]) / ln(2.0f); return sp }
            OP_INV -> { mStack[sp] = 1.0f / mStack[sp]; return sp }
            OP_FRACT -> { mStack[sp] = mStack[sp] - mStack[sp].toInt(); return sp }
            OP_PINGPONG -> {
                val max2x = mStack[sp] * 2
                val tmp = mStack[sp - 1] % max2x
                mStack[sp - 1] = if (tmp < mStack[sp]) tmp else max2x - tmp
                return sp - 1
            }
            OP_NOP -> return sp
            OP_STORE_R0 -> { mR0 = mStack[sp]; return sp - 1 }
            OP_STORE_R1 -> { mR1 = mStack[sp]; return sp - 1 }
            OP_STORE_R2 -> { mR2 = mStack[sp]; return sp - 1 }
            OP_STORE_R3 -> { mR3 = mStack[sp]; return sp - 1 }
            OP_LOAD_R0 -> { mStack[sp + 1] = mR0; return sp + 1 }
            OP_LOAD_R1 -> { mStack[sp + 1] = mR1; return sp + 1 }
            OP_LOAD_R2 -> { mStack[sp + 1] = mR2; return sp + 1 }
            OP_LOAD_R3 -> { mStack[sp + 1] = mR3; return sp + 1 }
            OP_FIRST_VAR -> { mStack[sp + 1] = mVar[0]; return sp + 1 }
            OP_SECOND_VAR -> { mStack[sp + 1] = mVar[1]; return sp + 1 }
            OP_THIRD_VAR -> { mStack[sp + 1] = mVar[2]; return sp + 1 }
            OP_CHANGE_SIGN -> { mStack[sp] = -mStack[sp]; return sp }
            OP_CUBIC -> {
                val x1 = mStack[sp - 4]
                val y1 = mStack[sp - 3]
                val x2 = mStack[sp - 2]
                val y2 = mStack[sp - 1]
                val pos = mStack[sp]
                if (mEasing == null) {
                    mEasing = CubicEasing()
                }
                mEasing!!.setup(x1, y1, x2, y2)
                mStack[sp - 4] = mEasing!!.get(pos)
                return sp - 4
            }
            OP_A_SPLINE_LOOP -> {
                arrayId = fromNaN(mStack[sp - 1])
                val i = mStack[sp].toInt()
                var r = mStack[sp] - i
                r = if (r < 0.0f) r + 1.0f else r
                mStack[sp - 1] = getSplineValue(arrayId, r)
                return sp - 1
            }
        }
        return sp
    }

    companion object {
        val sNames = IntMap<String>()

        /** The START POINT in the float NaN space for operators */
        const val OFFSET = 0x310_000

        val ADD = asNan(OFFSET + 1)
        val SUB = asNan(OFFSET + 2)
        val MUL = asNan(OFFSET + 3)
        val DIV = asNan(OFFSET + 4)
        val MOD = asNan(OFFSET + 5)
        val MIN = asNan(OFFSET + 6)
        val MAX = asNan(OFFSET + 7)
        val POW = asNan(OFFSET + 8)
        val SQRT = asNan(OFFSET + 9)
        val ABS = asNan(OFFSET + 10)
        val SIGN = asNan(OFFSET + 11)
        val COPY_SIGN = asNan(OFFSET + 12)
        val EXP = asNan(OFFSET + 13)
        val FLOOR = asNan(OFFSET + 14)
        val LOG = asNan(OFFSET + 15)
        val LN = asNan(OFFSET + 16)
        val ROUND = asNan(OFFSET + 17)
        val SIN = asNan(OFFSET + 18)
        val COS = asNan(OFFSET + 19)
        val TAN = asNan(OFFSET + 20)
        val ASIN = asNan(OFFSET + 21)
        val ACOS = asNan(OFFSET + 22)
        val ATAN = asNan(OFFSET + 23)
        val ATAN2 = asNan(OFFSET + 24)
        val MAD = asNan(OFFSET + 25)
        val IFELSE = asNan(OFFSET + 26)
        val CLAMP = asNan(OFFSET + 27)
        val CBRT = asNan(OFFSET + 28)
        val DEG = asNan(OFFSET + 29)
        val RAD = asNan(OFFSET + 30)
        val CEIL = asNan(OFFSET + 31)
        val A_DEREF = asNan(OFFSET + 32)
        val A_MAX = asNan(OFFSET + 33)
        val A_MIN = asNan(OFFSET + 34)
        val A_SUM = asNan(OFFSET + 35)
        val A_AVG = asNan(OFFSET + 36)
        val A_LEN = asNan(OFFSET + 37)
        val A_SPLINE = asNan(OFFSET + 38)
        val RAND = asNan(OFFSET + 39)
        val RAND_SEED = asNan(OFFSET + 40)
        val NOISE_FROM = asNan(OFFSET + 41)
        val RAND_IN_RANGE = asNan(OFFSET + 42)
        val SQUARE_SUM = asNan(OFFSET + 43)
        val STEP = asNan(OFFSET + 44)
        val SQUARE = asNan(OFFSET + 45)
        val DUP = asNan(OFFSET + 46)
        val HYPOT = asNan(OFFSET + 47)
        val SWAP = asNan(OFFSET + 48)
        val LERP = asNan(OFFSET + 49)
        val SMOOTH_STEP = asNan(OFFSET + 50)
        val LOG2 = asNan(OFFSET + 51)
        val INV = asNan(OFFSET + 52)
        val FRACT = asNan(OFFSET + 53)
        val PINGPONG = asNan(OFFSET + 54)
        val NOP = asNan(OFFSET + 55)
        val STORE_RO = asNan(OFFSET + 56)
        val STORE_R1 = asNan(OFFSET + 57)
        val STORE_R2 = asNan(OFFSET + 58)
        val STORE_R3 = asNan(OFFSET + 59)
        val LOAD_R0 = asNan(OFFSET + 60)
        val LOAD_R1 = asNan(OFFSET + 61)
        val LOAD_R2 = asNan(OFFSET + 62)
        val LOAD_R3 = asNan(OFFSET + 63)
        val CMD1 = asNan(OFFSET + 64)
        val CMD2 = asNan(OFFSET + 65)
        val CMD3 = asNan(OFFSET + 66)
        val CMD4 = asNan(OFFSET + 67)

        const val LAST_OP = OFFSET + 63

        val VAR1 = asNan(OFFSET + 70)
        val VAR2 = asNan(OFFSET + 71)
        val VAR3 = asNan(OFFSET + 72)
        val CHANGE_SIGN = asNan(OFFSET + 73)
        val CUBIC = asNan(OFFSET + 74)
        val A_SPLINE_LOOP = asNan(OFFSET + 75)

        private const val API_LEVEL6_MAX = OFFSET + 50
        private const val FP_TO_RAD = 57.29578f
        private const val FP_TO_DEG = 0.017453292f

        private var sRandom: Random = Random

        private val NO_OF_OPS = intArrayOf(
            -1, // no op
            2, 2, 2, 2, 2, // + - * / %
            2, 2, 2, // min max, power
            1, 1, 1, 1, 1, 1, 1, 1, // sqrt,abs,CopySign,exp,floor,log,ln
            1, 1, 1, 1, 1, 1, 1, 2, // round,sin,cos,tan,asin,acos,atan,atan2
            3, 3, 3, 1, 1, 1, 1, 0, 0, 0, // mad, ?:, clamp, cbrt, deg, rad, ceil, a[0],a[1],a[2]
            1, // log2
            1, // inv
            1, // fract
            2, // ping_pong
            1, // nop
            1, 1, 1, 1, // store
            0, 0, 0, 0, // load
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            1, 5, 2 // change_sign, cubic, a_spline_loop
        )

        init {
            var k = 0
            sNames.put(k++, "NOP")
            sNames.put(k++, "+")
            sNames.put(k++, "-")
            sNames.put(k++, "*")
            sNames.put(k++, "/")
            sNames.put(k++, "%")
            sNames.put(k++, "min")
            sNames.put(k++, "max")
            sNames.put(k++, "pow")
            sNames.put(k++, "sqrt")
            sNames.put(k++, "abs")
            sNames.put(k++, "sign")
            sNames.put(k++, "copySign")
            sNames.put(k++, "exp")
            sNames.put(k++, "floor")
            sNames.put(k++, "log")
            sNames.put(k++, "ln")
            sNames.put(k++, "round")
            sNames.put(k++, "sin")
            sNames.put(k++, "cos")
            sNames.put(k++, "tan")
            sNames.put(k++, "asin")
            sNames.put(k++, "acos")
            sNames.put(k++, "atan")
            sNames.put(k++, "atan2")
            sNames.put(k++, "mad")
            sNames.put(k++, "ifElse")
            sNames.put(k++, "clamp")
            sNames.put(k++, "cbrt")
            sNames.put(k++, "deg")
            sNames.put(k++, "rad")
            sNames.put(k++, "ceil")
            sNames.put(k++, "A_DEREF")
            sNames.put(k++, "A_MAX")
            sNames.put(k++, "A_MIN")
            sNames.put(k++, "A_SUM")
            sNames.put(k++, "A_AVG")
            sNames.put(k++, "A_LEN")
            sNames.put(k++, "A_SPLINE")
            sNames.put(k++, "RAND")
            sNames.put(k++, "RAND_SEED")
            sNames.put(k++, "noise_from")
            sNames.put(k++, "rand_in_range")
            sNames.put(k++, "square_sum")
            sNames.put(k++, "step")
            sNames.put(k++, "square")
            sNames.put(k++, "dup")
            sNames.put(k++, "hypot")
            sNames.put(k++, "swap")
            sNames.put(k++, "lerp")
            sNames.put(k++, "smooth_step")
            sNames.put(k++, "log2")
            sNames.put(k++, "inv")
            sNames.put(k++, "fract")
            sNames.put(k++, "ping_pong")
            sNames.put(k++, "nop")
            sNames.put(k++, "store0")
            sNames.put(k++, "store1")
            sNames.put(k++, "store2")
            sNames.put(k++, "store3")
            sNames.put(k++, "load0")
            sNames.put(k++, "load1")
            sNames.put(k++, "load2")
            sNames.put(k++, "load3")
            k = 70
            sNames.put(k++, "a[0]")
            sNames.put(k++, "a[1]")
            sNames.put(k++, "a[2]")
            sNames.put(k++, "change_sign")
            sNames.put(k++, "cubic")
            sNames.put(k++, "a_spline_loop")
        }

        /**
         * Is float a math operator
         */
        fun isMathOperator(v: Float): Boolean {
            if (v.isNaN()) {
                val pos = fromNaN(v)
                if (NanMap.isDataVariable(v)) return false
                return pos > OFFSET && pos <= LAST_OP
            }
            return false
        }

        /**
         * Given a float command return its math name
         */
        fun toMathName(f: Float): String? {
            val id = fromNaN(f) - OFFSET
            return sNames.get(id)
        }

        /**
         * Convert an expression encoded as an array of floats to a string
         */
        fun toString(exp: FloatArray, labels: Array<String?>?): String {
            val s = StringBuilder()
            for (i in exp.indices) {
                val v = exp[i]
                if (v.isNaN()) {
                    if (isMathOperator(v)) {
                        s.append(toMathName(v))
                    } else {
                        val id = fromNaN(v)
                        val idString = if (id > NanMap.ID_REGION_ARRAY) "A_${id and 0xFFFFF}" else "$id"
                        s.append("[$idString]")
                    }
                } else {
                    if (labels != null && labels[i] != null) {
                        s.append(labels[i])
                        if (!labels[i]!!.contains("_")) {
                            s.append(v)
                        }
                    } else {
                        s.append(v)
                    }
                }
                s.append(" ")
            }
            return s.toString()
        }

        internal fun toString(exp: FloatArray, sp: Int): String {
            if (exp[sp].isNaN()) {
                val id = fromNaN(exp[sp]) - OFFSET
                when (NO_OF_OPS[id]) {
                    -1 -> return "nop"
                    1 -> return "${sNames.get(id)}(${toString(exp, sp + 1)}) "
                    2 -> return if (infix(id)) {
                        "(${toString(exp, sp + 1)}${sNames.get(id)} ${toString(exp, sp + 2)}) "
                    } else {
                        "${sNames.get(id)}(${toString(exp, sp + 1)}, ${toString(exp, sp + 2)})"
                    }
                    3 -> return if (infix(id)) {
                        "((${toString(exp, sp + 1)}) ? ${toString(exp, sp + 2)}:${toString(exp, sp + 3)})"
                    } else {
                        "${sNames.get(id)}(${toString(exp, sp + 1)}, ${toString(exp, sp + 2)}, ${toString(exp, sp + 3)})"
                    }
                }
            }
            return exp[sp].toString()
        }

        internal fun infix(n: Int): Boolean {
            return (n < 6) || (n == 25) || (n == 26)
        }

        fun asNan(v: Int): Float {
            return Float.fromBits(v or -0x800000)
        }

        fun fromNaN(v: Float): Int {
            val b = v.toRawBits()
            return b and 0x7FFFFF
        }

        // Op constants
        private const val OP_ADD = OFFSET + 1
        private const val OP_SUB = OFFSET + 2
        private const val OP_MUL = OFFSET + 3
        private const val OP_DIV = OFFSET + 4
        private const val OP_MOD = OFFSET + 5
        private const val OP_MIN = OFFSET + 6
        private const val OP_MAX = OFFSET + 7
        private const val OP_POW = OFFSET + 8
        private const val OP_SQRT = OFFSET + 9
        private const val OP_ABS = OFFSET + 10
        private const val OP_SIGN = OFFSET + 11
        private const val OP_COPY_SIGN = OFFSET + 12
        private const val OP_EXP = OFFSET + 13
        private const val OP_FLOOR = OFFSET + 14
        private const val OP_LOG = OFFSET + 15
        private const val OP_LN = OFFSET + 16
        private const val OP_ROUND = OFFSET + 17
        private const val OP_SIN = OFFSET + 18
        private const val OP_COS = OFFSET + 19
        private const val OP_TAN = OFFSET + 20
        private const val OP_ASIN = OFFSET + 21
        private const val OP_ACOS = OFFSET + 22
        private const val OP_ATAN = OFFSET + 23
        private const val OP_ATAN2 = OFFSET + 24
        private const val OP_MAD = OFFSET + 25
        private const val OP_TERNARY_CONDITIONAL = OFFSET + 26
        private const val OP_CLAMP = OFFSET + 27
        private const val OP_CBRT = OFFSET + 28
        private const val OP_DEG = OFFSET + 29
        private const val OP_RAD = OFFSET + 30
        private const val OP_CEIL = OFFSET + 31
        private const val OP_A_DEREF = OFFSET + 32
        private const val OP_A_MAX = OFFSET + 33
        private const val OP_A_MIN = OFFSET + 34
        private const val OP_A_SUM = OFFSET + 35
        private const val OP_A_AVG = OFFSET + 36
        private const val OP_A_LEN = OFFSET + 37
        private const val OP_A_SPLINE = OFFSET + 38
        private const val OP_RAND = OFFSET + 39
        private const val OP_RAND_SEED = OFFSET + 40
        private const val OP_NOISE_FROM = OFFSET + 41
        private const val OP_RAND_IN_RANGE = OFFSET + 42
        private const val OP_SQUARE_SUM = OFFSET + 43
        private const val OP_STEP = OFFSET + 44
        private const val OP_SQUARE = OFFSET + 45
        private const val OP_DUP = OFFSET + 46
        private const val OP_HYPOT = OFFSET + 47
        private const val OP_SWAP = OFFSET + 48
        private const val OP_LERP = OFFSET + 49
        private const val OP_SMOOTH_STEP = OFFSET + 50
        private const val OP_LOG2 = OFFSET + 51
        private const val OP_INV = OFFSET + 52
        private const val OP_FRACT = OFFSET + 53
        private const val OP_PINGPONG = OFFSET + 54
        private const val OP_NOP = OFFSET + 55
        private const val OP_STORE_R0 = OFFSET + 56
        private const val OP_STORE_R1 = OFFSET + 57
        private const val OP_STORE_R2 = OFFSET + 58
        private const val OP_STORE_R3 = OFFSET + 59
        private const val OP_LOAD_R0 = OFFSET + 60
        private const val OP_LOAD_R1 = OFFSET + 61
        private const val OP_LOAD_R2 = OFFSET + 62
        private const val OP_LOAD_R3 = OFFSET + 63
        private const val OP_FIRST_VAR = OFFSET + 70
        private const val OP_SECOND_VAR = OFFSET + 71
        private const val OP_THIRD_VAR = OFFSET + 72
        private const val OP_CHANGE_SIGN = OFFSET + 73
        private const val OP_CUBIC = OFFSET + 74
        private const val OP_A_SPLINE_LOOP = OFFSET + 75
    }
}
