package com.unicorn.server.domain.schedule.service

import com.unicorn.server.common.domain.Event
import com.unicorn.server.common.port.out.event.EventPublisher
import com.unicorn.server.domain.schedule.Schedule
import com.unicorn.server.domain.schedule.enums.ScheduleReminderType
import com.unicorn.server.domain.schedule.event.ScheduleReminderDueEvent
import com.unicorn.server.domain.schedule.port.dto.SchedulePageCursor
import com.unicorn.server.domain.schedule.port.out.ScheduleOutPort
import com.unicorn.server.domain.schedule.vo.ScheduleId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@DisplayName("ScheduleReminderService 단위 테스트")
class ScheduleReminderServiceTest {
	private val scheduleOutPort = FakeScheduleOutPort()
	private val eventPublisher = RecordingEventPublisher()
	private val service = ScheduleReminderService(scheduleOutPort, eventPublisher)

	@Test
	@DisplayName("오전 9시 리마인드 실행 시 D-7, D-1, 종일 D-day 일정을 각각 이벤트로 발행한다")
	fun dispatchDaily_withEligibleSchedules_publishesDailyReminderEvents() {
		val now = LocalDateTime.of(2026, 8, 10, 9, 0)
		scheduleOutPort.schedules += listOf(
			schedule("SC-D7", now.toLocalDate().plusDays(7), null, now.minusDays(1)),
			schedule("SC-D1", now.toLocalDate().plusDays(1), null, now.minusDays(1)),
			schedule("SC-DDAY", now.toLocalDate(), null, now.minusDays(1)),
			schedule("SC-DDAY-TODAY", now.toLocalDate(), null, now.minusHours(1)),
			schedule("SC-LATE", now.toLocalDate().plusDays(7), null, now.plusMinutes(1)),
			schedule("SC-DELETED", now.toLocalDate().plusDays(1), null, now.minusDays(1), isDeleted = true),
		)

		service.dispatchDaily(now)

		val events = eventPublisher.events.filterIsInstance<ScheduleReminderDueEvent>()
		assertThat(events.map { it.reminderType to it.scheduleId })
			.containsExactlyInAnyOrder(
				ScheduleReminderType.D7 to "SC-D7",
				ScheduleReminderType.D1 to "SC-D1",
				ScheduleReminderType.DDAY_ALL_DAY to "SC-DDAY",
			)
	}

	@Test
	@DisplayName("시간 일정은 시작 1시간 전에 생성 시각 조건을 만족할 때만 D-day 이벤트를 발행한다")
	fun dispatchTimed_withEligibleTimedSchedule_publishesTimedReminderEvent() {
		val now = LocalDateTime.of(2026, 8, 10, 23, 30)
		val startAt = now.plusHours(1)
		scheduleOutPort.schedules += listOf(
			schedule("SC-TIMED", startAt.toLocalDate(), startAt.toLocalTime(), now.minusHours(1)),
			schedule("SC-TOO-LATE", startAt.toLocalDate(), startAt.toLocalTime(), now.plusMinutes(1)),
		)

		service.dispatchTimed(now)

		val event = eventPublisher.events.filterIsInstance<ScheduleReminderDueEvent>().single()
		assertThat(event.reminderType).isEqualTo(ScheduleReminderType.DDAY_TIMED)
		assertThat(event.scheduleId).isEqualTo("SC-TIMED")
		assertThat(event.startTime).isEqualTo(LocalTime.of(0, 30))
	}

	private fun schedule(
		id: String,
		startDate: LocalDate,
		startTime: LocalTime?,
		createdAt: LocalDateTime,
		isDeleted: Boolean = false,
	): Schedule = Schedule.reconstitute(
		id = ScheduleId.of(id),
		circleId = "circle-1",
		title = "일정 $id",
		startDate = startDate,
		endDate = startDate,
		startTime = startTime,
		endTime = startTime?.plusHours(1),
		needConfirm = false,
		memo = null,
		createdBy = "creator",
		updatedBy = "creator",
		createdAt = createdAt,
		updatedAt = createdAt,
		isDeleted = isDeleted,
	)

	private class FakeScheduleOutPort : ScheduleOutPort {
		val schedules = mutableListOf<Schedule>()

		override fun save(schedule: Schedule): Schedule = schedule

		override fun findById(scheduleId: ScheduleId): Schedule? =
			schedules.firstOrNull { it.id == scheduleId }

		override fun findActiveByIdAndCircleId(scheduleId: ScheduleId, circleId: String): Schedule? =
			schedules.firstOrNull { it.id == scheduleId && it.circleId == circleId && !it.isDeleted }

		override fun findActiveByCircleId(
			circleId: String,
			today: LocalDate,
			cursor: SchedulePageCursor?,
			size: Int,
		): List<Schedule> = error("not used")

		override fun findUpcomingByCircleId(circleId: String, today: LocalDate, limit: Int): List<Schedule> =
			error("not used")

		override fun countActiveByCircleId(circleId: String): Long = error("not used")

		override fun findActiveByStartDateAndCreatedBefore(
			startDate: LocalDate,
			createdBefore: LocalDateTime,
		): List<Schedule> =
			schedules.filter { !it.isDeleted && it.startDate == startDate && it.createdAt.isBefore(createdBefore) }

		override fun findActiveAllDayByStartDateAndCreatedBefore(
			startDate: LocalDate,
			createdBefore: LocalDateTime,
		): List<Schedule> =
			schedules.filter {
				!it.isDeleted && it.isAllDay && it.startDate == startDate && it.createdAt.isBefore(createdBefore)
			}

		override fun findActiveTimedByStartAtAndCreatedBefore(
			startDate: LocalDate,
			startTime: LocalTime,
			createdBefore: LocalDateTime,
		): List<Schedule> =
			schedules.filter {
				!it.isDeleted &&
					it.startDate == startDate &&
					it.startTime == startTime &&
					it.createdAt.isBefore(createdBefore)
			}

		override fun findActiveConfirmationRequiredCreatedBetween(
			createdFrom: LocalDateTime,
			createdBefore: LocalDateTime,
		): List<Schedule> = error("not used")
	}

	private class RecordingEventPublisher : EventPublisher {
		val events = mutableListOf<Event>()

		override fun publish(event: Event) {
			events += event
		}
	}
}
