package com.unicorn.server.domain.schedule.service

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.domain.schedule.Schedule
import com.unicorn.server.domain.schedule.ScheduleConfirmation
import com.unicorn.server.domain.schedule.enums.ConfirmationType
import com.unicorn.server.domain.schedule.exception.ScheduleErrorCode
import com.unicorn.server.domain.schedule.port.dto.ConfirmationCountResult
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

@DisplayName("CancelConfirmationService 단위 테스트")
class CancelConfirmationServiceTest {

	private val scheduleOutPort = FakeScheduleOutPort()
	private val confirmationOutPort = FakeScheduleConfirmationOutPort()
	private val circleAccessOutPort = FakeCircleAccessOutPort()
	private val cancelConfirmationService = CancelConfirmationService(
		scheduleOutPort,
		confirmationOutPort,
		circleAccessOutPort,
	)

	@Test
	@DisplayName("본인이 등록한 확인하기를 취소한다")
	fun cancel_withOwnConfirmation_deletesConfirmation() {
		circleAccessOutPort.seedMember(CIRCLE_ID, MEMBER_ID)
		scheduleOutPort.seed(schedule())
		confirmationOutPort.seed(confirmation(id = CONFIRMATION_ID, memberId = MEMBER_ID))

		cancelConfirmationService.cancel(CONFIRMATION_ID, SCHEDULE_ID, CIRCLE_ID, MEMBER_ID)

		assertThat(confirmationOutPort.deletedIds).containsExactly(CONFIRMATION_ID)
		assertThat(confirmationOutPort.findById(CONFIRMATION_ID)).isNull()
	}

	@Test
	@DisplayName("확인하기가 없으면 CONFIRMATION_NOT_FOUND 예외가 발생한다")
	fun cancel_withMissingConfirmation_throwsConfirmationNotFound() {
		circleAccessOutPort.seedMember(CIRCLE_ID, MEMBER_ID)
		scheduleOutPort.seed(schedule())

		assertThatThrownBy { cancelConfirmationService.cancel(CONFIRMATION_ID, SCHEDULE_ID, CIRCLE_ID, MEMBER_ID) }
			.isInstanceOf(BusinessException::class.java)
			.extracting { (it as BusinessException).errorCode }
			.isEqualTo(ScheduleErrorCode.CONFIRMATION_NOT_FOUND)
	}

	@Test
	@DisplayName("다른 멤버의 확인하기면 CONFIRMATION_ACCESS_DENIED 예외가 발생한다")
	fun cancel_withOtherMemberConfirmation_throwsConfirmationAccessDenied() {
		circleAccessOutPort.seedMember(CIRCLE_ID, MEMBER_ID)
		scheduleOutPort.seed(schedule())
		confirmationOutPort.seed(confirmation(id = CONFIRMATION_ID, memberId = "member-2"))

		assertThatThrownBy { cancelConfirmationService.cancel(CONFIRMATION_ID, SCHEDULE_ID, CIRCLE_ID, MEMBER_ID) }
			.isInstanceOf(BusinessException::class.java)
			.extracting { (it as BusinessException).errorCode }
			.isEqualTo(ScheduleErrorCode.CONFIRMATION_ACCESS_DENIED)
	}

	private fun schedule(): Schedule =
		Schedule.reconstitute(
			id = SCHEDULE_ID,
			circleId = CIRCLE_ID,
			title = "제주도 여행",
			startDate = LocalDate.of(2024, 8, 1),
			endDate = LocalDate.of(2024, 8, 1),
			startTime = LocalTime.of(9, 0),
			endTime = LocalTime.of(18, 0),
			needConfirm = true,
			memo = null,
			createdBy = "author",
			updatedBy = "author",
			createdAt = LocalDateTime.of(2024, 7, 1, 10, 0),
			updatedAt = LocalDateTime.of(2024, 7, 1, 10, 0),
			isDeleted = false,
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
	}

	private class FakeScheduleConfirmationOutPort : ScheduleConfirmationOutPort {
		private val store = mutableListOf<ScheduleConfirmation>()
		val deletedIds = mutableListOf<Long>()

		fun seed(confirmation: ScheduleConfirmation) {
			store += confirmation
		}

		override fun findById(id: Long): ScheduleConfirmation? =
			store.firstOrNull { it.id == id }

		override fun findByScheduleIdAndMemberId(scheduleId: ScheduleId, memberId: String): ScheduleConfirmation? =
			store.firstOrNull { it.scheduleId == scheduleId && it.memberId == memberId }

		override fun save(confirmation: ScheduleConfirmation): ScheduleConfirmation {
			store.removeIf { it.id == confirmation.id }
			store += confirmation
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

		override fun isInitiator(circleId: String, memberId: String): Boolean = false
	}

	companion object {
		private const val CIRCLE_ID = "CC202506010000000001"
		private val SCHEDULE_ID = ScheduleId.of("SC202407070000000001")
		private const val MEMBER_ID = "member-1"
		private const val CONFIRMATION_ID = 1L
	}
}
