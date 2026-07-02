package com.unicorn.server.infrastructure.adapter.out.persistence.member

import com.unicorn.server.common.annotation.PersistenceAdapter
import com.unicorn.server.common.vo.Email
import com.unicorn.server.domain.member.Member
import com.unicorn.server.domain.member.enums.MemberStatus
import com.unicorn.server.domain.member.port.out.MemberOutPort
import com.unicorn.server.domain.member.vo.MemberId
import com.unicorn.server.infrastructure.adapter.out.persistence.member.entity.toDomain
import com.unicorn.server.infrastructure.adapter.out.persistence.member.entity.toEntity
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

// MemberPersistenceAdapter - MemberOutPort를 JPA 저장소로 구현한다.
@PersistenceAdapter
class MemberPersistenceAdapter(
	private val memberJpaRepository: MemberJpaRepository,
) : MemberOutPort {

	// 멤버를 신규 저장하거나 기존 row에 도메인 변경사항을 반영한다.
	@Transactional
	override fun save(member: Member): Member {
		val entity = member.toEntity()
		return memberJpaRepository.save(entity).toDomain()
	}

	// 멤버 식별자로 멤버를 조회한다.
	@Transactional(readOnly = true)
	override fun findById(memberId: MemberId): Member? =
		memberJpaRepository.findById(memberId.toString())
			.map { it.toDomain() }
			.orElse(null)

	// 이메일로 멤버를 조회한다.
	@Transactional(readOnly = true)
	override fun findByEmail(email: Email): Member? =
		memberJpaRepository.findByEmail(email.value)?.toDomain()

	// 이메일 중복 여부를 조회한다.
	@Transactional(readOnly = true)
	override fun existsByEmail(email: Email): Boolean =
		memberJpaRepository.existsByEmail(email.value)

	// 탈퇴 상태이고 보관 기준 시각보다 오래된 멤버를 조회한다.
	@Transactional(readOnly = true)
	override fun findAllDeletedBefore(threshold: LocalDateTime): List<Member> =
		memberJpaRepository.findAllByStatusAndDeletedAtBefore(MemberStatus.DELETED, threshold)
			.map { it.toDomain() }
}
