package com.unicorn.server.domain.schedule.service

import com.unicorn.server.common.port.out.event.EventPublisher
import com.unicorn.server.domain.schedule.Schedule
import com.unicorn.server.domain.schedule.enums.ScheduleReminderType
import com.unicorn.server.domain.schedule.event.ScheduleReminderDueEvent
import com.unicorn.server.domain.schedule.port.`in`.ScheduleReminderInPort
import com.unicorn.server.domain.schedule.port.out.ScheduleOutPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.LocalTime

@Service
@Transactional
class ScheduleReminderService(
	private val scheduleOutPort: ScheduleOutPort,
	private val eventPublisher: EventPublisher,
) : ScheduleReminderInPort {

	override fun dispatchDaily(now: LocalDateTime) {
		val notificationAt = now.toLocalDate().atTime(DAILY_NOTIFICATION_TIME)
		publish(
			type = ScheduleReminderType.D7,
			schedules = scheduleOutPort.findActiveByStartDateAndCreatedBefore(
				startDate = now.toLocalDate().plusDays(7),
				createdBefore = notificationAt,
			),
		)
		publish(
			type = ScheduleReminderType.D1,
			schedules = scheduleOutPort.findActiveByStartDateAndCreatedBefore(
				startDate = now.toLocalDate().plusDays(1),
				createdBefore = notificationAt,
			),
		)
		publish(
			type = ScheduleReminderType.DDAY_ALL_DAY,
			schedules = scheduleOutPort.findActiveAllDayByStartDateAndCreatedBefore(
				startDate = now.toLocalDate(),
				createdBefore = now.toLocalDate().atStartOfDay(),
			),
		)
	}

	override fun dispatchTimed(now: LocalDateTime) {
		val notificationAt = now.withSecond(0).withNano(0)
		val startAt = notificationAt.plusHours(1)
		publish(
			type = ScheduleReminderType.DDAY_TIMED,
			schedules = scheduleOutPort.findActiveTimedByStartAtAndCreatedBefore(
				startDate = startAt.toLocalDate(),
				startTime = startAt.toLocalTime(),
				createdBefore = notificationAt,
			),
		)
	}

	private fun publish(type: ScheduleReminderType, schedules: List<Schedule>) {
		schedules.forEach { schedule ->
			eventPublisher.publish(
				ScheduleReminderDueEvent(
					reminderType = type,
					scheduleId = schedule.id.value,
					circleId = schedule.circleId,
					scheduleTitle = schedule.title,
					startTime = schedule.startTime,
				),
			)
		}
	}

	companion object {
		private val DAILY_NOTIFICATION_TIME: LocalTime = LocalTime.of(9, 0)
	}
}
