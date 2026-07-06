package com.unicorn.server.infrastructure.adapter.out.persistence.schedule

import com.unicorn.server.infrastructure.adapter.out.persistence.schedule.entity.ScheduleEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.time.LocalTime

interface ScheduleJpaRepository : JpaRepository<ScheduleEntity, Long> {

	fun findByIdAndCircleIdAndDelYn(id: Long, circleId: String, delYn: String = "N"): ScheduleEntity?

	@Query(
		value = """
			SELECT *
			FROM schedule
			WHERE circle_id = :circleId
			  AND del_yn = 'N'
			ORDER BY start_date ASC,
			         COALESCE(start_time, TIME '00:00:00') ASC,
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
			    OR (start_date = :cursorDate AND COALESCE(start_time, TIME '00:00:00') > :cursorTime)
			    OR (start_date = :cursorDate AND COALESCE(start_time, TIME '00:00:00') = :cursorTime AND schedule_id > :cursorId)
			  )
			ORDER BY start_date ASC,
			         COALESCE(start_time, TIME '00:00:00') ASC,
			         schedule_id ASC
			LIMIT :size
		""",
		nativeQuery = true,
	)
	fun findAfterCursor(
		@Param("circleId") circleId: String,
		@Param("cursorDate") cursorDate: LocalDate,
		@Param("cursorTime") cursorTime: LocalTime,
		@Param("cursorId") cursorId: Long,
		@Param("size") size: Int,
	): List<ScheduleEntity>
}
