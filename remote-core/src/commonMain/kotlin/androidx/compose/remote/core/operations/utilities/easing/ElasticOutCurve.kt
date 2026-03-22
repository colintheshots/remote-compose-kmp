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
package androidx.compose.remote.core.operations.utilities.easing

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin

/** Provide a bouncing Easing function */
class ElasticOutCurve : Easing() {

    override fun get(x: Float): Float {
        if (x <= 0) return 0.0f
        if (x >= 1) return 1.0f
        return (2.0f.toDouble().pow(-10.0 * x) * sin((x * 10 - 0.75f) * C4) + 1).toFloat()
    }

    override fun getDiff(x: Float): Float {
        if (x < 0 || x > 1) return 0.0f
        return (5 *
            2.0f.toDouble().pow(1.0 - 10.0 * x) *
            (LOG_8 * cos(TWENTY_PI * x / 3) +
                2 * F_PI * sin(TWENTY_PI * x / 3)) / 3).toFloat()
    }

    companion object {
        private val F_PI = PI.toFloat()
        private val C4 = 2 * F_PI / 3
        private val TWENTY_PI = 20 * F_PI
        private val LOG_8 = ln(8.0f)
    }
}
