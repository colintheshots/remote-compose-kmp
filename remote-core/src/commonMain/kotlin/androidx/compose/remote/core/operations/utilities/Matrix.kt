/*
 * Copyright (C) 2025 The Android Open Source Project
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

import androidx.compose.remote.core.operations.Utils
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/** This Matrix class is used to represent up to 4x4 matrix. */
class Matrix {
    var mDim0 = 4
        private set
    var mDim1 = 4
        private set
    var mMatrix: FloatArray = FloatArray(16)
        private set

    /** Creates a new identity matrix. */
    constructor() {
        setIdentity()
    }

    /**
     * Creates a new matrix with the given dimensions.
     *
     * @param dim0 The number of rows.
     * @param dim1 The number of columns.
     */
    constructor(dim0: Int, dim1: Int) {
        setDimensions(dim0, dim1)
    }

    /**
     * Sets the dimensions of the matrix.
     *
     * @param dim0 The number of rows.
     * @param dim1 The number of columns.
     */
    fun setDimensions(dim0: Int, dim1: Int) {
        this.mDim0 = dim0
        this.mDim1 = dim1
        this.mMatrix = FloatArray(dim0 * dim1)
    }

    /**
     * Copies the values from one matrix to another.
     *
     * @param src The source matrix.
     */
    fun copyFrom(src: Matrix) {
        setDimensions(src.mDim0, src.mDim1)
        src.mMatrix.copyInto(mMatrix)
    }

    /** Sets the matrix to the identity matrix. */
    fun setIdentity() {
        mMatrix.fill(0.0f)
        for (i in 0 until min(mDim0, mDim1)) {
            mMatrix[i * mDim1 + i] = 1.0f
        }
    }

    private fun getIndex(row: Int, col: Int): Int {
        if (row < 0 || row >= mDim0 || col < 0 || col >= mDim1) {
            throw IndexOutOfBoundsException(
                "Matrix index ($row, $col) out of bounds for (${mDim0}x${mDim1})"
            )
        }
        return row * mDim1 + col
    }

    /**
     * Gets the value of the matrix element.
     *
     * @param row The row index.
     * @param col The column index.
     * @return The value of the matrix element.
     */
    operator fun get(row: Int, col: Int): Float {
        return mMatrix[getIndex(row, col)]
    }

    /**
     * Sets the value of the matrix element.
     *
     * @param row The row index.
     * @param col The column index.
     * @param value The value to set.
     */
    operator fun set(row: Int, col: Int, value: Float) {
        mMatrix[getIndex(row, col)] = value
    }

    /**
     * Rotates the matrix around the X axis.
     *
     * @param degrees The rotation angle in degrees.
     */
    fun rotateX(degrees: Float) {
        val angleRadians = degrees * DEG_TO_RAD
        val cosTheta = cos(angleRadians)
        val sinTheta = sin(angleRadians)
        sTmpMatrix1.setIdentity()
        sTmpMatrix1[1, 1] = cosTheta
        sTmpMatrix1[1, 2] = -sinTheta
        sTmpMatrix1[2, 1] = sinTheta
        sTmpMatrix1[2, 2] = cosTheta
        multiply(this, sTmpMatrix1, sTmpMatrix2)
        copyFrom(sTmpMatrix2)
    }

    /**
     * Rotates the matrix around the Y axis.
     *
     * @param degrees The rotation angle in degrees.
     */
    fun rotateY(degrees: Float) {
        val angleRadians = degrees * DEG_TO_RAD
        val cosTheta = cos(angleRadians)
        val sinTheta = sin(angleRadians)
        sTmpMatrix1.setIdentity()
        sTmpMatrix1[0, 0] = cosTheta
        sTmpMatrix1[0, 2] = sinTheta
        sTmpMatrix1[2, 0] = -sinTheta
        sTmpMatrix1[2, 2] = cosTheta
        multiply(this, sTmpMatrix1, sTmpMatrix2)
        copyFrom(sTmpMatrix2)
    }

