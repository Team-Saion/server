package com.unicorn.server.domain.schedule.port.`in`

import com.unicorn.server.domain.schedule.port.dto.UpdateScheduleCommand

interface ScheduleUpdateInPort {
	fun update(command: UpdateScheduleCommand)
}
