package com.unicorn.server.domain.schedule.port.`in`

import com.unicorn.server.domain.schedule.port.dto.CreateScheduleCommand
import com.unicorn.server.domain.schedule.vo.ScheduleId

interface ScheduleCreateInPort {
	fun create(command: CreateScheduleCommand): ScheduleId
}
