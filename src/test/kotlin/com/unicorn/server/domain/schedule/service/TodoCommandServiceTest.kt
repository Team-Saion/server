package com.unicorn.server.domain.schedule.service

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.domain.schedule.Schedule
import com.unicorn.server.domain.schedule.Todo
import com.unicorn.server.domain.schedule.exception.TodoErrorCode
import com.unicorn.server.domain.schedule.port.dto.AssignTodoMembersCommand
import com.unicorn.server.domain.schedule.port.dto.CreateTodoCommand
import com.unicorn.server.domain.schedule.port.dto.SchedulePageCursor
import com.unicorn.server.domain.schedule.port.out.CircleAccessOutPort
import com.unicorn.server.domain.schedule.port.out.ScheduleOutPort
import com.unicorn.server.domain.schedule.port.out.TodoIdGenerator
import com.unicorn.server.domain.schedule.port.out.TodoOutPort
import com.unicorn.server.domain.schedule.vo.ScheduleId
import com.unicorn.server.domain.schedule.vo.TodoId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@DisplayName("TodoCommandService 단위 테스트")
class TodoCommandServiceTest {
	private val todoOutPort = FakeTodoOutPort()
	private val circleAccess = FakeCircleAccess()
	private val scheduleOutPort = FakeScheduleOutPort()
	private val service = TodoCommandService(
		todoOutPort,
		object : TodoIdGenerator {
			override fun next() = TodoId.of("TD20240801090000001")
		},
		circleAccess,
		scheduleOutPort,
	)

	@Test
	@DisplayName("활성 구성원은 Todo를 생성할 수 있다")
	fun create_withActiveMember_returnsTodoId() {
		circleAccess.members += "member"
		scheduleOutPort.active = true

		service.create(CreateTodoCommand("circle", "schedule", "member", "제목"))
	}

	@Test
	@DisplayName("탈퇴한 구성원은 Todo를 생성할 수 없다")
	fun create_withWithdrawnMember_throwsCircleAccessDenied() {
		assertError(TodoErrorCode.CIRCLE_ACCESS_DENIED) {
			service.create(CreateTodoCommand("circle", "schedule", "withdrawn", "제목"))
		}
	}

	@Test
	@DisplayName("일정 작성자만 담당자를 배정할 수 있다")
	fun assignMembers_withRegularMember_throwsTodoAssignDenied() {
		circleAccess.members += "regular"
		scheduleOutPort.active = true
		todoOutPort.save(todo())

		assertError(TodoErrorCode.TODO_ASSIGN_DENIED) {
			service.assignMembers(AssignTodoMembersCommand("circle", "schedule", TodoId.of("TD1"), "regular", listOf("member")))
		}
	}

	@Test
	@DisplayName("일정 작성자가 아닌 써클 initiator도 담당자를 배정할 수 없다")
	fun assignMembers_withInitiatorButNotAuthor_throwsTodoAssignDenied() {
		circleAccess.members += "initiator"
		scheduleOutPort.active = true
		todoOutPort.save(todo())

		assertError(TodoErrorCode.TODO_ASSIGN_DENIED) {
			service.assignMembers(AssignTodoMembersCommand("circle", "schedule", TodoId.of("TD1"), "initiator", listOf("member")))
		}
	}

	@Test
	@DisplayName("써클을 탈퇴한 작성자는 담당자를 배정할 수 없다")
	fun assignMembers_withWithdrawnAuthor_throwsCircleAccessDenied() {
		todoOutPort.save(todo())

		assertError(TodoErrorCode.CIRCLE_ACCESS_DENIED) {
			service.assignMembers(AssignTodoMembersCommand("circle", "schedule", TodoId.of("TD1"), "member", listOf("member")))
		}
	}

