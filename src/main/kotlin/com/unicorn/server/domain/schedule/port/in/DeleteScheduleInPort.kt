package com.unicorn.server.domain.schedule.port.`in`

interface DeleteScheduleInPort {
	fun delete(scheduleId: Long, circleId: String, memberId: String)
}
