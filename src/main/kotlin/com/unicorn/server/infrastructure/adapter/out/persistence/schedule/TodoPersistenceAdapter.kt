package com.unicorn.server.infrastructure.adapter.out.persistence.schedule

import com.unicorn.server.common.annotation.PersistenceAdapter
import com.unicorn.server.domain.schedule.Todo
import com.unicorn.server.domain.schedule.port.out.TodoOutPort
import com.unicorn.server.domain.schedule.vo.TodoId
import com.unicorn.server.infrastructure.adapter.out.persistence.schedule.entity.TodoEntity
import org.springframework.transaction.annotation.Transactional

@PersistenceAdapter
class TodoPersistenceAdapter(
	private val todoJpaRepository: TodoJpaRepository,
) : TodoOutPort {
	@Transactional
	override fun save(todo: Todo): Todo {
		val entity = todoJpaRepository.findById(todo.id.value)
			.map { it.apply { update(todo) } }
			.orElseGet { TodoEntity(todo) }

		return todoJpaRepository.save(entity).toDomain()
	}

	@Transactional(readOnly = true)
	override fun findById(todoId: TodoId): Todo? =
		todoJpaRepository.findByIdAndDelYn(todoId.value)
			.map { it.toDomain() }
			.orElse(null)

	@Transactional(readOnly = true)
	override fun findByScheduleId(scheduleId: String): List<Todo> =
		todoJpaRepository.findAllByScheduleIdAndDelYn(scheduleId)
			.map { it.toDomain() }

	@Transactional
	override fun deleteById(todoId: TodoId) {
		todoJpaRepository.findById(todoId.value).ifPresent { it.delete() }
	}
}
