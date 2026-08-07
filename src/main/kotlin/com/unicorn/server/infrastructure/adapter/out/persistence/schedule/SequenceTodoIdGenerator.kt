package com.unicorn.server.infrastructure.adapter.out.persistence.schedule

import com.unicorn.server.domain.schedule.port.out.TodoIdGenerator
import com.unicorn.server.domain.schedule.vo.TodoId
import com.unicorn.server.infrastructure.persistence.sequence.SequenceGenerator
import org.springframework.stereotype.Component

@Component
class SequenceTodoIdGenerator(
	private val sequenceGenerator: SequenceGenerator,
) : TodoIdGenerator {
	override fun next(): TodoId = TodoId.generate(sequenceGenerator.nextValue("todo_seq"))
}
