package com.unicorn.server.infrastructure.adapter.`in`.web.schedule

import com.unicorn.server.domain.schedule.enums.ScheduleStatus
import com.unicorn.server.domain.schedule.enums.UrgencyLevel
import com.unicorn.server.domain.schedule.port.dto.ScheduleDetailResult
import com.unicorn.server.domain.schedule.port.dto.TodoMemberResult
import com.unicorn.server.domain.schedule.vo.ScheduleId
import com.unicorn.server.domain.schedule.port.dto.TodoResult
import com.unicorn.server.domain.schedule.vo.TodoId
import com.unicorn.server.domain.member.enums.AvatarColor
import com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto.ScheduleDetailResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

@DisplayName("ScheduleController Todo 상세 조합 테스트")
class ScheduleDetailTodoResponseTest {
	@Test @DisplayName("일정 상세 응답은 공유 Todo 조회 결과를 myTodos와 allTodos로 포함한다")
	fun getScheduleDetail_withTodos_includesMyTodosAndAllTodos() {
		val schedule = ScheduleDetailResult(ScheduleId.of("SC1"), "일정", LocalDate.now(), LocalDate.now(), null, null, true, false, null, ScheduleStatus.UPCOMING, 1, UrgencyLevel.NORMAL, 0, emptyList(), null, "author", LocalDateTime.now())
		val response = ScheduleDetailResponse.from(schedule, listOf(TodoResult(TodoId.of("TD1"), "내 할일", emptyList(), true, false)))
		assertThat(response.myTodos).hasSize(1); assertThat(response.allTodos).hasSize(1); assertThat(response.allTodos.single().isMy).isTrue()
	}

	@Test
	@DisplayName("일정 상세 응답은 담당자 전원이 체크한 할일을 완료로 계산한 진행률을 포함한다")
	fun getScheduleDetail_withTodos_includesTodoProgressRate() {
		val schedule = ScheduleDetailResult(ScheduleId.of("SC1"), "일정", LocalDate.now(), LocalDate.now(), null, null, true, false, null, ScheduleStatus.UPCOMING, 1, UrgencyLevel.NORMAL, 0, emptyList(), null, "author", LocalDateTime.now())
		val todos = listOf(
			TodoResult(TodoId.of("TD1"), "모두 완료", listOf(todoMember("member-1", true), todoMember("member-2", true)), true, true),
			TodoResult(TodoId.of("TD2"), "내 미완료", listOf(todoMember("member-1", false)), true, false),
			TodoResult(TodoId.of("TD3"), "다른 사람 완료", listOf(todoMember("member-2", true)), false, true),
		)

		val response = ScheduleDetailResponse.from(schedule, todos)

		assertThat(response.todoProgressRate).extracting(
			"rate", "doneCount", "totalCount", "myRate", "myDoneCount", "myTotalCount",
		).containsExactly(66, 2, 3, 50, 1, 2)
	}

	@Test
	@DisplayName("일정 상세 응답은 할일이 없으면 진행률을 모두 0으로 포함한다")
	fun getScheduleDetail_withNoTodos_includesZeroTodoProgressRate() {
		val schedule = ScheduleDetailResult(ScheduleId.of("SC1"), "일정", LocalDate.now(), LocalDate.now(), null, null, true, false, null, ScheduleStatus.UPCOMING, 1, UrgencyLevel.NORMAL, 0, emptyList(), null, "author", LocalDateTime.now())

		val response = ScheduleDetailResponse.from(schedule, emptyList())

		assertThat(response.todoProgressRate).extracting(
			"rate", "doneCount", "totalCount", "myRate", "myDoneCount", "myTotalCount",
		).containsExactly(0, 0, 0, 0, 0, 0)
	}

	private fun todoMember(memberId: String, checked: Boolean) =
		TodoMemberResult(memberId, "닉네임", AvatarColor.TEAL_200, checked)
}
