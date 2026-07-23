package com.unicorn.server.domain.schedule.port.out

import com.unicorn.server.domain.schedule.Schedule
import com.unicorn.server.domain.schedule.port.dto.SchedulePageCursor
import com.unicorn.server.domain.schedule.vo.ScheduleId
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

interface ScheduleOutPort {
	fun save(schedule: Schedule): Schedule

	fun findById(scheduleId: ScheduleId): Schedule?

	fun findActiveByIdAndCircleId(scheduleId: ScheduleId, circleId: String): Schedule?

	fun findActiveByCircleId(
		circleId: String,
		today: LocalDate,
		cursor: SchedulePageCursor?,
		size: Int,
	): List<Schedule>

	fun findActiveByStartDateAndCreatedBefore(
		startDate: LocalDate,
		createdBefore: LocalDateTime,
	): List<Schedule>

	fun findActiveAllDayByStartDateAndCreatedBefore(
		startDate: LocalDate,
		createdBefore: LocalDateTime,
	): List<Schedule>

	fun findActiveTimedByStartAtAndCreatedBefore(
		startDate: LocalDate,
		startTime: LocalTime,
		createdBefore: LocalDateTime,
	): List<Schedule>

	fun findActiveConfirmationRequiredCreatedBetween(
		createdFrom: LocalDateTime,
		createdBefore: LocalDateTime,
	): List<Schedule>

	fun findUpcomingByCircleId(
		circleId: String,
		today: LocalDate,
		limit: Int,
	): List<Schedule>

	fun countActiveByCircleId(circleId: String): Long
}
