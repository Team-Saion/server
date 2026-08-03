package com.unicorn.server.infrastructure.adapter.out.persistence.schedule

import com.unicorn.server.infrastructure.adapter.out.persistence.schedule.entity.TodoEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface TodoJpaRepository : JpaRepository<TodoEntity, String> {
	fun findByIdAndDelYn(id: String, delYn: String = "N"): Optional<TodoEntity>

	fun findAllByScheduleIdAndDelYn(scheduleId: String, delYn: String = "N"): List<TodoEntity>
}
