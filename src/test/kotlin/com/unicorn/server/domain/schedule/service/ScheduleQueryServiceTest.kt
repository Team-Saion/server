package com.unicorn.server.domain.schedule.service

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.common.exception.CommonErrorCode
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

@DisplayName("ScheduleQueryService 단위 테스트")
class ScheduleQueryServiceTest {

	private val scheduleOutPort = FakeScheduleOutPort()
	private val confirmationOutPort = FakeScheduleConfirmationOutPort()
	private val circleAccessOutPort = FakeCircleAccessOutPort()
	private val scheduleQueryService = ScheduleQueryService(
		scheduleOutPort,
		confirmationOutPort,
		circleAccessOutPort,
	)

	@Test
	@DisplayName("일정 목록 조회 시 size보다 한 건 더 조회해 다음 커서를 만든다")
	fun getList_withMoreResultsThanSize_returnsNextCursor() {
		circleAccessOutPort.seedMember(CIRCLE_ID, MEMBER_ID)
		scheduleOutPort.seed(schedule(id = SCHEDULE_ID_1, startDate = LocalDate.now().plusDays(1)))
		scheduleOutPort.seed(schedule(id = SCHEDULE_ID_2, startDate = LocalDate.now().plusDays(2)))

		val result = scheduleQueryService.getList(CIRCLE_ID, MEMBER_ID, cursor = null, size = 1)

		assertThat(result.schedules).hasSize(1)
		assertThat(result.hasNext).isTrue()
		assertThat(result.nextCursor).isEqualTo(SchedulePageCursor.from(scheduleOutPort.findById(SCHEDULE_ID_1)!!).encode())
	}

	@Test
	@DisplayName("빈 커서로 일정 목록 조회 시 첫 페이지를 조회한다")
	fun getList_withBlankCursor_returnsFirstPage() {
		circleAccessOutPort.seedMember(CIRCLE_ID, MEMBER_ID)
		scheduleOutPort.seed(schedule(id = SCHEDULE_ID_1, startDate = LocalDate.now().plusDays(1)))

		val result = scheduleQueryService.getList(CIRCLE_ID, MEMBER_ID, cursor = "", size = 20)

		assertThat(result.schedules).hasSize(1)
		assertThat(result.schedules.single().scheduleId).isEqualTo(SCHEDULE_ID_1)
	}

	@Test
	@DisplayName("일정 상세 조회 시 확인하기 카운트와 내 확인하기를 포함한다")
	fun getDetail_withNeedConfirm_returnsConfirmationData() {
		circleAccessOutPort.seedMember(CIRCLE_ID, MEMBER_ID)
		scheduleOutPort.seed(schedule(id = SCHEDULE_ID_1, needConfirm = true, createdBy = "author"))
		confirmationOutPort.seed(
			confirmation(
				id = 1L,
				scheduleId = SCHEDULE_ID_1,
				memberId = MEMBER_ID,
				confirmationType = ConfirmationType.CONFIRMED,
				createdBy = MEMBER_ID,
			),
		)

		val result = scheduleQueryService.getDetail(SCHEDULE_ID_1, CIRCLE_ID, MEMBER_ID)

		assertThat(result.scheduleId).isEqualTo(SCHEDULE_ID_1)
		assertThat(result.confirmations).containsExactly(ConfirmationCountResult(ConfirmationType.CONFIRMED, 1))
		assertThat(result.myConfirmation?.confirmationId).isEqualTo(1L)
		assertThat(result.myConfirmation?.confirmationType).isEqualTo(ConfirmationType.CONFIRMED)
		assertThat(result.createdBy).isEqualTo("author")
	}

	@Test
	@DisplayName("확인하기를 사용하지 않는 일정 상세 조회 시 확인하기 정보는 비어 있다")
	fun getDetail_withoutNeedConfirm_returnsEmptyConfirmationData() {
		circleAccessOutPort.seedMember(CIRCLE_ID, MEMBER_ID)
		scheduleOutPort.seed(schedule(id = SCHEDULE_ID_1, needConfirm = false))

		val result = scheduleQueryService.getDetail(SCHEDULE_ID_1, CIRCLE_ID, MEMBER_ID)

		assertThat(result.confirmations).isEmpty()
		assertThat(result.myConfirmation).isNull()
	}

	@Test
	@DisplayName("써클 구성원이 아니면 일정 목록 조회 시 CIRCLE_ACCESS_DENIED 예외가 발생한다")
	fun getList_withNonMember_throwsCircleAccessDenied() {
		assertThatThrownBy { scheduleQueryService.getList(CIRCLE_ID, MEMBER_ID, cursor = null, size = 20) }
			.isInstanceOf(BusinessException::class.java)
			.extracting { (it as BusinessException).errorCode }
			.isEqualTo(ScheduleErrorCode.CIRCLE_ACCESS_DENIED)
	}

	@Test
	@DisplayName("페이지 크기가 허용 범위를 벗어나면 INVALID_INPUT 예외가 발생한다")
	fun getList_withInvalidSize_throwsInvalidInput() {
		circleAccessOutPort.seedMember(CIRCLE_ID, MEMBER_ID)

		assertThatThrownBy { scheduleQueryService.getList(CIRCLE_ID, MEMBER_ID, cursor = null, size = 51) }
			.isInstanceOf(BusinessException::class.java)
			.extracting { (it as BusinessException).errorCode }
			.isEqualTo(CommonErrorCode.INVALID_INPUT)
	}

