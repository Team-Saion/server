package com.unicorn.server.infrastructure.adapter.out.persistence.schedule

import com.unicorn.server.domain.schedule.enums.ConfirmationType
import com.unicorn.server.infrastructure.adapter.out.persistence.schedule.entity.ScheduleConfirmationJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ScheduleConfirmationJpaRepository : JpaRepository<ScheduleConfirmationJpaEntity, Long> {

	fun findByScheduleIdAndMemberId(scheduleId: Long, memberId: String): ScheduleConfirmationJpaEntity?

	@Query(
		"""
			SELECT c.confirmationType AS type, COUNT(c) AS count
			FROM ScheduleConfirmationJpaEntity c
			WHERE c.scheduleId = :scheduleId
			GROUP BY c.confirmationType
		""",
	)
	fun countGroupByType(@Param("scheduleId") scheduleId: Long): List<ConfirmationTypeCountProjection>

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("DELETE FROM ScheduleConfirmationJpaEntity c WHERE c.scheduleId = :scheduleId")
	fun deleteAllByScheduleId(@Param("scheduleId") scheduleId: Long)
}

interface ConfirmationTypeCountProjection {
	fun getType(): ConfirmationType

	fun getCount(): Long
}
