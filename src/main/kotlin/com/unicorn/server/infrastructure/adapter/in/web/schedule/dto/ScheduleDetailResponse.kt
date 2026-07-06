package com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.unicorn.server.domain.schedule.enums.ConfirmationType
import com.unicorn.server.domain.schedule.enums.ScheduleStatus
import com.unicorn.server.domain.schedule.port.dto.ScheduleDetailResult
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Schema(description = "일정 상세 응답")
data class ScheduleDetailResponse(
	@field:Schema(description = "일정 ID", example = "SC202407070000000001")
	val scheduleId: String,

	@field:Schema(description = "일정 제목", example = "제주도 여행")
	val title: String,

	@field:Schema(description = "시작일 (yyyy-MM-dd)", example = "2024-08-01")
	val startDate: LocalDate,

	@field:Schema(description = "종료일 (yyyy-MM-dd)", example = "2024-08-03")
	val endDate: LocalDate,

	@field:Schema(
		description = "시작시간 (HH:mm). isAllDay=true이면 null.",
		example = "09:00",
		nullable = true,
	)
	val startTime: LocalTime?,

	@field:Schema(
		description = "종료시간 (HH:mm). isAllDay=true이면 null.",
		example = "18:00",
		nullable = true,
	)
	val endTime: LocalTime?,

	@field:Schema(
		description = "종일 일정 여부. startTime/endTime이 모두 null이면 true.",
		example = "false",
	)
	@get:JsonProperty("isAllDay")
	val isAllDay: Boolean,

	@field:Schema(description = "확인하기 기능 활성 여부", example = "true")
	val needConfirm: Boolean,

	@field:Schema(
		description = """
			일정 상태. KST 현재 시각 기준으로 계산됩니다.
			종일 일정은 시작=startDate 00:00, 종료=endDate 23:59:59로 판단합니다.
			- UPCOMING: 시작일시 이전
			- IN_PROGRESS: 시작일시 이상, 종료일시 이하
			- COMPLETED: 종료일시 초과
		""",
		example = "UPCOMING",
		allowableValues = ["UPCOMING", "IN_PROGRESS", "COMPLETED"],
	)
	val status: ScheduleStatus,

	@field:Schema(
		description = """
			D-Day. KST 오늘 날짜 기준으로 startDate까지 남은 일수(양수).
			아직 시작하지 않은 일정(UPCOMING)에서만 값이 존재합니다.
			시작일이 오늘이면 0, 내일이면 1.
			진행 중(IN_PROGRESS)이거나 종료(COMPLETED)된 일정은 null.
		""",
		example = "10",
		nullable = true,
	)
	val dDay: Int?,

	@field:Schema(
		description = """
			진행률 (0~100 정수). KST 현재 시각 기준으로 계산됩니다.
			- 시작 전: 0
			- 종료 후: 100
			- 진행 중: 전체 기간 대비 경과 시간 비율
		""",
		example = "0",
	)
	val progressRate: Int,

	@field:Schema(
		description = "메모 (최대 500자). 등록된 메모가 없으면 null.",
		example = "숙소 체크인 15시",
		nullable = true,
	)
	val memo: String?,

	@field:Schema(
		description = """
			확인하기 종류별 카운트 목록.
			needConfirm=false이면 빈 배열([])을 반환합니다.
			각 항목의 type: CONFIRMED(참석) / CANNOT_ATTEND(불참)
		""",
	)
	val confirmations: List<ConfirmationCountResponse>,

	@field:Schema(
		description = """
			내가 등록한 확인하기 종류.
			- CONFIRMED: 참석
			- CANNOT_ATTEND: 불참
			등록한 확인하기가 없거나 needConfirm=false이면 null.
		""",
		nullable = true,
		allowableValues = ["CONFIRMED", "CANNOT_ATTEND"],
	)
	val myConfirmationType: ConfirmationType?,

	@field:Schema(description = "일정을 생성한 멤버 ID", example = "00000000-0000-0000-0000-000000000001")
	val createdBy: String,

	@field:Schema(description = "생성 일시 (ISO 8601)", example = "2024-07-01T10:00:00")
	val createdAt: LocalDateTime,
) {
	companion object {
		fun from(result: ScheduleDetailResult) = ScheduleDetailResponse(
			scheduleId = result.scheduleId.value,
			title = result.title,
			startDate = result.startDate,
			endDate = result.endDate,
			startTime = result.startTime,
			endTime = result.endTime,
			isAllDay = result.isAllDay,
			needConfirm = result.needConfirm,
			status = result.status,
			dDay = result.dDay,
			progressRate = result.progressRate,
			memo = result.memo,
			confirmations = result.confirmations.map { ConfirmationCountResponse.from(it) },
			myConfirmationType = result.myConfirmationType,
			createdBy = result.createdBy,
			createdAt = result.createdAt,
		)
	}
}
