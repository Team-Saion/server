package com.unicorn.server.domain.schedule.port.`in`

import com.unicorn.server.domain.schedule.port.dto.CreateScheduleCommand
import com.unicorn.server.domain.schedule.port.dto.UpdateScheduleCommand
import com.unicorn.server.domain.schedule.vo.ScheduleId

interface ScheduleCommandInPort {
	fun create(command: CreateScheduleCommand): ScheduleId

	fun update(command: UpdateScheduleCommand)

	fun delete(scheduleId: ScheduleId, circleId: String, memberId: String)
}
