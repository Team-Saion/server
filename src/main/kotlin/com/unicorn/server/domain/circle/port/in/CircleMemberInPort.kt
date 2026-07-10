package com.unicorn.server.domain.circle.port.`in`

import com.unicorn.server.domain.circle.port.dto.CircleMemberDto
import com.unicorn.server.domain.circle.port.dto.CircleSummary
import com.unicorn.server.domain.circle.port.dto.JoinCircleResult

interface CircleMemberInPort {
	fun join(circleId: String, memberId: String): JoinCircleResult
	fun getCircleMembers(circleId: String): List<CircleMemberDto>
	fun isCircleMember(circleId: String, memberId: String): Boolean
	fun transferInitiator(circleId: String, currentInitiatorId: String, newInitiatorId: String): CircleSummary
	fun handleMemberWithdrawal(memberId: String)
}
