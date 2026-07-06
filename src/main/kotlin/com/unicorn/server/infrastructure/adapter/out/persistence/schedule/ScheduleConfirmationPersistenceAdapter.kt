package com.unicorn.server.infrastructure.adapter.out.persistence.schedule

import com.unicorn.server.common.annotation.PersistenceAdapter
import com.unicorn.server.domain.schedule.ScheduleConfirmation
import com.unicorn.server.domain.schedule.port.dto.ConfirmationCountResult
import com.unicorn.server.domain.schedule.port.out.ScheduleConfirmationOutPort
import com.unicorn.server.domain.schedule.vo.ScheduleId
import com.unicorn.server.infrastructure.adapter.out.persistence.schedule.entity.ScheduleConfirmationEntity
import org.springframework.transaction.annotation.Transactional

@PersistenceAdapter
class ScheduleConfirmationPersistenceAdapter(
	private val scheduleConfirmationJpaRepository: ScheduleConfirmationJpaRepository,
) : ScheduleConfirmationOutPort {

	@Transactional(readOnly = true)
	override fun findByScheduleIdAndMemberId(scheduleId: ScheduleId, memberId: String): ScheduleConfirmation? =
		scheduleConfirmationJpaRepository.findByScheduleIdAndMemberId(scheduleId.value, memberId)?.toDomain()

	@Transactional
	override fun save(confirmation: ScheduleConfirmation): ScheduleConfirmation {
		val entity = if (confirmation.id == UNSAVED_ID) {
			ScheduleConfirmationEntity(confirmation)
		} else {
			scheduleConfirmationJpaRepository.findById(confirmation.id)
				.map { it.apply { update(confirmation) } }
				.orElseGet { ScheduleConfirmationEntity(confirmation) }
		}

		return scheduleConfirmationJpaRepository.save(entity).toDomain()
	}

	@Transactional(readOnly = true)
	override fun countGroupByType(scheduleId: ScheduleId): List<ConfirmationCountResult> =
		scheduleConfirmationJpaRepository.countGroupByType(scheduleId.value)
			.map { ConfirmationCountResult(type = it.getType(), count = it.getCount().toInt()) }

	@Transactional
	override fun deleteAllByScheduleId(scheduleId: ScheduleId) {
		scheduleConfirmationJpaRepository.deleteAllByScheduleId(scheduleId.value)
	}

	companion object {
		private const val UNSAVED_ID = 0L
	}
}
