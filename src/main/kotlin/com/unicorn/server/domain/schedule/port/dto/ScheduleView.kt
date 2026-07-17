package com.unicorn.server.domain.schedule.port.dto

import java.time.LocalDate

data class ScheduleView(
	val id: String,
	val name: String,
	val targetDate: LocalDate,
	val dDay: Long,
	val urgencyLevel: String,
	val preparationProgress: Double,
)
