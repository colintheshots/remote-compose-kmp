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
package androidx.compose.remote.core

/** Expect declarations for platform-specific time decomposition */
expect class PlatformDateTime

expect fun currentPlatformDateTime(clock: RemoteClock): PlatformDateTime

expect fun PlatformDateTime.month(): Int
expect fun PlatformDateTime.hour(): Int
expect fun PlatformDateTime.minute(): Int
expect fun PlatformDateTime.second(): Int
expect fun PlatformDateTime.nanoFraction(): Float
expect fun PlatformDateTime.dayOfWeek(): Int
expect fun PlatformDateTime.dayOfMonth(): Int
expect fun PlatformDateTime.dayOfYear(): Int
expect fun PlatformDateTime.year(): Int
expect fun PlatformDateTime.epochSecond(): Long
expect fun PlatformDateTime.utcOffsetSeconds(): Int

/** This generates the standard system variables for time. */
class TimeVariables(private val mClock: RemoteClock = SystemClock()) {

    fun getClock(): RemoteClock = mClock

    fun updateTime(context: RemoteContext, dateTime: PlatformDateTime) {
        val month = dateTime.month()
        val hour = dateTime.hour()
        val minute = dateTime.minute()
        val seconds = dateTime.second()
        val currentMinute = hour * 60 + minute
        val currentSeconds = minute * 60 + seconds
        val sec = currentSeconds + dateTime.nanoFraction()
        val dayWeek = dateTime.dayOfWeek()
        val dayOfMonth = dateTime.dayOfMonth()
        val dayOfYear = dateTime.dayOfYear()
        val year = dateTime.year()
        val epochSec = dateTime.epochSecond()
        val offsetSeconds = dateTime.utcOffsetSeconds()

        context.loadFloat(RemoteContext.ID_OFFSET_TO_UTC, offsetSeconds.toFloat())
        context.loadFloat(RemoteContext.ID_CONTINUOUS_SEC, sec)
        context.loadInteger(RemoteContext.ID_EPOCH_SECOND, epochSec.toInt())
        context.loadFloat(RemoteContext.ID_TIME_IN_SEC, currentSeconds.toFloat())
        context.loadFloat(RemoteContext.ID_TIME_IN_MIN, currentMinute.toFloat())
        context.loadFloat(RemoteContext.ID_TIME_IN_HR, hour.toFloat())
        context.loadFloat(RemoteContext.ID_CALENDAR_MONTH, month.toFloat())
        context.loadFloat(RemoteContext.ID_DAY_OF_MONTH, dayOfMonth.toFloat())
        context.loadFloat(RemoteContext.ID_WEEK_DAY, dayWeek.toFloat())
        context.loadFloat(RemoteContext.ID_DAY_OF_YEAR, dayOfYear.toFloat())
        context.loadFloat(RemoteContext.ID_YEAR, year.toFloat())
        context.loadFloat(
            RemoteContext.ID_API_LEVEL,
            CoreDocument.getDocumentApiLevel() + CoreDocument.BUILD
        )
    }

    fun updateTime(context: RemoteContext) {
        updateTime(context, currentPlatformDateTime(mClock))
    }
}
