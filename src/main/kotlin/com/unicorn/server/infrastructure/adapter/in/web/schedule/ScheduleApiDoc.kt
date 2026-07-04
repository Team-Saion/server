package com.unicorn.server.infrastructure.adapter.`in`.web.schedule

import com.unicorn.server.common.exception.CommonErrorCode
import com.unicorn.server.domain.schedule.exception.ScheduleErrorCode
import com.unicorn.server.infrastructure.adapter.`in`.web.common.dto.ApiResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiErrorCodeExample
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiErrorCodeExamples
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiSuccessCodeExample
import com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto.CreateScheduleRequest
import com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto.RegisterConfirmationRequest
import com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto.RegisterConfirmationResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto.ScheduleDetailResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto.ScheduleIdResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto.ScheduleListResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto.UpdateScheduleRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "Schedule API", description = "써클 일정 생성/수정/삭제/조회 및 확인하기 API")
interface ScheduleApiDoc {

	@Operation(
		summary = "일정 생성",
		description = """
			써클에 일정을 생성합니다.

			- 제목은 1~30자, 공백 전용 불가.
			- startTime/endTime 생략 시 종일 일정으로 저장됩니다 (00:00 ~ 23:59).
			- startTime 입력 시 endTime은 필수이며, endTime은 startTime보다 이후여야 합니다.
			- needConfirm: 구성원 확인하기 기능 활성 여부.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "BLANK_TITLE"),
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "TITLE_TOO_LONG"),
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "WHITESPACE_ONLY_TITLE"),
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "MISSING_START_DATE"),
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "MISSING_END_DATE"),
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "END_DATE_BEFORE_START_DATE"),
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "MISSING_END_TIME"),
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "END_TIME_NOT_AFTER_START_TIME"),
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "MEMO_TOO_LONG"),
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "CIRCLE_NOT_FOUND"),
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "CIRCLE_ACCESS_DENIED"),
	)
	@ApiSuccessCodeExample(ScheduleIdResponse::class)
	fun createSchedule(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
		@PathVariable circleId: Long,
		@RequestBody @Valid request: CreateScheduleRequest,
	): ApiResponse<ScheduleIdResponse>

	@Operation(
		summary = "일정 수정",
		description = """
			일정을 수정합니다.

			- 작성자 또는 써클 initiator만 수정할 수 있습니다.
			- 포함된 필드만 수정됩니다 (포함되지 않은 필드는 기존 값 유지).
			- needConfirm은 항상 포함해야 합니다.
			- startTime/endTime을 null로 명시하면 종일 일정으로 변경됩니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "SCHEDULE_NOT_FOUND"),
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "SCHEDULE_MODIFICATION_DENIED"),
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "CIRCLE_ACCESS_DENIED"),
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "END_DATE_BEFORE_START_DATE"),
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "END_TIME_NOT_AFTER_START_TIME"),
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "MEMO_TOO_LONG"),
	)
	@ApiSuccessCodeExample(Unit::class)
	fun updateSchedule(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
		@PathVariable circleId: Long,
		@PathVariable scheduleId: Long,
		@RequestBody @Valid request: UpdateScheduleRequest,
	): ApiResponse<Unit>

	@Operation(
		summary = "일정 삭제",
		description = """
			일정을 Soft Delete합니다.

			- 작성자 또는 써클 initiator만 삭제할 수 있습니다.
			- 이미 삭제된 일정은 S404_2를 반환합니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "SCHEDULE_NOT_FOUND"),
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "SCHEDULE_MODIFICATION_DENIED"),
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "CIRCLE_ACCESS_DENIED"),
	)
	@ApiSuccessCodeExample(Unit::class)
	fun deleteSchedule(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
		@PathVariable circleId: Long,
		@PathVariable scheduleId: Long,
	): ApiResponse<Unit>

	@Operation(
		summary = "일정 목록 조회",
		description = """
			써클의 일정 목록을 커서 기반 페이지네이션으로 조회합니다.

			- startDate ASC -> startTime ASC 정렬 (종일 일정은 00:00 기준).
			- 삭제된 일정은 제외합니다.
			- cursor 미전달 시 처음부터 조회합니다.
			- size 기본값은 20, 최대 50입니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "CIRCLE_NOT_FOUND"),
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "CIRCLE_ACCESS_DENIED"),
	)
	@ApiSuccessCodeExample(ScheduleListResponse::class)
	fun getScheduleList(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
		@PathVariable circleId: Long,
		@Parameter(description = "커서. 최초 요청 시 생략.")
		@RequestParam cursor: String?,
		@Parameter(description = "페이지 크기. 기본값 20, 최대 50.")
		@RequestParam(defaultValue = "20") size: Int,
	): ApiResponse<ScheduleListResponse>

	@Operation(
		summary = "일정 상세 조회",
		description = """
			일정 상세 정보를 조회합니다.

			- 삭제된 일정은 S404_2를 반환합니다.
			- needConfirm=true인 경우 confirmations(종류별 카운트)와 myConfirmationType이 포함됩니다.
			- needConfirm=false인 경우 confirmations는 빈 배열, myConfirmationType은 null입니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "SCHEDULE_NOT_FOUND"),
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "CIRCLE_ACCESS_DENIED"),
	)
	@ApiSuccessCodeExample(ScheduleDetailResponse::class)
	fun getScheduleDetail(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
		@PathVariable circleId: Long,
		@PathVariable scheduleId: Long,
	): ApiResponse<ScheduleDetailResponse>

	@Operation(
		summary = "확인하기 등록/변경",
		description = """
			일정에 확인하기를 등록하거나 종류를 변경합니다.

			- needConfirm=false인 일정은 S400_11을 반환합니다.
			- 멤버당 확인하기는 하나만 유지됩니다.
			- 기존 확인하기와 다른 종류 전달 시 새로운 종류로 변경됩니다.
			- 기존 확인하기와 동일한 종류 전달 시 그대로 유지됩니다 (멱등).
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "SCHEDULE_NOT_FOUND"),
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "CONFIRMATION_NOT_SUPPORTED"),
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "INVALID_CONFIRMATION_TYPE"),
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "CIRCLE_ACCESS_DENIED"),
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "CONFIRMATION_ACCESS_DENIED"),
	)
	@ApiSuccessCodeExample(RegisterConfirmationResponse::class)
	fun registerConfirmation(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
		@PathVariable circleId: Long,
		@PathVariable scheduleId: Long,
		@RequestBody @Valid request: RegisterConfirmationRequest,
	): ApiResponse<RegisterConfirmationResponse>
}
