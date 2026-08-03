package com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "할일 생성 요청")
data class CreateTodoRequest(
	@field:Schema(
		description = "할일 제목. 1~30자, 공백 전용 불가.",
		example = "숙소 예약하기",
	)
	@field:NotBlank
	@field:Size(max = 30)
	val title: String,
)
