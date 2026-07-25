package com.unicorn.server.domain.member.port.`in`

import com.unicorn.server.domain.member.port.dto.MemberProfileDto

interface MemberProfileInPort {
	fun getMemberProfile(memberId: String): MemberProfileDto?
}
