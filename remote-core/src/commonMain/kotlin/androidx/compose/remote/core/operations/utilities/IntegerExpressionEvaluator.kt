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
package androidx.compose.remote.core.operations.utilities

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * High performance Integer expression evaluator
 *
 * The evaluation is based on int opMask, int[]exp
 * exp[i] is an operator if (opMask * (1 shl i) != 0)
 */
class IntegerExpressionEvaluator {

    private var mStack = IntArray(0)
    private var mLocalStack = IntArray(128)
    private var mVar = IntArray(0)

    /**
     * Evaluate an integer expression
     *
     * @param mask bits that are operators
     * @param exp rpn sequence of values and operators
     * @param var variables if the expression is a function
     * @return return the results of evaluating the expression
     */
    fun eval(mask: Int, exp: IntArray, vararg `var`: Int): Int {
        mStack = exp
        mVar = `var`
        var sp = -1
        for (i in mStack.indices) {
            val v = mStack[i]
            if (((1 shl i) and mask) != 0) {
                sp = opEval(sp, v)
            } else {
                mStack[++sp] = v
            }
        }
        return mStack[sp]
    }

    /**
     * Evaluate an integer expression with length
     *
     * @param mask bits that are operators
     * @param exp rpn sequence of values and operators
     * @param len the number of values in the expression
     * @param var variables if the expression is a function
     * @return return the results of evaluating the expression
     */
    fun eval(mask: Int, exp: IntArray, len: Int, vararg `var`: Int): Int {
        exp.copyInto(mLocalStack, endIndex = len)
        mStack = mLocalStack
        mVar = `var`
        var sp = -1
        for (i in 0 until len) {
            val v = mStack[i]
            if (((1 shl i) and mask) != 0) {
                sp = opEval(sp, v)
            } else {
                mStack[++sp] = v
            }
        }
        return mStack[sp]
    }

    /**
     * Evaluate an int expression (debug variant)
     *
     * @param opMask bits that are operators
     * @param exp rpn sequence of values and operators
     * @param var variables if the expression is a function
     * @return return the results of evaluating the expression
     */
    fun evalDB(opMask: Int, exp: IntArray, vararg `var`: Int): Int {
        mStack = exp
        mVar = `var`
        var sp = -1
        for (i in exp.indices) {
            val v = mStack[i]
            if (((1 shl i) and opMask) != 0) {
                sp = opEval(sp, v)
            } else {
                mStack[++sp] = v
            }
        }
        return mStack[sp]
    }

    private fun opEval(sp: Int, id: Int): Int {
        when (id) {
            OP_ADD -> { mStack[sp - 1] = mStack[sp - 1] + mStack[sp]; return sp - 1 }
            OP_SUB -> { mStack[sp - 1] = mStack[sp - 1] - mStack[sp]; return sp - 1 }
            OP_MUL -> { mStack[sp - 1] = mStack[sp - 1] * mStack[sp]; return sp - 1 }
            OP_DIV -> { mStack[sp - 1] = mStack[sp - 1] / mStack[sp]; return sp - 1 }
            OP_MOD -> { mStack[sp - 1] = mStack[sp - 1] % mStack[sp]; return sp - 1 }
            OP_SHL -> { mStack[sp - 1] = mStack[sp - 1] shl mStack[sp]; return sp - 1 }
            OP_SHR -> { mStack[sp - 1] = mStack[sp - 1] shr mStack[sp]; return sp - 1 }
            OP_USHR -> { mStack[sp - 1] = mStack[sp - 1] ushr mStack[sp]; return sp - 1 }
            OP_OR -> { mStack[sp - 1] = mStack[sp - 1] or mStack[sp]; return sp - 1 }
            OP_AND -> { mStack[sp - 1] = mStack[sp - 1] and mStack[sp]; return sp - 1 }
            OP_XOR -> { mStack[sp - 1] = mStack[sp - 1] xor mStack[sp]; return sp - 1 }
            OP_COPY_SIGN -> {
                mStack[sp - 1] = (mStack[sp - 1] xor (mStack[sp] shr 31)) - (mStack[sp] shr 31)
                return sp - 1
            }
            OP_MIN -> { mStack[sp - 1] = min(mStack[sp - 1], mStack[sp]); return sp - 1 }
            OP_MAX -> { mStack[sp - 1] = max(mStack[sp - 1], mStack[sp]); return sp - 1 }
            OP_NEG -> { mStack[sp] = -mStack[sp]; return sp }
            OP_ABS -> { mStack[sp] = abs(mStack[sp]); return sp }
            OP_INCR -> { mStack[sp] = mStack[sp] + 1; return sp }
            OP_DECR -> { mStack[sp] = mStack[sp] - 1; return sp }
            OP_NOT -> { mStack[sp] = mStack[sp].inv(); return sp }
            OP_SIGN -> {
                mStack[sp] = (mStack[sp] shr 31) or ((-mStack[sp]) ushr 31)
                return sp
            }
            OP_CLAMP -> {
                mStack[sp - 2] = min(max(mStack[sp - 2], mStack[sp]), mStack[sp - 1])
                return sp - 2
            }
            OP_TERNARY_CONDITIONAL -> {
                mStack[sp - 2] = if (mStack[sp] > 0) mStack[sp - 1] else mStack[sp - 2]
                return sp - 2
            }
            OP_MAD -> {
                mStack[sp - 2] = mStack[sp] + mStack[sp - 1] * mStack[sp - 2]
                return sp - 2
            }
            OP_FIRST_VAR -> { mStack[sp] = mVar[0]; return sp }
            OP_SECOND_VAR -> { mStack[sp] = mVar[1]; return sp }
            OP_THIRD_VAR -> { mStack[sp] = mVar[2]; return sp }
        }
        return 0
    }

