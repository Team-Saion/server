package com.unicorn.server.domain.schedule.service

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.domain.schedule.Schedule
import com.unicorn.server.domain.schedule.ScheduleConfirmation
import com.unicorn.server.domain.schedule.exception.ScheduleErrorCode
import com.unicorn.server.domain.schedule.port.dto.ConfirmationCountResult
import com.unicorn.server.domain.schedule.port.dto.CreateScheduleCommand
import com.unicorn.server.domain.schedule.port.dto.SchedulePageCursor
import com.unicorn.server.domain.schedule.port.dto.UpdateScheduleCommand
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

@DisplayName("ScheduleCommandService 단위 테스트")
class ScheduleCommandServiceTest {

	private val scheduleOutPort = FakeScheduleOutPort()
	private val confirmationOutPort = FakeScheduleConfirmationOutPort()
	private val circleAccessOutPort = FakeCircleAccessOutPort()
	private val scheduleCommandService = ScheduleCommandService(
		scheduleOutPort,
		confirmationOutPort,
		circleAccessOutPort,
	)

	@Test
	@DisplayName("써클 구성원이 일정을 생성하면 저장된 일정 ID를 반환한다")
	fun create_withCircleMember_returnsSavedScheduleId() {
		circleAccessOutPort.seedCircle(CIRCLE_ID)
		circleAccessOutPort.seedMember(CIRCLE_ID, MEMBER_ID)
		val command = createCommand()

		val scheduleId = scheduleCommandService.create(command)

		assertThat(scheduleId).isEqualTo(1L)
		assertThat(scheduleOutPort.saved).hasSize(1)
		assertThat(scheduleOutPort.saved.single().title).isEqualTo("제주도 여행")
	}

	@Test
	@DisplayName("존재하지 않는 써클에 일정을 생성하면 CIRCLE_NOT_FOUND 예외가 발생한다")
	fun create_withMissingCircle_throwsCircleNotFound() {
		val command = createCommand()

		assertThatThrownBy { scheduleCommandService.create(command) }
			.isInstanceOf(BusinessException::class.java)
			.extracting { (it as BusinessException).errorCode }
			.isEqualTo(ScheduleErrorCode.CIRCLE_NOT_FOUND)
	}

	@Test
	@DisplayName("써클 구성원이 아니면 일정 생성 시 CIRCLE_ACCESS_DENIED 예외가 발생한다")
	fun create_withNonMember_throwsCircleAccessDenied() {
		circleAccessOutPort.seedCircle(CIRCLE_ID)
		val command = createCommand()

		assertThatThrownBy { scheduleCommandService.create(command) }
			.isInstanceOf(BusinessException::class.java)
			.extracting { (it as BusinessException).errorCode }
			.isEqualTo(ScheduleErrorCode.CIRCLE_ACCESS_DENIED)
	}

	@Test
	@DisplayName("작성자는 일정을 수정할 수 있다")
	fun update_withAuthor_updatesSchedule() {
		circleAccessOutPort.seedMember(CIRCLE_ID, MEMBER_ID)
		scheduleOutPort.seed(schedule(id = 1L, createdBy = MEMBER_ID))
		val command = UpdateScheduleCommand(
			scheduleId = 1L,
			circleId = CIRCLE_ID,
			memberId = MEMBER_ID,
			title = "수정된 제목",
			startDate = null,
			endDate = null,
			startTime = null,
			endTime = null,
			startTimeProvided = true,
			endTimeProvided = true,
			needConfirm = false,
			memo = "수정된 메모",
			memoProvided = true,
		)

		scheduleCommandService.update(command)

		val updated = scheduleOutPort.findById(1L)
		assertThat(updated?.title).isEqualTo("수정된 제목")
		assertThat(updated?.isAllDay).isTrue()
		assertThat(updated?.needConfirm).isFalse()
		assertThat(updated?.memo).isEqualTo("수정된 메모")
		assertThat(updated?.updatedBy).isEqualTo(MEMBER_ID)
	}

