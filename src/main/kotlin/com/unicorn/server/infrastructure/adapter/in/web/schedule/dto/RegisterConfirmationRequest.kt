package com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto

import com.unicorn.server.domain.schedule.enums.ConfirmationType
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

@Schema(description = "확인하기 등록/변경 요청")
data class RegisterConfirmationRequest(
	@field:Schema(
		description = """
			확인하기 종류.
			- CONFIRMED: 확인했어요
			- ETC: 기타
		""",
		example = "CONFIRMED",
		allowableValues = ["CONFIRMED", "ETC"],
	)
	@field:NotNull
	val confirmationType: ConfirmationType,
)