    /**
     * Rotates the matrix around the Z axis.
     *
     * @param degrees The rotation angle in degrees.
     */
    fun rotateZ(degrees: Float) {
        val angleRadians = degrees * DEG_TO_RAD
        val cosTheta = cos(angleRadians)
        val sinTheta = sin(angleRadians)
        sTmpMatrix1.setIdentity()
        sTmpMatrix1[0, 0] = cosTheta
        sTmpMatrix1[0, 1] = -sinTheta
        sTmpMatrix1[1, 0] = sinTheta
        sTmpMatrix1[1, 1] = cosTheta
        multiply(this, sTmpMatrix1, sTmpMatrix2)
        copyFrom(sTmpMatrix2)
    }

    /**
     * Translates the matrix.
     *
     * @param x The translation amount in the X direction.
     * @param y The translation amount in the Y direction.
     * @param z The translation amount in the Z direction.
     */
    fun translate(x: Float, y: Float, z: Float) {
        sTmpMatrix1.setIdentity()
        sTmpMatrix1[0, 3] = x
        sTmpMatrix1[1, 3] = y
        sTmpMatrix1[2, 3] = z
        multiply(this, sTmpMatrix1, sTmpMatrix2)
        copyFrom(sTmpMatrix2)
    }

    /**
     * Formats the matrix into a string.
     *
     * @return The formatted string.
     */
    override fun toString(): String {
        val str = StringBuilder()
        for (i in 0 until mDim0) {
            for (j in 0 until mDim1) {
                if (j != 0) str.append(" ")
                val s = formatFloat(mMatrix[i * mDim1 + j])
                str.append(six(s))
            }
            str.append("\n")
        }
        return str.toString()
    }

    /**
     * Scales the matrix.
     *
     * @param x The scaling factor in the X direction.
     * @param y The scaling factor in the Y direction.
     * @param z The scaling factor in the Z direction.
     */
    fun setScale(x: Float, y: Float, z: Float) {
        mMatrix[getIndex(0, 0)] *= x
        mMatrix[getIndex(1, 1)] *= y
        mMatrix[getIndex(2, 2)] *= z
    }

    /**
     * Rotates the matrix around the Z axis with a pivot point.
     *
     * @param pivotX The pivot point in the X direction.
     * @param pivotY The pivot point in the Y direction.
     * @param degrees The rotation angle in degrees.
     */
    fun rotateZ(pivotX: Float, pivotY: Float, degrees: Float) {
        val angleRadians = degrees * DEG_TO_RAD
        val cosTheta = cos(angleRadians)
        val sinTheta = sin(angleRadians)
        val oneMinusCos = 1.0f - cosTheta

        val tx = pivotX * oneMinusCos + pivotY * sinTheta
        val ty = pivotY * oneMinusCos - pivotX * sinTheta

        val resultData = FloatArray(16)

        for (i in 0 until 4) {
            for (j in 0 until 4) {
                var sum = 0.0f
                for (k in 0 until 4) {
                    val mIk = when {
                        i == 0 -> when (k) {
                            0 -> cosTheta
                            1 -> -sinTheta
                            3 -> tx
                            else -> 0.0f
                        }
                        i == 1 -> when (k) {
                            0 -> sinTheta
                            1 -> cosTheta
                            3 -> ty
                            else -> 0.0f
                        }
                        i == 2 -> if (k == 2) 1.0f else 0.0f
                        else -> if (k == 3) 1.0f else 0.0f
                    }
                    val thisKj = this.mMatrix[k * 4 + j]
                    sum += mIk * thisKj
                }
                resultData[i * 4 + j] = sum
            }
        }
        resultData.copyInto(this.mMatrix)
    }

    /**
     * Copies the values from the matrix to an array.
     *
     * @param dest The destination array.
     */
    fun putValues(dest: FloatArray) {
        for (i in dest.indices) {
            dest[i] = mMatrix[i]
        }
    }