	@Test
	@DisplayName("써클 구성원이 아닌 담당자를 배정하면 찾을 수 없음 예외가 발생한다")
	fun assignMembers_withNonCircleMember_throwsAssigneeNotFound() {
		circleAccess.members += "author"
		scheduleOutPort.active = true
		todoOutPort.save(todo())

		assertThatThrownBy {
			service.assignMembers(AssignTodoMembersCommand("circle", "schedule", TodoId.of("TD1"), "author", listOf("missing")))
		}
			.isInstanceOf(BusinessException::class.java)
			.extracting { (it as BusinessException).errorCode }
			.isEqualTo(TodoErrorCode.ASSIGNEE_NOT_FOUND)
	}

	@Test
	@DisplayName("존재하지 않는 담당자를 배정하면 에러 메시지에 어떤 memberId가 문제인지 담긴다")
	fun assignMembers_withMissingAssignee_includesOffendingMemberIdInMessage() {
		circleAccess.members += "author"
		scheduleOutPort.active = true
		todoOutPort.save(todo())

		assertThatThrownBy {
			service.assignMembers(AssignTodoMembersCommand("circle", "schedule", TodoId.of("TD1"), "author", listOf("missing")))
		}
			.isInstanceOf(BusinessException::class.java)
			.hasMessageContaining("missing")
	}

	@Test
	@DisplayName("유효하지 않은 담당자가 하나라도 있으면 기존 담당자 목록을 변경하지 않는다")
	fun assignMembers_withMixedValidAndInvalidAssignees_throwsAssigneeNotFoundWithoutPartialApplication() {
		circleAccess.members += "author"
		circleAccess.members += "valid"
		scheduleOutPort.active = true
		val todo = todo().assignMembers(listOf("existing"))
		todoOutPort.save(todo)

		assertError(TodoErrorCode.ASSIGNEE_NOT_FOUND) {
			service.assignMembers(AssignTodoMembersCommand("circle", "schedule", TodoId.of("TD1"), "author", listOf("valid", "missing")))
		}

		assertThat(todoOutPort.findById(TodoId.of("TD1"))!!.members.map { it.memberId })
			.containsExactly("existing")
	}

	@Test
	@DisplayName("삭제는 활성 구성원에게 허용된다")
	fun delete_withActiveMember_deletesTodo() {
		circleAccess.members += "member"
		scheduleOutPort.active = true
		todoOutPort.save(todo())

		service.delete(TodoId.of("TD1"), "schedule", "circle", "member")
	}

	@Test
	@DisplayName("삭제는 삭제된 부모 일정에서 차단된다")
	fun delete_withDeletedSchedule_throwsScheduleNotFound() {
		circleAccess.members += "member"

		assertError(TodoErrorCode.SCHEDULE_NOT_FOUND) {
			service.delete(TodoId.of("TD1"), "schedule", "circle", "member")
		}
	}

	@Test
	@DisplayName("담당자만 체크할 수 있다")
	fun check_withNonAssignee_throwsTodoCheckDenied() {
		circleAccess.members += "other"
		scheduleOutPort.active = true
		todoOutPort.save(todo().assignMembers(listOf("someone-else")))

		assertError(TodoErrorCode.TODO_CHECK_DENIED) {
			service.check(TodoId.of("TD1"), "schedule", "circle", "other")
		}
	}

	@Test
	@DisplayName("탈퇴한 구성원은 체크 해제할 수 없다")
	fun uncheck_withWithdrawnMember_throwsCircleAccessDenied() {
		assertError(TodoErrorCode.CIRCLE_ACCESS_DENIED) {
			service.uncheck(TodoId.of("TD1"), "schedule", "circle", "withdrawn")
		}
	}

	@Test
	@DisplayName("다른 일정 또는 써클의 Todo는 삭제할 수 없다")
	fun delete_withTodoOutsidePathScope_throwsTodoNotFound() {
		circleAccess.members += "member"
		scheduleOutPort.active = true
		todoOutPort.save(Todo.create(TodoId.of("TD1"), "other-schedule", "other-circle", "title", "member"))

		assertError(TodoErrorCode.TODO_NOT_FOUND) {
			service.delete(TodoId.of("TD1"), "schedule", "circle", "member")
		}
	}

