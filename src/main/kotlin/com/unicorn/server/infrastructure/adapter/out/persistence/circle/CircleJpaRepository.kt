package com.unicorn.server.infrastructure.adapter.out.persistence.circle

import com.unicorn.server.infrastructure.adapter.out.persistence.circle.entity.CircleEntity
import org.springframework.data.jpa.repository.JpaRepository

interface CircleJpaRepository : JpaRepository<CircleEntity, String> {
	fun findAllByOwnerId(ownerId: String): List<CircleEntity>
}
