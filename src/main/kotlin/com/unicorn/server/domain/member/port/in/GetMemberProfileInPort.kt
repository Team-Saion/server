package com.unicorn.server.domain.member.port.`in`

import com.unicorn.server.domain.member.port.dto.MemberProfileDto

interface GetMemberProfileInPort {
	fun getMemberProfile(memberId: String): MemberProfileDto?
}
