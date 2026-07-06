package com.unicorn.server.domain.schedule.port.dto

import com.unicorn.server.domain.schedule.enums.ConfirmationType
import com.unicorn.server.domain.schedule.vo.ScheduleId

data class RegisterConfirmationCommand(
	val scheduleId: ScheduleId,
	val circleId: String,
	val memberId: String,
	val confirmationType: ConfirmationType,
)
