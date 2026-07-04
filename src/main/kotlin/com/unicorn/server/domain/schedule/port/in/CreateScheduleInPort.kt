package com.unicorn.server.domain.schedule.port.`in`

import com.unicorn.server.domain.schedule.port.dto.CreateScheduleCommand

interface CreateScheduleInPort {
	fun create(command: CreateScheduleCommand): Long
}
