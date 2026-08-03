package com.unicorn.server.domain.schedule.service

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.domain.member.port.`in`.MemberProfileInPort
import com.unicorn.server.domain.member.port.dto.MemberProfileDto
import com.unicorn.server.domain.schedule.Schedule
import com.unicorn.server.domain.schedule.Todo
import com.unicorn.server.domain.schedule.exception.TodoErrorCode
import com.unicorn.server.domain.schedule.port.dto.SchedulePageCursor
import com.unicorn.server.domain.schedule.port.out.ScheduleOutPort
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

@DisplayName("TodoQueryService 단위 테스트")
class TodoQueryServiceTest {
	private val schedules = FakeScheduleOutPort()
	private val todoOutPort = FakeTodoOutPort()
	private val service = TodoQueryService(
		todoOutPort,
		object : MemberProfileInPort {
			override fun getMemberProfile(memberId: String): MemberProfileDto? = null
		},
		schedules,
	)

	@Test
	@DisplayName("조회 결과는 호출자별 isMy와 생성일 최신순 정렬을 제공한다")
	fun getTodosByScheduleId_withTodos_returnsMyFlagsAndNewestFirst() {
		todoOutPort.save(todo("TD20240801090000001", listOf("member")))
		todoOutPort.save(todo("TD20240801090000002", listOf("other")))
		todoOutPort.save(todo("TD20240801090100001", listOf("member")))

		val results = service.getTodosByScheduleId("schedule", "circle", "member")

		assertThat(results.map { it.todoId.value })
			.containsExactly("TD20240801090100001", "TD20240801090000002", "TD20240801090000001")
		assertThat(results.map { it.isMy })
			.containsExactly(true, false, true)
	}

	@Test
	@DisplayName("삭제된 부모 일정의 Todo 조회는 찾을 수 없음으로 처리한다")
	fun getTodosByScheduleId_withDeletedSchedule_throwsScheduleNotFound() {
		schedules.active = false

		assertError { service.getTodosByScheduleId("schedule", "circle", "member") }
	}

	@Test
	@DisplayName("없는 부모 일정의 Todo 조회는 찾을 수 없음으로 처리한다")
	fun getTodosByScheduleId_withMissingSchedule_throwsScheduleNotFound() {
		assertError { service.getTodosByScheduleId("missing", "circle", "member") }
	}

	private fun assertError(action: () -> Unit) {
		assertThatThrownBy { action() }
			.isInstanceOf(BusinessException::class.java)
			.extracting { (it as BusinessException).errorCode }
			.isEqualTo(TodoErrorCode.SCHEDULE_NOT_FOUND)
	}

	private fun todo(id: String, members: List<String>): Todo = Todo.create(
		id = TodoId.of(id),
		scheduleId = "schedule",
		circleId = "circle",
		title = "할일",
		createdBy = "author",
	).assignMembers(members)

	private class FakeTodoOutPort : TodoOutPort {
		private val todos = mutableMapOf<TodoId, Todo>()

		override fun save(todo: Todo): Todo {
			todos[todo.id] = todo
			return todo
		}

		override fun findById(todoId: TodoId): Todo? = todos[todoId]

		override fun findByScheduleId(scheduleId: String): List<Todo> =
			todos.values.filter { it.scheduleId == scheduleId }

		override fun deleteById(todoId: TodoId) {
			todos.remove(todoId)
		}
	}

	private class FakeScheduleOutPort : ScheduleOutPort {
		var active = true

		override fun save(schedule: Schedule): Schedule = schedule
		override fun findById(scheduleId: ScheduleId): Schedule? = null
		override fun findActiveByIdAndCircleId(scheduleId: ScheduleId, circleId: String): Schedule? =
			if (active && scheduleId.value == "schedule" && circleId == "circle") schedule(scheduleId) else null

		override fun findActiveByCircleId(circleId: String, now: LocalDateTime, cursor: SchedulePageCursor?, size: Int): List<Schedule> = emptyList()
		override fun findActiveByStartDateAndCreatedBefore(startDate: LocalDate, createdBefore: LocalDateTime): List<Schedule> = emptyList()
		override fun findActiveAllDayByStartDateAndCreatedBefore(startDate: LocalDate, createdBefore: LocalDateTime): List<Schedule> = emptyList()
		override fun findActiveTimedByStartAtAndCreatedBefore(startDate: LocalDate, startTime: LocalTime, createdBefore: LocalDateTime): List<Schedule> = emptyList()
		override fun findActiveConfirmationRequiredCreatedBetween(createdFrom: LocalDateTime, createdBefore: LocalDateTime): List<Schedule> = emptyList()
		override fun findUpcomingByCircleId(circleId: String, now: LocalDateTime, limit: Int): List<Schedule> = emptyList()
		override fun countActiveByCircleId(circleId: String): Long = 0

		private fun schedule(id: ScheduleId): Schedule = Schedule.reconstitute(
			id = id,
			circleId = "circle",
			title = "일정",
			startDate = LocalDate.of(2024, 8, 1),
			endDate = LocalDate.of(2024, 8, 1),
			startTime = null,
			endTime = null,
			needConfirm = false,
			memo = null,
			createdBy = "author",
			updatedBy = "author",
			createdAt = LocalDateTime.of(2024, 7, 1, 10, 0),
			updatedAt = LocalDateTime.of(2024, 7, 1, 10, 0),
			isDeleted = false,
		)
	}
}