    /**
     * Sets up a projection matrix.
     *
     * @param fovDegrees The field of view in degrees.
     * @param aspectRatio The aspect ratio of the viewport.
     * @param near The near clipping plane distance.
     * @param far The far clipping plane distance.
     */
    fun projection(fovDegrees: Float, aspectRatio: Float, near: Float, far: Float) {
        val matrix = sTmpMatrix1.mMatrix
        val fovRadians = fovDegrees * DEG_TO_RAD
        val f = 1.0f / tan(fovRadians / 2.0f)
        val rangeInv = 1.0f / (near - far)

        matrix[0] = f / aspectRatio
        matrix[1] = 0.0f
        matrix[2] = 0.0f
        matrix[3] = 0.0f
        matrix[4] = 0.0f
        matrix[5] = f
        matrix[6] = 0.0f
        matrix[7] = 0.0f
        matrix[8] = 0.0f
        matrix[9] = 0.0f
        matrix[10] = (far + near) * rangeInv
        matrix[11] = -1.0f
        matrix[12] = 0.0f
        matrix[13] = 0.0f
        matrix[14] = (2.0f * far * near) * rangeInv
        matrix[15] = 0.0f

        multiply(this, sTmpMatrix1, sTmpMatrix2)
        copyFrom(sTmpMatrix2)
    }

    /**
     * Copies the values from an array to the matrix.
     *
     * @param values The source array.
     */
    fun copyFrom(values: FloatArray) {
        if (values.size == 16) {
            values.copyInto(mMatrix)
        } else if (values.size == 9) {
            mMatrix[0] = values[0]
            mMatrix[1] = values[1]
            mMatrix[3] = values[2]
            mMatrix[4] = values[3]
            mMatrix[5] = values[4]
            mMatrix[6] = values[5]
            mMatrix[8] = values[6]
            mMatrix[9] = values[7]
            mMatrix[10] = values[8]
            mMatrix[11] = 0.0f
            mMatrix[12] = 0.0f
            mMatrix[13] = 0.0f
            mMatrix[14] = 0.0f
            mMatrix[15] = 1.0f
        }
    }

    /**
     * Applies a rotation around an arbitrary axis vector (vx, vy, vz) passing through the origin.
     * Assumes this is a 4x4 matrix. Performs: this = R_axis * this.
     *
     * @param vx The x-component of the rotation axis vector.
     * @param vy The y-component of the rotation axis vector.
     * @param vz The z-component of the rotation axis vector.
     * @param angleDegrees The rotation angle in degrees.
     */
    fun rotateAroundAxis(vx: Float, vy: Float, vz: Float, angleDegrees: Float) {
        val angleRadians = angleDegrees * DEG_TO_RAD_DOUBLE
        val lenSq = vx * vx + vy * vy + vz * vz
        if (lenSq == 0.0f) {
            if (angleRadians != 0.0) {
                println(
                    "Warning: Rotation axis vector is zero. No rotation applied for non-zero angle."
                )
            }
            return
        }
        val len = sqrt(lenSq.toDouble()).toFloat()
        val ux = vx / len
        val uy = vy / len
        val uz = vz / len

        val cosTheta = cos(angleRadians).toFloat()
        val sinTheta = sin(angleRadians).toFloat()
        val oneMinusCos = 1.0f - cosTheta

        val r00 = cosTheta + ux * ux * oneMinusCos
        val r01 = ux * uy * oneMinusCos - uz * sinTheta
        val r02 = ux * uz * oneMinusCos + uy * sinTheta

        val r10 = uy * ux * oneMinusCos + uz * sinTheta
        val r11 = cosTheta + uy * uy * oneMinusCos
        val r12 = uy * uz * oneMinusCos - ux * sinTheta

        val r20 = uz * ux * oneMinusCos - uy * sinTheta
        val r21 = uz * uy * oneMinusCos + ux * sinTheta
        val r22 = cosTheta + uz * uz * oneMinusCos

        val resultData = sTmpMatrix1.mMatrix

        for (i in 0 until 4) {
            for (j in 0 until 4) {
                var sum = 0.0f
                for (k in 0 until 4) {
                    val rIk = when {
                        i == 0 -> when (k) {
                            0 -> r00; 1 -> r01; 2 -> r02; else -> 0.0f
                        }
                        i == 1 -> when (k) {
                            0 -> r10; 1 -> r11; 2 -> r12; else -> 0.0f
                        }
                        i == 2 -> when (k) {
                            0 -> r20; 1 -> r21; 2 -> r22; else -> 0.0f
                        }
                        else -> if (k == 3) 1.0f else 0.0f
                    }
                    val thisKj = this.mMatrix[k * 4 + j]
                    sum += rIk * thisKj
                }
                resultData[i * 4 + j] = sum
            }
        }
        multiply(this, sTmpMatrix1, sTmpMatrix2)
        copyFrom(sTmpMatrix2)
        resultData.copyInto(this.mMatrix, endIndex = 16)
    }

