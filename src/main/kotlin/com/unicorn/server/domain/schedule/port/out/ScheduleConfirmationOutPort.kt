package com.unicorn.server.domain.schedule.port.out

import com.unicorn.server.domain.schedule.ScheduleConfirmation
import com.unicorn.server.domain.schedule.port.dto.ConfirmationCountResult

interface ScheduleConfirmationOutPort {
	fun findByScheduleIdAndMemberId(scheduleId: Long, memberId: String): ScheduleConfirmation?

	fun save(confirmation: ScheduleConfirmation): ScheduleConfirmation

	fun countGroupByType(scheduleId: Long): List<ConfirmationCountResult>

	fun deleteAllByScheduleId(scheduleId: Long)
}