	@Test
	@DisplayName("다른 일정 또는 써클의 Todo는 체크할 수 없다")
	fun check_withTodoOutsidePathScope_throwsTodoNotFound() {
		circleAccess.members += "member"
		scheduleOutPort.active = true
		todoOutPort.save(Todo.create(TodoId.of("TD1"), "other-schedule", "other-circle", "title", "member").assignMembers(listOf("member")))

		assertError(TodoErrorCode.TODO_NOT_FOUND) {
			service.check(TodoId.of("TD1"), "schedule", "circle", "member")
		}
	}

	@Test
	@DisplayName("다른 일정 또는 써클의 Todo는 체크 해제할 수 없다")
	fun uncheck_withTodoOutsidePathScope_throwsTodoNotFound() {
		circleAccess.members += "member"
		scheduleOutPort.active = true
		todoOutPort.save(Todo.create(TodoId.of("TD1"), "other-schedule", "other-circle", "title", "member").assignMembers(listOf("member")))

		assertError(TodoErrorCode.TODO_NOT_FOUND) {
			service.uncheck(TodoId.of("TD1"), "schedule", "circle", "member")
		}
	}

	@Test
	@DisplayName("다른 일정 또는 써클의 Todo에는 담당자를 배정할 수 없다")
	fun assignMembers_withTodoOutsidePathScope_throwsTodoNotFound() {
		circleAccess.members += "author"
		scheduleOutPort.active = true
		todoOutPort.save(Todo.create(TodoId.of("TD1"), "other-schedule", "other-circle", "title", "author"))

		assertError(TodoErrorCode.TODO_NOT_FOUND) {
			service.assignMembers(AssignTodoMembersCommand("circle", "schedule", TodoId.of("TD1"), "author", listOf("member")))
		}
	}

	@Test
	@DisplayName("비활성 부모 일정의 Todo에는 담당자를 배정할 수 없다")
	fun assignMembers_withInactiveSchedule_throwsScheduleNotFound() {
		circleAccess.members += "author"
		todoOutPort.save(todo())

		assertError(TodoErrorCode.SCHEDULE_NOT_FOUND) {
			service.assignMembers(AssignTodoMembersCommand("circle", "schedule", TodoId.of("TD1"), "author", listOf("author")))
		}
	}

	@Test
	@DisplayName("이미 삭제된 Todo를 다시 삭제하면 찾을 수 없음 예외가 발생한다")
	fun delete_withAlreadyDeletedTodo_throwsTodoNotFound() {
		circleAccess.members += "member"
		scheduleOutPort.active = true
		todoOutPort.save(todo())
		service.delete(TodoId.of("TD1"), "schedule", "circle", "member")

		assertError(TodoErrorCode.TODO_NOT_FOUND) {
			service.delete(TodoId.of("TD1"), "schedule", "circle", "member")
		}
	}

	@Test
	@DisplayName("이미 삭제된 Todo는 체크할 수 없다")
	fun check_withAlreadyDeletedTodo_throwsTodoNotFound() {
		circleAccess.members += "member"
		scheduleOutPort.active = true
		todoOutPort.save(todo().assignMembers(listOf("member")))
		todoOutPort.deleteById(TodoId.of("TD1"))

		assertError(TodoErrorCode.TODO_NOT_FOUND) {
			service.check(TodoId.of("TD1"), "schedule", "circle", "member")
		}
	}

	@Test
	@DisplayName("이미 삭제된 Todo는 체크를 해제할 수 없다")
	fun uncheck_withAlreadyDeletedTodo_throwsTodoNotFound() {
		circleAccess.members += "member"
		scheduleOutPort.active = true
		todoOutPort.save(todo().assignMembers(listOf("member")))
		todoOutPort.deleteById(TodoId.of("TD1"))

		assertError(TodoErrorCode.TODO_NOT_FOUND) {
			service.uncheck(TodoId.of("TD1"), "schedule", "circle", "member")
		}
	}

