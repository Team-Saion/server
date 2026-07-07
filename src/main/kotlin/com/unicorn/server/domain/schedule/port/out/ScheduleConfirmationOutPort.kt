package com.unicorn.server.domain.schedule.port.out

import com.unicorn.server.domain.schedule.ScheduleConfirmation
import com.unicorn.server.domain.schedule.port.dto.ConfirmationCountResult
import com.unicorn.server.domain.schedule.vo.ScheduleId

interface ScheduleConfirmationOutPort {
	fun findById(id: Long): ScheduleConfirmation?

	fun findByScheduleIdAndMemberId(scheduleId: ScheduleId, memberId: String): ScheduleConfirmation?

	fun save(confirmation: ScheduleConfirmation): ScheduleConfirmation

	fun deleteById(id: Long)

	fun countGroupByType(scheduleId: ScheduleId): List<ConfirmationCountResult>

	fun deleteAllByScheduleId(scheduleId: ScheduleId)
}
