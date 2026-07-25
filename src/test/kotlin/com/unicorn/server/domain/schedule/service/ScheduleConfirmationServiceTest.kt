package com.unicorn.server.domain.schedule.service

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.common.domain.Event
import com.unicorn.server.common.port.out.event.EventOutPort
import com.unicorn.server.domain.schedule.Schedule
import com.unicorn.server.domain.schedule.ScheduleConfirmation
import com.unicorn.server.domain.schedule.enums.ConfirmationType
import com.unicorn.server.domain.schedule.event.ScheduleConfirmedEvent
import com.unicorn.server.domain.schedule.exception.ScheduleErrorCode
import com.unicorn.server.domain.schedule.port.dto.ConfirmationCountResult
import com.unicorn.server.domain.schedule.port.dto.RegisterConfirmationCommand
import com.unicorn.server.domain.schedule.port.dto.SchedulePageCursor
import com.unicorn.server.domain.schedule.port.out.CircleAccessOutPort
import com.unicorn.server.domain.schedule.port.out.ScheduleConfirmationOutPort
import com.unicorn.server.domain.schedule.port.out.ScheduleOutPort
import com.unicorn.server.domain.schedule.vo.ScheduleId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@DisplayName("ScheduleConfirmationService 단위 테스트")
class ScheduleConfirmationServiceTest {

	private val scheduleOutPort = FakeScheduleOutPort()
	private val confirmationOutPort = FakeScheduleConfirmationOutPort()
	private val circleAccessOutPort = FakeCircleAccessOutPort()
	private val eventPublisher = RecordingEventPublisher()
	private val scheduleConfirmationService = ScheduleConfirmationService(
		scheduleOutPort,
		confirmationOutPort,
		circleAccessOutPort,
		eventPublisher,
	)

	@Test
	@DisplayName("확인하기가 없으면 새로 생성한다")
	fun register_withoutExistingConfirmation_createsConfirmation() {
		circleAccessOutPort.seedMember(CIRCLE_ID, MEMBER_ID)
		scheduleOutPort.seed(schedule(needConfirm = true))
		val command = command()

		val type = scheduleConfirmationService.register(command)

		assertThat(type).isEqualTo(ConfirmationType.CONFIRMED)
		assertThat(confirmationOutPort.saved).hasSize(1)
		assertThat(confirmationOutPort.saved.single().memberId).isEqualTo(MEMBER_ID)
		val event = eventPublisher.events.filterIsInstance<ScheduleConfirmedEvent>().single()
		assertThat(event.scheduleCreatorMemberId).isEqualTo("author")
		assertThat(event.confirmerMemberId).isEqualTo(MEMBER_ID)
	}

	@Test
	@DisplayName("기존 확인하기와 같은 종류면 저장하지 않고 멱등 처리한다")
	fun register_withSameExistingConfirmation_doesNotSave() {
		circleAccessOutPort.seedMember(CIRCLE_ID, MEMBER_ID)
		scheduleOutPort.seed(schedule(needConfirm = true))
		confirmationOutPort.seed(
			ScheduleConfirmation.create(
				scheduleId = SCHEDULE_ID,
				memberId = MEMBER_ID,
				confirmationType = ConfirmationType.CONFIRMED,
				createdBy = MEMBER_ID,
			),
		)
		val command = command()

		val type = scheduleConfirmationService.register(command)

		assertThat(type).isEqualTo(ConfirmationType.CONFIRMED)
		assertThat(confirmationOutPort.saved).isEmpty()
	}

	@Test
	@DisplayName("기존 확인하기와 다른 종류면 타입을 변경해 저장한다")
	fun register_withDifferentExistingConfirmation_updatesConfirmation() {
		circleAccessOutPort.seedMember(CIRCLE_ID, MEMBER_ID)
		scheduleOutPort.seed(schedule(needConfirm = true))
		confirmationOutPort.seed(
			ScheduleConfirmation.create(
				scheduleId = SCHEDULE_ID,
				memberId = MEMBER_ID,
				confirmationType = ConfirmationType.CONFIRMED,
				createdBy = MEMBER_ID,
			),
		)
		val command = RegisterConfirmationCommand(
			scheduleId = SCHEDULE_ID,
			circleId = CIRCLE_ID,
			memberId = MEMBER_ID,
			confirmationType = ConfirmationType.ETC,
		)

		val type = scheduleConfirmationService.register(command)

		assertThat(type).isEqualTo(ConfirmationType.ETC)
		assertThat(confirmationOutPort.saved.single().confirmationType).isEqualTo(ConfirmationType.ETC)
	}