	@Test
	@DisplayName("이미 삭제된 Todo에는 담당자를 배정할 수 없다")
	fun assignMembers_withAlreadyDeletedTodo_throwsTodoNotFound() {
		circleAccess.members += "author"
		scheduleOutPort.active = true
		todoOutPort.save(todo())
		todoOutPort.deleteById(TodoId.of("TD1"))

		assertError(TodoErrorCode.TODO_NOT_FOUND) {
			service.assignMembers(AssignTodoMembersCommand("circle", "schedule", TodoId.of("TD1"), "author", listOf("author")))
		}
	}

	private fun assertError(error: TodoErrorCode, action: () -> Unit) {
		assertThatThrownBy { action() }
			.isInstanceOf(BusinessException::class.java)
			.extracting { (it as BusinessException).errorCode }
			.isEqualTo(error)
	}

	private fun todo() = Todo.create(TodoId.of("TD1"), "schedule", "circle", "title", "member")

	private class FakeTodoOutPort : TodoOutPort {
		private val todos = mutableMapOf<TodoId, Todo>()

		override fun save(todo: Todo): Todo {
			todos[todo.id] = todo
			return todo
		}

		override fun findById(todoId: TodoId): Todo? = todos[todoId]

		override fun findByScheduleId(scheduleId: String): List<Todo> = todos.values.filter { it.scheduleId == scheduleId }

		override fun deleteById(todoId: TodoId) {
			todos.remove(todoId)
		}
	}

	private class FakeCircleAccess : CircleAccessOutPort {
		val members = mutableSetOf<String>()

		override fun existsById(circleId: String): Boolean = true
		override fun isMember(circleId: String, memberId: String): Boolean = memberId in members
		override fun hasOtherActiveMember(circleId: String, excludedMemberId: String): Boolean = false
		override fun isInitiator(circleId: String, memberId: String): Boolean = false
	}

	private class FakeScheduleOutPort : ScheduleOutPort {
		var active = false

		override fun save(schedule: Schedule): Schedule = schedule

		override fun findById(scheduleId: ScheduleId): Schedule? =
			if (scheduleId.value == "schedule") schedule(scheduleId, "circle", "author") else null

		override fun findActiveByIdAndCircleId(scheduleId: ScheduleId, circleId: String): Schedule? =
			if (active && scheduleId.value == "schedule" && circleId == "circle") schedule(scheduleId, circleId, "author") else null

		override fun findActiveByCircleId(circleId: String, now: LocalDateTime, cursor: SchedulePageCursor?, size: Int): List<Schedule> = emptyList()
		override fun findActiveByStartDateAndCreatedBefore(startDate: LocalDate, createdBefore: LocalDateTime): List<Schedule> = emptyList()
		override fun findActiveAllDayByStartDateAndCreatedBefore(startDate: LocalDate, createdBefore: LocalDateTime): List<Schedule> = emptyList()
		override fun findActiveTimedByStartAtAndCreatedBefore(startDate: LocalDate, startTime: LocalTime, createdBefore: LocalDateTime): List<Schedule> = emptyList()
		override fun findActiveConfirmationRequiredCreatedBetween(createdFrom: LocalDateTime, createdBefore: LocalDateTime): List<Schedule> = emptyList()
		override fun findUpcomingByCircleId(circleId: String, now: LocalDateTime, limit: Int): List<Schedule> = emptyList()
		override fun countActiveByCircleId(circleId: String): Long = 0

		private fun schedule(id: ScheduleId, circleId: String, createdBy: String): Schedule = Schedule.reconstitute(
			id = id,
			circleId = circleId,
			title = "일정",
			startDate = LocalDate.of(2024, 8, 1),
			endDate = LocalDate.of(2024, 8, 1),
			startTime = null,
			endTime = null,
			needConfirm = false,
			memo = null,
			createdBy = createdBy,
			updatedBy = createdBy,
			createdAt = LocalDateTime.of(2024, 7, 1, 10, 0),
			updatedAt = LocalDateTime.of(2024, 7, 1, 10, 0),
			isDeleted = false,
		)
	}
}
