package com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDate
import java.time.LocalTime

@Schema(description = "일정 생성 요청")
data class CreateScheduleRequest(
	@field:Schema(
		description = "일정 제목. 1~30자, 공백 전용 불가.",
		example = "제주도 여행",
	)
	@field:NotBlank
	@field:Size(max = 30)
	val title: String,

	@field:Schema(description = "시작일 (yyyy-MM-dd)", example = "2024-08-01")
	@field:NotNull
	val startDate: LocalDate,

	@field:Schema(
		description = "종료일 (yyyy-MM-dd). startDate 이상이어야 합니다.",
		example = "2024-08-03",
	)
	@field:NotNull
	val endDate: LocalDate,

	@field:Schema(
		description = """
			시작시간 (HH:mm).
			생략하거나 null이면 종일 일정(isAllDay=true)으로 저장됩니다.
			지정하면 endTime도 반드시 함께 지정해야 합니다.
		""",
		example = "09:00",
		nullable = true,
	)
	val startTime: LocalTime?,

	@field:Schema(
		description = """
			종료시간 (HH:mm).
			startTime을 지정한 경우 필수입니다.
			같은 날(startDate == endDate) 일정이면 startTime보다 이후여야 합니다.
		""",
		example = "18:00",
		nullable = true,
	)
	val endTime: LocalTime?,

	@field:Schema(
		description = "확인하기 기능 활성 여부. true이면 멤버들이 참석/불참 의사를 등록할 수 있습니다.",
		example = "true",
	)
	@field:NotNull
	val needConfirm: Boolean,

	@field:Schema(
		description = "메모. 최대 500자, 생략 가능.",
		example = "숙소 체크인 15시",
		nullable = true,
	)
	@field:Size(max = 500)
	val memo: String?,
)
