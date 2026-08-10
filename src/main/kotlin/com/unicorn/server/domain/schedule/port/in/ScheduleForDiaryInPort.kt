package com.unicorn.server.domain.schedule.port.`in`

import com.unicorn.server.domain.schedule.port.dto.CompletedScheduleInfo
import com.unicorn.server.domain.schedule.vo.ScheduleId

interface ScheduleForDiaryInPort {
	/**
	 * 활성 써클 구성원이 접근 가능한 종료 일정을 반환한다.
	 * 일정이 없거나 접근할 수 없거나 아직 종료되지 않았으면 예외가 발생한다.
	 */
	fun getCompletedSchedule(
		scheduleId: ScheduleId,
		memberId: String,
	): CompletedScheduleInfo
}
