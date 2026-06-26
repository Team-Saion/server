package com.unicorn.server.domain.member.service

import com.unicorn.server.domain.member.exception.MemberNotFoundException
import com.unicorn.server.domain.member.exception.WithdrawnMemberException
import com.unicorn.server.domain.member.port.`in`.CompleteOnboardingInPort
import com.unicorn.server.domain.member.port.dto.CompleteOnboardingCommand
import com.unicorn.server.domain.member.port.dto.TokenPair
import com.unicorn.server.domain.member.port.out.MemberOutPort
import com.unicorn.server.domain.member.port.out.TokenIssuer
import com.unicorn.server.domain.member.port.out.TokenStore
import com.unicorn.server.domain.member.vo.MemberId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class OnboardingService(
	private val memberOutPort: MemberOutPort,
	private val tokenIssuer: TokenIssuer,
	private val tokenStore: TokenStore,
) : CompleteOnboardingInPort {

	override fun completeOnboarding(memberId: String, command: CompleteOnboardingCommand): TokenPair {
		val member = memberOutPort.findById(MemberId.of(memberId))
			?: throw MemberNotFoundException(memberId)
		if (member.isDeleted()) throw WithdrawnMemberException(memberId)

		member.completeOnboarding(command.nickname)
		val savedMember = memberOutPort.save(member)
		val tokenPair = tokenIssuer.issue(savedMember.id.toString(), savedMember.role)
		tokenStore.save(savedMember.id.toString(), tokenPair.refreshToken)
		return tokenPair
	}
}
