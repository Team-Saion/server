package com.unicorn.server.domain.schedule.port.`in`

import com.unicorn.server.domain.schedule.enums.ConfirmationType
import com.unicorn.server.domain.schedule.port.dto.RegisterConfirmationCommand

interface ScheduleConfirmationRegisterInPort {
	fun register(command: RegisterConfirmationCommand): ConfirmationType
}