    companion object {
        val sNames = IntMap<String>()
        const val OFFSET = 0x10000

        const val I_ADD = OFFSET + 1
        const val I_SUB = OFFSET + 2
        const val I_MUL = OFFSET + 3
        const val I_DIV = OFFSET + 4
        const val I_MOD = OFFSET + 5
        const val I_SHL = OFFSET + 6
        const val I_SHR = OFFSET + 7
        const val I_USHR = OFFSET + 8
        const val I_OR = OFFSET + 9
        const val I_AND = OFFSET + 10
        const val I_XOR = OFFSET + 11
        const val I_COPY_SIGN = OFFSET + 12
        const val I_MIN = OFFSET + 13
        const val I_MAX = OFFSET + 14
        const val I_NEG = OFFSET + 15
        const val I_ABS = OFFSET + 16
        const val I_INCR = OFFSET + 17
        const val I_DECR = OFFSET + 18
        const val I_NOT = OFFSET + 19
        const val I_SIGN = OFFSET + 20
        const val I_CLAMP = OFFSET + 21
        const val I_IFELSE = OFFSET + 22
        const val I_MAD = OFFSET + 23

        const val LAST_OP = 25f

        const val I_VAR1 = OFFSET + 24
        const val I_VAR2 = OFFSET + 25

        private const val OP_ADD = OFFSET + 1
        private const val OP_SUB = OFFSET + 2
        private const val OP_MUL = OFFSET + 3
        private const val OP_DIV = OFFSET + 4
        private const val OP_MOD = OFFSET + 5
        private const val OP_SHL = OFFSET + 6
        private const val OP_SHR = OFFSET + 7
        private const val OP_USHR = OFFSET + 8
        private const val OP_OR = OFFSET + 9
        private const val OP_AND = OFFSET + 10
        private const val OP_XOR = OFFSET + 11
        private const val OP_COPY_SIGN = OFFSET + 12
        private const val OP_MIN = OFFSET + 13
        private const val OP_MAX = OFFSET + 14
        private const val OP_NEG = OFFSET + 15
        private const val OP_ABS = OFFSET + 16
        private const val OP_INCR = OFFSET + 17
        private const val OP_DECR = OFFSET + 18
        private const val OP_NOT = OFFSET + 19
        private const val OP_SIGN = OFFSET + 20
        private const val OP_CLAMP = OFFSET + 21
        private const val OP_TERNARY_CONDITIONAL = OFFSET + 22
        private const val OP_MAD = OFFSET + 23
        private const val OP_FIRST_VAR = OFFSET + 24
        private const val OP_SECOND_VAR = OFFSET + 25
        private const val OP_THIRD_VAR = OFFSET + 26

        private val NO_OF_OPS = intArrayOf(
            -1, // no op
            2, 2, 2, 2, 2, // + - * / %
            2, 2, 2, 2, 2, 2, 2, 2, 2, // <<, >> , >>> , | , &, ^, min max
            1, 1, 1, 1, 1, 1, // neg, abs, ++, -- , not , sign
            3, 3, 3, // clamp, ifElse, mad,
            0, 0, 0 // a[0],a[1],a[2]
        )

        init {
            var k = 0
            sNames.put(k++, "NOP")
            sNames.put(k++, "+")
            sNames.put(k++, "-")
            sNames.put(k++, "*")
            sNames.put(k++, "/")
            sNames.put(k++, "%")
            sNames.put(k++, "<<")
            sNames.put(k++, ">>")
            sNames.put(k++, ">>>")
            sNames.put(k++, "|")
            sNames.put(k++, "&")
            sNames.put(k++, "^")
            sNames.put(k++, "copySign")
            sNames.put(k++, "min")
            sNames.put(k++, "max")
            sNames.put(k++, "neg")
            sNames.put(k++, "abs")
            sNames.put(k++, "incr")
            sNames.put(k++, "decr")
            sNames.put(k++, "not")
            sNames.put(k++, "sign")
            sNames.put(k++, "clamp")
            sNames.put(k++, "ifElse")
            sNames.put(k++, "mad")
            sNames.put(k++, "ceil")
            sNames.put(k++, "a[0]")
            sNames.put(k++, "a[1]")
            sNames.put(k++, "a[2]")
        }

        /**
         * Given an int command return its math name
         */
        fun toMathName(f: Int): String? {
            val id = f - OFFSET
            return sNames.get(id)
        }

        /**
         * Convert an expression encoded as an array of ints to a string
         */
        fun toString(opMask: Int, exp: IntArray, labels: Array<String>): String {
            val s = StringBuilder()
            for (i in exp.indices) {
                val v = exp[i]
                if (((1 shl i) and opMask) != 0) {
                    if (v < OFFSET) {
                        s.append(toMathName(v))
                    } else {
                        s.append("[$v]")
                    }
                } else {
                    if (labels[i] != null) {
                        s.append(labels[i])
                    }
                    s.append(v)
                }
                s.append(" ")
            }
            return s.toString()
        }

        /**
         * Convert an expression encoded as an array of ints to a string
         */
        fun toString(opMask: Int, exp: IntArray): String {
            val s = StringBuilder()
            s.append(opMask.toString(2))
            s.append(" : ")
            for (i in exp.indices) {
                val v = exp[i]
                if (((1 shl i) and opMask) != 0) {
                    if (v > OFFSET) {
                        s.append(" ")
                        s.append(toMathName(v))
                        s.append(" ")
                    } else {
                        s.append("[$v]")
                    }
                }
                s.append(" $v")
            }
            return s.toString()
        }

        /**
         * This creates an infix string expression
         */
        fun toStringInfix(opMask: Int, exp: IntArray): String {
            return toString(opMask, exp, exp.size - 1)
        }

        internal fun toString(mask: Int, exp: IntArray, sp: Int): String {
            if (((1 shl sp) and mask) != 0) {
                val id = exp[sp] - OFFSET
                when (NO_OF_OPS[id]) {
                    -1 -> return "nop"
                    1 -> return "${sNames.get(id)}(${toString(mask, exp, sp - 1)}) "
                    2 -> return if (infix(id)) {
                        "(${toString(mask, exp, sp - 2)} ${sNames.get(id)} ${toString(mask, exp, sp - 1)}) "
                    } else {
                        "${sNames.get(id)}(${toString(mask, exp, sp - 2)}, ${toString(mask, exp, sp - 1)})"
                    }
                    3 -> return if (infix(id)) {
                        "((${toString(mask, exp, sp + 3)}) ? ${toString(mask, exp, sp - 2)}:${toString(mask, exp, sp - 1)})"
                    } else {
                        "${sNames.get(id)}(${toString(mask, exp, sp - 3)}, ${toString(mask, exp, sp - 2)}, ${toString(mask, exp, sp - 1)})"
                    }
                }
            }
            return exp[sp].toString()
        }

        internal fun infix(n: Int): Boolean = n < 12

        /**
         * Is it an id or operation
         */
        fun isOperation(opMask: Int, i: Int): Boolean {
            return ((1 shl i) and opMask) != 0
        }
    }
}
