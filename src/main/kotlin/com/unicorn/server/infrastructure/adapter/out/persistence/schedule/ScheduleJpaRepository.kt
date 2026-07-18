package com.unicorn.server.infrastructure.adapter.out.persistence.schedule

import com.unicorn.server.infrastructure.adapter.out.persistence.schedule.entity.ScheduleEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.time.LocalTime

interface ScheduleJpaRepository : JpaRepository<ScheduleEntity, String> {

	fun findByIdAndCircleIdAndDelYn(id: String, circleId: String, delYn: String = "N"): ScheduleEntity?

	@Query(
		value = """
			SELECT *
			FROM schedule
			WHERE circle_id = :circleId
			  AND del_yn = 'N'
			  AND end_date >= :today
			ORDER BY start_date ASC,
			         start_time_sort ASC,
			         schedule_id ASC
			LIMIT :size
		""",
		nativeQuery = true,
	)
	fun findFirstPage(
		@Param("circleId") circleId: String,
		@Param("today") today: LocalDate,
		@Param("size") size: Int,
	): List<ScheduleEntity>

	@Query(
		value = """
			SELECT *
			FROM schedule
			WHERE circle_id = :circleId
			  AND del_yn = 'N'
			  AND end_date >= :today
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
		@Param("today") today: LocalDate,
		@Param("cursorDate") cursorDate: LocalDate,
		@Param("cursorTime") cursorTime: LocalTime,
		@Param("cursorId") cursorId: String,
		@Param("size") size: Int,
	): List<ScheduleEntity>

	// end_date >= today 조건으로 아직 종료되지 않은(예정 + 진행 중) 일정을 임박한 순서로 조회한다.
	@Query(
		value = """
			SELECT *
			FROM schedule
			WHERE circle_id = :circleId
			  AND del_yn = 'N'
			  AND end_date >= :today
			ORDER BY start_date ASC,
			         start_time_sort ASC,
			         schedule_id ASC
			LIMIT :limit
		""",
		nativeQuery = true,
	)
	fun findUpcoming(
		@Param("circleId") circleId: String,
		@Param("today") today: LocalDate,
		@Param("limit") limit: Int,
	): List<ScheduleEntity>

	fun countByCircleIdAndDelYn(circleId: String, delYn: String = "N"): Long
}
