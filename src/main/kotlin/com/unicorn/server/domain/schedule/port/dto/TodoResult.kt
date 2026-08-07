package com.unicorn.server.domain.schedule.port.dto

import com.unicorn.server.domain.schedule.vo.TodoId

data class TodoResult(
	val todoId: TodoId,
	val title: String,
	val members: List<TodoMemberResult>,
	val isMy: Boolean,
	val isCompleted: Boolean,
)
