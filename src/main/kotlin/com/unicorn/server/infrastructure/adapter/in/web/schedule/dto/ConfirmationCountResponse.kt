package com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto

import com.unicorn.server.domain.schedule.enums.ConfirmationType
import com.unicorn.server.domain.schedule.port.dto.ConfirmationCountResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "확인하기 종류별 카운트")
data class ConfirmationCountResponse(
	@field:Schema(
		description = """
			확인하기 종류.
			- CONFIRMED: 참석
			- CANNOT_ATTEND: 불참
		""",
		example = "CONFIRMED",
		allowableValues = ["CONFIRMED", "CANNOT_ATTEND"],
	)
	val type: ConfirmationType,

	@field:Schema(description = "해당 종류를 선택한 멤버 수", example = "5")
	val count: Int,
) {
	companion object {
		fun from(result: ConfirmationCountResult) = ConfirmationCountResponse(type = result.type, count = result.count)
	}
}
