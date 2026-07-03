package com.unicorn.server.infrastructure.adapter.`in`.web.circle.dto

import com.unicorn.server.domain.home.port.dto.HomeView

data class CircleHomeResponse(
	val circle: CircleSummaryResponse,
	val members: List<CircleMemberResponse>,
	val canInvite: Boolean,
	val mainSchedule: Any?,
	val schedules: List<Any>,
	val totalScheduleCount: Long,
) {
	companion object {
		fun from(view: HomeView): CircleHomeResponse = CircleHomeResponse(
			circle = CircleSummaryResponse.from(view.circle),
			members = view.members.map { CircleMemberResponse.from(it) },
			canInvite = view.canInvite,
			mainSchedule = view.mainSchedule,
			schedules = view.schedules,
			totalScheduleCount = view.totalScheduleCount,
		)
	}
}
