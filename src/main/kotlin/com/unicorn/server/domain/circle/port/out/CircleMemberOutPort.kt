package com.unicorn.server.domain.circle.port.out

import com.unicorn.server.domain.circle.CircleMember
import com.unicorn.server.domain.circle.vo.CircleId
import com.unicorn.server.domain.member.vo.MemberId

interface CircleMemberOutPort {
	fun save(circleMember: CircleMember): CircleMember
	fun findByCircleAndMember(circleId: CircleId, memberId: MemberId): CircleMember?
	fun findAllActiveByCircleId(circleId: CircleId): List<CircleMember>
	fun existsByCircleAndMember(circleId: CircleId, memberId: MemberId): Boolean
	fun countActiveByCircleId(circleId: CircleId): Long
}