    /**
     * The matrix multiplication operation.
     *
     * @param input input vector
     * @param out output vector
     */
    fun multiply(input: FloatArray, out: FloatArray) {
        for (j in out.indices) {
            var tmp = 0f
            for (i in input.indices) {
                tmp += mMatrix[i + j * 4] * input[i]
            }
            out[j] = tmp + mMatrix[3 + j * 4]
        }
    }

    /**
     * The matrix multiplication operation. This can also be used to perform perspective transform.
     *
     * @param input input needs to be 2, 3, or 4 floats
     * @param out output needs to be 2, 3, or 4 floats
     */
    fun evalPerspective(input: FloatArray, out: FloatArray) {
        if (input.size < 4) {
            if (sTempInVec == null) {
                sTempInVec = FloatArray(4)
                sTempOutVec = FloatArray(4)
                sTempInVec!![3] = 1f
                Utils.log("perspective transform ")
            }
            input.copyInto(sTempInVec!!, endIndex = input.size)
        }

        val inVec = sTempInVec!!
        val outVec = sTempOutVec!!
        for (j in outVec.indices) {
            var tmp = 0f
            for (i in inVec.indices) {
                tmp += mMatrix[i + j * 4] * inVec[i]
            }
            outVec[j] = tmp
        }

        for (i in out.indices) {
            outVec[i] /= outVec[3]
        }

        outVec.copyInto(out, endIndex = out.size)
    }

    companion object {
        val sTmpMatrix1 = Matrix()
        val sTmpMatrix2 = Matrix()
        var sTempOutVec: FloatArray? = null
        var sTempInVec: FloatArray? = null

        private const val DEG_TO_RAD = (kotlin.math.PI / 180.0).toFloat()
        private const val DEG_TO_RAD_DOUBLE = kotlin.math.PI / 180.0

        /**
         * Copies the values from one matrix to another.
         *
         * @param src The source matrix.
         * @param dest The destination matrix.
         */
        fun copy(src: Matrix, dest: Matrix) {
            dest.setDimensions(src.mDim0, src.mDim1)
            src.mMatrix.copyInto(dest.mMatrix)
        }

        /**
         * The matrix multiplication operation.
         *
         * @param a The first matrix.
         * @param b The second matrix.
         * @param dest The destination matrix.
         */
        fun multiply(a: Matrix, b: Matrix, dest: Matrix) {
            dest.setDimensions(a.mDim0, b.mDim1)
            for (i in 0 until dest.mDim0) {
                for (j in 0 until dest.mDim1) {
                    var sum = 0.0f
                    for (k in 0 until a.mDim1) {
                        sum += a.mMatrix[i * a.mDim1 + k] * b.mMatrix[k * b.mDim1 + j]
                    }
                    dest.mMatrix[i * dest.mDim1 + j] = sum
                }
            }
        }

        private fun six(s: String): String {
            return if (s.length < 6) s.padStart(6) else s
        }

        private fun formatFloat(v: Float): String {
            // Simple two-decimal format without DecimalFormat
            val intPart = v.toInt()
            val fracPart = ((v - intPart) * 100).toInt()
            val sign = if (v < 0 && intPart == 0) "-" else ""
            return "$sign$intPart.${kotlin.math.abs(fracPart).toString().padStart(2, '0')}"
        }
    }
}
