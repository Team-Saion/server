package com.unicorn.server.infrastructure.adapter.`in`.web.schedule

import com.unicorn.server.common.exception.CommonErrorCode
import com.unicorn.server.domain.schedule.exception.ScheduleErrorCode
import com.unicorn.server.infrastructure.adapter.`in`.web.common.dto.ApiResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiErrorCodeExample
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiErrorCodeExamples
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiSuccessCodeExample
import com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto.ConfirmationTypesListResponse
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
			써클에 새 일정을 생성합니다.

			**권한**: 써클 구성원(MEMBER 이상)만 생성 가능합니다.

			**시간 필드 규칙**
			- startTime / endTime을 모두 생략하거나 null이면 종일 일정(isAllDay=true)으로 저장됩니다.
			- startTime을 지정하면 endTime도 반드시 지정해야 합니다. 반대도 마찬가지입니다.
			- 같은 날(startDate == endDate) 시간 일정이면 endTime은 startTime보다 이후여야 합니다.

			**제목 / 메모 제약**
			- 제목: 1~30자, 공백 전용 불가.
			- 메모: 최대 500자, 생략 가능.

			**응답**: 생성된 일정 ID를 반환합니다.
		""",
	)
	@ApiErrorCodeExamples(
		// 인증
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		// 요청 바디 유효성
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "INVALID_INPUT"),        // needConfirm 누락 등 @NotNull 위반
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "BLANK_TITLE"),        // S400_1: 제목이 빈 문자열
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "WHITESPACE_ONLY_TITLE"), // S400_3: 제목이 공백만으로 구성
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "TITLE_TOO_LONG"),     // S400_2: 제목 31자 이상
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "END_DATE_BEFORE_START_DATE"), // S400_6: 종료일이 시작일 이전
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "MISSING_START_TIME"), // S400_7: endTime만 있고 startTime 없음
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "MISSING_END_TIME"),   // S400_8: startTime만 있고 endTime 없음
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "END_TIME_NOT_AFTER_START_TIME"), // S400_9: 같은 날 endTime이 startTime 이전/같음
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "MEMO_TOO_LONG"),      // S400_10: 메모 501자 이상
		// 권한
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "CIRCLE_NOT_FOUND"),   // S404_1: 존재하지 않는 써클 ID
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "CIRCLE_ACCESS_DENIED"), // S403_1: 써클 구성원이 아님
	)
	@ApiSuccessCodeExample(ScheduleIdResponse::class)
	fun createSchedule(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
		@Parameter(description = "써클 ID", example = "CC202506010000000001")
		@PathVariable circleId: String,
		@RequestBody @Valid request: CreateScheduleRequest,
	): ApiResponse<ScheduleIdResponse>

	@Operation(
		summary = "일정 수정",
		description = """
			일정을 부분 수정합니다 (Partial Update).

			**권한**: 일정 작성자 또는 써클 initiator만 수정 가능합니다.

			**Partial Update 방식**
			- 요청 body에 포함된 필드만 수정됩니다. 포함하지 않으면 기존 값이 유지됩니다.
			- needConfirm은 항상 포함해야 합니다.

			**시간 필드 초기화**
			- startTime / endTime을 명시적으로 null로 전달하면 종일 일정(isAllDay=true)으로 변경됩니다.
			- 필드 자체를 생략하면 기존 시간 값이 그대로 유지됩니다.

			**메모 초기화**
			- memo를 명시적으로 null로 전달하면 메모가 삭제됩니다.
			- 필드 자체를 생략하면 기존 메모가 그대로 유지됩니다.

			**응답**: 성공 시 빈 data를 반환합니다.
		""",
	)
	@ApiErrorCodeExamples(
		// 인증
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		// 요청 바디 유효성
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "INVALID_INPUT"),        // needConfirm 누락 등 @NotNull 위반
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "END_DATE_BEFORE_START_DATE"), // S400_6: 수정 후 종료일이 시작일 이전
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "MISSING_START_TIME"), // S400_7: 수정 후 endTime만 있고 startTime 없음
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "MISSING_END_TIME"),   // S400_8: 수정 후 startTime만 있고 endTime 없음
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "END_TIME_NOT_AFTER_START_TIME"), // S400_9: 수정 후 같은 날 endTime이 startTime 이전/같음
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "MEMO_TOO_LONG"),      // S400_10: 메모 501자 이상
		// 권한
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "CIRCLE_NOT_FOUND"),   // S404_1: 존재하지 않는 써클 ID
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "CIRCLE_ACCESS_DENIED"), // S403_1: 써클 구성원이 아님
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "SCHEDULE_NOT_FOUND"), // S404_2: 존재하지 않거나 삭제된 일정 ID
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "SCHEDULE_MODIFICATION_DENIED"), // S403_2: 작성자도 initiator도 아님
	)
	@ApiSuccessCodeExample(Unit::class)
	fun updateSchedule(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
		@Parameter(description = "써클 ID", example = "CC202506010000000001")
		@PathVariable circleId: String,
		@Parameter(description = "일정 ID", example = "SC202407070000000001")
		@PathVariable scheduleId: String,
		@RequestBody @Valid request: UpdateScheduleRequest,
	): ApiResponse<Unit>

	@Operation(
		summary = "일정 삭제",
		description = """
			일정을 소프트 삭제합니다 (del_yn='Y').

			**권한**: 일정 작성자 또는 써클 initiator만 삭제 가능합니다.

			- 이미 삭제된 일정은 S404_2를 반환합니다.
			- 일정에 연결된 모든 확인하기(schedule_confirmation)도 함께 삭제됩니다.

			**응답**: 성공 시 빈 data를 반환합니다.
		""",
	)
	@ApiErrorCodeExamples(
		// 인증
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		// 권한 / 존재 여부
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "CIRCLE_NOT_FOUND"),   // S404_1: 존재하지 않는 써클 ID
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "CIRCLE_ACCESS_DENIED"), // S403_1: 써클 구성원이 아님
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "SCHEDULE_NOT_FOUND"), // S404_2: 존재하지 않거나 이미 삭제된 일정 ID
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "SCHEDULE_MODIFICATION_DENIED"), // S403_2: 작성자도 initiator도 아님
	)
	@ApiSuccessCodeExample(Unit::class)
	fun deleteSchedule(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
		@Parameter(description = "써클 ID", example = "CC202506010000000001")
		@PathVariable circleId: String,
		@Parameter(description = "일정 ID", example = "SC202407070000000001")
		@PathVariable scheduleId: String,
	): ApiResponse<Unit>

	@Operation(
		summary = "일정 목록 조회",
		description = """
			써클의 일정 목록을 커서 기반 페이지네이션으로 조회합니다.

			**권한**: 써클 구성원(MEMBER 이상)만 조회 가능합니다.

			**조회 범위**: 아직 종료되지 않은 일정만 반환합니다. (endDate가 오늘(KST) 이상)
			- 종료일이 지난 일정은 목록에서 제외됩니다.

			**정렬 순서**: startDate ASC → startTime ASC → scheduleId ASC
			- 종일 일정(startTime=null)은 해당 날짜의 00:00 기준으로 정렬합니다.

			**커서 페이지네이션**
			- 최초 요청 시 cursor를 생략합니다.
			- 응답의 nextCursor를 다음 요청에 cursor로 전달하면 이후 데이터를 조회합니다.
			- hasNext=false이면 마지막 페이지입니다.
			- size 기본값은 20, 최대 50입니다.

			**각 일정 항목(ScheduleSummaryResponse) 필드**
			- status: UPCOMING(시작 전) / IN_PROGRESS(진행 중) / COMPLETED(종료), KST 현재 시각 기준
			- isAllDay: startTime/endTime이 모두 null이면 true
			- dDay: startDate와 오늘(KST) 간의 양수 차이. 진행 중이거나 과거 일정은 null
			- urgencyLevel: 긴급도. dDay가 10 미만이면 URGENT, 10 이상이거나 dDay가 null(진행 중/종료)이면 NORMAL
			- progressRate: 0~100 정수. 시작 전=0, 종료 후=100, 진행 중=경과 시간 비율
		""",
	)
	@ApiErrorCodeExamples(
		// 인증
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		// 요청 파라미터 유효성
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "INVALID_INPUT"),        // size가 1~50 범위 외
		// 권한
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "CIRCLE_ACCESS_DENIED"), // S403_1: 써클 구성원이 아님
	)
	@ApiSuccessCodeExample(ScheduleListResponse::class)
	fun getScheduleList(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
		@Parameter(description = "써클 ID", example = "CC202506010000000001")
		@PathVariable circleId: String,
		@Parameter(description = "커서. 최초 요청 시 생략.")
		@RequestParam cursor: String?,
		@Parameter(description = "페이지 크기. 기본값 20, 최대 50.")
		@RequestParam(defaultValue = "20") size: Int,
	): ApiResponse<ScheduleListResponse>

	@Operation(
		summary = "일정 상세 조회",
		description = """
			일정 상세 정보를 조회합니다.

			**권한**: 써클 구성원(MEMBER 이상)만 조회 가능합니다.

			**status 계산 (KST 기준 현재 시각)**
			- UPCOMING: 현재 시각이 시작일시 이전
			- IN_PROGRESS: 시작일시 ≤ 현재 시각 ≤ 종료일시
			- COMPLETED: 현재 시각이 종료일시 이후
			- 종일 일정의 경우 시작일시=startDate 00:00, 종료일시=endDate 23:59:59로 계산합니다.

			**dDay 계산 (KST 기준 오늘 날짜)**
			- 오늘 기준 startDate까지 남은 일수 (양수)
			- 진행 중이거나 시작일이 이미 지난 경우 null

			**urgencyLevel (긴급도)**
			- URGENT: dDay가 10 미만
			- NORMAL: dDay가 10 이상이거나, 진행 중/종료되어 dDay가 null인 경우

			**progressRate 계산**
			- 시작 전: 0
			- 종료 후: 100
			- 진행 중: 전체 기간 대비 경과 시간 비율 (0~100 정수)

			**confirmations (확인하기 종류별 카운트)**
			- needConfirm=true인 일정에서만 데이터가 채워집니다.
			- needConfirm=false이면 빈 배열([])을 반환합니다.
			- type: CONFIRMED(확인했어요) / ETC(기타)

			**myConfirmation**
			- 내가 등록한 확인하기 정보입니다.
			- confirmationId와 confirmationType(CONFIRMED/ETC)을 포함합니다.
			- 등록한 확인하기가 없거나 needConfirm=false이면 null
		""",
	)
	@ApiErrorCodeExamples(
		// 인증
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		// 권한 / 존재 여부
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "CIRCLE_ACCESS_DENIED"), // S403_1: 써클 구성원이 아님
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "SCHEDULE_NOT_FOUND"), // S404_2: 존재하지 않거나 삭제된 일정 ID
	)
	@ApiSuccessCodeExample(ScheduleDetailResponse::class)
	fun getScheduleDetail(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
		@Parameter(description = "써클 ID", example = "CC202506010000000001")
		@PathVariable circleId: String,
		@Parameter(description = "일정 ID", example = "SC202407070000000001")
		@PathVariable scheduleId: String,
	): ApiResponse<ScheduleDetailResponse>

	@Operation(
		summary = "확인하기 등록/변경",
		description = """
			일정에 확인하기를 등록하거나 종류를 변경합니다.

			**권한**: 써클 구성원(MEMBER 이상)만 등록 가능합니다.

			**확인하기 종류 (confirmationType)**
			- 일정 확인 종류 조회 통해 조회할 수 있습니다.
			- 서버 내부에서 Enum으로 관리 중입니다.

			**처리 방식**
			- 멤버당 확인하기는 1건만 유지됩니다.
			- 기존 확인하기가 없으면 새로 생성합니다.
			- 기존과 다른 종류를 전달하면 해당 종류로 변경합니다.
			- 기존과 같은 종류를 전달하면 그대로 유지합니다.

			**응답**: 최종 반영된 confirmationType을 반환합니다.
		""",
	)
	@ApiErrorCodeExamples(
		// 인증
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		// 요청 바디 유효성
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "INVALID_INPUT"),        // confirmationType 값이 enum에 없는 문자열
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "CONFIRMATION_NOT_SUPPORTED"), // S400_11: needConfirm=false인 일정
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "INVALID_CONFIRMATION_TYPE"), // S400_12: available=false인 확인 종류
		// 권한 / 존재 여부
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "CIRCLE_ACCESS_DENIED"), // S403_1: 써클 구성원이 아님
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "SCHEDULE_NOT_FOUND"), // S404_2: 존재하지 않거나 삭제된 일정 ID
	)
	@ApiSuccessCodeExample(RegisterConfirmationResponse::class)
	fun registerConfirmation(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
		@Parameter(description = "써클 ID", example = "CC202506010000000001")
		@PathVariable circleId: String,
		@Parameter(description = "일정 ID", example = "SC202407070000000001")
		@PathVariable scheduleId: String,
		@RequestBody @Valid request: RegisterConfirmationRequest,
	): ApiResponse<RegisterConfirmationResponse>

	@Operation(
		summary = "확인하기 취소",
		description = """
			내가 등록한 확인하기를 취소합니다.

			**권한**: 써클 구성원이며 해당 확인하기를 등록한 멤버만 취소 가능합니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "CIRCLE_ACCESS_DENIED"),
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "CONFIRMATION_ACCESS_DENIED"),
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "SCHEDULE_NOT_FOUND"),
		ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "CONFIRMATION_NOT_FOUND"),
	)
	fun cancelConfirmation(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
		@Parameter(description = "써클 ID", example = "CC202506010000000001")
		@PathVariable circleId: String,
		@Parameter(description = "일정 ID", example = "SC202407070000000001")
		@PathVariable scheduleId: String,
		@Parameter(description = "확인하기 ID", example = "1")
		@PathVariable confirmationId: Long,
	): ApiResponse<Unit>

	@Operation(
		summary = "일정 확인 종류 조회",
		description = """

		일정에 등록할 수 있는 확인 종류를 반환합니다.
		서버 내부에서 Enum으로 관리하며, value, label 쌍으로 반환합니다.

		available=false인 값은 반환하지 않습니다.

	""")
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
	)
	@ApiSuccessCodeExample(ConfirmationTypesListResponse::class)
	fun getConfirmationTypes(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
	): ApiResponse<ConfirmationTypesListResponse>

}
