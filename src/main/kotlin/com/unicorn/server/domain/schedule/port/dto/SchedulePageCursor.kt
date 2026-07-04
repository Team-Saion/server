package com.unicorn.server.domain.schedule.port.dto

import com.unicorn.server.domain.schedule.Schedule
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalTime
import java.util.Base64

data class SchedulePageCursor(
	val startDate: LocalDate,
	val startTime: LocalTime?,
	val scheduleId: Long,
) {
	fun encode(): String {
		val startTimeJson = startTime?.let { "\"$it\"" } ?: "null"
		val raw = """{"startDate":"$startDate","startTime":$startTimeJson,"scheduleId":$scheduleId}"""

		return Base64.getUrlEncoder()
			.withoutPadding()
			.encodeToString(raw.toByteArray(StandardCharsets.UTF_8))
	}

	companion object {
		private val CURSOR_PATTERN =
			Regex("""\{"startDate":"([^"]+)","startTime":(?:"([^"]+)"|null),"scheduleId":(\d+)}""")

		fun decode(cursor: String): SchedulePageCursor {
			val raw = String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8)
			val match = CURSOR_PATTERN.matchEntire(raw)
				?: throw IllegalArgumentException("Invalid schedule page cursor")

			return SchedulePageCursor(
				startDate = LocalDate.parse(match.groupValues[1]),
				startTime = match.groupValues[2].takeIf { it.isNotEmpty() }?.let { LocalTime.parse(it) },
				scheduleId = match.groupValues[3].toLong(),
			)
		}

		fun from(schedule: Schedule) = SchedulePageCursor(
			startDate = schedule.startDate,
			startTime = schedule.startTime,
			scheduleId = schedule.id,
		)
	}
}
