package com.unicorn.server.infrastructure.adapter.out.persistence.schedule

import com.unicorn.server.common.annotation.PersistenceAdapter
import com.unicorn.server.domain.schedule.Schedule
import com.unicorn.server.domain.schedule.port.dto.SchedulePageCursor
import com.unicorn.server.domain.schedule.port.out.ScheduleOutPort
import com.unicorn.server.domain.schedule.vo.ScheduleId
import com.unicorn.server.infrastructure.adapter.out.persistence.schedule.entity.ScheduleEntity
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
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
				cursorId = cursor.scheduleId.value,
				size = size,
			)
		}

		return entities.map { it.toDomain() }
	}

	@Transactional(readOnly = true)
	override fun findActiveByStartDateAndCreatedBefore(
		startDate: LocalDate,
		createdBefore: LocalDateTime,
	): List<Schedule> =
		scheduleJpaRepository.findActiveByStartDateAndCreatedBefore(startDate, createdBefore).map { it.toDomain() }

	@Transactional(readOnly = true)
	override fun findActiveAllDayByStartDateAndCreatedBefore(
		startDate: LocalDate,
		createdBefore: LocalDateTime,
	): List<Schedule> =
		scheduleJpaRepository.findActiveAllDayByStartDateAndCreatedBefore(startDate, createdBefore).map { it.toDomain() }

	@Transactional(readOnly = true)
	override fun findActiveTimedByStartAtAndCreatedBefore(
		startDate: LocalDate,
		startTime: LocalTime,
		createdBefore: LocalDateTime,
	): List<Schedule> =
		scheduleJpaRepository.findActiveTimedByStartAtAndCreatedBefore(startDate, startTime, createdBefore).map { it.toDomain() }

	@Transactional(readOnly = true)
	override fun findActiveConfirmationRequiredCreatedBetween(
		createdFrom: LocalDateTime,
		createdBefore: LocalDateTime,
	): List<Schedule> =
		scheduleJpaRepository.findActiveConfirmationRequiredCreatedBetween(createdFrom, createdBefore).map { it.toDomain() }
}
