package com.unicorn.server.infrastructure.adapter.`in`.web.schedule

import com.unicorn.server.common.exception.CommonErrorCode
import com.unicorn.server.domain.schedule.exception.TodoErrorCode
import com.unicorn.server.infrastructure.adapter.`in`.web.common.dto.ApiResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiErrorCodeExample
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiErrorCodeExamples
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiSuccessCodeExample
import com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto.AssignTodoMembersRequest
import com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto.CreateTodoRequest
import com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto.TodoIdResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto.TodoListResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "Todo API", description = "일정 하위 할일(Todo) 생성/삭제/담당자 배정/체크/조회 API")
interface TodoApiDoc {

	@Operation(
		summary = "할일 생성",
		description = """
			일정에 새 할일을 생성합니다.

			**권한**: 활성 써클 구성원이라면 누구나 생성할 수 있습니다.
			부모 일정은 존재하며 삭제되지 않은 활성 상태여야 합니다.

			**입력 규칙**
			- title은 필수입니다.

			**응답**: 생성된 할일 ID를 반환합니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "INVALID_INPUT"),
		ApiErrorCodeExample(codeType = TodoErrorCode::class, code = "CIRCLE_ACCESS_DENIED"),
		ApiErrorCodeExample(codeType = TodoErrorCode::class, code = "SCHEDULE_NOT_FOUND"),
	)
	@ApiSuccessCodeExample(TodoIdResponse::class)
	fun create(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
		@Parameter(description = "써클 ID", example = "CC202506010000000001")
		@PathVariable circleId: String,
		@Parameter(description = "일정 ID", example = "SC202407070000000001")
		@PathVariable scheduleId: String,
		@RequestBody @Valid request: CreateTodoRequest,
	): ApiResponse<TodoIdResponse>

	@Operation(
		summary = "할일 삭제",
		description = """
			일정에 연결된 할일을 삭제합니다.

			**권한**: 활성 써클 구성원이라면 누구나 삭제할 수 있습니다.
			부모 일정은 존재하며 삭제되지 않은 활성 상태여야 합니다.

			**범위 검증**
			- todoId가 요청 경로의 scheduleId 및 circleId에 실제로 소속되어야 합니다.
			- 다른 일정 또는 다른 써클 소속 todoId로 접근하면 404(TODO_NOT_FOUND)를 반환합니다.

			**응답**: 성공 시 빈 data를 반환합니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		ApiErrorCodeExample(codeType = TodoErrorCode::class, code = "CIRCLE_ACCESS_DENIED"),
		ApiErrorCodeExample(codeType = TodoErrorCode::class, code = "SCHEDULE_NOT_FOUND"),
		ApiErrorCodeExample(codeType = TodoErrorCode::class, code = "TODO_NOT_FOUND"),
	)
	@ApiSuccessCodeExample(Unit::class)
	fun delete(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
		@Parameter(description = "써클 ID", example = "CC202506010000000001")
		@PathVariable circleId: String,
		@Parameter(description = "일정 ID", example = "SC202407070000000001")
		@PathVariable scheduleId: String,
		@Parameter(description = "할일 ID", example = "TD20240801090000001")
		@PathVariable todoId: String,
	): ApiResponse<Unit>

