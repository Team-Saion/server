package com.unicorn.server.domain.schedule.port.`in`

import com.unicorn.server.domain.schedule.vo.ScheduleId

interface DeleteScheduleInPort {
	fun delete(scheduleId: ScheduleId, circleId: String, memberId: String)
}
