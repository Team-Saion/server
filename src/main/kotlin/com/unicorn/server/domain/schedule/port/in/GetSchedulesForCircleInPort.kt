package com.unicorn.server.domain.schedule.port.`in`

import com.unicorn.server.domain.circle.vo.CircleId
import com.unicorn.server.domain.schedule.port.dto.ScheduleView
import java.time.LocalDate

interface GetSchedulesForCircleInPort {
    fun findMainScheduleByCircleId(circleId: CircleId, today: LocalDate): ScheduleView?
    fun findUpcomingSchedulesByCircleId(circleId: CircleId, today: LocalDate, limit: Int): List<ScheduleView>
    fun countByCircleId(circleId: CircleId): Long
}
