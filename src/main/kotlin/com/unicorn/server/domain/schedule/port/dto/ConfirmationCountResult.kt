package com.unicorn.server.domain.schedule.port.dto

import com.unicorn.server.domain.schedule.enums.ConfirmationType

data class ConfirmationCountResult(
	val type: ConfirmationType,
	val count: Int,
)
