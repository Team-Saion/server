package com.unicorn.server.domain.home.port.dto

import com.unicorn.server.domain.schedule.port.dto.ScheduleSummaryResult

data class HomeView(
	val circle: HomeCircleDto,
	val members: List<HomeMemberDto>,
	val canInvite: Boolean,
	val mainSchedule: ScheduleSummaryResult?,
	val schedules: List<ScheduleSummaryResult>,
	val totalScheduleCount: Long,
)
