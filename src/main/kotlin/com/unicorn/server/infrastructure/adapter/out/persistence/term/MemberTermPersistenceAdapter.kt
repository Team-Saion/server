package com.unicorn.server.infrastructure.adapter.out.persistence.term

import com.unicorn.server.common.annotation.PersistenceAdapter
import com.unicorn.server.domain.member.vo.MemberId
import com.unicorn.server.domain.term.MemberTerm
import com.unicorn.server.domain.term.port.out.MemberTermOutPort
import com.unicorn.server.infrastructure.adapter.out.persistence.term.entity.toDomain
import com.unicorn.server.infrastructure.adapter.out.persistence.term.entity.toEntity
import org.springframework.transaction.annotation.Transactional

@PersistenceAdapter
class MemberTermPersistenceAdapter(
	private val memberTermJpaRepository: MemberTermJpaRepository,
) : MemberTermOutPort {

	@Transactional
	override fun saveAll(memberTerms: List<MemberTerm>): List<MemberTerm> =
		memberTermJpaRepository.saveAll(memberTerms.map { it.toEntity() })
			.map { it.toDomain() }

	override fun findAllByMemberId(memberId: MemberId): List<MemberTerm> =
		memberTermJpaRepository.findAllByMemberId(memberId.toString())
			.map { it.toDomain() }
}