	@Test
	@DisplayName("확인하기를 지원하지 않는 일정이면 CONFIRMATION_NOT_SUPPORTED 예외가 발생한다")
	fun register_withScheduleNotSupportingConfirmation_throwsConfirmationNotSupported() {
		circleAccessOutPort.seedMember(CIRCLE_ID, MEMBER_ID)
		scheduleOutPort.seed(schedule(needConfirm = false))
		val command = command()

		assertThatThrownBy { scheduleConfirmationService.register(command) }
			.isInstanceOf(BusinessException::class.java)
			.extracting { (it as BusinessException).errorCode }
			.isEqualTo(ScheduleErrorCode.CONFIRMATION_NOT_SUPPORTED)
	}

	@Test
	@DisplayName("써클 구성원이 아니면 CIRCLE_ACCESS_DENIED 예외가 발생한다")
	fun register_withNonMember_throwsCircleAccessDenied() {
		scheduleOutPort.seed(schedule(needConfirm = true))
		val command = command()

		assertThatThrownBy { scheduleConfirmationService.register(command) }
			.isInstanceOf(BusinessException::class.java)
			.extracting { (it as BusinessException).errorCode }
			.isEqualTo(ScheduleErrorCode.CIRCLE_ACCESS_DENIED)
	}

	@Test
	@DisplayName("본인이 등록한 확인하기를 취소한다")
	fun cancel_withOwnConfirmation_deletesConfirmation() {
		circleAccessOutPort.seedMember(CIRCLE_ID, MEMBER_ID)
		scheduleOutPort.seed(schedule(needConfirm = true))
		confirmationOutPort.seed(confirmation(id = CONFIRMATION_ID, memberId = MEMBER_ID))

		scheduleConfirmationService.cancel(CONFIRMATION_ID, SCHEDULE_ID, CIRCLE_ID, MEMBER_ID)

		assertThat(confirmationOutPort.deletedIds).containsExactly(CONFIRMATION_ID)
		assertThat(confirmationOutPort.findById(CONFIRMATION_ID)).isNull()
	}

	@Test
	@DisplayName("확인하기가 없으면 CONFIRMATION_NOT_FOUND 예외가 발생한다")
	fun cancel_withMissingConfirmation_throwsConfirmationNotFound() {
		circleAccessOutPort.seedMember(CIRCLE_ID, MEMBER_ID)
		scheduleOutPort.seed(schedule(needConfirm = true))

		assertThatThrownBy { scheduleConfirmationService.cancel(CONFIRMATION_ID, SCHEDULE_ID, CIRCLE_ID, MEMBER_ID) }
			.isInstanceOf(BusinessException::class.java)
			.extracting { (it as BusinessException).errorCode }
			.isEqualTo(ScheduleErrorCode.CONFIRMATION_NOT_FOUND)
	}

	@Test
	@DisplayName("다른 멤버의 확인하기면 CONFIRMATION_ACCESS_DENIED 예외가 발생한다")
	fun cancel_withOtherMemberConfirmation_throwsConfirmationAccessDenied() {
		circleAccessOutPort.seedMember(CIRCLE_ID, MEMBER_ID)
		scheduleOutPort.seed(schedule(needConfirm = true))
		confirmationOutPort.seed(confirmation(id = CONFIRMATION_ID, memberId = "member-2"))

		assertThatThrownBy { scheduleConfirmationService.cancel(CONFIRMATION_ID, SCHEDULE_ID, CIRCLE_ID, MEMBER_ID) }
			.isInstanceOf(BusinessException::class.java)
			.extracting { (it as BusinessException).errorCode }
			.isEqualTo(ScheduleErrorCode.CONFIRMATION_ACCESS_DENIED)
	}

	private fun command(): RegisterConfirmationCommand =
		RegisterConfirmationCommand(
			scheduleId = SCHEDULE_ID,
			circleId = CIRCLE_ID,
			memberId = MEMBER_ID,
			confirmationType = ConfirmationType.CONFIRMED,
		)

