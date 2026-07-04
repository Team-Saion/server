package com.unicorn.server.domain.schedule.port.dto

import java.time.LocalDate
import java.time.LocalTime

data class UpdateScheduleCommand(
	val scheduleId: Long,
	val circleId: Long,
	val memberId: String,
	val title: String?,
	val startDate: LocalDate?,
	val endDate: LocalDate?,
	val startTime: LocalTime?,
	val endTime: LocalTime?,
	val needConfirm: Boolean,
	val memo: String?,
)
