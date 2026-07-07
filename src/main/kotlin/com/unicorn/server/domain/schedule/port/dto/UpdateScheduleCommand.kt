package com.unicorn.server.domain.schedule.port.dto

import com.unicorn.server.domain.schedule.vo.ScheduleId
import java.time.LocalDate
import java.time.LocalTime

data class UpdateScheduleCommand(
	val scheduleId: ScheduleId,
	val circleId: String,
	val memberId: String,
	val title: String?,
	val startDate: LocalDate?,
	val endDate: LocalDate?,
	val startTime: LocalTime?,
	val endTime: LocalTime?,
	val startTimeProvided: Boolean,
	val endTimeProvided: Boolean,
	val needConfirm: Boolean,
	val memo: String?,
	val memoProvided: Boolean,
)
