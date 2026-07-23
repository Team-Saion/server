package com.unicorn.server.domain.schedule.service

import com.unicorn.server.common.domain.Event
import com.unicorn.server.common.port.out.event.EventPublisher
import com.unicorn.server.domain.schedule.Schedule
import com.unicorn.server.domain.schedule.event.ScheduleConfirmationRequestDueEvent
import com.unicorn.server.domain.schedule.port.dto.SchedulePageCursor
import com.unicorn.server.domain.schedule.port.out.ScheduleOutPort
import com.unicorn.server.domain.schedule.vo.ScheduleId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@DisplayName("ScheduleConfirmationRequestService 단위 테스트")
class ScheduleConfirmationRequestServiceTest {
	private val scheduleOutPort = FakeScheduleOutPort()
	private val eventPublisher = RecordingEventPublisher()
	private val service = ScheduleConfirmationRequestService(scheduleOutPort, eventPublisher)

	@Test
	@DisplayName("생성 후 24시간이 지나고 시작까지 24시간 이상 남은 확인 필요 일정에 요청 이벤트를 발행한다")
	fun dispatchDue_withEligibleSchedule_publishesConfirmationRequestEvent() {
		val now = LocalDateTime.of(2026, 8, 11, 10, 1)
		scheduleOutPort.schedules += listOf(
			schedule("SC-ELIGIBLE", LocalDateTime.of(2026, 8, 10, 10, 0, 30), LocalDateTime.of(2026, 8, 11, 12, 0)),
			schedule("SC-TOO-SOON", LocalDateTime.of(2026, 8, 10, 10, 0, 30), LocalDateTime.of(2026, 8, 11, 9, 0)),
		)

		service.dispatchDue(now)

		val event = eventPublisher.events.filterIsInstance<ScheduleConfirmationRequestDueEvent>().single()
		assertThat(event.scheduleId).isEqualTo("SC-ELIGIBLE")
		assertThat(event.scheduleCreatorMemberId).isEqualTo("creator")
	}

	private fun schedule(id: String, createdAt: LocalDateTime, startAt: LocalDateTime): Schedule =
		Schedule.reconstitute(
			id = ScheduleId.of(id),
			circleId = "circle-1",
			title = "일정 $id",
			startDate = startAt.toLocalDate(),
			endDate = startAt.toLocalDate(),
			startTime = startAt.toLocalTime(),
			endTime = startAt.toLocalTime().plusHours(1),
			needConfirm = true,
			memo = null,
			createdBy = "creator",
			updatedBy = "creator",
			createdAt = createdAt,
			updatedAt = createdAt,
			isDeleted = false,
		)

	private class FakeScheduleOutPort : ScheduleOutPort {
		val schedules = mutableListOf<Schedule>()

		override fun save(schedule: Schedule): Schedule = schedule
		override fun findById(scheduleId: ScheduleId): Schedule? = schedules.firstOrNull { it.id == scheduleId }
		override fun findActiveByIdAndCircleId(scheduleId: ScheduleId, circleId: String): Schedule? =
			schedules.firstOrNull { it.id == scheduleId && it.circleId == circleId && !it.isDeleted }
		override fun findActiveByCircleId(circleId: String, cursor: SchedulePageCursor?, size: Int): List<Schedule> = error("not used")
		override fun findActiveByStartDateAndCreatedBefore(startDate: LocalDate, createdBefore: LocalDateTime): List<Schedule> = error("not used")
		override fun findActiveAllDayByStartDateAndCreatedBefore(startDate: LocalDate, createdBefore: LocalDateTime): List<Schedule> = error("not used")
		override fun findActiveTimedByStartAtAndCreatedBefore(startDate: LocalDate, startTime: LocalTime, createdBefore: LocalDateTime): List<Schedule> = error("not used")
		override fun findActiveConfirmationRequiredCreatedBetween(
			createdFrom: LocalDateTime,
			createdBefore: LocalDateTime,
		): List<Schedule> = schedules.filter {
			!it.isDeleted && it.needConfirm && !it.createdAt.isBefore(createdFrom) && it.createdAt.isBefore(createdBefore)
		}
	}

	private class RecordingEventPublisher : EventPublisher {
		val events = mutableListOf<Event>()

		override fun publish(event: Event) {
			events += event
		}
	}
}
