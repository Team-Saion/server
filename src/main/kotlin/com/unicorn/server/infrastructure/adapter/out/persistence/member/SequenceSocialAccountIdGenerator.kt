package com.unicorn.server.infrastructure.adapter.out.persistence.member

import com.unicorn.server.domain.member.port.out.SocialAccountIdGenerator
import com.unicorn.server.domain.member.vo.SocialAccountId
import com.unicorn.server.infrastructure.persistence.sequence.SequenceGenerator
import org.springframework.stereotype.Component

@Component
class SequenceSocialAccountIdGenerator(
	private val sequenceGenerator: SequenceGenerator,
) : SocialAccountIdGenerator {
	override fun next(): SocialAccountId = SocialAccountId.generate(sequenceGenerator.nextValue("social_account_seq"))
}
