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
package androidx.compose.remote.sample

import androidx.compose.remote.core.RcPlatformServices
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.operations.utilities.AnimatedFloatExpression.Companion.MUL
import androidx.compose.remote.creation.RemoteComposeWriter

/**
 * Creates a simple RemoteCompose document containing a colored rectangle.
 *
 * The document draws a blue filled rectangle on a white background.
 */
fun createSimpleRectDocument(): ByteArray {
    val width = 200
    val height = 200
    val writer = RemoteComposeWriter(width, height, "Simple rectangle", 7, 0, RcPlatformServices.None)

    // White background
    writer.rcPaint.setColor(0xFFFFFFFF.toInt()).commit()
    writer.drawRect(0f, 0f, width.toFloat(), height.toFloat())

    // Blue rectangle
    writer.rcPaint.setColor(0xFF4285F4.toInt()).commit()
    writer.drawRect(20f, 20f, 180f, 180f)

    // Red border rectangle (stroke)
    writer.rcPaint.setColor(0xFFEA4335.toInt()).setStrokeWidth(4f).setStyle(1).commit()
    writer.drawRect(20f, 20f, 180f, 180f)

    val buffer = writer.getBuffer().getBuffer()
    return buffer.cloneBytes()
}

/**
 * Creates a RemoteCompose document with text rendering.
 *
 * The document displays text with different sizes and colors.
 */
fun createTextDocument(): ByteArray {
    val width = 300
    val height = 200
    val writer = RemoteComposeWriter(width, height, "Text document", 7, 0, RcPlatformServices.None)

    // Light gray background
    writer.rcPaint.setColor(0xFFF5F5F5.toInt()).commit()
    writer.drawRect(0f, 0f, width.toFloat(), height.toFloat())

    // Title text in dark color
    writer.rcPaint
        .setColor(0xFF333333.toInt())
        .setTextSize(32f)
        .commit()
    writer.drawTextAnchored("Hello Remote!", 150f, 50f, 0.5f, 0.5f, 0)

    // Subtitle text in blue
    writer.rcPaint
        .setColor(0xFF4285F4.toInt())
        .setTextSize(20f)
        .commit()
    writer.drawTextAnchored("Compose Multiplatform", 150f, 100f, 0.5f, 0.5f, 0)

    // Body text in gray
    writer.rcPaint
        .setColor(0xFF666666.toInt())
        .setTextSize(14f)
        .commit()
    writer.drawTextAnchored("Rendered via RemoteCompose", 150f, 150f, 0.5f, 0.5f, 0)

    val buffer = writer.getBuffer().getBuffer()
    return buffer.cloneBytes()
}

/**
 * Creates a RemoteCompose document with animation (a rotating rectangle).
 *
 * The rectangle rotates continuously around the center of the document using
 * a float expression driven by the animation time.
 */
fun createAnimatedDocument(): ByteArray {
    val width = 200
    val height = 200
    val writer = RemoteComposeWriter(width, height, "Animated document", 7, 0, RcPlatformServices.None)

    val centerX = width / 2f
    val centerY = height / 2f

    // Background
    writer.rcPaint.setColor(0xFFE8EAF6.toInt()).commit()
    writer.drawRect(0f, 0f, width.toFloat(), height.toFloat())

    // Create an animated rotation angle: animationTime * 90 (degrees per second)
    val animAngle = writer.floatExpression(
        RemoteContext.FLOAT_ANIMATION_TIME, 90f, MUL
    )

    // Save matrix, apply rotation, draw, restore
    writer.save()
    writer.rotate(animAngle, centerX, centerY)

    // Green filled rectangle
    writer.rcPaint.setColor(0xFF34A853.toInt()).commit()
    writer.drawRect(centerX - 40f, centerY - 40f, centerX + 40f, centerY + 40f)

    // Dark border
    writer.rcPaint.setColor(0xFF1B5E20.toInt()).setStrokeWidth(3f).setStyle(1).commit()
    writer.drawRect(centerX - 40f, centerY - 40f, centerX + 40f, centerY + 40f)

    writer.restore()

    // Static center dot
    writer.rcPaint.setColor(0xFFFF5722.toInt()).setStyle(0).commit()
    writer.drawCircle(centerX, centerY, 6f)

    val buffer = writer.getBuffer().getBuffer()
    return buffer.cloneBytes()
}
