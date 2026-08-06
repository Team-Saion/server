package com.unicorn.server.infrastructure.adapter.`in`.web.schedule

import com.unicorn.server.domain.schedule.port.`in`.TodoCheckInPort
import com.unicorn.server.domain.schedule.port.`in`.TodoCommandInPort
import com.unicorn.server.domain.schedule.port.`in`.TodoMemberAssignInPort
import com.unicorn.server.domain.schedule.port.`in`.TodoQueryInPort
import com.unicorn.server.domain.schedule.port.dto.AssignTodoMembersCommand
import com.unicorn.server.domain.schedule.port.dto.CreateTodoCommand
import com.unicorn.server.domain.schedule.vo.TodoId
import com.unicorn.server.infrastructure.adapter.`in`.web.common.dto.ApiResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto.AssignTodoMembersRequest
import com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto.CreateTodoRequest
import com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto.TodoIdResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto.TodoListResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto.TodoResponse
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/circles/{circleId}/schedules/{scheduleId}/todos")
class TodoController(
	private val todoCommandInPort: TodoCommandInPort,
	private val todoMemberAssignInPort: TodoMemberAssignInPort,
	private val todoCheckInPort: TodoCheckInPort,
	private val todoQueryInPort: TodoQueryInPort,
) : TodoApiDoc {
	@PostMapping
	override fun create(
		@AuthenticationPrincipal memberId: String,
		@PathVariable circleId: String,
		@PathVariable scheduleId: String,
		@RequestBody @Valid request: CreateTodoRequest,
	): ApiResponse<TodoIdResponse> {
		val todoId = todoCommandInPort.create(
			CreateTodoCommand(circleId, scheduleId, memberId, request.title),
		)
		return ApiResponse.created(TodoIdResponse.of(todoId))
	}

	@DeleteMapping("/{todoId}")
	override fun delete(
		@AuthenticationPrincipal memberId: String,
		@PathVariable circleId: String,
		@PathVariable scheduleId: String,
		@PathVariable todoId: String,
	): ApiResponse<Unit> {
		todoCommandInPort.delete(TodoId.of(todoId), scheduleId, circleId, memberId)
		return ApiResponse.success()
	}

	@PatchMapping("/{todoId}/members")
	override fun assignMembers(
		@AuthenticationPrincipal memberId: String,
		@PathVariable circleId: String,
		@PathVariable scheduleId: String,
		@PathVariable todoId: String,
		@RequestBody request: AssignTodoMembersRequest,
	): ApiResponse<Unit> {
		todoMemberAssignInPort.assignMembers(
			AssignTodoMembersCommand(circleId, scheduleId, TodoId.of(todoId), memberId, request.assigneeMemberIds),
		)
		return ApiResponse.success()
	}

	@PatchMapping("/{todoId}/check")
	override fun check(
		@AuthenticationPrincipal memberId: String,
		@PathVariable circleId: String,
		@PathVariable scheduleId: String,
		@PathVariable todoId: String,
	): ApiResponse<Unit> {
		todoCheckInPort.check(TodoId.of(todoId), scheduleId, circleId, memberId)
		return ApiResponse.success()
	}

	@DeleteMapping("/{todoId}/check")
	override fun uncheck(
		@AuthenticationPrincipal memberId: String,
		@PathVariable circleId: String,
		@PathVariable scheduleId: String,
		@PathVariable todoId: String,
	): ApiResponse<Unit> {
		todoCheckInPort.uncheck(TodoId.of(todoId), scheduleId, circleId, memberId)
		return ApiResponse.success()
	}

	@GetMapping
	override fun getTodos(
		@AuthenticationPrincipal memberId: String,
		@PathVariable circleId: String,
		@PathVariable scheduleId: String,
	): ApiResponse<TodoListResponse> {
		val todos = todoQueryInPort.getTodosByScheduleId(scheduleId, circleId, memberId)
		return ApiResponse.success(
			TodoListResponse(
				myTodos = todos.filter { it.isMy }.map(TodoResponse::from),
				allTodos = todos.map(TodoResponse::from),
			),
		)
	}
}
