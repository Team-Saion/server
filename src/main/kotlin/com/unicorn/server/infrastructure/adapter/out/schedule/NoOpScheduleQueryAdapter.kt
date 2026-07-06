package com.unicorn.server.infrastructure.adapter.out.schedule

import com.unicorn.server.domain.circle.vo.CircleId
import com.unicorn.server.domain.schedule.port.`in`.GetSchedulesForCircleInPort
import com.unicorn.server.domain.schedule.port.dto.ScheduleView
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class NoOpScheduleQueryAdapter : GetSchedulesForCircleInPort {
	override fun findMainScheduleByCircleId(circleId: CircleId, today: LocalDate): ScheduleView? = null
	override fun findUpcomingSchedulesByCircleId(circleId: CircleId, today: LocalDate, limit: Int): List<ScheduleView> = emptyList()
	override fun countByCircleId(circleId: CircleId): Long = 0L
}
