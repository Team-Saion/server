package com.unicorn.server.domain.member.port.out

import com.unicorn.server.domain.member.enums.Role
import com.unicorn.server.domain.member.port.dto.TokenPair

// TokenIssuer - 멤버 인증 토큰을 발급하는 포트를 정의한다.
interface TokenIssuer {
	fun issue(memberId: String, role: Role): TokenPair
}
