package com.unicorn.server.infrastructure.adapter.out.persistence.schedule

import com.unicorn.server.domain.schedule.Schedule
import com.unicorn.server.domain.schedule.ScheduleConfirmation
import com.unicorn.server.domain.schedule.enums.ConfirmationType
import com.unicorn.server.domain.schedule.port.out.ScheduleIdGenerator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalTime

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ScheduleConfirmationPersistenceAdapter 통합 테스트")
class ScheduleConfirmationPersistenceAdapterTest(
	@param:Autowired private val schedulePersistenceAdapter: SchedulePersistenceAdapter,
	@param:Autowired private val scheduleConfirmationPersistenceAdapter: ScheduleConfirmationPersistenceAdapter,
	@param:Autowired private val scheduleIdGenerator: ScheduleIdGenerator,
) {

	@Test
	@DisplayName("확인하기를 저장하고 도메인으로 복원한다")
	fun save_persistsAndReturnsDomain() {
		val schedule = schedulePersistenceAdapter.save(schedule(circleId = "CC000000000000000201"))
		val confirmation = ScheduleConfirmation.create(
			scheduleId = schedule.id,
			memberId = "member-1",
			confirmationType = ConfirmationType.CONFIRMED,
			createdBy = "member-1",
		)

		val result = scheduleConfirmationPersistenceAdapter.save(confirmation)

		assertThat(result.id).isPositive()
		assertThat(result.scheduleId).isEqualTo(schedule.id)
		assertThat(result.memberId).isEqualTo("member-1")
		assertThat(result.confirmationType).isEqualTo(ConfirmationType.CONFIRMED)
	}

	@Test
	@DisplayName("같은 일정과 멤버의 확인하기를 저장하면 기존 row를 갱신한다")
	fun save_withExistingScheduleAndMember_updatesRow() {
		val schedule = schedulePersistenceAdapter.save(schedule(circleId = "CC000000000000000202"))
		val saved = scheduleConfirmationPersistenceAdapter.save(
			ScheduleConfirmation.create(
				scheduleId = schedule.id,
				memberId = "member-1",
				confirmationType = ConfirmationType.CONFIRMED,
				createdBy = "member-1",
			),
		)
		saved.changeType(ConfirmationType.CANNOT_ATTEND, "member-1")

		val result = scheduleConfirmationPersistenceAdapter.save(saved)

		assertThat(result.id).isEqualTo(saved.id)
		assertThat(result.confirmationType).isEqualTo(ConfirmationType.CANNOT_ATTEND)
	}

	@Test
	@DisplayName("확인하기 타입별 카운트를 조회한다")
	fun countGroupByType_returnsCountsByType() {
		val schedule = schedulePersistenceAdapter.save(schedule(circleId = "CC000000000000000203"))
		scheduleConfirmationPersistenceAdapter.save(
			ScheduleConfirmation.create(schedule.id, "member-1", ConfirmationType.CONFIRMED, "member-1"),
		)
		scheduleConfirmationPersistenceAdapter.save(
			ScheduleConfirmation.create(schedule.id, "member-2", ConfirmationType.CANNOT_ATTEND, "member-2"),
		)

		val result = scheduleConfirmationPersistenceAdapter.countGroupByType(schedule.id)

		assertThat(result).hasSize(2)
		assertThat(result.associate { it.type to it.count })
			.containsEntry(ConfirmationType.CONFIRMED, 1)
			.containsEntry(ConfirmationType.CANNOT_ATTEND, 1)
	}

	@Test
	@DisplayName("일정의 모든 확인하기를 삭제한다")
	fun deleteAllByScheduleId_deletesConfirmations() {
		val schedule = schedulePersistenceAdapter.save(schedule(circleId = "CC000000000000000204"))
		scheduleConfirmationPersistenceAdapter.save(
			ScheduleConfirmation.create(schedule.id, "member-1", ConfirmationType.CONFIRMED, "member-1"),
		)

		scheduleConfirmationPersistenceAdapter.deleteAllByScheduleId(schedule.id)

		assertThat(scheduleConfirmationPersistenceAdapter.findByScheduleIdAndMemberId(schedule.id, "member-1")).isNull()
	}

	private fun schedule(circleId: String): Schedule =
		Schedule.create(
			id = scheduleIdGenerator.next(),
			circleId = circleId,
			title = "제주도 여행",
			startDate = LocalDate.of(2024, 8, 1),
			endDate = LocalDate.of(2024, 8, 1),
			startTime = LocalTime.of(9, 0),
			endTime = LocalTime.of(18, 0),
			needConfirm = true,
			memo = null,
			createdBy = "member-1",
		)
}
