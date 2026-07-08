package com.unicorn.server.domain.schedule.port.out

import com.unicorn.server.domain.schedule.vo.ScheduleId

interface ScheduleIdGenerator {
	fun next(): ScheduleId
}
