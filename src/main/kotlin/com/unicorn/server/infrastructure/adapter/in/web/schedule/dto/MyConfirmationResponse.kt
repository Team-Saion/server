package com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto

import com.unicorn.server.domain.schedule.enums.ConfirmationType
import com.unicorn.server.domain.schedule.port.dto.MyConfirmationInfo
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "내 확인하기 정보")
data class MyConfirmationResponse(
	@field:Schema(description = "확인하기 ID", example = "1")
	val confirmationId: Long,

	@field:Schema(
		description = """
			확인하기 종류.
			- CONFIRMED: 확인했어요
			- ETC: 기타
		""",
		example = "CONFIRMED",
		allowableValues = ["CONFIRMED", "ETC"],
	)
	val confirmationType: ConfirmationType,
) {
	companion object {
		fun from(info: MyConfirmationInfo) = MyConfirmationResponse(
			confirmationId = info.confirmationId,
			confirmationType = info.confirmationType,
		)
	}
}
