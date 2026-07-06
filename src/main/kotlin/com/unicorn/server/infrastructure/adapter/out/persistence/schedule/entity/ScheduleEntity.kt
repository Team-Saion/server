package com.unicorn.server.infrastructure.adapter.out.persistence.schedule.entity

import com.unicorn.server.domain.schedule.Schedule
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

// JpaConfig의 AuditorAware가 "system"을 반환하므로 작성자 권한 검증에 필요한 memberId를 보존하기 위해 감사 컬럼을 직접 매핑한다.
@Entity
@Table(
	name = "tb_schedule",
	indexes = [
		Index(name = "idx_schedule_circle_sort", columnList = "circle_id, del_yn, start_date, start_time, schedule_id"),
	],
)
class ScheduleEntity protected constructor() {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "schedule_id", nullable = false)
	var id: Long? = null
		protected set

	@Column(name = "circle_id", nullable = false, length = 21)
	var circleId: String = ""
		protected set

	@Column(name = "title", nullable = false, length = 30)
	var title: String = ""
		protected set

	@Column(name = "start_date", nullable = false)
	lateinit var startDate: LocalDate
		protected set

	@Column(name = "end_date", nullable = false)
	lateinit var endDate: LocalDate
		protected set

	@Column(name = "start_time")
	var startTime: LocalTime? = null
		protected set

	@Column(name = "end_time")
	var endTime: LocalTime? = null
		protected set

	@Column(name = "need_confirm", nullable = false, length = 1)
	var needConfirm: String = "N"
		protected set

	@Column(name = "memo", length = 500)
	var memo: String? = null
		protected set

	@Column(name = "del_yn", nullable = false, length = 1)
	var delYn: String = "N"
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

	constructor(schedule: Schedule) : this() {
		if (schedule.id != UNSAVED_ID) {
			id = schedule.id
		}
		applyDomain(schedule)
		createdAt = schedule.createdAt
		createdBy = schedule.createdBy
	}

	fun update(schedule: Schedule) {
		applyDomain(schedule)
	}

	fun toDomain(): Schedule = Schedule.reconstitute(
		id = requireNotNull(id) { "schedule_id must not be null" },
		circleId = circleId,
		title = title,
		startDate = startDate,
		endDate = endDate,
		startTime = startTime,
		endTime = endTime,
		needConfirm = needConfirm == "Y",
		memo = memo,
		createdBy = requireNotNull(createdBy) { "createdBy must not be null" },
		updatedBy = requireNotNull(updatedBy) { "updatedBy must not be null" },
		createdAt = requireNotNull(createdAt) { "createdAt must not be null" },
		updatedAt = requireNotNull(updatedAt) { "updatedAt must not be null" },
		isDeleted = delYn == "Y",
	)

	private fun applyDomain(schedule: Schedule) {
		circleId = schedule.circleId
		title = schedule.title
		startDate = schedule.startDate
		endDate = schedule.endDate
		startTime = schedule.startTime
		endTime = schedule.endTime
		needConfirm = if (schedule.needConfirm) "Y" else "N"
		memo = schedule.memo
		delYn = if (schedule.isDeleted) "Y" else "N"
		updatedAt = schedule.updatedAt
		updatedBy = schedule.updatedBy
	}

	companion object {
		private const val UNSAVED_ID = 0L
	}
}
