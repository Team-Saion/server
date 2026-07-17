package com.unicorn.server.infrastructure.adapter.out.persistence.schedule

import com.unicorn.server.common.annotation.PersistenceAdapter
import com.unicorn.server.domain.schedule.Schedule
import com.unicorn.server.domain.schedule.port.dto.SchedulePageCursor
import com.unicorn.server.domain.schedule.port.out.ScheduleOutPort
import com.unicorn.server.domain.schedule.vo.ScheduleId
import com.unicorn.server.infrastructure.adapter.out.persistence.schedule.entity.ScheduleEntity
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalTime

@PersistenceAdapter
class SchedulePersistenceAdapter(
	private val scheduleJpaRepository: ScheduleJpaRepository,
) : ScheduleOutPort {

	@Transactional
	override fun save(schedule: Schedule): Schedule {
		val entity = scheduleJpaRepository.findById(schedule.id.value)
			.map { it.apply { update(schedule) } }
			.orElseGet { ScheduleEntity(schedule) }

		return scheduleJpaRepository.save(entity).toDomain()
	}

	@Transactional(readOnly = true)
	override fun findById(scheduleId: ScheduleId): Schedule? =
		scheduleJpaRepository.findById(scheduleId.value)
			.map { it.toDomain() }
			.orElse(null)

	@Transactional(readOnly = true)
	override fun findActiveByIdAndCircleId(scheduleId: ScheduleId, circleId: String): Schedule? =
		scheduleJpaRepository.findByIdAndCircleIdAndDelYn(scheduleId.value, circleId)?.toDomain()

	@Transactional(readOnly = true)
	override fun findActiveByCircleId(
		circleId: String,
		today: LocalDate,
		cursor: SchedulePageCursor?,
		size: Int,
	): List<Schedule> {
		val entities = if (cursor == null) {
			scheduleJpaRepository.findFirstPage(circleId, today, size)
		} else {
			scheduleJpaRepository.findAfterCursor(
				circleId = circleId,
				today = today,
				cursorDate = cursor.startDate,
				cursorTime = cursor.startTime ?: LocalTime.MIDNIGHT,
				cursorId = cursor.scheduleId.value,
				size = size,
			)
		}

		return entities.map { it.toDomain() }
	}

	@Transactional(readOnly = true)
	override fun findUpcomingByCircleId(
		circleId: String,
		today: LocalDate,
		limit: Int,
	): List<Schedule> =
		scheduleJpaRepository.findUpcoming(circleId, today, limit).map { it.toDomain() }

	@Transactional(readOnly = true)
	override fun countActiveByCircleId(circleId: String): Long =
		scheduleJpaRepository.countByCircleIdAndDelYn(circleId)
}
