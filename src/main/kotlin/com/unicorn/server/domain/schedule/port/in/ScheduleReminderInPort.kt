package com.unicorn.server.domain.schedule.port.`in`

import java.time.LocalDateTime

interface ScheduleReminderInPort {
	fun dispatchDaily(now: LocalDateTime)

	fun dispatchTimed(now: LocalDateTime)
}
