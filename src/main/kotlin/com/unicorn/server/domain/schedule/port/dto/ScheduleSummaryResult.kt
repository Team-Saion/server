package com.unicorn.server.domain.schedule.port.dto

import com.unicorn.server.domain.schedule.enums.ScheduleStatus
import java.time.LocalDate
import java.time.LocalTime

data class ScheduleSummaryResult(
	val scheduleId: Long,
	val title: String,
	val startDate: LocalDate,
	val endDate: LocalDate,
	val startTime: LocalTime?,
	val endTime: LocalTime?,
	val isAllDay: Boolean,
	val needConfirm: Boolean,
	val status: ScheduleStatus,
	val dDay: Int?,
	val progressRate: Int,
)
