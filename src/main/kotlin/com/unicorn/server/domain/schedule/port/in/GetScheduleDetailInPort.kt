package com.unicorn.server.domain.schedule.port.`in`

import com.unicorn.server.domain.schedule.port.dto.ScheduleDetailResult
import com.unicorn.server.domain.schedule.vo.ScheduleId

interface GetScheduleDetailInPort {
	fun getDetail(scheduleId: ScheduleId, circleId: String, memberId: String): ScheduleDetailResult
}
