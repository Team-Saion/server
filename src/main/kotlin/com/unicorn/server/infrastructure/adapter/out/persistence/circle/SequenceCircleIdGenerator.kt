package com.unicorn.server.infrastructure.adapter.out.persistence.circle

import com.unicorn.server.domain.circle.port.out.CircleIdGenerator
import com.unicorn.server.domain.circle.vo.CircleId
import com.unicorn.server.infrastructure.persistence.sequence.SequenceGenerator
import org.springframework.stereotype.Component

@Component
class SequenceCircleIdGenerator(
	private val sequenceGenerator: SequenceGenerator,
) : CircleIdGenerator {
	override fun next(): CircleId = CircleId.generate(sequenceGenerator.nextValue("circle_seq"))
}
