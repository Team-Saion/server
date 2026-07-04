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
		scheduleOutPort.seed(schedule(id = 1L, startDate = LocalDate.now().plusDays(1)))
		scheduleOutPort.seed(schedule(id = 2L, startDate = LocalDate.now().plusDays(2)))

		val result = scheduleQueryService.getList(CIRCLE_ID, MEMBER_ID, cursor = null, size = 1)

		assertThat(result.schedules).hasSize(1)
		assertThat(result.hasNext).isTrue()
		assertThat(result.nextCursor).isEqualTo(SchedulePageCursor.from(scheduleOutPort.findById(1L)!!).encode())
	}

	@Test
	@DisplayName("일정 상세 조회 시 확인하기 카운트와 내 확인하기를 포함한다")
	fun getDetail_withNeedConfirm_returnsConfirmationData() {
		circleAccessOutPort.seedMember(CIRCLE_ID, MEMBER_ID)
		scheduleOutPort.seed(schedule(id = 1L, needConfirm = true, createdBy = "author"))
		confirmationOutPort.seed(
			ScheduleConfirmation.create(
				scheduleId = 1L,
				memberId = MEMBER_ID,
				confirmationType = ConfirmationType.CONFIRMED,
				createdBy = MEMBER_ID,
			),
		)

		val result = scheduleQueryService.getDetail(1L, CIRCLE_ID, MEMBER_ID)

		assertThat(result.scheduleId).isEqualTo(1L)
		assertThat(result.confirmations).containsExactly(ConfirmationCountResult(ConfirmationType.CONFIRMED, 1))
		assertThat(result.myConfirmationType).isEqualTo(ConfirmationType.CONFIRMED)
		assertThat(result.createdBy).isEqualTo("author")
	}

	@Test
	@DisplayName("확인하기를 사용하지 않는 일정 상세 조회 시 확인하기 정보는 비어 있다")
	fun getDetail_withoutNeedConfirm_returnsEmptyConfirmationData() {
		circleAccessOutPort.seedMember(CIRCLE_ID, MEMBER_ID)
		scheduleOutPort.seed(schedule(id = 1L, needConfirm = false))

		val result = scheduleQueryService.getDetail(1L, CIRCLE_ID, MEMBER_ID)

		assertThat(result.confirmations).isEmpty()
		assertThat(result.myConfirmationType).isNull()
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

		assertThatThrownBy { scheduleQueryService.getDetail(999L, CIRCLE_ID, MEMBER_ID) }
			.isInstanceOf(BusinessException::class.java)
			.extracting { (it as BusinessException).errorCode }
			.isEqualTo(ScheduleErrorCode.SCHEDULE_NOT_FOUND)
	}

	private fun schedule(
		id: Long,
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

	private class FakeScheduleOutPort : ScheduleOutPort {
		private val store = linkedMapOf<Long, Schedule>()

		fun seed(schedule: Schedule) {
			store[schedule.id] = schedule
		}

		override fun save(schedule: Schedule): Schedule {
			store[schedule.id] = schedule
			return schedule
		}

		override fun findById(scheduleId: Long): Schedule? = store[scheduleId]

		override fun findActiveByIdAndCircleId(scheduleId: Long, circleId: Long): Schedule? =
			store[scheduleId]?.takeIf { it.circleId == circleId && !it.isDeleted }

		override fun findActiveByCircleId(
			circleId: Long,
			cursor: SchedulePageCursor?,
			size: Int,
		): List<Schedule> =
			store.values
				.filter { it.circleId == circleId && !it.isDeleted }
				.sortedWith(compareBy<Schedule> { it.startDate }.thenBy { it.startTime ?: LocalTime.MIDNIGHT }.thenBy { it.id })
				.dropWhile { cursor != null && !isAfterCursor(it, cursor) }
				.take(size)

		private fun isAfterCursor(schedule: Schedule, cursor: SchedulePageCursor): Boolean =
			compareValuesBy(
				schedule,
				cursor,
				{ it.startDate },
				{ it.startTime ?: LocalTime.MIDNIGHT },
				{ it.scheduleId },
			) > 0

		private fun compareValuesBy(
			schedule: Schedule,
			cursor: SchedulePageCursor,
			dateSelector: (Schedule) -> LocalDate,
			timeSelector: (Schedule) -> LocalTime,
			idSelector: (SchedulePageCursor) -> Long,
		): Int {
			val dateCompare = dateSelector(schedule).compareTo(cursor.startDate)
			if (dateCompare != 0) return dateCompare
			val timeCompare = timeSelector(schedule).compareTo(cursor.startTime ?: LocalTime.MIDNIGHT)
			if (timeCompare != 0) return timeCompare
			return schedule.id.compareTo(idSelector(cursor))
		}
	}

	private class FakeScheduleConfirmationOutPort : ScheduleConfirmationOutPort {
		private val store = mutableListOf<ScheduleConfirmation>()

		fun seed(confirmation: ScheduleConfirmation) {
			store += confirmation
		}

		override fun findByScheduleIdAndMemberId(scheduleId: Long, memberId: String): ScheduleConfirmation? =
			store.firstOrNull { it.scheduleId == scheduleId && it.memberId == memberId }

		override fun save(confirmation: ScheduleConfirmation): ScheduleConfirmation {
			store += confirmation
			return confirmation
		}

		override fun countGroupByType(scheduleId: Long): List<ConfirmationCountResult> =
			store
				.filter { it.scheduleId == scheduleId }
				.groupingBy { it.confirmationType }
				.eachCount()
				.map { (type, count) -> ConfirmationCountResult(type, count) }

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
		private const val MEMBER_ID = "member-1"
	}
}
