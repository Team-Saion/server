package com.unicorn.server.infrastructure.adapter.out.persistence.circle

import com.unicorn.server.common.annotation.PersistenceAdapter
import com.unicorn.server.domain.circle.enums.CircleMemberStatus
import com.unicorn.server.domain.circle.enums.CircleRole
import com.unicorn.server.domain.schedule.port.out.CircleAccessOutPort
import org.springframework.transaction.annotation.Transactional

@PersistenceAdapter
class CircleAccessPersistenceAdapter(
	private val circleJpaRepository: CircleJpaRepository,
	private val circleMemberJpaRepository: CircleMemberJpaRepository,
) : CircleAccessOutPort {

	@Transactional(readOnly = true)
	override fun existsById(circleId: String): Boolean =
		circleJpaRepository.findById(circleId)
			.map { it.delYn == "N" }
			.orElse(false)

	@Transactional(readOnly = true)
	override fun isMember(circleId: String, memberId: String): Boolean =
		circleMemberJpaRepository.findByCircleIdAndMemberId(circleId, memberId)
			?.let { it.status == CircleMemberStatus.ACTIVE && it.delYn == "N" }
			?: false

	@Transactional(readOnly = true)
	override fun isInitiator(circleId: String, memberId: String): Boolean =
		circleMemberJpaRepository.findByCircleIdAndMemberId(circleId, memberId)
			?.let { it.role == CircleRole.INITIATOR && it.status == CircleMemberStatus.ACTIVE && it.delYn == "N" }
			?: false
}
