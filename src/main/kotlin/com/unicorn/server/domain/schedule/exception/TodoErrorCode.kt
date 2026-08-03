package com.unicorn.server.domain.schedule.exception

import com.unicorn.server.common.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class TodoErrorCode(
	override val code: String,
	override val message: String,
	override val httpStatus: HttpStatus,
) : ErrorCode {
	CIRCLE_ACCESS_DENIED("T403_1", "No access to this circle", HttpStatus.FORBIDDEN),
	TODO_ASSIGN_DENIED("T403_2", "Only the schedule author can assign todo members", HttpStatus.FORBIDDEN),
	TODO_CHECK_DENIED("T403_3", "Only an assignee can check this todo", HttpStatus.FORBIDDEN),
	SCHEDULE_NOT_FOUND("T404_1", "Schedule not found", HttpStatus.NOT_FOUND),
	TODO_NOT_FOUND("T404_2", "Todo not found", HttpStatus.NOT_FOUND),
	ASSIGNEE_NOT_FOUND("T404_3", "Circle member not found", HttpStatus.NOT_FOUND),
}
