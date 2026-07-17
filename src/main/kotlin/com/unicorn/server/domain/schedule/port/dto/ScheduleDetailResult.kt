package com.unicorn.server.domain.schedule.port.dto

import com.unicorn.server.domain.schedule.enums.ScheduleStatus
import com.unicorn.server.domain.schedule.enums.UrgencyLevel
import com.unicorn.server.domain.schedule.vo.ScheduleId
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class ScheduleDetailResult(
	val scheduleId: ScheduleId,
	val title: String,
	val startDate: LocalDate,
	val endDate: LocalDate,
	val startTime: LocalTime?,
	val endTime: LocalTime?,
	val isAllDay: Boolean,
	val needConfirm: Boolean,
	val memo: String?,
	val status: ScheduleStatus,
	val dDay: Int?,
	val urgencyLevel: UrgencyLevel,
	val progressRate: Int,
	val confirmations: List<ConfirmationCountResult>,
	val myConfirmation: MyConfirmationInfo?,
	val createdBy: String,
	val createdAt: LocalDateTime,
)
