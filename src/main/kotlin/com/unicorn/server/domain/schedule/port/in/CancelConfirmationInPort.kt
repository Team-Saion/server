package com.unicorn.server.domain.schedule.port.`in`

import com.unicorn.server.domain.schedule.vo.ScheduleId

interface CancelConfirmationInPort {
	fun cancel(confirmationId: Long, scheduleId: ScheduleId, circleId: String, memberId: String)
}
