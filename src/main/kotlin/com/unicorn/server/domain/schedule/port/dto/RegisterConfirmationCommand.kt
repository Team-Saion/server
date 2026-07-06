package com.unicorn.server.domain.schedule.port.dto

import com.unicorn.server.domain.schedule.enums.ConfirmationType

data class RegisterConfirmationCommand(
	val scheduleId: Long,
	val circleId: String,
	val memberId: String,
	val confirmationType: ConfirmationType,
)