	@Operation(
		summary = "할일 담당자 배정",
		description = """
			할일의 담당자 목록을 변경합니다.

			**권한**: 부모 일정의 작성자만 담당자를 배정할 수 있습니다.
			써클 initiator라도 일정 작성자가 아니면 배정할 수 없습니다.

			**배정 방식**
			- 기존 담당자 목록은 요청의 assigneeMemberIds 목록으로 전체 교체됩니다.
			- 빈 배열을 전달하면 담당자가 없는 할일이 됩니다.
			- assigneeMemberIds는 `GET /api/v1/homes/{circleId}/members` 응답의 memberId 값을 사용해야 합니다.
			- 존재하지 않거나 해당 써클의 구성원이 아닌 멤버 ID가 하나라도 포함되면 404(ASSIGNEE_NOT_FOUND)로 전체 요청이 실패하며, 유효한 담당자만 부분 적용되지 않습니다.
			- 재배정 시 기존 담당자의 체크 상태는 초기화됩니다. 이는 의도된 동작입니다.

			**범위 검증**: todoId가 요청 경로의 scheduleId/circleId 소속이 아니면 404(TODO_NOT_FOUND)를 반환합니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "INVALID_INPUT"),
		ApiErrorCodeExample(codeType = TodoErrorCode::class, code = "TODO_ASSIGN_DENIED"),
		ApiErrorCodeExample(codeType = TodoErrorCode::class, code = "TODO_NOT_FOUND"),
		ApiErrorCodeExample(codeType = TodoErrorCode::class, code = "ASSIGNEE_NOT_FOUND"),
	)
	@ApiSuccessCodeExample(Unit::class)
	fun assignMembers(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
		@Parameter(description = "써클 ID", example = "CC202506010000000001")
		@PathVariable circleId: String,
		@Parameter(description = "일정 ID", example = "SC202407070000000001")
		@PathVariable scheduleId: String,
		@Parameter(description = "할일 ID", example = "TD20240801090000001")
		@PathVariable todoId: String,
		@RequestBody request: AssignTodoMembersRequest,
	): ApiResponse<Unit>

	@Operation(
		summary = "할일 체크",
		description = """
			본인에게 배정된 할일을 체크합니다.

			**권한 및 검증**
			- 활성 써클 구성원이어야 합니다.
			- 부모 일정은 존재하며 삭제되지 않은 활성 상태여야 합니다.
			- 본인이 해당 할일의 담당자로 배정되어 있어야 합니다.
			- todoId가 요청 경로의 scheduleId/circleId 소속이 아니면 404(TODO_NOT_FOUND)를 반환합니다.

			담당자 전원이 체크하면 이 할일은 완료 상태가 됩니다.
			체크는 본인 담당자 상태에만 반영되며, 담당자 재배정 시에는 assignMembers 정책에 따라 체크 상태가 초기화됩니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		ApiErrorCodeExample(codeType = TodoErrorCode::class, code = "CIRCLE_ACCESS_DENIED"),
		ApiErrorCodeExample(codeType = TodoErrorCode::class, code = "SCHEDULE_NOT_FOUND"),
		ApiErrorCodeExample(codeType = TodoErrorCode::class, code = "TODO_CHECK_DENIED"),
		ApiErrorCodeExample(codeType = TodoErrorCode::class, code = "TODO_NOT_FOUND"),
	)
	@ApiSuccessCodeExample(Unit::class)
	fun check(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
		@Parameter(description = "써클 ID", example = "CC202506010000000001")
		@PathVariable circleId: String,
		@Parameter(description = "일정 ID", example = "SC202407070000000001")
		@PathVariable scheduleId: String,
		@Parameter(description = "할일 ID", example = "TD20240801090000001")
		@PathVariable todoId: String,
	): ApiResponse<Unit>

	@Operation(
		summary = "할일 체크 해제",
		description = """
			본인에게 배정된 할일의 체크를 해제합니다.

			**권한 및 검증**
			- 활성 써클 구성원이어야 합니다.
			- 부모 일정은 존재하며 삭제되지 않은 활성 상태여야 합니다.
			- 본인이 해당 할일의 담당자로 배정되어 있어야 합니다.
			- todoId가 요청 경로의 scheduleId/circleId 소속이 아니면 404(TODO_NOT_FOUND)를 반환합니다.

			체크를 해제하면 담당자 전원 체크 조건이 깨져 할일은 미완료 상태가 됩니다.
			체크 해제는 본인 담당자 상태에만 반영되며, 담당자 재배정 시에는 assignMembers 정책에 따라 체크 상태가 초기화됩니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		ApiErrorCodeExample(codeType = TodoErrorCode::class, code = "CIRCLE_ACCESS_DENIED"),
		ApiErrorCodeExample(codeType = TodoErrorCode::class, code = "SCHEDULE_NOT_FOUND"),
		ApiErrorCodeExample(codeType = TodoErrorCode::class, code = "TODO_CHECK_DENIED"),
		ApiErrorCodeExample(codeType = TodoErrorCode::class, code = "TODO_NOT_FOUND"),
	)
	@ApiSuccessCodeExample(Unit::class)
	fun uncheck(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
		@Parameter(description = "써클 ID", example = "CC202506010000000001")
		@PathVariable circleId: String,
		@Parameter(description = "일정 ID", example = "SC202407070000000001")
		@PathVariable scheduleId: String,
		@Parameter(description = "할일 ID", example = "TD20240801090000001")
		@PathVariable todoId: String,
	): ApiResponse<Unit>

	@Operation(
		summary = "할일 목록 조회",
		description = """
			일정에 연결된 할일 목록을 조회합니다.

			**권한**: 활성 써클 구성원이라면 누구나 조회할 수 있습니다.
			부모 일정이 존재하지 않거나 삭제된 상태이면 404(SCHEDULE_NOT_FOUND)를 반환합니다.

			**응답 목록**
			- allTodos: 일정에 연결된 전체 할일 목록이며, 할일 생성일 기준 최신순으로 정렬됩니다.
			- myTodos: 호출자가 담당자로 포함된 할일만 반환하며, 할일 생성일 기준 최신순으로 정렬됩니다.
			- 각 항목의 isMy는 호출자가 해당 할일의 담당자인지 여부입니다.

			할일은 담당자 전원이 체크한 경우에만 완료 상태로 판단합니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		ApiErrorCodeExample(codeType = TodoErrorCode::class, code = "SCHEDULE_NOT_FOUND"),
	)
	@ApiSuccessCodeExample(TodoListResponse::class)
	fun getTodos(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
		@Parameter(description = "써클 ID", example = "CC202506010000000001")
		@PathVariable circleId: String,
		@Parameter(description = "일정 ID", example = "SC202407070000000001")
		@PathVariable scheduleId: String,
	): ApiResponse<TodoListResponse>
}
