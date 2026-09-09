package com.unicorn.server.infrastructure.adapter.`in`.web.schedule

import com.unicorn.server.domain.schedule.port.dto.TodoMemberResult
import com.unicorn.server.domain.schedule.port.dto.TodoResult
import com.unicorn.server.domain.schedule.vo.TodoId
import com.unicorn.server.domain.member.enums.AvatarColor
import com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto.TodoProgressRateResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("TodoProgressRateResponse 단위 테스트")
class TodoProgressRateResponseTest {
	@Test
	@DisplayName("할일이 없으면 전체와 내 할일 진행률 모두 0을 반환한다")
	fun from_withNoTodos_returnsZeroRates() {
		val response = TodoProgressRateResponse.from(emptyList())

		assertThat(response).extracting(
			"rate", "doneCount", "totalCount", "myRate", "myDoneCount", "myTotalCount",
		).containsExactly(0, 0, 0, 0, 0, 0)
	}

	@Test
	@DisplayName("담당자 전원이 체크한 할일을 완료로 계산해 전체/내 진행률을 반환한다")
	fun from_withPartiallyCompletedTodos_returnsCalculatedRates() {
		val todos = listOf(
			TodoResult(TodoId.of("TD1"), "모두 완료", listOf(todoMember("member-1", true), todoMember("member-2", true)), true, true),
			TodoResult(TodoId.of("TD2"), "내 미완료", listOf(todoMember("member-1", false)), true, false),
			TodoResult(TodoId.of("TD3"), "다른 사람 완료", listOf(todoMember("member-2", true)), false, true),
		)

		val response = TodoProgressRateResponse.from(todos)

		assertThat(response).extracting(
			"rate", "doneCount", "totalCount", "myRate", "myDoneCount", "myTotalCount",
		).containsExactly(66, 2, 3, 50, 1, 2)
	}

	private fun todoMember(memberId: String, checked: Boolean) =
		TodoMemberResult(memberId, "닉네임", AvatarColor.TEAL_200, checked)
}
