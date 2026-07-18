package com.unicorn.server.domain.home.port.dto

import com.unicorn.server.domain.schedule.port.dto.ScheduleView

data class HomeView(
	val circle: HomeCircleDto,
	val members: List<HomeMemberDto>,
	val canInvite: Boolean,
	val mainSchedule: ScheduleView?,
	val schedules: List<ScheduleView>,
	val totalScheduleCount: Long,
)
