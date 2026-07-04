package com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDate
import java.time.LocalTime

@Schema(description = "일정 수정 요청. 포함된 필드만 수정되며 needConfirm은 항상 포함해야 한다.")
data class UpdateScheduleRequest(
	@field:Schema(description = "수정할 제목 (최대 30자)", example = "수정된 제목", nullable = true)
	@field:Size(max = 30)
	val title: String?,

	@field:Schema(description = "수정할 시작일 (yyyy-MM-dd)", example = "2024-08-02", nullable = true)
	val startDate: LocalDate?,

	@field:Schema(description = "수정할 종료일 (yyyy-MM-dd)", example = "2024-08-04", nullable = true)
	val endDate: LocalDate?,

	@field:Schema(description = "수정할 시작시간 (HH:mm). null 전달 시 종일 일정으로 변경.", example = "10:00", nullable = true)
	val startTime: LocalTime?,

	@field:Schema(description = "수정할 종료시간 (HH:mm). null 전달 시 종일 일정으로 변경.", example = "19:00", nullable = true)
	val endTime: LocalTime?,

	@field:Schema(description = "확인하기 기능 활성 여부", example = "false")
	@field:NotNull
	val needConfirm: Boolean,

	@field:Schema(description = "수정할 메모 (최대 500자)", example = "수정된 메모", nullable = true)
	@field:Size(max = 500)
	val memo: String?,
)