	@Test
	@DisplayName("메모가 요청에 포함되지 않으면 기존 메모를 유지한다")
	fun update_withoutMemoField_keepsExistingMemo() {
		circleAccessOutPort.seedMember(CIRCLE_ID, MEMBER_ID)
		scheduleOutPort.seed(schedule(id = 1L, createdBy = MEMBER_ID))
		val command = UpdateScheduleCommand(
			scheduleId = 1L,
			circleId = CIRCLE_ID,
			memberId = MEMBER_ID,
			title = null,
			startDate = null,
			endDate = null,
			startTime = null,
			endTime = null,
			startTimeProvided = false,
			endTimeProvided = false,
			needConfirm = true,
			memo = null,
			memoProvided = false,
		)

		scheduleCommandService.update(command)

		assertThat(scheduleOutPort.findById(1L)?.memo).isEqualTo("숙소 체크인 15시")
	}

	@Test
	@DisplayName("작성자도 initiator도 아니면 일정 수정 시 SCHEDULE_MODIFICATION_DENIED 예외가 발생한다")
	fun update_withoutModificationPermission_throwsScheduleModificationDenied() {
		circleAccessOutPort.seedMember(CIRCLE_ID, MEMBER_ID)
		scheduleOutPort.seed(schedule(id = 1L, createdBy = "other-member"))
		val command = UpdateScheduleCommand(
			scheduleId = 1L,
			circleId = CIRCLE_ID,
			memberId = MEMBER_ID,
			title = "수정된 제목",
			startDate = null,
			endDate = null,
			startTime = null,
			endTime = null,
			startTimeProvided = false,
			endTimeProvided = false,
			needConfirm = true,
			memo = null,
			memoProvided = false,
		)

		assertThatThrownBy { scheduleCommandService.update(command) }
			.isInstanceOf(BusinessException::class.java)
			.extracting { (it as BusinessException).errorCode }
			.isEqualTo(ScheduleErrorCode.SCHEDULE_MODIFICATION_DENIED)
	}

	@Test
	@DisplayName("initiator는 다른 작성자의 일정을 삭제할 수 있고 확인하기도 함께 삭제한다")
	fun delete_withInitiator_deletesScheduleAndConfirmations() {
		circleAccessOutPort.seedMember(CIRCLE_ID, MEMBER_ID)
		circleAccessOutPort.seedInitiator(CIRCLE_ID, MEMBER_ID)
		scheduleOutPort.seed(schedule(id = 1L, createdBy = "other-member"))
		confirmationOutPort.seed(
			ScheduleConfirmation.create(
				scheduleId = 1L,
				memberId = MEMBER_ID,
				confirmationType = com.unicorn.server.domain.schedule.enums.ConfirmationType.CONFIRMED,
				createdBy = MEMBER_ID,
			),
		)

		scheduleCommandService.delete(1L, CIRCLE_ID, MEMBER_ID)

		assertThat(scheduleOutPort.findById(1L)?.isDeleted).isTrue()
		assertThat(confirmationOutPort.deletedScheduleIds).containsExactly(1L)
	}

	private fun createCommand(): CreateScheduleCommand =
		CreateScheduleCommand(
			memberId = MEMBER_ID,
			circleId = CIRCLE_ID,
			title = "제주도 여행",
			startDate = LocalDate.of(2024, 8, 1),
			endDate = LocalDate.of(2024, 8, 1),
			startTime = LocalTime.of(9, 0),
			endTime = LocalTime.of(18, 0),
			needConfirm = true,
			memo = "숙소 체크인 15시",
		)

	private fun schedule(
		id: Long,
		createdBy: String,
		needConfirm: Boolean = true,
	): Schedule =
		Schedule.reconstitute(
			id = id,
			circleId = CIRCLE_ID,
			title = "제주도 여행",
			startDate = LocalDate.of(2024, 8, 1),
			endDate = LocalDate.of(2024, 8, 1),
			startTime = LocalTime.of(9, 0),
			endTime = LocalTime.of(18, 0),
			needConfirm = needConfirm,
			memo = "숙소 체크인 15시",
			createdBy = createdBy,
			updatedBy = createdBy,
			createdAt = LocalDateTime.of(2024, 7, 1, 10, 0),
			updatedAt = LocalDateTime.of(2024, 7, 1, 10, 0),
			isDeleted = false,
		)

