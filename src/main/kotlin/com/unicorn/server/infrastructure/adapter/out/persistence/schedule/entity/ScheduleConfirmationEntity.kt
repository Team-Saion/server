package com.unicorn.server.infrastructure.adapter.out.persistence.schedule.entity

import com.unicorn.server.domain.schedule.ScheduleConfirmation
import com.unicorn.server.domain.schedule.enums.ConfirmationType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

// JpaConfig의 AuditorAware가 "system"을 반환하므로 확인하기 작성자 memberId를 보존하기 위해 감사 컬럼을 직접 매핑한다.
@Entity
@Table(
	name = "tb_schedule_confirmation",
	uniqueConstraints = [
		UniqueConstraint(name = "uq_schedule_confirmation_schedule_member", columnNames = ["schedule_id", "member_id"]),
	],
	indexes = [
		Index(name = "idx_schedule_confirmation_schedule", columnList = "schedule_id"),
	],
)
class ScheduleConfirmationEntity protected constructor() {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "confirmation_id", nullable = false)
	var id: Long? = null
		protected set

	@Column(name = "schedule_id", nullable = false)
	var scheduleId: Long = 0
		protected set

	@Column(name = "member_id", nullable = false, length = 100)
	var memberId: String = ""
		protected set

	@Enumerated(EnumType.STRING)
	@Column(name = "confirmation_type", nullable = false, length = 30)
	lateinit var confirmationType: ConfirmationType
		protected set

	@Column(name = "created_at", nullable = false, updatable = false)
	var createdAt: LocalDateTime? = null
		protected set

	@Column(name = "updated_at")
	var updatedAt: LocalDateTime? = null
		protected set

	@Column(name = "created_by", updatable = false, length = 100)
	var createdBy: String? = null
		protected set

	@Column(name = "updated_by", length = 100)
	var updatedBy: String? = null
		protected set

	constructor(confirmation: ScheduleConfirmation) : this() {
		if (confirmation.id != UNSAVED_ID) {
			id = confirmation.id
		}
		applyDomain(confirmation)
		createdAt = confirmation.createdAt
		createdBy = confirmation.createdBy
	}

	fun update(confirmation: ScheduleConfirmation) {
		applyDomain(confirmation)
	}

	fun toDomain(): ScheduleConfirmation = ScheduleConfirmation.reconstitute(
		id = requireNotNull(id) { "confirmation_id must not be null" },
		scheduleId = scheduleId,
		memberId = memberId,
		confirmationType = confirmationType,
		createdBy = requireNotNull(createdBy) { "createdBy must not be null" },
		updatedBy = requireNotNull(updatedBy) { "updatedBy must not be null" },
		createdAt = requireNotNull(createdAt) { "createdAt must not be null" },
		updatedAt = requireNotNull(updatedAt) { "updatedAt must not be null" },
	)

	private fun applyDomain(confirmation: ScheduleConfirmation) {
		scheduleId = confirmation.scheduleId
		memberId = confirmation.memberId
		confirmationType = confirmation.confirmationType
		updatedAt = confirmation.updatedAt
		updatedBy = confirmation.updatedBy
	}

	companion object {
		private const val UNSAVED_ID = 0L
	}
}
