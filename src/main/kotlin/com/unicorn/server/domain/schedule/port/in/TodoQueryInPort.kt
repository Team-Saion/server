package com.unicorn.server.domain.schedule.port.`in`

import com.unicorn.server.domain.schedule.port.dto.TodoResult

interface TodoQueryInPort {
	fun getTodosByScheduleId(
		scheduleId: String,
		circleId: String,
		memberId: String,
	): List<TodoResult>
}
