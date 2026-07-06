package com.unicorn.server.domain.schedule

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.domain.schedule.enums.ScheduleStatus
import com.unicorn.server.domain.schedule.exception.ScheduleErrorCode
import com.unicorn.server.domain.schedule.port.dto.SchedulePageCursor
import com.unicorn.server.domain.schedule.vo.ScheduleId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@DisplayName("Schedule 도메인 단위 테스트")
class ScheduleTest {

	@Test
	@DisplayName("정상적인 일정 생성 시 지정된 ID와 생성자 정보가 세팅된다")
	fun create_withValidValues_setsInitialState() {
		val schedule = createSchedule()

		assertThat(schedule.id).isEqualTo(TEST_SCHEDULE_ID)
		assertThat(schedule.createdBy).isEqualTo("member-1")
		assertThat(schedule.updatedBy).isEqualTo("member-1")
		assertThat(schedule.isDeleted).isFalse()
	}

	@Test
	@DisplayName("시간이 없는 일정은 종일 일정이다")
	fun create_withoutTimes_isAllDay() {
		val schedule = createSchedule(startTime = null, endTime = null)

		assertThat(schedule.isAllDay).isTrue()
	}

	@Test
	@DisplayName("빈 제목으로 일정 생성 시 BLANK_TITLE 예외가 발생한다")
	fun create_withEmptyTitle_throwsBlankTitle() {
		assertThatThrownBy { createSchedule(title = "") }
			.isInstanceOf(BusinessException::class.java)
			.extracting("errorCode")
			.isEqualTo(ScheduleErrorCode.BLANK_TITLE)
	}

	@Test
	@DisplayName("공백만 있는 제목으로 일정 생성 시 WHITESPACE_ONLY_TITLE 예외가 발생한다")
	fun create_withWhitespaceOnlyTitle_throwsWhitespaceOnlyTitle() {
		assertThatThrownBy { createSchedule(title = "   ") }
			.isInstanceOf(BusinessException::class.java)
			.extracting("errorCode")
			.isEqualTo(ScheduleErrorCode.WHITESPACE_ONLY_TITLE)
	}

	@Test
	@DisplayName("제목이 30자를 초과하면 TITLE_TOO_LONG 예외가 발생한다")
	fun create_withTooLongTitle_throwsTitleTooLong() {
		assertThatThrownBy { createSchedule(title = "a".repeat(31)) }
			.isInstanceOf(BusinessException::class.java)
			.extracting("errorCode")
			.isEqualTo(ScheduleErrorCode.TITLE_TOO_LONG)
	}

	@Test
	@DisplayName("종료일이 시작일보다 빠르면 END_DATE_BEFORE_START_DATE 예외가 발생한다")
	fun create_withEndDateBeforeStartDate_throwsEndDateBeforeStartDate() {
		assertThatThrownBy {
			createSchedule(
				startDate = LocalDate.of(2024, 8, 3),
				endDate = LocalDate.of(2024, 8, 1),
			)
		}
			.isInstanceOf(BusinessException::class.java)
			.extracting("errorCode")
			.isEqualTo(ScheduleErrorCode.END_DATE_BEFORE_START_DATE)
	}

	@Test
	@DisplayName("시작시간만 있으면 MISSING_END_TIME 예외가 발생한다")
	fun create_withOnlyStartTime_throwsMissingEndTime() {
		assertThatThrownBy {
			createSchedule(
				startTime = LocalTime.of(9, 0),
				endTime = null,
			)
		}
			.isInstanceOf(BusinessException::class.java)
			.extracting("errorCode")
			.isEqualTo(ScheduleErrorCode.MISSING_END_TIME)
	}

	@Test
	@DisplayName("종료시간만 있으면 MISSING_START_TIME 예외가 발생한다")
	fun create_withOnlyEndTime_throwsMissingStartTime() {
		assertThatThrownBy {
			createSchedule(
				startTime = null,
				endTime = LocalTime.of(18, 0),
			)
		}
			.isInstanceOf(BusinessException::class.java)
			.extracting("errorCode")
			.isEqualTo(ScheduleErrorCode.MISSING_START_TIME)
	}

	@Test
	@DisplayName("같은 날짜에서 종료시간이 시작시간보다 이후가 아니면 END_TIME_NOT_AFTER_START_TIME 예외가 발생한다")
	fun create_withInvalidSameDayTimeRange_throwsEndTimeNotAfterStartTime() {
		assertThatThrownBy {
			createSchedule(
				startDate = LocalDate.of(2024, 8, 1),
				endDate = LocalDate.of(2024, 8, 1),
				startTime = LocalTime.of(9, 0),
				endTime = LocalTime.of(9, 0),
			)
		}
			.isInstanceOf(BusinessException::class.java)
			.extracting("errorCode")
			.isEqualTo(ScheduleErrorCode.END_TIME_NOT_AFTER_START_TIME)
	}

	@Test
	@DisplayName("메모가 500자를 초과하면 MEMO_TOO_LONG 예외가 발생한다")
	fun create_withTooLongMemo_throwsMemoTooLong() {
		assertThatThrownBy { createSchedule(memo = "a".repeat(501)) }
			.isInstanceOf(BusinessException::class.java)
			.extracting("errorCode")
			.isEqualTo(ScheduleErrorCode.MEMO_TOO_LONG)
	}

