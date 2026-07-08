package com.unicorn.server.domain.schedule.port.dto

import com.unicorn.server.domain.schedule.Schedule
import com.unicorn.server.domain.schedule.vo.ScheduleId
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalTime
import java.util.Base64

data class SchedulePageCursor(
	val startDate: LocalDate,
	val startTime: LocalTime?,
	val scheduleId: ScheduleId,
) {
	fun encode(): String {
		val startTimeJson = startTime?.let { "\"$it\"" } ?: "null"
		val raw = """{"startDate":"$startDate","startTime":$startTimeJson,"scheduleId":"${scheduleId.value}"}"""

		return Base64.getUrlEncoder()
			.withoutPadding()
			.encodeToString(raw.toByteArray(StandardCharsets.UTF_8))
	}

	companion object {
		private val CURSOR_PATTERN =
			Regex("""\{"startDate":"([^"]+)","startTime":(?:"([^"]+)"|null),"scheduleId":"([^"]+)"}""")

		fun decode(cursor: String): SchedulePageCursor {
			try {
				val raw = String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8)
				val match = CURSOR_PATTERN.matchEntire(raw)
					?: throw IllegalArgumentException("Invalid schedule page cursor")

				return SchedulePageCursor(
					startDate = LocalDate.parse(match.groupValues[1]),
					startTime = match.groupValues[2].takeIf { it.isNotEmpty() }?.let { LocalTime.parse(it) },
					scheduleId = ScheduleId.of(match.groupValues[3]),
				)
			} catch (exception: Exception) {
				throw IllegalArgumentException("Invalid schedule page cursor", exception)
			}
		}

		fun from(schedule: Schedule) = SchedulePageCursor(
			startDate = schedule.startDate,
			startTime = schedule.startTime,
			scheduleId = schedule.id,
		)
	}
}
