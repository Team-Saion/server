package com.unicorn.server.domain.schedule.port.dto

data class ScheduleListResult(
	val schedules: List<ScheduleSummaryResult>,
	val nextCursor: String?,
	val hasNext: Boolean,
)
