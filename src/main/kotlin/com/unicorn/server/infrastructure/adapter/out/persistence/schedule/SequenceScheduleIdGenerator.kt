package com.unicorn.server.infrastructure.adapter.out.persistence.schedule

import com.unicorn.server.domain.schedule.port.out.ScheduleIdGenerator
import com.unicorn.server.domain.schedule.vo.ScheduleId
import com.unicorn.server.infrastructure.persistence.sequence.SequenceGenerator
import org.springframework.stereotype.Component

@Component
class SequenceScheduleIdGenerator(
	private val sequenceGenerator: SequenceGenerator,
) : ScheduleIdGenerator {
	override fun next(): ScheduleId = ScheduleId.generate(sequenceGenerator.nextValue("schedule_seq"))
}
