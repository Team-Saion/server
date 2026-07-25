package com.unicorn.server.domain.schedule.port.`in`

import com.unicorn.server.domain.schedule.enums.ConfirmationType
import com.unicorn.server.domain.schedule.port.dto.RegisterConfirmationCommand
import com.unicorn.server.domain.schedule.vo.ScheduleId

interface ScheduleConfirmationInPort {
	fun register(command: RegisterConfirmationCommand): ConfirmationType

	fun cancel(confirmationId: Long, scheduleId: ScheduleId, circleId: String, memberId: String)
}
