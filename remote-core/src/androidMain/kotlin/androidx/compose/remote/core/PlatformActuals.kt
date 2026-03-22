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
package androidx.compose.remote.core

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Calendar
import java.util.TimeZone

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

actual fun localUtcOffsetSeconds(): Int {
    return TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 1000
}

actual class PlatformDateTime(
    val month: Int,
    val hour: Int,
    val minute: Int,
    val second: Int,
    val nanoFraction: Float,
    val dayOfWeek: Int,
    val dayOfMonth: Int,
    val dayOfYear: Int,
    val year: Int,
    val epochSecond: Long,
    val utcOffsetSeconds: Int
)

actual fun currentPlatformDateTime(clock: RemoteClock): PlatformDateTime {
    val now = ZonedDateTime.now()
    val epochSec = now.toEpochSecond()
    val nanoFrac = now.nano.toFloat()
    // ISO day of week: 1=Monday..7=Sunday
    val isoDow = now.dayOfWeek.value

    return PlatformDateTime(
        month = now.monthValue,
        hour = now.hour,
        minute = now.minute,
        second = now.second,
        nanoFraction = nanoFrac,
        dayOfWeek = isoDow,
        dayOfMonth = now.dayOfMonth,
        dayOfYear = now.dayOfYear,
        year = now.year,
        epochSecond = epochSec,
        utcOffsetSeconds = now.offset.totalSeconds
    )
}

actual fun PlatformDateTime.month(): Int = month
actual fun PlatformDateTime.hour(): Int = hour
actual fun PlatformDateTime.minute(): Int = minute
actual fun PlatformDateTime.second(): Int = second
actual fun PlatformDateTime.nanoFraction(): Float = nanoFraction
actual fun PlatformDateTime.dayOfWeek(): Int = dayOfWeek
actual fun PlatformDateTime.dayOfMonth(): Int = dayOfMonth
actual fun PlatformDateTime.dayOfYear(): Int = dayOfYear
actual fun PlatformDateTime.year(): Int = year
actual fun PlatformDateTime.epochSecond(): Long = epochSecond
actual fun PlatformDateTime.utcOffsetSeconds(): Int = utcOffsetSeconds
