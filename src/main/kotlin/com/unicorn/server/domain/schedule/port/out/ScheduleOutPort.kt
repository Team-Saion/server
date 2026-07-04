package com.unicorn.server.domain.schedule.port.out

import com.unicorn.server.domain.schedule.Schedule
import com.unicorn.server.domain.schedule.port.dto.SchedulePageCursor

interface ScheduleOutPort {
	fun save(schedule: Schedule): Schedule

	fun findById(scheduleId: Long): Schedule?

	fun findActiveByIdAndCircleId(scheduleId: Long, circleId: Long): Schedule?

	fun findActiveByCircleId(
		circleId: Long,
		cursor: SchedulePageCursor?,
		size: Int,
	): List<Schedule>
}
