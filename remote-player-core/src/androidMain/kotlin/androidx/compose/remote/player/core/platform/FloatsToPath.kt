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
package androidx.compose.remote.player.core.platform

import android.graphics.Path
import android.graphics.PathMeasure
import android.os.Build
import androidx.compose.remote.core.operations.PathData
import androidx.compose.remote.core.operations.Utils.idFromNan

/**
 * Utility class to convert a float array representation of a path into an Android [Path] object.
 */
object FloatsToPath {

    /**
     * Converts a float array representing a path into a Path object.
     *
     * @param retPath   The Path object to populate with the converted path data.
     * @param floatPath The float array representing the path.
     * @param start     The starting percentage (0.0 to 1.0) of the path to include.
     * @param stop      The ending percentage (0.0 to 1.0) of the path to include.
     */
    fun genPath(retPath: Path, floatPath: FloatArray?, start: Float, stop: Float) {
        if (floatPath == null) return
        var i = 0
        val path = Path()
        while (i < floatPath.size) {
            when (idFromNan(floatPath[i])) {
                PathData.MOVE -> {
                    i++
                    path.moveTo(floatPath[i], floatPath[i + 1])
                    i += 2
                }
                PathData.LINE -> {
                    i += 3
                    path.lineTo(floatPath[i], floatPath[i + 1])
                    i += 2
                }
                PathData.QUADRATIC -> {
                    i += 3
                    path.quadTo(floatPath[i], floatPath[i + 1], floatPath[i + 2], floatPath[i + 3])
                    i += 4
                }
                PathData.CONIC -> {
                    i += 3
                    if (Build.VERSION.SDK_INT >= 34) {
                        path.conicTo(
                            floatPath[i], floatPath[i + 1],
                            floatPath[i + 2], floatPath[i + 3],
                            floatPath[i + 4]
                        )
                    }
                    i += 5
                }
                PathData.CUBIC -> {
                    i += 3
                    path.cubicTo(
                        floatPath[i], floatPath[i + 1],
                        floatPath[i + 2], floatPath[i + 3],
                        floatPath[i + 4], floatPath[i + 5]
                    )
                    i += 6
                }
                PathData.CLOSE -> {
                    path.close()
                    i++
                }
                PathData.DONE -> {
                    i++
                }
                else -> {
                    System.err.println(" Odd command " + idFromNan(floatPath[i]))
                }
            }
        }

        retPath.reset()
        if (start > 0f || stop < 1f) {
            if (start < stop) {
                val measure = PathMeasure()
                measure.setPath(path, false)
                val len = measure.length
                val scaleStart = maxOf(start, 0f) * len
                val scaleStop = minOf(stop, 1f) * len
                measure.getSegment(scaleStart, scaleStop, retPath, true)
            }
        } else {
            retPath.addPath(path)
        }
    }
}
