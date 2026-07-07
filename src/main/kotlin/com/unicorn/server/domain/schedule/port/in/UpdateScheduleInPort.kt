package com.unicorn.server.domain.schedule.port.`in`

import com.unicorn.server.domain.schedule.port.dto.UpdateScheduleCommand

interface UpdateScheduleInPort {
	fun update(command: UpdateScheduleCommand)
}
