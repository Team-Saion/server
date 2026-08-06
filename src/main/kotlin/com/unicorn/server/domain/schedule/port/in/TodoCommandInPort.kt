package com.unicorn.server.domain.schedule.port.`in`

import com.unicorn.server.domain.schedule.port.dto.CreateTodoCommand
import com.unicorn.server.domain.schedule.vo.TodoId

interface TodoCommandInPort {
	fun create(command: CreateTodoCommand): TodoId

	fun delete(
		todoId: TodoId,
		scheduleId: String,
		circleId: String,
		memberId: String,
	)
}
