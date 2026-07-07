package com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto

import com.unicorn.server.domain.schedule.enums.ConfirmationType
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "확인하기 등록/변경 응답")
data class RegisterConfirmationResponse(
	@field:Schema(
		description = """
			최종 반영된 확인하기 종류.
			- CONFIRMED: 확인했어요
			- ETC: 기타
		""",
		example = "CONFIRMED",
		allowableValues = ["CONFIRMED", "ETC"],
	)
	val confirmationType: ConfirmationType,
) {
	companion object {
		fun of(type: ConfirmationType) = RegisterConfirmationResponse(type)
	}
}
