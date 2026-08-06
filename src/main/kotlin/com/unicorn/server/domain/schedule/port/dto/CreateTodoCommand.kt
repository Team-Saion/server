package com.unicorn.server.domain.schedule.port.dto

data class CreateTodoCommand(
	val circleId: String,
	val scheduleId: String,
	val memberId: String,
	val title: String,
)
