package com.unicorn.server.infrastructure.adapter.out.persistence.schedule

import com.unicorn.server.domain.schedule.Schedule
import com.unicorn.server.domain.schedule.port.dto.SchedulePageCursor
import com.unicorn.server.domain.schedule.port.out.ScheduleIdGenerator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("SchedulePersistenceAdapter 통합 테스트")
class SchedulePersistenceAdapterTest(
	@param:Autowired private val schedulePersistenceAdapter: SchedulePersistenceAdapter,
	@param:Autowired private val scheduleIdGenerator: ScheduleIdGenerator,
) {

	@Test
	@DisplayName("일정을 저장하고 도메인으로 복원한다")
	fun save_persistsAndReturnsDomain() {
		val schedule = schedule(circleId = "CC000000000000000101", title = "제주도 여행")

		val result = schedulePersistenceAdapter.save(schedule)

		assertThat(result.id.value).startsWith("SC")
		assertThat(result.circleId).isEqualTo("CC000000000000000101")
		assertThat(result.title).isEqualTo("제주도 여행")
		assertThat(result.createdBy).isEqualTo("member-1")
		assertThat(result.updatedBy).isEqualTo("member-1")
	}

	@Test
	@DisplayName("기존 일정을 저장하면 같은 row를 갱신한다")
	fun save_withExistingSchedule_updatesRow() {
		val saved = schedulePersistenceAdapter.save(schedule(circleId = "CC000000000000000102", title = "제주도 여행"))
		saved.update(
			title = "수정된 제목",
			startDate = null,
			endDate = null,
			startTime = null,
			endTime = null,
			startTimeProvided = true,
			endTimeProvided = true,
			needConfirm = false,
			memo = null,
			memoProvided = true,
			updatedBy = "member-2",
		)

		val result = schedulePersistenceAdapter.save(saved)

		assertThat(result.id).isEqualTo(saved.id)
		assertThat(result.title).isEqualTo("수정된 제목")
		assertThat(result.isAllDay).isTrue()
		assertThat(result.needConfirm).isFalse()
		assertThat(result.memo).isNull()
		assertThat(result.updatedBy).isEqualTo("member-2")
	}

	@Test
	@DisplayName("삭제되지 않은 일정만 circleId와 scheduleId로 조회한다")
	fun findActiveByIdAndCircleId_excludesDeletedSchedule() {
		val saved = schedulePersistenceAdapter.save(schedule(circleId = "CC000000000000000103", title = "제주도 여행"))
		saved.delete("member-1")
		schedulePersistenceAdapter.save(saved)

		val result = schedulePersistenceAdapter.findActiveByIdAndCircleId(saved.id, "CC000000000000000103")

		assertThat(result).isNull()
	}

	@Test
	@DisplayName("커서 이후 일정을 정렬 순서대로 조회한다")
	fun findActiveByCircleId_withCursor_returnsSchedulesAfterCursor() {
		val first = schedulePersistenceAdapter.save(
			schedule(circleId = "CC000000000000000104", title = "첫 번째", startDate = LocalDate.of(2024, 8, 1), startTime = null),
		)
		val second = schedulePersistenceAdapter.save(
			schedule(circleId = "CC000000000000000104", title = "두 번째", startDate = LocalDate.of(2024, 8, 2), startTime = LocalTime.of(9, 0)),
		)
		val third = schedulePersistenceAdapter.save(
			schedule(circleId = "CC000000000000000104", title = "세 번째", startDate = LocalDate.of(2024, 8, 3), startTime = LocalTime.of(9, 0)),
		)

		val firstPage = schedulePersistenceAdapter.findActiveByCircleId("CC000000000000000104", null, 2)
		val secondPage = schedulePersistenceAdapter.findActiveByCircleId("CC000000000000000104", SchedulePageCursor.from(second), 2)

		assertThat(firstPage.map { it.id }).containsExactly(first.id, second.id)
		assertThat(secondPage.map { it.id }).containsExactly(third.id)
	}

	private fun schedule(
		circleId: String,
		title: String,
		startDate: LocalDate = LocalDate.of(2024, 8, 1),
		startTime: LocalTime? = LocalTime.of(9, 0),
	): Schedule =
		Schedule.create(
			id = scheduleIdGenerator.next(),
			circleId = circleId,
			title = title,
			startDate = startDate,
			endDate = startDate,
			startTime = startTime,
			endTime = startTime?.plusHours(1),
			needConfirm = true,
			memo = "메모",
			createdBy = "member-1",
		)
}
