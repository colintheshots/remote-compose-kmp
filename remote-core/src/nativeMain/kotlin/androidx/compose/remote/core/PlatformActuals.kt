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

import kotlin.time.TimeSource
import platform.Foundation.NSDate
import platform.Foundation.NSCalendar
import platform.Foundation.timeIntervalSince1970

actual fun currentTimeMillis(): Long {
    return (NSDate().timeIntervalSince1970 * 1000).toLong()
}

actual fun localUtcOffsetSeconds(): Int {
    val tz = NSCalendar.currentCalendar.timeZone
    return tz.secondsFromGMTForDate(NSDate()).toInt()
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
    val cal = NSCalendar.currentCalendar
    val date = NSDate()
    val unitFlags: ULong =
        (1uL shl 2) or  // NSCalendarUnitYear
        (1uL shl 3) or  // NSCalendarUnitMonth
        (1uL shl 4) or  // NSCalendarUnitDay
        (1uL shl 7) or  // NSCalendarUnitWeekday
        (1uL shl 10) or // NSCalendarUnitHour
        (1uL shl 11) or // NSCalendarUnitMinute
        (1uL shl 12)    // NSCalendarUnitSecond
    val components = cal.components(unitFlags, date)
    val epochSec = date.timeIntervalSince1970.toLong()
    val nanoFrac = ((date.timeIntervalSince1970 % 1.0) * 1_000_000_000).toFloat()
    // NSCalendar weekday: 1=Sunday, convert to ISO: 1=Monday
    val isoWeekday = if (components.weekday.toInt() == 1) 7 else components.weekday.toInt() - 1
    val startOfYear = cal.components((1uL shl 2), date).let { yearComp ->
        val yearComponents = platform.Foundation.NSDateComponents()
        yearComponents.setYear(components.year)
        yearComponents.setMonth(1)
        yearComponents.setDay(1)
        cal.dateFromComponents(yearComponents)
    }
    val dayOfYear = if (startOfYear != null) {
        val diff = date.timeIntervalSince1970 - startOfYear.timeIntervalSince1970
        (diff / 86400).toInt() + 1
    } else 1

    return PlatformDateTime(
        month = components.month.toInt(),
        hour = components.hour.toInt(),
        minute = components.minute.toInt(),
        second = components.second.toInt(),
        nanoFraction = nanoFrac,
        dayOfWeek = isoWeekday,
        dayOfMonth = components.day.toInt(),
        dayOfYear = dayOfYear,
        year = components.year.toInt(),
        epochSecond = epochSec,
        utcOffsetSeconds = NSCalendar.currentCalendar.timeZone.secondsFromGMTForDate(NSDate()).toInt()
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
