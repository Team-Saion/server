package com.unicorn.server.domain.member.port.`in`

import com.unicorn.server.domain.member.Member

// GetMemberInPort - 멤버 조회 유스케이스 진입점을 정의한다.
interface GetMemberInPort {
	// 멤버 식별자로 멤버를 조회한다.
	fun getById(memberId: String): Member
}
