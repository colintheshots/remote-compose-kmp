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

import android.graphics.Bitmap
import androidx.compose.remote.core.RcPlatformServices
import androidx.compose.remote.core.operations.Utils
import androidx.compose.remote.creation.profile.Profile

/**
 * Android-specific [RemoteComposeContext] that adds convenience methods
 * for working with Android Bitmaps and Number-typed parameters.
 */
class RemoteComposeContextAndroid : RemoteComposeContext {

    constructor(
        width: Int,
        height: Int,
        contentDescription: String,
        profile: Profile,
        content: RemoteComposeContextAndroid.() -> Unit,
    ) : super(width, height, contentDescription, profile) {
        content()
    }

    constructor(
        width: Int,
        height: Int,
        contentDescription: String,
        platform: RcPlatformServices,
        content: RemoteComposeContextAndroid.() -> Unit,
    ) : super(width, height, contentDescription, platform) {
        content()
    }

    constructor(
        width: Int,
        height: Int,
        contentDescription: String,
        apiLevel: Int,
        profiles: Int,
        platform: RcPlatformServices,
        content: RemoteComposeContextAndroid.() -> Unit,
    ) : super(width, height, contentDescription, apiLevel, profiles, platform) {
        content()
    }

    constructor(
        vararg tags: RemoteComposeWriter.HTag,
        profile: Profile,
        content: RemoteComposeContextAndroid.() -> Unit,
    ) : super(*tags, profile = profile, content = {}) {
        content()
    }

    constructor(
        platform: RcPlatformServices,
        vararg tags: RemoteComposeWriter.HTag,
        content: RemoteComposeContextAndroid.() -> Unit,
    ) : super(*tags, platform = platform, content = {}) {
        content()
    }

    fun addBitmap(image: Bitmap): Int {
        return mRemoteWriter.addBitmap(image)
    }

    fun drawBitmap(image: Bitmap, contentDescription: String) {
        mRemoteWriter.drawBitmap(image, image.width, image.height, contentDescription)
    }

    fun createCirclePath(x: Float, y: Float, rad: Float): RemotePath {
        return RemotePath.createCirclePath(mRemoteWriter, x, y, rad)
    }

    fun drawRoundRect(
        x: Number,
        y: Number,
        w: Number,
        h: Number,
        radX: Number,
        radY: Number,
    ) {
        drawRoundRect(
            x.toFloat(),
            y.toFloat(),
            w.toFloat(),
            h.toFloat(),
            radX.toFloat(),
            radY.toFloat(),
        )
    }

    fun drawRect(x: Number, y: Number, w: Number, h: Number) {
        drawRect(x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat())
    }

    fun drawLine(x1: Number, y1: Number, x2: Number, y2: Number) {
        drawLine(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat())
    }

    fun save(content: RemoteComposeContextAndroid.() -> Unit) {
        save()
        content()
        restore()
    }

    fun rotate(angle: Number) {
        rotate(angle.toFloat())
    }

    fun rotate(angle: Number, x: Number, y: Number) {
        rotate(angle.toFloat(), x.toFloat(), y.toFloat())
    }

    fun scale(sx: Number, sy: Number, centerX: Number, centerY: Number) {
        scale(sx.toFloat(), sy.toFloat(), centerX.toFloat(), centerY.toFloat())
    }

    fun drawArc(
        left: Number,
        top: Number,
        right: Number,
        bottom: Number,
        startAngle: Number,
        sweepAngle: Number,
    ) {
        drawArc(
            left.toFloat(),
            top.toFloat(),
            right.toFloat(),
            bottom.toFloat(),
            startAngle.toFloat(),
            sweepAngle.toFloat(),
        )
    }

    fun drawSector(
        left: Number,
        top: Number,
        right: Number,
        bottom: Number,
        startAngle: Number,
        sweepAngle: Number,
    ) {
        drawSector(
            left.toFloat(),
            top.toFloat(),
            right.toFloat(),
            bottom.toFloat(),
            startAngle.toFloat(),
            sweepAngle.toFloat(),
        )
    }

    fun drawTextAnchored(
        id: Int,
        x: Number,
        y: Number,
        panX: Number,
        panY: Number,
        flags: Int,
    ) {
        drawTextAnchored(id, x.toFloat(), y.toFloat(), panX.toFloat(), panY.toFloat(), flags)
    }

    fun loop(from: Float, step: Float, until: Float, content: RcFloatArgumentCallback) {
        val indexId = createID(0)
        mRemoteWriter.startLoop(indexId, from, step, until)
        content.run(rf(Utils.asNan(indexId)))
        endLoop()
    }

    fun loop(
        fromN: Number,
        stepN: Number,
        untilN: Number,
        content: RcFloatArgumentCallback,
    ) {
        val from = fromN as? Float ?: fromN.toFloat()
        val step = stepN as? Float ?: stepN.toFloat()
        val until = untilN as? Float ?: untilN.toFloat()

        val indexId = createID(0)
        mRemoteWriter.startLoop(indexId, from, step, until)
        content.run(rf(Utils.asNan(indexId)))
        endLoop()
    }

    fun createTextFromFloat(value: RFloat, before: Int, after: Int, flags: Int): Int {
        return mRemoteWriter.createTextFromFloat(value.toFloat(), before, after, flags)
    }

    fun beginGlobal() {
        mRemoteWriter.beginGlobal()
    }

    fun endGlobal() {
        mRemoteWriter.endGlobal()
    }
}
