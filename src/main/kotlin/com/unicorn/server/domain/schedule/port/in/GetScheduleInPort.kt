package com.unicorn.server.domain.schedule.port.`in`

import com.unicorn.server.domain.schedule.port.dto.ScheduleDetailResult
import com.unicorn.server.domain.schedule.port.dto.ScheduleListResult
import com.unicorn.server.domain.schedule.vo.ScheduleId

interface GetScheduleInPort {
	fun getList(circleId: String, memberId: String, cursor: String?, size: Int): ScheduleListResult

	fun getDetail(scheduleId: ScheduleId, circleId: String, memberId: String): ScheduleDetailResult
}
