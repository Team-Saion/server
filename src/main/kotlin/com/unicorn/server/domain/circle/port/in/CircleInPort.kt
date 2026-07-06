package com.unicorn.server.domain.circle.port.`in`

import com.unicorn.server.domain.circle.port.dto.CircleSummary
import com.unicorn.server.domain.circle.port.dto.CreateCircleCommand

interface CircleInPort {
	fun create(memberId: String, command: CreateCircleCommand): CircleSummary
	fun listCircles(memberId: String): List<CircleSummary>
	fun getCircleSummary(circleId: String): CircleSummary
}
