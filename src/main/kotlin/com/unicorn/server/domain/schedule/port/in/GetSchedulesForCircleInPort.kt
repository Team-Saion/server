package com.unicorn.server.domain.schedule.port.`in`

import com.unicorn.server.domain.circle.vo.CircleId
import com.unicorn.server.domain.schedule.port.dto.ScheduleSummaryResult
import java.time.LocalDate

interface GetSchedulesForCircleInPort {
    // 아직 종료되지 않은(예정 + 진행 중) 일정을 임박한 순서로 최대 limit개 반환한다. 첫 항목이 가장 임박한 일정이다.
    fun findUpcomingSchedulesByCircleId(circleId: CircleId, today: LocalDate, limit: Int): List<ScheduleSummaryResult>
    fun countByCircleId(circleId: CircleId): Long
}
