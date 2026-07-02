package com.unicorn.server.infrastructure.adapter.out.persistence.circle.entity

import com.unicorn.server.domain.circle.Circle
import com.unicorn.server.domain.circle.CircleMember
import com.unicorn.server.domain.circle.vo.CircleId
import com.unicorn.server.domain.circle.vo.CircleMemberId
import com.unicorn.server.domain.member.vo.MemberId


// ========== CircleEntity ↔ Circle ==========

/**
 * Circle 도메인 객체를 CircleEntity로 변환한다.
 */
fun Circle.toEntity(): CircleEntity = CircleEntity().apply {
	id = this@toEntity.id.toString()
	name = this@toEntity.name
	ownerId = this@toEntity.ownerId.toString()
	delYn = if (this@toEntity.deleted) "Y" else "N"
	createdAt = this@toEntity.createdAt
	updatedAt = this@toEntity.updatedAt
}

/**
 * CircleEntity를 Circle 도메인 객체로 복원한다.
 */
fun CircleEntity.toDomain(): Circle = Circle(
	id = CircleId.of(this.id),
	name = this.name,
	ownerId = MemberId.of(this.ownerId),
	deleted = this.delYn == "Y",
	createdAt = requireNotNull(this.createdAt),
	updatedAt = requireNotNull(this.updatedAt),
)

// ========== CircleMemberEntity ↔ CircleMember ==========

/**
 * CircleMember 도메인 객체를 CircleMemberEntity로 변환한다.
 */
fun CircleMember.toEntity(): CircleMemberEntity = CircleMemberEntity().apply {
	id = this@toEntity.id.toString()
	circleId = this@toEntity.circleId.toString()
	memberId = this@toEntity.memberId.toString()
	nickname = this@toEntity.nickname
	role = this@toEntity.role
	status = this@toEntity.status
	joinedAt = this@toEntity.joinedAt
	leftAt = this@toEntity.leftAt
	delYn = if (this@toEntity.deleted) "Y" else "N"
	createdAt = this@toEntity.createdAt
	updatedAt = this@toEntity.updatedAt
}

/**
 * CircleMemberEntity를 CircleMember 도메인 객체로 복원한다.
 */
fun CircleMemberEntity.toDomain(): CircleMember = CircleMember(
	id = CircleMemberId.of(this.id),
	circleId = CircleId.of(this.circleId),
	memberId = MemberId.of(this.memberId),
	nickname = this.nickname,
	role = this.role,
	status = this.status,
	joinedAt = this.joinedAt,
	leftAt = this.leftAt,
	deleted = this.delYn == "Y",
	createdAt = requireNotNull(this.createdAt),
	updatedAt = requireNotNull(this.updatedAt),
)