	private fun confirmation(id: Long, memberId: String): ScheduleConfirmation =
		ScheduleConfirmation.reconstitute(
			id = id,
			scheduleId = SCHEDULE_ID,
			memberId = memberId,
			confirmationType = ConfirmationType.CONFIRMED,
			createdBy = memberId,
			updatedBy = memberId,
			createdAt = LocalDateTime.of(2024, 7, 1, 10, 0),
			updatedAt = LocalDateTime.of(2024, 7, 1, 10, 0),
		)

	private fun schedule(needConfirm: Boolean): Schedule =
		Schedule.reconstitute(
			id = SCHEDULE_ID,
			circleId = CIRCLE_ID,
			title = "제주도 여행",
			startDate = LocalDate.of(2024, 8, 1),
			endDate = LocalDate.of(2024, 8, 1),
			startTime = LocalTime.of(9, 0),
			endTime = LocalTime.of(18, 0),
			needConfirm = needConfirm,
			memo = null,
			createdBy = "author",
			updatedBy = "author",
			createdAt = LocalDateTime.of(2024, 7, 1, 10, 0),
			updatedAt = LocalDateTime.of(2024, 7, 1, 10, 0),
			isDeleted = false,
		)

	private class FakeScheduleOutPort : ScheduleOutPort {
		private val store = linkedMapOf<ScheduleId, Schedule>()

		fun seed(schedule: Schedule) {
			store[schedule.id] = schedule
		}

		override fun save(schedule: Schedule): Schedule = schedule

		override fun findById(scheduleId: ScheduleId): Schedule? = store[scheduleId]

		override fun findActiveByIdAndCircleId(scheduleId: ScheduleId, circleId: String): Schedule? =
			store[scheduleId]?.takeIf { it.circleId == circleId && !it.isDeleted }

		override fun findActiveByCircleId(
			circleId: String,
			today: LocalDate,
			cursor: SchedulePageCursor?,
			size: Int,
		): List<Schedule> = emptyList()

		override fun findActiveByStartDateAndCreatedBefore(
			startDate: java.time.LocalDate,
			createdBefore: java.time.LocalDateTime,
		): List<Schedule> = error("not used")

		override fun findActiveAllDayByStartDateAndCreatedBefore(
			startDate: java.time.LocalDate,
			createdBefore: java.time.LocalDateTime,
		): List<Schedule> = error("not used")

		override fun findActiveTimedByStartAtAndCreatedBefore(
			startDate: java.time.LocalDate,
			startTime: java.time.LocalTime,
			createdBefore: java.time.LocalDateTime,
		): List<Schedule> = error("not used")

		override fun findActiveConfirmationRequiredCreatedBetween(
			createdFrom: java.time.LocalDateTime,
			createdBefore: java.time.LocalDateTime,
		): List<Schedule> = error("not used")

		override fun findUpcomingByCircleId(
			circleId: String,
			today: LocalDate,
			limit: Int,
		): List<Schedule> = emptyList()

		override fun countActiveByCircleId(circleId: String): Long = 0L
	}

	private class FakeScheduleConfirmationOutPort : ScheduleConfirmationOutPort {
		private val store = mutableListOf<ScheduleConfirmation>()
		val saved = mutableListOf<ScheduleConfirmation>()
		val deletedIds = mutableListOf<Long>()

		fun seed(confirmation: ScheduleConfirmation) {
			store += confirmation
		}

		override fun findById(id: Long): ScheduleConfirmation? =
			store.firstOrNull { it.id == id }

		override fun findByScheduleIdAndMemberId(scheduleId: ScheduleId, memberId: String): ScheduleConfirmation? =
			store.firstOrNull { it.scheduleId == scheduleId && it.memberId == memberId }

		override fun save(confirmation: ScheduleConfirmation): ScheduleConfirmation {
			store.removeIf { it.scheduleId == confirmation.scheduleId && it.memberId == confirmation.memberId }
			store += confirmation
			saved += confirmation
			return confirmation
		}

		override fun deleteById(id: Long) {
			deletedIds += id
			store.removeIf { it.id == id }
		}

		override fun countGroupByType(scheduleId: ScheduleId): List<ConfirmationCountResult> = emptyList()

		override fun deleteAllByScheduleId(scheduleId: ScheduleId) {
			store.removeIf { it.scheduleId == scheduleId }
		}
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
		private val SCHEDULE_ID = ScheduleId.of("SC202407070000000001")
		private const val MEMBER_ID = "member-1"
		private const val CONFIRMATION_ID = 1L
	}
}
