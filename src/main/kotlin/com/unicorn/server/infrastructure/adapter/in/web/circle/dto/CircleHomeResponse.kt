package com.unicorn.server.infrastructure.adapter.`in`.web.circle.dto

import com.unicorn.server.domain.home.port.dto.HomeView
import com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto.ScheduleSummaryResponse

data class CircleHomeResponse(
	val circle: CircleSummaryResponse,
	val members: List<CircleMemberResponse>,
	val canInvite: Boolean,
	val mainSchedule: ScheduleSummaryResponse?,
	val schedules: List<ScheduleSummaryResponse>,
	val totalScheduleCount: Long,
) {
	companion object {
		fun from(view: HomeView, serverUrl: String): CircleHomeResponse = CircleHomeResponse(
			circle = CircleSummaryResponse.from(view.circle),
			members = view.members.map { CircleMemberResponse.from(it, serverUrl) },
			canInvite = view.canInvite,
			mainSchedule = view.mainSchedule?.let { ScheduleSummaryResponse.from(it) },
			schedules = view.schedules.map { ScheduleSummaryResponse.from(it) },
			totalScheduleCount = view.totalScheduleCount,
		)
	}
}
