package com.unicorn.server.infrastructure.adapter.out.persistence.schedule

import com.unicorn.server.common.annotation.PersistenceAdapter
import com.unicorn.server.domain.schedule.Schedule
import com.unicorn.server.domain.schedule.port.dto.SchedulePageCursor
import com.unicorn.server.domain.schedule.port.out.ScheduleOutPort
import com.unicorn.server.infrastructure.adapter.out.persistence.schedule.entity.ScheduleEntity
import org.springframework.transaction.annotation.Transactional
import java.time.LocalTime

@PersistenceAdapter
class SchedulePersistenceAdapter(
	private val scheduleJpaRepository: ScheduleJpaRepository,
) : ScheduleOutPort {

	@Transactional
	override fun save(schedule: Schedule): Schedule {
		val entity = if (schedule.id == UNSAVED_ID) {
			ScheduleEntity(schedule)
		} else {
			scheduleJpaRepository.findById(schedule.id)
				.map { it.apply { update(schedule) } }
				.orElseGet { ScheduleEntity(schedule) }
		}

		return scheduleJpaRepository.save(entity).toDomain()
	}

	@Transactional(readOnly = true)
	override fun findById(scheduleId: Long): Schedule? =
		scheduleJpaRepository.findById(scheduleId)
			.map { it.toDomain() }
			.orElse(null)

	@Transactional(readOnly = true)
	override fun findActiveByIdAndCircleId(scheduleId: Long, circleId: String): Schedule? =
		scheduleJpaRepository.findByIdAndCircleIdAndDelYn(scheduleId, circleId)?.toDomain()

	@Transactional(readOnly = true)
	override fun findActiveByCircleId(
		circleId: String,
		cursor: SchedulePageCursor?,
		size: Int,
	): List<Schedule> {
		val entities = if (cursor == null) {
			scheduleJpaRepository.findFirstPage(circleId, size)
		} else {
			scheduleJpaRepository.findAfterCursor(
				circleId = circleId,
				cursorDate = cursor.startDate,
				cursorTime = cursor.startTime ?: LocalTime.MIDNIGHT,
				cursorId = cursor.scheduleId,
				size = size,
			)
		}

		return entities.map { it.toDomain() }
	}

	companion object {
		private const val UNSAVED_ID = 0L
	}
}
