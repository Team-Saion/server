package com.unicorn.server.infrastructure.adapter.out.persistence.member

import com.unicorn.server.domain.member.port.out.MemberIdGenerator
import com.unicorn.server.domain.member.vo.MemberId
import com.unicorn.server.infrastructure.persistence.sequence.SequenceGenerator
import org.springframework.stereotype.Component

@Component
class SequenceMemberIdGenerator(
	private val sequenceGenerator: SequenceGenerator,
) : MemberIdGenerator {
	override fun next(): MemberId = MemberId.generate(sequenceGenerator.nextValue("member_seq"))
}
