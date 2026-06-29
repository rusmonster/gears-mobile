package app.constructor.csdk.logging

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal object LogMessageFormatter {
    fun format(tag: String, t: Throwable?, message: () -> String): String {
        val stackTrace = t?.stackTraceToString()?.let { ": $it" } ?: ""
        return "$timestamp [$tag] ${message()}$stackTrace"
    }

    private val timestamp: String
        get() {
            val now = Clock.System.now()
            val localDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())

            val day = localDateTime.dayOfMonth.toString().padStart(2, '0')
            val month = localDateTime.monthNumber.toString().padStart(2, '0')
            val year = localDateTime.year
            val hour = localDateTime.hour.toString().padStart(2, '0')
            val minute = localDateTime.minute.toString().padStart(2, '0')
            val second = localDateTime.second.toString().padStart(2, '0')
            val millisecond = (now.toEpochMilliseconds() % 1000).toString().padStart(3, '0')

            return "$day-$month-$year $hour:$minute:$second.$millisecond"
        }
}
