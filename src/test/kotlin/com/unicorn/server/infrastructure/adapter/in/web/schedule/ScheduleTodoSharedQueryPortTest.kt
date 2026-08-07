package com.unicorn.server.infrastructure.adapter.`in`.web.schedule

import com.unicorn.server.domain.member.enums.AvatarColor
import com.unicorn.server.domain.schedule.port.`in`.TodoQueryInPort
import com.unicorn.server.domain.schedule.port.dto.TodoMemberResult
import com.unicorn.server.domain.schedule.port.dto.TodoResult
import com.unicorn.server.domain.schedule.vo.TodoId
import com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto.TodoResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("일정 상세와 Todo 목록의 공유 조회 포트 테스트")
class ScheduleTodoSharedQueryPortTest {
	@Test @DisplayName("같은 공유 TodoQueryInPort 결과는 두 화면에서 동일한 목록과 순서를 만든다")
	fun getTodos_withSharedPort_returnsIdenticalLists() {
		val port = object : TodoQueryInPort { override fun getTodosByScheduleId(scheduleId: String, circleId: String, memberId: String) = listOf(TodoResult(TodoId.of("TD1"), "미완료", listOf(TodoMemberResult(memberId, "닉네임", AvatarColor.TEAL_200, false)), true, false), TodoResult(TodoId.of("TD2"), "완료", emptyList(), false, true)) }
		val results = port.getTodosByScheduleId("schedule", "circle", "member")
		val scheduleMy = results.filter { it.isMy }.map(TodoResponse::from); val scheduleAll = results.map(TodoResponse::from)
		val todoMy = results.filter { it.isMy }.map(TodoResponse::from); val todoAll = results.map(TodoResponse::from)
		assertThat(todoMy).isEqualTo(scheduleMy); assertThat(todoAll).isEqualTo(scheduleAll)
	}
}