	@Test
	@DisplayName("현재 시각이 시작 전이면 UPCOMING 상태를 반환한다")
	fun computeStatus_beforeStart_returnsUpcoming() {
		val schedule = createSchedule()

		val status = schedule.computeStatus(LocalDateTime.of(2024, 7, 31, 23, 59))

		assertThat(status).isEqualTo(ScheduleStatus.UPCOMING)
	}

	@Test
	@DisplayName("현재 시각이 기간 안에 있으면 IN_PROGRESS 상태를 반환한다")
	fun computeStatus_duringSchedule_returnsInProgress() {
		val schedule = createSchedule()

		val status = schedule.computeStatus(LocalDateTime.of(2024, 8, 1, 12, 0))

		assertThat(status).isEqualTo(ScheduleStatus.IN_PROGRESS)
	}

	@Test
	@DisplayName("현재 시각이 종료 후이면 COMPLETED 상태를 반환한다")
	fun computeStatus_afterEnd_returnsCompleted() {
		val schedule = createSchedule()

		val status = schedule.computeStatus(LocalDateTime.of(2024, 8, 2, 0, 0))

		assertThat(status).isEqualTo(ScheduleStatus.COMPLETED)
	}

	@Test
	@DisplayName("D-Day는 시작일까지 남은 일수를 반환한다")
	fun computeDDay_beforeStart_returnsRemainingDays() {
		val schedule = createSchedule(startDate = LocalDate.of(2024, 8, 10), endDate = LocalDate.of(2024, 8, 12))

		val dDay = schedule.computeDDay(LocalDate.of(2024, 8, 1))

		assertThat(dDay).isEqualTo(9)
	}

	@Test
	@DisplayName("진행 중인 기간 일정의 D-Day는 null이다")
	fun computeDDay_duringPeriod_returnsNull() {
		val schedule = createSchedule(startDate = LocalDate.of(2024, 8, 1), endDate = LocalDate.of(2024, 8, 3))

		val dDay = schedule.computeDDay(LocalDate.of(2024, 8, 2))

		assertThat(dDay).isNull()
	}

	@Test
	@DisplayName("진행률은 경과 시간 비율을 정수로 반환한다")
	fun computeProgressRate_duringSchedule_returnsElapsedPercent() {
		val schedule = createSchedule(
			startDate = LocalDate.of(2024, 8, 1),
			endDate = LocalDate.of(2024, 8, 1),
			startTime = LocalTime.of(9, 0),
			endTime = LocalTime.of(19, 0),
		)

		val progressRate = schedule.computeProgressRate(LocalDateTime.of(2024, 8, 1, 14, 0))

		assertThat(progressRate).isEqualTo(50)
	}

	@Test
	@DisplayName("delete 호출 시 삭제 상태와 수정자가 변경된다")
	fun delete_marksDeletedAndUpdatesModifier() {
		val schedule = createSchedule()

		schedule.delete("member-2")

		assertThat(schedule.isDeleted).isTrue()
		assertThat(schedule.updatedBy).isEqualTo("member-2")
	}

	@Test
	@DisplayName("커서는 일정 정렬 키를 인코딩하고 디코딩할 수 있다")
	fun schedulePageCursor_encodeAndDecode_roundTrips() {
		val schedule = Schedule.reconstitute(
			id = TEST_SCHEDULE_ID,
			circleId = "CC202506010000000001",
			title = "제주도 여행",
			startDate = LocalDate.of(2024, 8, 1),
			endDate = LocalDate.of(2024, 8, 3),
			startTime = LocalTime.of(9, 0),
			endTime = LocalTime.of(18, 0),
			needConfirm = true,
			memo = null,
			createdBy = "member-1",
			updatedBy = "member-1",
			createdAt = LocalDateTime.of(2024, 7, 1, 10, 0),
			updatedAt = LocalDateTime.of(2024, 7, 1, 10, 0),
			isDeleted = false,
		)

		val decoded = SchedulePageCursor.decode(SchedulePageCursor.from(schedule).encode())

		assertThat(decoded).isEqualTo(
			SchedulePageCursor(
				startDate = LocalDate.of(2024, 8, 1),
				startTime = LocalTime.of(9, 0),
				scheduleId = TEST_SCHEDULE_ID,
			),
		)
	}

	private fun createSchedule(
		title: String = "제주도 여행",
		startDate: LocalDate = LocalDate.of(2024, 8, 1),
		endDate: LocalDate = LocalDate.of(2024, 8, 1),
		startTime: LocalTime? = LocalTime.of(9, 0),
		endTime: LocalTime? = LocalTime.of(18, 0),
		needConfirm: Boolean = true,
		memo: String? = "숙소 체크인 15시",
	): Schedule = Schedule.create(
		id = TEST_SCHEDULE_ID,
		circleId = "CC202506010000000001",
		title = title,
		startDate = startDate,
		endDate = endDate,
		startTime = startTime,
		endTime = endTime,
		needConfirm = needConfirm,
		memo = memo,
		createdBy = "member-1",
	)

	companion object {
		private val TEST_SCHEDULE_ID = ScheduleId.of("SC202407070000000001")
	}
}
