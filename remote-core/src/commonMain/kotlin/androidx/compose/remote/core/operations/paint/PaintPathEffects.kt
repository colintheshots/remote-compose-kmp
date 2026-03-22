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
package androidx.compose.remote.core.operations.paint

import androidx.compose.remote.core.operations.Utils

abstract class PaintPathEffects {
    var mType: Int = 0
    var mDataLength: Int = 0

    /** Interface to register a path effect */
    fun interface Register {
        fun id(id: Int)
    }

    abstract fun toFloatArray(): FloatArray

    /** Get the type of path effect */
    fun getType(): Int = mType

    class Dash(var mPhase: Float, vararg var mIntervals: Float) : PaintPathEffects() {
        init {
            mType = DASH
        }

        override fun toFloatArray(): FloatArray = dash(mPhase, *mIntervals)

        companion object {
            fun decode(data: FloatArray, offset: Int): PaintPathEffects {
                val phase = data[offset]
                val intervals = FloatArray(data[offset + 1].toRawBits())
                data.copyInto(intervals, 0, offset + 2, offset + 2 + intervals.size)
                val ret = Dash(phase, *intervals)
                ret.mDataLength = intervals.size + 2
                return ret
            }

            fun gitIds(data: IntArray, offset: Int, register: Register): Int {
                registerIfId(data, offset, register)
                val count = data[offset + 1]
                for (i in 0 until count) {
                    registerIfId(data, offset + 2 + i, register)
                }
                return offset + 2 + count
            }
        }
    }

    /** Chop the path into lines of segmentLength, randomly deviating from the original path */
    class Discrete(var mSegmentLength: Float, var mDeviation: Float) : PaintPathEffects() {
        init {
            mType = DISCRETE_PATH
        }

        override fun toFloatArray(): FloatArray = discrete(mSegmentLength, mDeviation)

        companion object {
            fun decode(data: FloatArray, offset: Int): PaintPathEffects {
                val segmentLength = data[offset]
                val deviation = data[offset + 1]
                val ret = Discrete(segmentLength, deviation)
                ret.mDataLength = 2
                return ret
            }

            fun gitIds(data: IntArray, offset: Int, register: Register): Int {
                registerIfId(data, offset, register)
                registerIfId(data, offset + 1, register)
                return offset + 2
            }
        }
    }

    /** class represents a dash path effect */
    class PathDash(
        var mShapeId: Int,
        var mAdvance: Float,
        var mPhase: Float,
        var mStyle: Int
    ) : PaintPathEffects() {
        init {
            mType = PATH_DASH
        }

        override fun toFloatArray(): FloatArray = pathDash(mShapeId, mAdvance, mPhase, mStyle)

        companion object {
            fun decode(data: FloatArray, offset: Int): PaintPathEffects {
                val shapeId = data[offset].toRawBits()
                val advance = data[offset + 1]
                val phase = data[offset + 2]
                val style = data[offset + 3].toRawBits()
                val ret = PathDash(shapeId, advance, phase, style)
                ret.mDataLength = 4
                return ret
            }

            fun gitIds(data: IntArray, offset: Int, register: Register): Int {
                registerIfId(data, offset + 1, register) // advance
                registerIfId(data, offset + 2, register) // phase
                return offset + 4
            }
        }
    }

    /** Sum two path effects together */
    class Sum(var mFirst: PaintPathEffects, var mSecond: PaintPathEffects) : PaintPathEffects() {
        init {
            mType = SUM
        }

        override fun toFloatArray(): FloatArray {
            val f = encode(mFirst)
            val s = encode(mSecond)
            return fuze(SUM, f, s)
        }

        companion object {
            fun decode(data: FloatArray, offset: Int): PaintPathEffects {
                val first = parse(data, offset)
                val second = parse(data, offset + first.mDataLength + 1)
                val ret = Sum(first, second)
                ret.mDataLength = first.mDataLength + second.mDataLength + 1
                return ret
            }

            fun gitIds(data: IntArray, offset: Int, register: Register): Int {
                var off = getIds(data, offset, register)
                off = getIds(data, off, register)
                return off
            }
        }
    }

