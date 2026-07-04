package com.unicorn.server.domain.schedule

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.domain.schedule.enums.ScheduleStatus
import com.unicorn.server.domain.schedule.exception.ScheduleErrorCode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

class Schedule private constructor(
	val id: Long,
	val circleId: Long,
	title: String,
	startDate: LocalDate,
	endDate: LocalDate,
	startTime: LocalTime?,
	endTime: LocalTime?,
	needConfirm: Boolean,
	memo: String?,
	val createdBy: String,
	updatedBy: String,
	val createdAt: LocalDateTime,
	updatedAt: LocalDateTime,
	isDeleted: Boolean,
) {
	var title: String = title
		private set

	var startDate: LocalDate = startDate
		private set

	var endDate: LocalDate = endDate
		private set

	var startTime: LocalTime? = startTime
		private set

	var endTime: LocalTime? = endTime
		private set

	var needConfirm: Boolean = needConfirm
		private set

	var memo: String? = memo
		private set

	var updatedBy: String = updatedBy
		private set

	var updatedAt: LocalDateTime = updatedAt
		private set

	var isDeleted: Boolean = isDeleted
		private set

	val isAllDay: Boolean
		get() = startTime == null && endTime == null

	fun update(
		title: String?,
		startDate: LocalDate?,
		endDate: LocalDate?,
		startTime: LocalTime?,
		endTime: LocalTime?,
		needConfirm: Boolean,
		memo: String?,
		updatedBy: String,
	) {
		val newTitle = title ?: this.title
		val newStartDate = startDate ?: this.startDate
		val newEndDate = endDate ?: this.endDate
		val newStartTime = startTime ?: this.startTime
		val newEndTime = endTime ?: this.endTime

		validate(newTitle, newStartDate, newEndDate, newStartTime, newEndTime, memo)

		this.title = newTitle
		this.startDate = newStartDate
		this.endDate = newEndDate
		this.startTime = newStartTime
		this.endTime = newEndTime
		this.needConfirm = needConfirm
		this.memo = memo
		this.updatedBy = updatedBy
		this.updatedAt = LocalDateTime.now()
	}

	fun delete(deletedBy: String) {
		isDeleted = true
		updatedBy = deletedBy
		updatedAt = LocalDateTime.now()
	}

	fun computeStatus(now: LocalDateTime): ScheduleStatus {
		val startDateTime = startDateTime()
		val endDateTime = endDateTime()

		return when {
			now.isBefore(startDateTime) -> ScheduleStatus.UPCOMING
			now.isAfter(endDateTime) -> ScheduleStatus.COMPLETED
			else -> ScheduleStatus.IN_PROGRESS
		}
	}

	fun computeDDay(today: LocalDate): Int? {
		if (endDate.isBefore(today)) return null
		if (startDate.isBefore(today)) return null
		return ChronoUnit.DAYS.between(today, startDate).toInt()
	}

	fun computeProgressRate(now: LocalDateTime): Int {
		val startDateTime = startDateTime()
		val endDateTime = endDateTime()

		return when {
			now.isBefore(startDateTime) -> 0
			now.isAfter(endDateTime) -> 100
			else -> {
				val totalSeconds = ChronoUnit.SECONDS.between(startDateTime, endDateTime)
				val elapsedSeconds = ChronoUnit.SECONDS.between(startDateTime, now)
				if (totalSeconds == 0L) {
					100
				} else {
					((elapsedSeconds.toDouble() / totalSeconds) * 100).toInt().coerceIn(0, 100)
				}
			}
		}
	}

	private fun startDateTime(): LocalDateTime =
		LocalDateTime.of(startDate, startTime ?: LocalTime.of(0, 0))

	private fun endDateTime(): LocalDateTime =
		LocalDateTime.of(endDate, endTime ?: LocalTime.of(23, 59))

	companion object {
		private const val UNSAVED_ID = 0L
		private const val MAX_TITLE_LENGTH = 30
		private const val MAX_MEMO_LENGTH = 500

		fun create(
			circleId: Long,
			title: String,
			startDate: LocalDate,
			endDate: LocalDate,
			startTime: LocalTime?,
			endTime: LocalTime?,
			needConfirm: Boolean,
			memo: String?,
			createdBy: String,
		): Schedule {
			validate(title, startDate, endDate, startTime, endTime, memo)

			val now = LocalDateTime.now()
			return Schedule(
				id = UNSAVED_ID,
				circleId = circleId,
				title = title,
				startDate = startDate,
				endDate = endDate,
				startTime = startTime,
				endTime = endTime,
				needConfirm = needConfirm,
				memo = memo,
				createdBy = createdBy,
				updatedBy = createdBy,
				createdAt = now,
				updatedAt = now,
				isDeleted = false,
			)
		}

		fun reconstitute(
			id: Long,
			circleId: Long,
			title: String,
			startDate: LocalDate,
			endDate: LocalDate,
			startTime: LocalTime?,
			endTime: LocalTime?,
			needConfirm: Boolean,
			memo: String?,
			createdBy: String,
			updatedBy: String,
			createdAt: LocalDateTime,
			updatedAt: LocalDateTime,
			isDeleted: Boolean,
		): Schedule = Schedule(
			id = id,
			circleId = circleId,
			title = title,
			startDate = startDate,
			endDate = endDate,
			startTime = startTime,
			endTime = endTime,
			needConfirm = needConfirm,
			memo = memo,
			createdBy = createdBy,
			updatedBy = updatedBy,
			createdAt = createdAt,
			updatedAt = updatedAt,
			isDeleted = isDeleted,
		)

		private fun validate(
			title: String,
			startDate: LocalDate,
			endDate: LocalDate,
			startTime: LocalTime?,
			endTime: LocalTime?,
			memo: String?,
		) {
			if (title.isEmpty()) throw BusinessException(ScheduleErrorCode.BLANK_TITLE)
			if (title.isBlank()) throw BusinessException(ScheduleErrorCode.WHITESPACE_ONLY_TITLE)
			if (title.length > MAX_TITLE_LENGTH) throw BusinessException(ScheduleErrorCode.TITLE_TOO_LONG)
			if (endDate.isBefore(startDate)) throw BusinessException(ScheduleErrorCode.END_DATE_BEFORE_START_DATE)
			if (startTime == null && endTime != null) throw BusinessException(ScheduleErrorCode.MISSING_START_TIME)
			if (startTime != null && endTime == null) throw BusinessException(ScheduleErrorCode.MISSING_END_TIME)
			if (
				startDate == endDate &&
				startTime != null &&
				endTime != null &&
				!endTime.isAfter(startTime)
			) {
				throw BusinessException(ScheduleErrorCode.END_TIME_NOT_AFTER_START_TIME)
			}
			if (memo != null && memo.length > MAX_MEMO_LENGTH) throw BusinessException(ScheduleErrorCode.MEMO_TOO_LONG)
		}
	}
}
