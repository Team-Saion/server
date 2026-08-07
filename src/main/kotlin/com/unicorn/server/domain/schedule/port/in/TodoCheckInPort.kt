package com.unicorn.server.domain.schedule.port.`in`

import com.unicorn.server.domain.schedule.vo.TodoId

interface TodoCheckInPort {
	fun check(
		todoId: TodoId,
		scheduleId: String,
		circleId: String,
		memberId: String,
	)

	fun uncheck(
		todoId: TodoId,
		scheduleId: String,
		circleId: String,
		memberId: String,
	)
}
