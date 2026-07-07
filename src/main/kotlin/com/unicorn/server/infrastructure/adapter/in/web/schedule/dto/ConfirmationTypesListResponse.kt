package com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto

import com.unicorn.server.domain.schedule.enums.ConfirmationType
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "확인 종류 목록")
data class ConfirmationTypesListResponse(

	@field:Schema(description = "활성화된 확인 종류 목록")
	val confirmationTypes: List<ConfirmationTypesResponse>,

) {

	companion object {
		fun from(types: List<ConfirmationType>) = ConfirmationTypesListResponse(
			confirmationTypes = types.map { ConfirmationTypesResponse.from(it) },
		)
	}

}
