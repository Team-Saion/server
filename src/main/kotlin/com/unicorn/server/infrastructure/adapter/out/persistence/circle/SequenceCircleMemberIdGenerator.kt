package com.unicorn.server.infrastructure.adapter.out.persistence.circle

import com.unicorn.server.domain.circle.port.out.CircleMemberIdGenerator
import com.unicorn.server.domain.circle.vo.CircleMemberId
import com.unicorn.server.infrastructure.persistence.sequence.SequenceGenerator
import org.springframework.stereotype.Component

@Component
class SequenceCircleMemberIdGenerator(
	private val sequenceGenerator: SequenceGenerator,
) : CircleMemberIdGenerator {
	override fun next(): CircleMemberId = CircleMemberId.generate(sequenceGenerator.nextValue("circle_member_seq"))
}
