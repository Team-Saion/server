package com.unicorn.server.infrastructure.adapter.out.persistence.circle

import com.unicorn.server.domain.circle.enums.CircleMemberStatus
import com.unicorn.server.infrastructure.adapter.out.persistence.circle.entity.CircleMemberEntity
import org.springframework.data.jpa.repository.JpaRepository

interface CircleMemberJpaRepository : JpaRepository<CircleMemberEntity, String> {
	fun findByCircleIdAndMemberId(circleId: String, memberId: String): CircleMemberEntity?
	fun findAllByCircleIdAndStatusAndDelYn(circleId: String, status: CircleMemberStatus, delYn: String): List<CircleMemberEntity>
	fun findFirstByCircleIdAndStatusAndDelYnAndMemberIdNotOrderByJoinedAtAscMemberIdAsc(
		circleId: String,
		status: CircleMemberStatus,
		delYn: String,
		excludedMemberId: String,
	): CircleMemberEntity?
	fun findAllByMemberIdAndStatusAndDelYn(memberId: String, status: CircleMemberStatus, delYn: String): List<CircleMemberEntity>
	fun existsByCircleIdAndMemberId(circleId: String, memberId: String): Boolean
	fun existsByCircleIdAndMemberIdAndStatusAndDelYn(
		circleId: String,
		memberId: String,
		status: CircleMemberStatus,
		delYn: String,
	): Boolean
	fun countByCircleIdAndStatusAndDelYn(circleId: String, status: CircleMemberStatus, delYn: String): Long
}
