package com.unicorn.server.domain.schedule.port.`in`

import com.unicorn.server.domain.schedule.vo.ScheduleId

interface ScheduleConfirmationStatusInPort {
	fun hasConfirmed(scheduleId: ScheduleId, memberId: String): Boolean
}
