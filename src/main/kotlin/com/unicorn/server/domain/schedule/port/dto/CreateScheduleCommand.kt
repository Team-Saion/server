package com.unicorn.server.domain.schedule.port.dto

import java.time.LocalDate
import java.time.LocalTime

data class CreateScheduleCommand(
	val memberId: String,
	val circleId: Long,
	val title: String,
	val startDate: LocalDate,
	val endDate: LocalDate,
	val startTime: LocalTime?,
	val endTime: LocalTime?,
	val needConfirm: Boolean,
	val memo: String?,
)
