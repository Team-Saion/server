package com.unicorn.server.domain.schedule.port.`in`

import com.unicorn.server.domain.schedule.port.dto.ScheduleDetailResult

interface GetScheduleDetailInPort {
	fun getDetail(scheduleId: Long, circleId: Long, memberId: String): ScheduleDetailResult
}
