package com.unicorn.server.domain.schedule.service

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.domain.schedule.Todo
import com.unicorn.server.domain.schedule.exception.TodoErrorCode
import com.unicorn.server.domain.schedule.port.`in`.TodoCheckInPort
import com.unicorn.server.domain.schedule.port.`in`.TodoCommandInPort
import com.unicorn.server.domain.schedule.port.`in`.TodoMemberAssignInPort
import com.unicorn.server.domain.schedule.port.dto.AssignTodoMembersCommand
import com.unicorn.server.domain.schedule.port.dto.CreateTodoCommand
import com.unicorn.server.domain.schedule.port.out.CircleAccessOutPort
import com.unicorn.server.domain.schedule.port.out.ScheduleOutPort
import com.unicorn.server.domain.schedule.port.out.TodoIdGenerator
import com.unicorn.server.domain.schedule.port.out.TodoOutPort
import com.unicorn.server.domain.schedule.vo.ScheduleId
import com.unicorn.server.domain.schedule.vo.TodoId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class TodoCommandService(
	private val todoOutPort: TodoOutPort,
	private val todoIdGenerator: TodoIdGenerator,
	private val circleAccessOutPort: CircleAccessOutPort,
	private val scheduleOutPort: ScheduleOutPort,
) : TodoCommandInPort, TodoMemberAssignInPort, TodoCheckInPort {
	override fun create(command: CreateTodoCommand): TodoId {
		requireCircleMember(command.circleId, command.memberId)
		requireActiveSchedule(command.scheduleId, command.circleId)
		val todo = Todo.create(
			id = todoIdGenerator.next(),
			scheduleId = command.scheduleId,
			circleId = command.circleId,
			title = command.title,
			createdBy = command.memberId,
		)
		return todoOutPort.save(todo).id
	}

	override fun delete(
		todoId: TodoId,
		scheduleId: String,
		circleId: String,
		memberId: String,
	) {
		requireCircleMember(circleId, memberId)
		requireActiveSchedule(scheduleId, circleId)
		requireTodoInScope(todoId, scheduleId, circleId)
		todoOutPort.deleteById(todoId)
	}

	override fun assignMembers(command: AssignTodoMembersCommand) {
		requireCircleMember(command.circleId, command.memberId)
		requireActiveSchedule(command.scheduleId, command.circleId)
		val todo = requireTodoInScope(command.todoId, command.scheduleId, command.circleId)
		if (scheduleOutPort.findById(ScheduleId.of(command.scheduleId))?.createdBy != command.memberId) {
			throw BusinessException(TodoErrorCode.TODO_ASSIGN_DENIED)
		}
		val invalidAssigneeIds = command.assigneeMemberIds.filterNot { circleAccessOutPort.isMember(command.circleId, it) }
		if (invalidAssigneeIds.isNotEmpty()) {
			throw BusinessException(TodoErrorCode.ASSIGNEE_NOT_FOUND, detail = invalidAssigneeIds.joinToString(", "))
		}
		todo.assignMembers(command.assigneeMemberIds)
		todoOutPort.save(todo)
	}

	override fun check(
		todoId: TodoId,
		scheduleId: String,
		circleId: String,
		memberId: String,
	) {
		requireCircleMember(circleId, memberId)
		requireActiveSchedule(scheduleId, circleId)
		val todo = requireTodoInScope(todoId, scheduleId, circleId)
		changeCheck(todo, circleId, memberId) { it.check(memberId) }
	}

	override fun uncheck(
		todoId: TodoId,
		scheduleId: String,
		circleId: String,
		memberId: String,
	) {
		requireCircleMember(circleId, memberId)
		requireActiveSchedule(scheduleId, circleId)
		val todo = requireTodoInScope(todoId, scheduleId, circleId)
		changeCheck(todo, circleId, memberId) { it.uncheck(memberId) }
	}

	private fun changeCheck(
		todo: Todo,
		circleId: String,
		memberId: String,
		action: (Todo) -> Todo,
	) {
		if (todo.members.none { it.memberId == memberId }) {
			throw BusinessException(TodoErrorCode.TODO_CHECK_DENIED)
		}
		requireCircleMember(circleId, memberId)
		todoOutPort.save(action(todo))
	}

	private fun requireCircleMember(circleId: String, memberId: String) {
		if (!circleAccessOutPort.isMember(circleId, memberId)) {
			throw BusinessException(TodoErrorCode.CIRCLE_ACCESS_DENIED)
		}
	}

	private fun requireActiveSchedule(scheduleId: String, circleId: String) {
		if (scheduleOutPort.findActiveByIdAndCircleId(ScheduleId.of(scheduleId), circleId) == null) {
			throw BusinessException(TodoErrorCode.SCHEDULE_NOT_FOUND)
		}
	}

	private fun requireTodoInScope(todoId: TodoId, scheduleId: String, circleId: String): Todo {
		val todo = todoOutPort.findById(todoId)
		if (todo == null || todo.scheduleId != scheduleId || todo.circleId != circleId) {
			throw BusinessException(TodoErrorCode.TODO_NOT_FOUND)
		}
		return todo
	}
}
