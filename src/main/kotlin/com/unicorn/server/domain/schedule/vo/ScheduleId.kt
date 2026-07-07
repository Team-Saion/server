package com.unicorn.server.domain.schedule.vo

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@JvmInline
value class ScheduleId(val value: String) {
	override fun toString(): String = value

	companion object {
		private const val PREFIX = "SC"
		private val KST: ZoneId = ZoneId.of("Asia/Seoul")
		private val TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm")

		fun generate(sequence: Long): ScheduleId {
			val timestamp = LocalDateTime.now(KST).format(TIMESTAMP_FORMATTER)
			val paddedSequence = sequence.toString().padStart(5, '0')
			return ScheduleId("$PREFIX$timestamp$paddedSequence")
		}

		fun of(value: String): ScheduleId = ScheduleId(value)
	}
}