	private class FakeScheduleOutPort : ScheduleOutPort {
		private val store = linkedMapOf<Long, Schedule>()
		val saved = mutableListOf<Schedule>()
		private var nextId = 1L

		fun seed(schedule: Schedule) {
			store[schedule.id] = schedule
			nextId = maxOf(nextId, schedule.id + 1)
		}

		override fun save(schedule: Schedule): Schedule {
			val savedSchedule = if (schedule.id == 0L) {
				Schedule.reconstitute(
					id = nextId++,
					circleId = schedule.circleId,
					title = schedule.title,
					startDate = schedule.startDate,
					endDate = schedule.endDate,
					startTime = schedule.startTime,
					endTime = schedule.endTime,
					needConfirm = schedule.needConfirm,
					memo = schedule.memo,
					createdBy = schedule.createdBy,
					updatedBy = schedule.updatedBy,
					createdAt = schedule.createdAt,
					updatedAt = schedule.updatedAt,
					isDeleted = schedule.isDeleted,
				)
			} else {
				schedule
			}
			store[savedSchedule.id] = savedSchedule
			saved += savedSchedule
			return savedSchedule
		}

		override fun findById(scheduleId: Long): Schedule? = store[scheduleId]

		override fun findActiveByIdAndCircleId(scheduleId: Long, circleId: Long): Schedule? =
			store[scheduleId]?.takeIf { it.circleId == circleId && !it.isDeleted }

		override fun findActiveByCircleId(
			circleId: Long,
			cursor: SchedulePageCursor?,
			size: Int,
		): List<Schedule> = store.values.filter { it.circleId == circleId && !it.isDeleted }.take(size)
	}

	private class FakeScheduleConfirmationOutPort : ScheduleConfirmationOutPort {
		private val store = mutableListOf<ScheduleConfirmation>()
		val deletedScheduleIds = mutableListOf<Long>()

		fun seed(confirmation: ScheduleConfirmation) {
			store += confirmation
		}

		override fun findByScheduleIdAndMemberId(scheduleId: Long, memberId: String): ScheduleConfirmation? =
			store.firstOrNull { it.scheduleId == scheduleId && it.memberId == memberId }

		override fun save(confirmation: ScheduleConfirmation): ScheduleConfirmation {
			store.removeIf { it.scheduleId == confirmation.scheduleId && it.memberId == confirmation.memberId }
			store += confirmation
			return confirmation
		}

		override fun countGroupByType(scheduleId: Long): List<ConfirmationCountResult> = emptyList()

		override fun deleteAllByScheduleId(scheduleId: Long) {
			deletedScheduleIds += scheduleId
			store.removeIf { it.scheduleId == scheduleId }
		}
	}

	private class FakeCircleAccessOutPort : CircleAccessOutPort {
		private val circles = mutableSetOf<Long>()
		private val members = mutableSetOf<Pair<Long, String>>()
		private val initiators = mutableSetOf<Pair<Long, String>>()

		fun seedCircle(circleId: Long) {
			circles += circleId
		}

		fun seedMember(circleId: Long, memberId: String) {
			seedCircle(circleId)
			members += circleId to memberId
		}

		fun seedInitiator(circleId: Long, memberId: String) {
			initiators += circleId to memberId
		}

		override fun existsById(circleId: Long): Boolean = circleId in circles

		override fun isMember(circleId: Long, memberId: String): Boolean = circleId to memberId in members

		override fun isInitiator(circleId: Long, memberId: String): Boolean = circleId to memberId in initiators
	}

	companion object {
		private const val CIRCLE_ID = 1L
		private const val MEMBER_ID = "member-1"
	}
}
