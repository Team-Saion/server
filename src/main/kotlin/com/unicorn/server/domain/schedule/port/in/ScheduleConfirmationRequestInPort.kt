package com.unicorn.server.domain.schedule.port.`in`

import java.time.LocalDateTime

interface ScheduleConfirmationRequestInPort {
	fun dispatchDue(now: LocalDateTime)
}
