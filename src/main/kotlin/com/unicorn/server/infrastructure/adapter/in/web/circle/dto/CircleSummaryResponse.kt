package com.unicorn.server.infrastructure.adapter.`in`.web.circle.dto

import com.unicorn.server.domain.circle.port.dto.CircleSummary
import com.unicorn.server.domain.home.port.dto.HomeCircleDto

data class CircleSummaryResponse(
	val circleId: String,
	val name: String,
	val ownerId: String,
) {
	companion object {
		fun from(summary: CircleSummary): CircleSummaryResponse = CircleSummaryResponse(summary.id, summary.name, summary.ownerId)

		fun from(summary: HomeCircleDto): CircleSummaryResponse = CircleSummaryResponse(summary.id, summary.name, summary.ownerId)
	}
}
