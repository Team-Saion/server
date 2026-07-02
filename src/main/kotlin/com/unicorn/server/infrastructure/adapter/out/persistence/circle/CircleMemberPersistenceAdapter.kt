package com.unicorn.server.infrastructure.adapter.out.persistence.circle

import com.unicorn.server.common.annotation.PersistenceAdapter
import com.unicorn.server.domain.circle.CircleMember
import com.unicorn.server.domain.circle.enums.CircleMemberStatus
import com.unicorn.server.domain.circle.port.out.CircleMemberOutPort
import com.unicorn.server.domain.circle.vo.CircleId
import com.unicorn.server.domain.member.vo.MemberId
import com.unicorn.server.infrastructure.adapter.out.persistence.circle.entity.toDomain
import com.unicorn.server.infrastructure.adapter.out.persistence.circle.entity.toEntity
import org.springframework.transaction.annotation.Transactional

@PersistenceAdapter
class CircleMemberPersistenceAdapter(
	private val circleMemberJpaRepository: CircleMemberJpaRepository,
) : CircleMemberOutPort {
	@Transactional
	override fun save(circleMember: CircleMember): CircleMember {
		val entity = circleMember.toEntity()
		return circleMemberJpaRepository.save(entity).toDomain()
	}

	@Transactional(readOnly = true)
	override fun findByCircleAndMember(circleId: CircleId, memberId: MemberId): CircleMember? =
		circleMemberJpaRepository.findByCircleIdAndMemberId(circleId.toString(), memberId.toString())?.toDomain()

	@Transactional(readOnly = true)
	override fun findAllActiveByCircleId(circleId: CircleId): List<CircleMember> =
		circleMemberJpaRepository.findAllByCircleIdAndStatusAndDelYn(circleId.toString(), CircleMemberStatus.ACTIVE, "N")
			.map { it.toDomain() }

	@Transactional(readOnly = true)
	override fun existsByCircleAndMember(circleId: CircleId, memberId: MemberId): Boolean =
		circleMemberJpaRepository.existsByCircleIdAndMemberId(circleId.toString(), memberId.toString())

	@Transactional(readOnly = true)
	override fun countActiveByCircleId(circleId: CircleId): Long =
		circleMemberJpaRepository.countByCircleIdAndStatusAndDelYn(circleId.toString(), CircleMemberStatus.ACTIVE, "N")
}
