package com.unicorn.server.domain.member.port.`in`

import com.unicorn.server.domain.member.Member
import com.unicorn.server.domain.member.enums.MemberStatus
import com.unicorn.server.domain.member.enums.Role

// UpdateMemberStateInPort - 임시로 멤버 상태/역할을 강제 변경하는 유스케이스 진입점을 정의한다.
interface UpdateMemberStateInPort {
	// 멤버 식별자로 상태와 역할을 강제 변경한다. null인 필드는 변경하지 않는다.
	fun updateMemberState(memberId: String, status: MemberStatus?, role: Role?): Member
}
