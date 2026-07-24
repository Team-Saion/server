package com.unicorn.server.domain.schedule.port.dto

import com.unicorn.server.domain.schedule.vo.ScheduleId

data class RequestFamilyScheduleNotificationCommand(
	val scheduleId: ScheduleId,
	val circleId: String,
	val memberId: String,
)
