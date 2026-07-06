package com.unicorn.server.domain.schedule.port.out

import com.unicorn.server.domain.schedule.Schedule
import com.unicorn.server.domain.schedule.port.dto.SchedulePageCursor
import com.unicorn.server.domain.schedule.vo.ScheduleId

interface ScheduleOutPort {
	fun save(schedule: Schedule): Schedule

	fun findById(scheduleId: ScheduleId): Schedule?

	fun findActiveByIdAndCircleId(scheduleId: ScheduleId, circleId: String): Schedule?

	fun findActiveByCircleId(
		circleId: String,
		cursor: SchedulePageCursor?,
		size: Int,
	): List<Schedule>
}