    /** Compose two path effects together */
    class Compose(var mOuterPE: PaintPathEffects, var mInnerPE: PaintPathEffects) :
        PaintPathEffects() {
        init {
            mType = COMPOSE
        }

        override fun toFloatArray(): FloatArray {
            val f = encode(mOuterPE)
            val s = encode(mInnerPE)
            return fuze(COMPOSE, f, s)
        }

        companion object {
            fun decode(data: FloatArray, offset: Int): PaintPathEffects {
                val outerPE = parse(data, offset)
                val innerPE = parse(data, offset + outerPE.mDataLength + 1)
                val ret = Compose(outerPE, innerPE)
                ret.mDataLength = outerPE.mDataLength + innerPE.mDataLength + 1
                return ret
            }

            @Suppress("UNUSED_PARAMETER")
            fun gitIds(data: IntArray, offset: Int, register: Register): Int = 0
        }
    }

    companion object {
        const val DASH = 1
        const val DISCRETE_PATH = 2
        const val PATH_DASH = 3
        const val SUM = 4
        const val COMPOSE = 5

        /** Get the ids for the path effects */
        fun getIds(data: IntArray, offset: Int, register: Register): Int {
            var off = offset
            return when (data[off++]) {
                DASH -> Dash.gitIds(data, off, register)
                DISCRETE_PATH -> Discrete.gitIds(data, off, register)
                PATH_DASH -> PathDash.gitIds(data, off, register)
                SUM -> Sum.gitIds(data, off, register)
                COMPOSE -> Compose.gitIds(data, off, register)
                else -> {
                    Utils.log("should not get here offset  ${off - 1}  data =${data[off - 1]}")
                    -1
                }
            }
        }

        /** Parse a path effect from a float array */
        fun parse(data: FloatArray, offset: Int): PaintPathEffects {
            var off = offset
            return when (data[off++].toRawBits()) {
                DASH -> Dash.decode(data, off)
                DISCRETE_PATH -> Discrete.decode(data, off)
                PATH_DASH -> PathDash.decode(data, off)
                SUM -> Sum.decode(data, off)
                COMPOSE -> Compose.decode(data, off)
                else -> throw RuntimeException("Unknown type of path effect")
            }
        }

        /** Convert a path effect to a float array */
        fun encode(pe: PaintPathEffects): FloatArray = pe.toFloatArray()

        /** Fuse two path effects together */
        fun fuze(type: Int, a: FloatArray, b: FloatArray): FloatArray {
            val ret = FloatArray(a.size + b.size + 1)
            ret[0] = Float.fromBits(type)
            a.copyInto(ret, 1)
            b.copyInto(ret, a.size + 1)
            return ret
        }

        /** Create a dash path effect */
        fun dash(phase: Float, vararg intervals: Float): FloatArray {
            val ret = FloatArray(intervals.size + 3)
            ret[0] = Float.fromBits(DASH)
            ret[1] = phase
            ret[2] = Float.fromBits(intervals.size)
            intervals.copyInto(ret, 3)
            return ret
        }

        /** Create a discrete path effect */
        fun discrete(segmentLength: Float, deviation: Float): FloatArray {
            val ret = FloatArray(3)
            ret[0] = Float.fromBits(DISCRETE_PATH)
            ret[1] = segmentLength
            ret[2] = deviation
            return ret
        }

        /** Create a pathDash effect */
        fun pathDash(shapeId: Int, advance: Float, phase: Float, style: Int): FloatArray {
            val ret = FloatArray(5)
            ret[0] = Float.fromBits(PATH_DASH)
            ret[1] = Float.fromBits(shapeId)
            ret[2] = advance
            ret[3] = phase
            ret[4] = Float.fromBits(style)
            return ret
        }

        /** Sum two path effects together */
        fun sum(first: FloatArray, second: FloatArray): FloatArray {
            val ret = FloatArray(first.size + second.size + 1)
            ret[0] = Float.fromBits(SUM)
            first.copyInto(ret, 1)
            second.copyInto(ret, first.size + 1)
            return ret
        }

        /** Compose two path effects together */
        fun compose(outerPE: FloatArray, innerPE: FloatArray): FloatArray {
            val ret = FloatArray(outerPE.size + innerPE.size + 1)
            ret[0] = Float.fromBits(COMPOSE)
            outerPE.copyInto(ret, 1)
            innerPE.copyInto(ret, outerPE.size + 1)
            return ret
        }

        private fun registerIfId(data: IntArray, offset: Int, register: Register) {
            val v = Float.fromBits(data[offset])
            if (v.isNaN()) {
                register.id(offset)
            }
        }
    }
}
