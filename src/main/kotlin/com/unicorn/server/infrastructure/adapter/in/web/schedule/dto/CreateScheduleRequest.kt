package com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDate
import java.time.LocalTime

@Schema(description = "일정 생성 요청")
data class CreateScheduleRequest(
	@field:Schema(description = "일정 제목 (최대 30자)", example = "제주도 여행")
	@field:NotBlank
	@field:Size(max = 30)
	val title: String,

	@field:Schema(description = "시작일 (yyyy-MM-dd)", example = "2024-08-01")
	@field:NotNull
	val startDate: LocalDate,

	@field:Schema(description = "종료일 (yyyy-MM-dd). 시작일 이상이어야 한다.", example = "2024-08-03")
	@field:NotNull
	val endDate: LocalDate,

	@field:Schema(description = "시작시간 (HH:mm). 생략 시 종일 일정으로 저장된다.", example = "09:00", nullable = true)
	val startTime: LocalTime?,

	@field:Schema(description = "종료시간 (HH:mm). startTime 입력 시 필수.", example = "18:00", nullable = true)
	val endTime: LocalTime?,

	@field:Schema(description = "확인하기 기능 활성 여부", example = "true")
	@field:NotNull
	val needConfirm: Boolean,

	@field:Schema(description = "메모 (최대 500자)", example = "숙소 체크인 15시", nullable = true)
	@field:Size(max = 500)
	val memo: String?,
)
