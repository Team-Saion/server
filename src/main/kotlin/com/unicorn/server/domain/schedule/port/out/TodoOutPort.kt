package com.unicorn.server.domain.schedule.port.out

import com.unicorn.server.domain.schedule.Todo
import com.unicorn.server.domain.schedule.vo.TodoId

interface TodoOutPort {
	fun save(todo: Todo): Todo

	fun findById(todoId: TodoId): Todo?

	fun findByScheduleId(scheduleId: String): List<Todo>

	fun deleteById(todoId: TodoId)
}
