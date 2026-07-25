package com.unicorn.server.domain.schedule.port.`in`

import com.unicorn.server.domain.schedule.port.dto.ScheduleListResult

interface ScheduleListInPort {
	fun getList(
		circleId: String,
		memberId: String,
		cursor: String?,
		size: Int,
	): ScheduleListResult
}
