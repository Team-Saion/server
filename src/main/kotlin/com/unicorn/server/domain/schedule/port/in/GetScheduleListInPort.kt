package com.unicorn.server.domain.schedule.port.`in`

import com.unicorn.server.domain.schedule.port.dto.ScheduleListResult

interface GetScheduleListInPort {
	fun getList(
		circleId: Long,
		memberId: String,
		cursor: String?,
		size: Int,
	): ScheduleListResult
}
