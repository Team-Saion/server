package com.unicorn.server.domain.term.service

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.domain.member.vo.MemberId
import com.unicorn.server.domain.term.MemberTerm
import com.unicorn.server.domain.term.exception.TermErrorCode
import com.unicorn.server.domain.term.port.`in`.AgreeTermsInPort
import com.unicorn.server.domain.term.port.dto.AgreeTermsCommand
import com.unicorn.server.domain.term.port.out.MemberTermOutPort
import com.unicorn.server.domain.term.port.out.TermOutPort
import com.unicorn.server.domain.term.vo.TermId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class TermAgreementService(
	private val termOutPort: TermOutPort,
	private val memberTermOutPort: MemberTermOutPort,
) : AgreeTermsInPort {

	override fun agreeTerms(command: AgreeTermsCommand) {
		val activeTerms = termOutPort.findAllEffectiveAsOf(LocalDateTime.now())
			.groupBy { it.termCode }
			.mapNotNull { (_, versions) -> versions.maxByOrNull { it.version } }

		val requiredTermIds = activeTerms.filter { it.required }.map { it.id }
		val agreedTermIds = command.termIds.map { TermId.of(it) }

		if (!agreedTermIds.containsAll(requiredTermIds)) {
			throw BusinessException(TermErrorCode.REQUIRED_TERMS_NOT_AGREED)
		}

		val memberId = MemberId.of(command.memberId)
		val memberTerms = agreedTermIds.map { termId -> MemberTerm.create(memberId, termId) }
		memberTermOutPort.saveAll(memberTerms)
	}
}
