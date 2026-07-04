package com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto

import com.unicorn.server.domain.schedule.port.dto.ScheduleListResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "일정 목록 응답")
data class ScheduleListResponse(
	@field:Schema(description = "일정 요약 목록")
	val schedules: List<ScheduleSummaryResponse>,

	@field:Schema(description = "다음 페이지 커서. hasNext=false이면 null.", nullable = true)
	val nextCursor: String?,

	@field:Schema(description = "다음 페이지 존재 여부", example = "true")
	val hasNext: Boolean,
) {
	companion object {
		fun from(result: ScheduleListResult) = ScheduleListResponse(
			schedules = result.schedules.map { ScheduleSummaryResponse.from(it) },
			nextCursor = result.nextCursor,
			hasNext = result.hasNext,
		)
	}
}
