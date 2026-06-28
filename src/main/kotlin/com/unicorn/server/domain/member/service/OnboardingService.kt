package com.unicorn.server.domain.member.service

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.domain.member.exception.MemberNotFoundException
import com.unicorn.server.domain.member.exception.WithdrawnMemberException
import com.unicorn.server.domain.member.port.`in`.CompleteOnboardingInPort
import com.unicorn.server.domain.member.port.dto.CompleteOnboardingCommand
import com.unicorn.server.domain.member.port.dto.TokenPair
import com.unicorn.server.domain.member.port.out.MemberOutPort
import com.unicorn.server.domain.member.port.out.TokenIssuer
import com.unicorn.server.domain.member.port.out.TokenStore
import com.unicorn.server.domain.member.vo.MemberId
import com.unicorn.server.domain.term.exception.TermErrorCode
import com.unicorn.server.domain.term.port.out.MemberTermOutPort
import com.unicorn.server.domain.term.port.out.TermOutPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class OnboardingService(
	private val memberOutPort: MemberOutPort,
	private val termOutPort: TermOutPort,
	private val memberTermOutPort: MemberTermOutPort,
	private val tokenIssuer: TokenIssuer,
	private val tokenStore: TokenStore,
) : CompleteOnboardingInPort {

	override fun completeOnboarding(memberId: String, command: CompleteOnboardingCommand): TokenPair {
		val member = memberOutPort.findById(MemberId.of(memberId))
			?: throw MemberNotFoundException(memberId)
		if (member.isDeleted()) throw WithdrawnMemberException(memberId)

		val requiredTermIds = termOutPort.findAllEffectiveAsOf(LocalDateTime.now())
			.groupBy { it.termCode }
			.mapNotNull { (_, versions) -> versions.maxByOrNull { it.version } }
			.filter { it.required }
			.map { it.id }
			.toSet()
		val agreedTermIds = memberTermOutPort.findAllByMemberId(member.id).map { it.termId }.toSet()
		if (!agreedTermIds.containsAll(requiredTermIds)) {
			throw BusinessException(TermErrorCode.REQUIRED_TERMS_NOT_AGREED)
		}

		member.completeOnboarding(command.nickname)
		val savedMember = memberOutPort.save(member)
		val tokenPair = tokenIssuer.issue(savedMember.id.toString(), savedMember.role)
		tokenStore.save(savedMember.id.toString(), tokenPair.refreshToken)
		return tokenPair
	}
}
