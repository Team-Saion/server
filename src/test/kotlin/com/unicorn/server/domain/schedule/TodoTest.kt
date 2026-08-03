package com.unicorn.server.domain.schedule

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import com.unicorn.server.domain.schedule.vo.TodoId

@DisplayName("Todo 도메인 단위 테스트")
class TodoTest {
	@Test @DisplayName("담당자를 할당할 수 있다")
	fun assignMembers_withMembers_assignsMembers() { assertThat(todo().assignMembers(listOf("member-1", "member-2")).members.map { it.memberId }).containsExactly("member-1", "member-2") }
	@Test @DisplayName("중복된 담당자 ID는 하나의 담당자로 할당된다")
	fun assignMembers_withDuplicateMemberIds_deduplicatesMembers() { assertThat(todo().assignMembers(listOf("member-1", "member-1", "member-2")).members.map { it.memberId }).containsExactly("member-1", "member-2") }
	@Test @DisplayName("담당자가 아닌 구성원은 체크할 수 없다")
	fun check_withNonAssignee_throwsException() { assertThatThrownBy { todo().assignMembers(listOf("member-1")).check("member-2") }.isInstanceOf(RuntimeException::class.java) }
	@Test @DisplayName("담당자가 아닌 구성원은 체크를 해제할 수 없다")
	fun uncheck_withNonAssignee_throwsException() { assertThatThrownBy { todo().assignMembers(listOf("member-1")).uncheck("member-2") }.isInstanceOf(RuntimeException::class.java) }
	@Test @DisplayName("모든 담당자가 체크하면 완료된다")
	fun isCompleted_whenAllAssigneesChecked_returnsTrue() { assertThat(todo().assignMembers(listOf("member-1", "member-2")).check("member-1").check("member-2").isCompleted()).isTrue() }
	@Test @DisplayName("담당자가 없으면 완료가 아니다")
	fun isCompleted_withNoAssignees_returnsFalse() { assertThat(todo().isCompleted()).isFalse() }
	@Test @DisplayName("일부 담당자만 체크하면 완료가 아니다")
	fun isCompleted_withPartialChecks_returnsFalse() { assertThat(todo().assignMembers(listOf("member-1", "member-2")).check("member-1").isCompleted()).isFalse() }
	@Test @DisplayName("체크를 해제하면 담당자의 체크 상태가 해제된다")
	fun uncheck_withAssignee_clearsCheckedState() { assertThat(todo().assignMembers(listOf("member-1")).check("member-1").uncheck("member-1").members.single().checked).isFalse() }
	private fun todo() = Todo.create(TodoId.of("TD20240801090000001"), "schedule-1", "circle-1", "할일", "author")
}
