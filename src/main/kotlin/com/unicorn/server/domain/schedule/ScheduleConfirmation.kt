package com.unicorn.server.domain.schedule

import com.unicorn.server.domain.schedule.enums.ConfirmationType
import java.time.LocalDateTime

class ScheduleConfirmation private constructor(
	val id: Long,
	val scheduleId: Long,
	val memberId: String,
	confirmationType: ConfirmationType,
	val createdBy: String,
	updatedBy: String,
	val createdAt: LocalDateTime,
	updatedAt: LocalDateTime,
) {
	var confirmationType: ConfirmationType = confirmationType
		private set

	var updatedBy: String = updatedBy
		private set

	var updatedAt: LocalDateTime = updatedAt
		private set

	fun changeType(newType: ConfirmationType, updatedBy: String) {
		confirmationType = newType
		this.updatedBy = updatedBy
		updatedAt = LocalDateTime.now()
	}

	companion object {
		private const val UNSAVED_ID = 0L

		fun create(
			scheduleId: Long,
			memberId: String,
			confirmationType: ConfirmationType,
			createdBy: String,
		): ScheduleConfirmation {
			val now = LocalDateTime.now()
			return ScheduleConfirmation(
				id = UNSAVED_ID,
				scheduleId = scheduleId,
				memberId = memberId,
				confirmationType = confirmationType,
				createdBy = createdBy,
				updatedBy = createdBy,
				createdAt = now,
				updatedAt = now,
			)
		}

		fun reconstitute(
			id: Long,
			scheduleId: Long,
			memberId: String,
			confirmationType: ConfirmationType,
			createdBy: String,
			updatedBy: String,
			createdAt: LocalDateTime,
			updatedAt: LocalDateTime,
		): ScheduleConfirmation = ScheduleConfirmation(
			id = id,
			scheduleId = scheduleId,
			memberId = memberId,
			confirmationType = confirmationType,
			createdBy = createdBy,
			updatedBy = updatedBy,
			createdAt = createdAt,
			updatedAt = updatedAt,
		)
	}
}
