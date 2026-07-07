package com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto

import com.unicorn.server.domain.schedule.enums.ConfirmationType
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "확인하기 종류")
data class ConfirmationTypesResponse(

    @field:Schema(
        description = "확인 종류의 상수값",
        example = "CONFIRMED",
    )
    val value: String,

    @field:Schema(
        description = "확인 종류의 라벨",
        example = "확인했어요",
    )
    val label: String,

) {

    companion object {
        fun from(type: ConfirmationType) = ConfirmationTypesResponse(type.name, type.label)
    }

}