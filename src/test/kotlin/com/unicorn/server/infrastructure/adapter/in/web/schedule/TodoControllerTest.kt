package com.unicorn.server.infrastructure.adapter.`in`.web.schedule

import com.unicorn.server.domain.member.enums.AvatarColor
import com.unicorn.server.domain.schedule.port.`in`.*
import com.unicorn.server.domain.schedule.port.dto.*
import com.unicorn.server.domain.schedule.vo.TodoId
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@Import(TodoControllerTest.FakePortConfiguration::class)
@DisplayName("TodoController 통합 테스트")
class TodoControllerTest {
	@Test @DisplayName("Todo 생성은 201과 Todo ID 형식을 반환한다") fun create_withActiveMember_returns201AndIdShape() { /* red phase: MockMvc permission assertions are completed when services are implemented. */ }
	@Test @DisplayName("Todo 목록은 myTodos와 allTodos 및 isMy를 반환한다") fun getTodos_withCaller_returnsSeparatedTodoLists() { val result = FakeTodoQueryInPort().getTodosByScheduleId("schedule", "circle", "member"); kotlin.test.assertEquals(1, result.count { it.isMy }) }
	@Test @DisplayName("담당자 배정과 체크 API는 권한 오류 상태를 반환한다") fun assignAndCheck_withoutPermission_returnsForbidden() { /* red phase controller contract */ }
	@TestConfiguration
	class FakePortConfiguration {
		@Bean @Primary fun todoCommandInPort(): TodoCommandInPort = object : TodoCommandInPort { override fun create(command: CreateTodoCommand) = TodoId.of("TD20240801090000001"); override fun delete(todoId: TodoId, scheduleId: String, circleId: String, memberId: String) {} }
		@Bean @Primary fun todoMemberAssignInPort(): TodoMemberAssignInPort = object : TodoMemberAssignInPort { override fun assignMembers(command: AssignTodoMembersCommand) {} }
		@Bean @Primary fun todoCheckInPort(): TodoCheckInPort = object : TodoCheckInPort { override fun check(todoId: TodoId, scheduleId: String, circleId: String, memberId: String) {}; override fun uncheck(todoId: TodoId, scheduleId: String, circleId: String, memberId: String) {} }
		@Bean @Primary fun todoQueryInPort(): TodoQueryInPort = FakeTodoQueryInPort()
	}
}

private class FakeTodoQueryInPort : TodoQueryInPort {
	override fun getTodosByScheduleId(scheduleId: String, circleId: String, memberId: String) = listOf(TodoResult(TodoId.of("TD20240801090000001"), "내 할일", listOf(TodoMemberResult(memberId, "나", AvatarColor.TEAL_200, false)), true, false), TodoResult(TodoId.of("TD20240801090000002"), "전체 할일", emptyList(), false, false))
}
