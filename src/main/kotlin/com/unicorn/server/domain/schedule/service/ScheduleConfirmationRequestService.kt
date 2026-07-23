package com.unicorn.server.domain.schedule.service

import com.unicorn.server.common.port.out.event.EventPublisher
import com.unicorn.server.domain.schedule.Schedule
import com.unicorn.server.domain.schedule.event.ScheduleConfirmationRequestDueEvent
import com.unicorn.server.domain.schedule.port.`in`.ScheduleConfirmationRequestInPort
import com.unicorn.server.domain.schedule.port.out.ScheduleOutPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.LocalTime

@Service
@Transactional
class ScheduleConfirmationRequestService(
	private val scheduleOutPort: ScheduleOutPort,
	private val eventPublisher: EventPublisher,
) : ScheduleConfirmationRequestInPort {

	override fun dispatchDue(now: LocalDateTime) {
		val windowEnd = now.withSecond(0).withNano(0).minusHours(24)
		val windowStart = windowEnd.minusMinutes(1)
		scheduleOutPort.findActiveConfirmationRequiredCreatedBetween(windowStart, windowEnd)
			.filter { it.startsAt().isAfterOrEqual(it.createdAt.plusHours(24)) }
			.forEach { schedule ->
				eventPublisher.publish(
					ScheduleConfirmationRequestDueEvent(
						scheduleId = schedule.id.value,
						circleId = schedule.circleId,
						scheduleCreatorMemberId = schedule.createdBy,
						scheduleTitle = schedule.title,
					),
				)
			}
	}

	private fun Schedule.startsAt(): LocalDateTime =
		LocalDateTime.of(startDate, startTime ?: LocalTime.MIDNIGHT)

	private fun LocalDateTime.isAfterOrEqual(other: LocalDateTime): Boolean =
		isAfter(other) || isEqual(other)
}
