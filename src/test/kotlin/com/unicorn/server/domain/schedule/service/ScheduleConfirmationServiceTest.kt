package com.unicorn.server.domain.schedule.service

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.domain.schedule.Schedule
import com.unicorn.server.domain.schedule.ScheduleConfirmation
import com.unicorn.server.domain.schedule.enums.ConfirmationType
import com.unicorn.server.domain.schedule.exception.ScheduleErrorCode
import com.unicorn.server.domain.schedule.port.dto.ConfirmationCountResult
import com.unicorn.server.domain.schedule.port.dto.RegisterConfirmationCommand
import com.unicorn.server.domain.schedule.port.dto.SchedulePageCursor
import com.unicorn.server.domain.schedule.port.out.CircleAccessOutPort
import com.unicorn.server.domain.schedule.port.out.ScheduleConfirmationOutPort
import com.unicorn.server.domain.schedule.port.out.ScheduleOutPort
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
	private val scheduleConfirmationService = ScheduleConfirmationService(
		scheduleOutPort,
		confirmationOutPort,
		circleAccessOutPort,
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
			confirmationType = ConfirmationType.CANNOT_ATTEND,
		)

		val type = scheduleConfirmationService.register(command)

		assertThat(type).isEqualTo(ConfirmationType.CANNOT_ATTEND)
		assertThat(confirmationOutPort.saved.single().confirmationType).isEqualTo(ConfirmationType.CANNOT_ATTEND)
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

	private fun command(): RegisterConfirmationCommand =
		RegisterConfirmationCommand(
			scheduleId = SCHEDULE_ID,
			circleId = CIRCLE_ID,
			memberId = MEMBER_ID,
			confirmationType = ConfirmationType.CONFIRMED,
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
		private val store = linkedMapOf<Long, Schedule>()

		fun seed(schedule: Schedule) {
			store[schedule.id] = schedule
		}

		override fun save(schedule: Schedule): Schedule = schedule

		override fun findById(scheduleId: Long): Schedule? = store[scheduleId]

		override fun findActiveByIdAndCircleId(scheduleId: Long, circleId: Long): Schedule? =
			store[scheduleId]?.takeIf { it.circleId == circleId && !it.isDeleted }

		override fun findActiveByCircleId(
			circleId: Long,
			cursor: SchedulePageCursor?,
			size: Int,
		): List<Schedule> = emptyList()
	}

	private class FakeScheduleConfirmationOutPort : ScheduleConfirmationOutPort {
		private val store = mutableListOf<ScheduleConfirmation>()
		val saved = mutableListOf<ScheduleConfirmation>()

		fun seed(confirmation: ScheduleConfirmation) {
			store += confirmation
		}

		override fun findByScheduleIdAndMemberId(scheduleId: Long, memberId: String): ScheduleConfirmation? =
			store.firstOrNull { it.scheduleId == scheduleId && it.memberId == memberId }

		override fun save(confirmation: ScheduleConfirmation): ScheduleConfirmation {
			store.removeIf { it.scheduleId == confirmation.scheduleId && it.memberId == confirmation.memberId }
			store += confirmation
			saved += confirmation
			return confirmation
		}

		override fun countGroupByType(scheduleId: Long): List<ConfirmationCountResult> = emptyList()

		override fun deleteAllByScheduleId(scheduleId: Long) {
			store.removeIf { it.scheduleId == scheduleId }
		}
	}

	private class FakeCircleAccessOutPort : CircleAccessOutPort {
		private val members = mutableSetOf<Pair<Long, String>>()

		fun seedMember(circleId: Long, memberId: String) {
			members += circleId to memberId
		}

		override fun existsById(circleId: Long): Boolean = true

		override fun isMember(circleId: Long, memberId: String): Boolean = circleId to memberId in members

		override fun isInitiator(circleId: Long, memberId: String): Boolean = false
	}

	companion object {
		private const val CIRCLE_ID = 1L
		private const val SCHEDULE_ID = 1L
		private const val MEMBER_ID = "member-1"
	}
}
