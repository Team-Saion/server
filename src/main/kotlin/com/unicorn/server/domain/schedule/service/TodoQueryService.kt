package com.unicorn.server.domain.schedule.service

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.domain.member.port.`in`.MemberProfileInPort
import com.unicorn.server.domain.schedule.exception.TodoErrorCode
import com.unicorn.server.domain.schedule.port.`in`.TodoQueryInPort
import com.unicorn.server.domain.schedule.port.dto.TodoMemberResult
import com.unicorn.server.domain.schedule.port.dto.TodoResult
import com.unicorn.server.domain.schedule.port.out.ScheduleOutPort
import com.unicorn.server.domain.schedule.port.out.TodoOutPort
import com.unicorn.server.domain.schedule.vo.ScheduleId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class TodoQueryService(
	private val todoOutPort: TodoOutPort,
	private val memberProfileInPort: MemberProfileInPort,
	private val scheduleOutPort: ScheduleOutPort,
) : TodoQueryInPort {
	override fun getTodosByScheduleId(
		scheduleId: String,
		circleId: String,
		memberId: String,
	): List<TodoResult> {
		if (scheduleOutPort.findActiveByIdAndCircleId(ScheduleId.of(scheduleId), circleId) == null) {
			throw BusinessException(TodoErrorCode.SCHEDULE_NOT_FOUND)
		}

		return todoOutPort.findByScheduleId(scheduleId)
			.map { todo ->
				TodoResult(
					todoId = todo.id,
					title = todo.title,
					members = todo.members.mapNotNull { member ->
						memberProfileInPort.getMemberProfile(member.memberId)?.let { profile ->
							TodoMemberResult(
								memberId = member.memberId,
								nickname = profile.nickname,
								avatarColor = profile.avatarColor,
								checked = member.checked,
							)
						}
					},
					isMy = todo.members.any { it.memberId == memberId },
					isCompleted = todo.isCompleted(),
				)
			}
			.sortedByDescending { it.todoId.value }
	}
}
