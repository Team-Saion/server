package com.unicorn.server.domain.schedule.port.`in`

import com.unicorn.server.domain.schedule.port.dto.RequestFamilyScheduleNotificationCommand

interface ScheduleFamilyNotificationRequestInPort {
	fun request(command: RequestFamilyScheduleNotificationCommand)
}
