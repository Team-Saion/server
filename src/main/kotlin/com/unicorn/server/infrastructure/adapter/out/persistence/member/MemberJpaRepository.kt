package com.unicorn.server.infrastructure.adapter.out.persistence.member

import com.unicorn.server.domain.member.enums.MemberStatus
import com.unicorn.server.infrastructure.adapter.out.persistence.member.entity.MemberEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

// MemberJpaRepository - member 테이블 접근을 담당하는 Spring Data JPA repository다.
interface MemberJpaRepository : JpaRepository<MemberEntity, String> {
	fun findByEmail(email: String): MemberEntity?
	fun existsByEmail(email: String): Boolean
	fun findAllByStatusAndDeletedAtBefore(status: MemberStatus, threshold: LocalDateTime): List<MemberEntity>
}
