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
package androidx.compose.remote.player.compose.platform

import androidx.compose.remote.core.RcPlatformServices
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.operations.layout.managers.TextLayout
import androidx.compose.remote.core.operations.paint.PaintBundle
import androidx.compose.remote.core.operations.paint.PaintChanges
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Paint

/**
 * Platform-specific text measurement and rendering support.
 */
expect class PlatformTextSupport() {
    /**
     * Get text bounds for the given text, storing results in [bounds].
     */
    fun getTextBounds(
        text: String,
        start: Int,
        end: Int,
        flags: Int,
        bounds: FloatArray,
        paint: Paint,
    )

    /**
     * Draw a text run on the canvas.
     */
    fun drawTextRun(
        text: String,
        x: Float,
        y: Float,
        canvas: Canvas,
        paint: Paint,
    )

    /**
     * Draw text on a path.
     */
    fun drawTextOnPath(
        text: String,
        nativePath: Any,
        hOffset: Float,
        vOffset: Float,
        canvas: Canvas,
        paint: Paint,
    )

    /**
     * Layout complex (multi-line) text.
     */
    fun layoutComplexText(
        text: String,
        start: Int,
        end: Int,
        alignment: Int,
        overflow: Int,
        maxLines: Int,
        maxWidth: Float,
        paint: Paint,
    ): RcPlatformServices.ComputedTextLayout?

    /**
     * Draw a pre-computed complex text layout.
     */
    fun drawComplexText(
        computedTextLayout: RcPlatformServices.ComputedTextLayout,
        canvas: Canvas,
    )
}

/**
 * Platform-specific bitmap support (decode, draw).
 */
expect class PlatformBitmapSupport() {
    /**
     * Draw a bitmap identified by [imageData] onto the canvas.
     */
    fun drawBitmap(
        imageData: Any,
        srcLeft: Int,
        srcTop: Int,
        srcRight: Int,
        srcBottom: Int,
        dstLeft: Int,
        dstTop: Int,
        dstRight: Int,
        dstBottom: Int,
        canvas: Canvas,
        paint: Paint,
    )

    /**
     * Draw a bitmap to fill a destination rect.
     */
    fun drawBitmap(
        imageData: Any,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        canvas: Canvas,
        paint: Paint,
    )

    /**
     * Get a canvas for drawing into a bitmap.
     */
    fun getCanvasForBitmap(bitmap: Any): Canvas

    /**
     * Erase a bitmap to a given color.
     */
    fun eraseBitmap(bitmap: Any, color: Int)
}

/**
 * Platform-specific graphics layer support (RenderNode on Android, no-op elsewhere).
 */
expect class PlatformGraphicsLayerSupport() {
    /**
     * Start recording a graphics layer of the given size.
     * Returns a new Canvas to draw into.
     */
    fun startGraphicsLayer(w: Int, h: Int): Canvas?

    /**
     * Set attributes on the current graphics layer.
     */
    fun setGraphicsLayer(attributes: HashMap<Int, Any>)

    /**
     * End the graphics layer recording and draw it onto [canvas].
     */
    fun endGraphicsLayer(canvas: Canvas)
}

/**
 * Platform-specific paint operations (typeface, shader, paint reset).
 */
expect class PlatformPaintSupport() {
    /**
     * Create a [PaintChanges] implementation for the given context and paint provider.
     */
    fun createPaintChanges(
        remoteContext: RemoteContext,
        getPaint: () -> Paint,
    ): PaintChanges

    /**
     * Reset a paint to default state.
     */
    fun resetPaint(paint: Paint)
}

/**
 * Check if platform animations are enabled (e.g. accessibility settings on Android).
 */
expect fun isPlatformAnimationEnabled(): Boolean

/**
 * Get the current time in nanoseconds for animation timing.
 */
expect fun nanoTime(): Long

/**
 * Log a warning message.
 */
expect fun logWarning(tag: String, message: String)
