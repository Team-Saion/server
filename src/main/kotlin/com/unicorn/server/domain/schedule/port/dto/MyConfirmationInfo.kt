package com.unicorn.server.domain.schedule.port.dto

import com.unicorn.server.domain.schedule.enums.ConfirmationType

data class MyConfirmationInfo(
	val confirmationId: Long,
	val confirmationType: ConfirmationType,
)
