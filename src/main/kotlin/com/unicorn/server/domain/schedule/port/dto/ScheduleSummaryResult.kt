package com.unicorn.server.domain.schedule.port.dto

import com.unicorn.server.domain.schedule.enums.ScheduleStatus
import com.unicorn.server.domain.schedule.enums.UrgencyLevel
import com.unicorn.server.domain.schedule.vo.ScheduleId
import java.time.LocalDate
import java.time.LocalTime

data class ScheduleSummaryResult(
	val scheduleId: ScheduleId,
	val title: String,
	val startDate: LocalDate,
	val endDate: LocalDate,
	val startTime: LocalTime?,
	val endTime: LocalTime?,
	val isAllDay: Boolean,
	val needConfirm: Boolean,
	val status: ScheduleStatus,
	val dDay: Int?,
	val urgencyLevel: UrgencyLevel,
	val progressRate: Int,
)