	@Test
	@DisplayName("없는 일정 상세 조회 시 SCHEDULE_NOT_FOUND 예외가 발생한다")
	fun getDetail_withMissingSchedule_throwsScheduleNotFound() {
		circleAccessOutPort.seedMember(CIRCLE_ID, MEMBER_ID)

		assertThatThrownBy { scheduleQueryService.getDetail(ScheduleId.of("SC999999999999999"), CIRCLE_ID, MEMBER_ID) }
			.isInstanceOf(BusinessException::class.java)
			.extracting { (it as BusinessException).errorCode }
			.isEqualTo(ScheduleErrorCode.SCHEDULE_NOT_FOUND)
	}

	private fun schedule(
		id: ScheduleId,
		startDate: LocalDate = LocalDate.now().plusDays(1),
		needConfirm: Boolean = true,
		createdBy: String = "author",
	): Schedule =
		Schedule.reconstitute(
			id = id,
			circleId = CIRCLE_ID,
			title = "제주도 여행",
			startDate = startDate,
			endDate = startDate,
			startTime = LocalTime.of(9, 0),
			endTime = LocalTime.of(18, 0),
			needConfirm = needConfirm,
			memo = null,
			createdBy = createdBy,
			updatedBy = createdBy,
			createdAt = LocalDateTime.of(2024, 7, 1, 10, 0),
			updatedAt = LocalDateTime.of(2024, 7, 1, 10, 0),
			isDeleted = false,
		)

	private fun confirmation(
		id: Long,
		scheduleId: ScheduleId,
		memberId: String,
		confirmationType: ConfirmationType,
		createdBy: String,
	): ScheduleConfirmation =
		ScheduleConfirmation.reconstitute(
			id = id,
			scheduleId = scheduleId,
			memberId = memberId,
			confirmationType = confirmationType,
			createdBy = createdBy,
			updatedBy = createdBy,
			createdAt = LocalDateTime.of(2024, 7, 1, 10, 0),
			updatedAt = LocalDateTime.of(2024, 7, 1, 10, 0),
		)

	private class FakeScheduleOutPort : ScheduleOutPort {
		private val store = linkedMapOf<ScheduleId, Schedule>()

		fun seed(schedule: Schedule) {
			store[schedule.id] = schedule
		}

		override fun save(schedule: Schedule): Schedule {
			store[schedule.id] = schedule
			return schedule
		}

		override fun findById(scheduleId: ScheduleId): Schedule? = store[scheduleId]

		override fun findActiveByIdAndCircleId(scheduleId: ScheduleId, circleId: String): Schedule? =
			store[scheduleId]?.takeIf { it.circleId == circleId && !it.isDeleted }

		override fun findActiveByCircleId(
			circleId: String,
			cursor: SchedulePageCursor?,
			size: Int,
		): List<Schedule> =
			store.values
				.filter { it.circleId == circleId && !it.isDeleted }
				.sortedWith(compareBy<Schedule> { it.startDate }.thenBy { it.startTime ?: LocalTime.MIDNIGHT }.thenBy { it.id.value })
				.dropWhile { cursor != null && !isAfterCursor(it, cursor) }
				.take(size)

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
			startTime: LocalTime,
			createdBefore: java.time.LocalDateTime,
		): List<Schedule> = error("not used")

		private fun isAfterCursor(schedule: Schedule, cursor: SchedulePageCursor): Boolean {
			val dateCompare = schedule.startDate.compareTo(cursor.startDate)
			if (dateCompare != 0) return dateCompare > 0
			val timeCompare = (schedule.startTime ?: LocalTime.MIDNIGHT).compareTo(cursor.startTime ?: LocalTime.MIDNIGHT)
			if (timeCompare != 0) return timeCompare > 0
			return schedule.id.value > cursor.scheduleId.value
		}
	}

	private class FakeScheduleConfirmationOutPort : ScheduleConfirmationOutPort {
		private val store = mutableListOf<ScheduleConfirmation>()

		fun seed(confirmation: ScheduleConfirmation) {
			store += confirmation
		}

		override fun findById(id: Long): ScheduleConfirmation? =
			store.firstOrNull { it.id == id }

		override fun findByScheduleIdAndMemberId(scheduleId: ScheduleId, memberId: String): ScheduleConfirmation? =
			store.firstOrNull { it.scheduleId == scheduleId && it.memberId == memberId }

		override fun save(confirmation: ScheduleConfirmation): ScheduleConfirmation {
			store += confirmation
			return confirmation
		}

		override fun deleteById(id: Long) {
			store.removeIf { it.id == id }
		}

		override fun countGroupByType(scheduleId: ScheduleId): List<ConfirmationCountResult> =
			store
				.filter { it.scheduleId == scheduleId }
				.groupingBy { it.confirmationType }
				.eachCount()
				.map { (type, count) -> ConfirmationCountResult(type, count) }

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
		private const val MEMBER_ID = "member-1"
		private val SCHEDULE_ID_1 = ScheduleId.of("SC202407070000000001")
		private val SCHEDULE_ID_2 = ScheduleId.of("SC202407070000000002")
	}
}
