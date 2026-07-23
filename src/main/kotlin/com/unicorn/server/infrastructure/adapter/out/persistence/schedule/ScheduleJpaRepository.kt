package com.unicorn.server.infrastructure.adapter.out.persistence.schedule

import com.unicorn.server.infrastructure.adapter.out.persistence.schedule.entity.ScheduleEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

interface ScheduleJpaRepository : JpaRepository<ScheduleEntity, String> {

	fun findByIdAndCircleIdAndDelYn(id: String, circleId: String, delYn: String = "N"): ScheduleEntity?

	@Query(
		value = """
			SELECT *
			FROM schedule
			WHERE circle_id = :circleId
			  AND del_yn = 'N'
			ORDER BY start_date ASC,
			         start_time_sort ASC,
			         schedule_id ASC
			LIMIT :size
		""",
		nativeQuery = true,
	)
	fun findFirstPage(
		@Param("circleId") circleId: String,
		@Param("size") size: Int,
	): List<ScheduleEntity>

	@Query(
		value = """
			SELECT *
			FROM schedule
			WHERE circle_id = :circleId
			  AND del_yn = 'N'
			  AND (
			    start_date > :cursorDate
			    OR (start_date = :cursorDate AND start_time_sort > :cursorTime)
			    OR (start_date = :cursorDate AND start_time_sort = :cursorTime AND schedule_id > :cursorId)
			  )
			ORDER BY start_date ASC,
			         start_time_sort ASC,
			         schedule_id ASC
			LIMIT :size
		""",
		nativeQuery = true,
	)
	fun findAfterCursor(
		@Param("circleId") circleId: String,
		@Param("cursorDate") cursorDate: LocalDate,
		@Param("cursorTime") cursorTime: LocalTime,
		@Param("cursorId") cursorId: String,
		@Param("size") size: Int,
	): List<ScheduleEntity>

	@Query(
		value = """
			SELECT *
			FROM schedule
			WHERE del_yn = 'N'
			  AND start_date = :startDate
			  AND created_at < :createdBefore
		""",
		nativeQuery = true,
	)
	fun findActiveByStartDateAndCreatedBefore(
		@Param("startDate") startDate: LocalDate,
		@Param("createdBefore") createdBefore: LocalDateTime,
	): List<ScheduleEntity>

	@Query(
		value = """
			SELECT *
			FROM schedule
			WHERE del_yn = 'N'
			  AND start_date = :startDate
			  AND start_time IS NULL
			  AND created_at < :createdBefore
		""",
		nativeQuery = true,
	)
	fun findActiveAllDayByStartDateAndCreatedBefore(
		@Param("startDate") startDate: LocalDate,
		@Param("createdBefore") createdBefore: LocalDateTime,
	): List<ScheduleEntity>

	@Query(
		value = """
			SELECT *
			FROM schedule
			WHERE del_yn = 'N'
			  AND start_date = :startDate
			  AND start_time = :startTime
			  AND created_at < :createdBefore
		""",
		nativeQuery = true,
	)
	fun findActiveTimedByStartAtAndCreatedBefore(
		@Param("startDate") startDate: LocalDate,
		@Param("startTime") startTime: LocalTime,
		@Param("createdBefore") createdBefore: LocalDateTime,
	): List<ScheduleEntity>

	@Query(
		value = """
			SELECT *
			FROM schedule
			WHERE del_yn = 'N'
			  AND need_confirm = 'Y'
			  AND created_at >= :createdFrom
			  AND created_at < :createdBefore
		""",
		nativeQuery = true,
	)
	fun findActiveConfirmationRequiredCreatedBetween(
		@Param("createdFrom") createdFrom: LocalDateTime,
		@Param("createdBefore") createdBefore: LocalDateTime,
	): List<ScheduleEntity>
}
