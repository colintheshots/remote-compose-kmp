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

/** High performance matrix processing engine */
class MatrixOperations {

    private val mMatrices = Array(10) { Matrix(4, 4) }
    private val mTmpMatrix = Matrix()
    private var mMatrixIndex = -1

    private var mStack = FloatArray(0)
    private var mVar = FloatArray(0)

    /**
     * Evaluate the matrix expression
     *
     * @param exp expression encoded as an array of floats
     * @param var variables
     * @return resulting Matrix object
     */
    fun eval(exp: FloatArray, vararg `var`: Float): Matrix {
        mStack = exp
        mVar = `var`
        mMatrixIndex = 0
        mMatrices[mMatrixIndex].setIdentity()
        for (i in mStack.indices) {
            val v = mStack[i]
            if (v.isNaN()) {
                opEval(i, fromNaN(v))
            }
        }
        return mMatrices[0]
    }

    private fun opEval(sp: Int, id: Int) {
        when (id) {
            OP_IDENTITY -> mMatrices[++mMatrixIndex].setIdentity()
            OP_ROT_X -> mMatrices[mMatrixIndex].rotateX(mStack[sp - 1])
            OP_ROT_Y -> mMatrices[mMatrixIndex].rotateY(mStack[sp - 1])
            OP_ROT_Z -> mMatrices[mMatrixIndex].rotateZ(mStack[sp - 1])
            OP_TRANSLATE_X -> mMatrices[mMatrixIndex].translate(mStack[sp - 1], 0f, 0f)
            OP_TRANSLATE_Y -> mMatrices[mMatrixIndex].translate(0f, mStack[sp - 1], 0f)
            OP_TRANSLATE_Z -> mMatrices[mMatrixIndex].translate(0f, 0f, mStack[sp - 1])
            OP_TRANSLATE2 -> mMatrices[mMatrixIndex].translate(mStack[sp - 2], mStack[sp - 1], 0f)
            OP_TRANSLATE3 -> mMatrices[mMatrixIndex].translate(
                mStack[sp - 3], mStack[sp - 2], mStack[sp - 1]
            )
            OP_SCALE_X -> mMatrices[mMatrixIndex].setScale(mStack[sp - 1], 1f, 1f)
            OP_SCALE_Y -> mMatrices[mMatrixIndex].setScale(1f, mStack[sp - 1], 1f)
            OP_SCALE_Z -> mMatrices[mMatrixIndex].setScale(1f, 1f, mStack[sp - 1])
            OP_SCALE2 -> mMatrices[mMatrixIndex].setScale(mStack[sp - 2], mStack[sp - 1], 0f)
            OP_SCALE3 -> mMatrices[mMatrixIndex].setScale(
                mStack[sp - 3], mStack[sp - 2], mStack[sp - 1]
            )
            OP_MUL -> {
                Matrix.multiply(mMatrices[mMatrixIndex - 1], mMatrices[mMatrixIndex], mTmpMatrix)
                mMatrices[mMatrixIndex - 1].copyFrom(mTmpMatrix)
                mMatrixIndex--
            }
            OP_ROT_PZ -> mMatrices[mMatrixIndex].rotateZ(
                mStack[sp - 2], mStack[sp - 1], mStack[sp - 3]
            )
            OP_ROT_AXIS -> mMatrices[mMatrixIndex].rotateAroundAxis(
                mStack[sp - 3], mStack[sp - 2], mStack[sp - 1], mStack[sp - 4]
            )
            OP_PROJECTION -> mMatrices[mMatrixIndex].projection(
                mStack[sp - 4], mStack[sp - 3], mStack[sp - 2], mStack[sp - 1]
            )
        }
    }

    companion object {
        /** The START POINT in the float NaN space for operators */
        const val OFFSET = 0x320_000

        /** Add identity operator */
        val IDENTITY = asNan(OFFSET + 1)

        /** ROT X axis operator */
        val ROT_X = asNan(OFFSET + 2)

        /** ROT Y axis operator */
        val ROT_Y = asNan(OFFSET + 3)

        /** ROT Z axis operator */
        val ROT_Z = asNan(OFFSET + 4)

        /** TRANSLATE x axis operator */
        val TRANSLATE_X = asNan(OFFSET + 5)

        /** TRANSLATE y axis operator */
        val TRANSLATE_Y = asNan(OFFSET + 6)

        /** TRANSLATE z axis operator */
        val TRANSLATE_Z = asNan(OFFSET + 7)

        /** TRANSLATE x,y axis operator */
        val TRANSLATE2 = asNan(OFFSET + 8)

        /** TRANSLATE x,y,z axis operator */
        val TRANSLATE3 = asNan(OFFSET + 9)

        /** SCALE X axis operator */
        val SCALE_X = asNan(OFFSET + 10)

        /** SCALE Y axis operator */
        val SCALE_Y = asNan(OFFSET + 11)

        /** SCALE Z axis operator */
        val SCALE_Z = asNan(OFFSET + 12)

        /** SCALE2 operator */
        val SCALE2 = asNan(OFFSET + 13)

        /** SCALE3 operator */
        val SCALE3 = asNan(OFFSET + 14)

        /** Multiply operator */
        val MUL = asNan(OFFSET + 15)

        /** ROT about pivot z axis operator */
        val ROT_PZ = asNan(OFFSET + 16)

        /** Rotate about a vector operator */
        val ROT_AXIS = asNan(OFFSET + 17)

        /** Add a projection matrix */
        val PROJECTION = asNan(OFFSET + 18)

        /** LAST valid operator */
        const val LAST_OP = OFFSET + 54

        val sNames = IntMap<String>()

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
        }

        /**
         * Get the max op for a given API level
         */
        fun getMaxOpForLevel(level: Int): Int {
            return if (level == 7) LAST_OP else 0
        }

        /**
         * Is float a math operator
         */
        fun isOperator(v: Float): Boolean {
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
                    if (isOperator(v)) {
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

        /**
         * Convert an id into a NaN object
         */
        fun asNan(v: Int): Float {
            return Float.fromBits(v or -0x800000)
        }

        /**
         * Get ID from a NaN float
         */
        fun fromNaN(v: Float): Int {
            val b = v.toRawBits()
            return b and 0x7FFFFF
        }

        // Internal op constants
        private const val OP_IDENTITY = OFFSET + 1
        private const val OP_ROT_X = OFFSET + 2
        private const val OP_ROT_Y = OFFSET + 3
        private const val OP_ROT_Z = OFFSET + 4
        private const val OP_TRANSLATE_X = OFFSET + 5
        private const val OP_TRANSLATE_Y = OFFSET + 6
        private const val OP_TRANSLATE_Z = OFFSET + 7
        private const val OP_TRANSLATE2 = OFFSET + 8
        private const val OP_TRANSLATE3 = OFFSET + 9
        private const val OP_SCALE_X = OFFSET + 10
        private const val OP_SCALE_Y = OFFSET + 11
        private const val OP_SCALE_Z = OFFSET + 12
        private const val OP_SCALE2 = OFFSET + 13
        private const val OP_SCALE3 = OFFSET + 14
        private const val OP_MUL = OFFSET + 15
        private const val OP_ROT_PZ = OFFSET + 16
        private const val OP_ROT_AXIS = OFFSET + 17
        private const val OP_PROJECTION = OFFSET + 18
    }
}
