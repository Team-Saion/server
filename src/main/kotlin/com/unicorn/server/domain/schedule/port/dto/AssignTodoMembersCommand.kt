package com.unicorn.server.domain.schedule.port.dto

import com.unicorn.server.domain.schedule.vo.TodoId

data class AssignTodoMembersCommand(
	val circleId: String,
	val scheduleId: String,
	val todoId: TodoId,
	val memberId: String,
	val assigneeMemberIds: List<String>,
)
