package com.unicorn.server.domain.schedule.service

import com.unicorn.server.common.domain.Event
import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.common.port.out.event.EventOutPort
import com.unicorn.server.domain.schedule.Schedule
import com.unicorn.server.domain.schedule.event.FamilyScheduleNotificationRequestedEvent
import com.unicorn.server.domain.schedule.exception.ScheduleErrorCode
import com.unicorn.server.domain.schedule.port.dto.RequestFamilyScheduleNotificationCommand
import com.unicorn.server.domain.schedule.port.dto.SchedulePageCursor
import com.unicorn.server.domain.schedule.port.out.CircleAccessOutPort
import com.unicorn.server.domain.schedule.port.out.ScheduleOutPort
import com.unicorn.server.domain.schedule.vo.ScheduleId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@DisplayName("FamilyScheduleNotificationService 단위 테스트")
class FamilyScheduleNotificationServiceTest {
	private val scheduleOutPort = FakeScheduleOutPort()
	private val circleAccessOutPort = FakeCircleAccessOutPort()
	private val eventPublisher = RecordingEventPublisher()
	private val service = FamilyScheduleNotificationService(
		scheduleOutPort,
		circleAccessOutPort,
		eventPublisher,
	)

	@Test
	@DisplayName("다가오는 일정에 가족에게 전하기를 요청하면 가족 일정 알림 이벤트를 발행한다")
	fun request_withUpcomingSchedule_publishesFamilyScheduleNotificationEvent() {
		circleAccessOutPort.seedMember(CIRCLE_ID, MEMBER_ID)
		circleAccessOutPort.seedMember(CIRCLE_ID, OTHER_MEMBER_ID)
		scheduleOutPort.seed(schedule(startDate = LocalDate.now().plusDays(3)))

		service.request(command())

		val event = eventPublisher.events.filterIsInstance<FamilyScheduleNotificationRequestedEvent>().single()
		assertThat(event.requestId).isNotBlank()
		assertThat(event.scheduleId).isEqualTo(SCHEDULE_ID.value)
		assertThat(event.circleId).isEqualTo(CIRCLE_ID)
		assertThat(event.senderMemberId).isEqualTo(MEMBER_ID)
		assertThat(event.scheduleTitle).isEqualTo("제주도 여행")
		assertThat(event.dDay).isEqualTo("D-3")
	}

	@Test
	@DisplayName("시작일이 지난 일정에 가족에게 전하기를 요청하면 사용할 수 없다는 예외가 발생한다")
	fun request_withStartedSchedule_throwsNotAvailable() {
		circleAccessOutPort.seedMember(CIRCLE_ID, MEMBER_ID)
		circleAccessOutPort.seedMember(CIRCLE_ID, OTHER_MEMBER_ID)
		scheduleOutPort.seed(schedule(startDate = LocalDate.now().minusDays(1)))

		assertThatThrownBy { service.request(command()) }
			.isInstanceOf(BusinessException::class.java)
			.extracting { (it as BusinessException).errorCode }
			.isEqualTo(ScheduleErrorCode.FAMILY_SCHEDULE_NOTIFICATION_NOT_AVAILABLE)
	}

	@Test
	@DisplayName("다른 활성 구성원이 없는 써클에서 가족에게 전하기를 요청하면 수신자가 없다는 예외가 발생한다")
	fun request_withNoOtherActiveMember_throwsRecipientNotFound() {
		circleAccessOutPort.seedMember(CIRCLE_ID, MEMBER_ID)

		assertThatThrownBy { service.request(command()) }
			.isInstanceOf(BusinessException::class.java)
			.extracting { (it as BusinessException).errorCode }
			.isEqualTo(ScheduleErrorCode.FAMILY_SCHEDULE_NOTIFICATION_RECIPIENT_NOT_FOUND)
		assertThat(eventPublisher.events).isEmpty()
	}

	private fun command() = RequestFamilyScheduleNotificationCommand(
		scheduleId = SCHEDULE_ID,
		circleId = CIRCLE_ID,
		memberId = MEMBER_ID,
	)

	private fun schedule(startDate: LocalDate): Schedule = Schedule.reconstitute(
		id = SCHEDULE_ID,
		circleId = CIRCLE_ID,
		title = "제주도 여행",
		startDate = startDate,
		endDate = startDate,
		startTime = null,
		endTime = null,
		needConfirm = false,
		memo = null,
		createdBy = "author",
		updatedBy = "author",
		createdAt = LocalDateTime.now().minusDays(1),
		updatedAt = LocalDateTime.now().minusDays(1),
		isDeleted = false,
	)

	private class FakeScheduleOutPort : ScheduleOutPort {
		private val schedules = linkedMapOf<ScheduleId, Schedule>()

		fun seed(schedule: Schedule) {
			schedules[schedule.id] = schedule
		}

		override fun save(schedule: Schedule): Schedule = schedule

		override fun findById(scheduleId: ScheduleId): Schedule? = schedules[scheduleId]

		override fun findActiveByIdAndCircleId(scheduleId: ScheduleId, circleId: String): Schedule? =
			schedules[scheduleId]?.takeIf { it.circleId == circleId && !it.isDeleted }

		override fun findActiveByCircleId(
			circleId: String,
			today: LocalDate,
			cursor: SchedulePageCursor?,
			size: Int,
		): List<Schedule> = emptyList()

		override fun findActiveByStartDateAndCreatedBefore(
			startDate: LocalDate,
			createdBefore: LocalDateTime,
		): List<Schedule> = emptyList()

		override fun findActiveAllDayByStartDateAndCreatedBefore(
			startDate: LocalDate,
			createdBefore: LocalDateTime,
		): List<Schedule> = emptyList()

		override fun findActiveTimedByStartAtAndCreatedBefore(
			startDate: LocalDate,
			startTime: LocalTime,
			createdBefore: LocalDateTime,
		): List<Schedule> = emptyList()

		override fun findActiveConfirmationRequiredCreatedBetween(
			createdFrom: LocalDateTime,
			createdBefore: LocalDateTime,
		): List<Schedule> = emptyList()

		override fun findUpcomingByCircleId(circleId: String, today: LocalDate, limit: Int): List<Schedule> = emptyList()

		override fun countActiveByCircleId(circleId: String): Long = 0L
	}

	private class FakeCircleAccessOutPort : CircleAccessOutPort {
		private val members = mutableSetOf<Pair<String, String>>()

		fun seedMember(circleId: String, memberId: String) {
			members += circleId to memberId
		}

		override fun existsById(circleId: String): Boolean = true

		override fun isMember(circleId: String, memberId: String): Boolean = circleId to memberId in members

		override fun hasOtherActiveMember(circleId: String, excludedMemberId: String): Boolean =
			members.any { (memberCircleId, memberId) -> memberCircleId == circleId && memberId != excludedMemberId }

		override fun isInitiator(circleId: String, memberId: String): Boolean = false
	}

	private class RecordingEventPublisher : EventOutPort {
		val events = mutableListOf<Event>()

		override fun publish(event: Event) {
			events += event
		}
	}

	companion object {
		private const val CIRCLE_ID = "CC202506010000000001"
		private const val MEMBER_ID = "member-1"
		private const val OTHER_MEMBER_ID = "member-2"
		private val SCHEDULE_ID = ScheduleId.of("SC202407070000000001")
	}
}
