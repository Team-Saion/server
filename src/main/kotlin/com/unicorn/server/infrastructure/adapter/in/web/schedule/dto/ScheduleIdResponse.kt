package com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto

import com.unicorn.server.domain.schedule.vo.ScheduleId
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "일정 생성 응답")
data class ScheduleIdResponse(
	@field:Schema(description = "생성된 일정 ID", example = "SC202407070000000001")
	val scheduleId: String,
) {
	companion object {
		fun of(scheduleId: ScheduleId) = ScheduleIdResponse(scheduleId.value)
	}
}
