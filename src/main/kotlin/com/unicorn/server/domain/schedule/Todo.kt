package com.unicorn.server.domain.schedule

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.domain.schedule.exception.TodoErrorCode
import com.unicorn.server.domain.schedule.vo.TodoId

class Todo private constructor(
	val id: TodoId,
	val scheduleId: String,
	val circleId: String,
	val title: String,
	val createdBy: String,
	members: List<TodoMember>,
) {
	var members: List<TodoMember> = members
		private set

	/**
	 * 담당자를 전체 교체합니다. 재배정 시 기존 담당자의 체크 상태는 의도적으로 초기화됩니다.
	 */
	fun assignMembers(memberIds: List<String>): Todo = apply {
		members = memberIds.distinct().map(::TodoMember)
	}

	fun check(memberId: String): Todo = apply {
		checkAssignee(memberId)
		members = members.map { if (it.memberId == memberId) it.check() else it }
	}

	fun uncheck(memberId: String): Todo = apply {
		checkAssignee(memberId)
		members = members.map { if (it.memberId == memberId) it.uncheck() else it }
	}

	fun isCompleted(): Boolean = members.isNotEmpty() && members.all { it.checked }

	private fun checkAssignee(memberId: String) {
		if (members.none { it.memberId == memberId }) {
			throw BusinessException(TodoErrorCode.TODO_CHECK_DENIED)
		}
	}

	companion object {
		fun create(
			id: TodoId,
			scheduleId: String,
			circleId: String,
			title: String,
			createdBy: String,
		): Todo = Todo(id, scheduleId, circleId, title, createdBy, emptyList())

		fun reconstitute(
			id: TodoId,
			scheduleId: String,
			circleId: String,
			title: String,
			createdBy: String,
			members: List<TodoMember>,
		): Todo = Todo(id, scheduleId, circleId, title, createdBy, members)
	}
}
