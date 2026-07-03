package com.unicorn.server.infrastructure.adapter.out.persistence.circle

import com.unicorn.server.common.annotation.PersistenceAdapter
import com.unicorn.server.domain.circle.Circle
import com.unicorn.server.domain.circle.port.out.CircleOutPort
import com.unicorn.server.domain.circle.vo.CircleId
import com.unicorn.server.domain.member.vo.MemberId
import com.unicorn.server.infrastructure.adapter.out.persistence.circle.entity.toDomain
import com.unicorn.server.infrastructure.adapter.out.persistence.circle.entity.toEntity
import org.springframework.transaction.annotation.Transactional

@PersistenceAdapter
class CirclePersistenceAdapter(
	private val circleJpaRepository: CircleJpaRepository,
) : CircleOutPort {
	@Transactional
	override fun save(circle: Circle): Circle {
		val entity = circle.toEntity()
		return circleJpaRepository.save(entity).toDomain()
	}

	@Transactional(readOnly = true)
	override fun findById(circleId: CircleId): Circle? =
		circleJpaRepository.findById(circleId.toString()).map { it.toDomain() }.orElse(null)

	@Transactional(readOnly = true)
	override fun findAllByOwnerId(ownerId: MemberId): List<Circle> =
		circleJpaRepository.findAllByOwnerId(ownerId.toString()).map { it.toDomain() }
}
