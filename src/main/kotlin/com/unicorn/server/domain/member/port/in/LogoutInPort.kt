package com.unicorn.server.domain.member.port.`in`

// LogoutInPort - 로그아웃 유스케이스 진입점을 정의한다.
interface LogoutInPort {
	// 멤버의 현재 인증 세션을 무효화한다.
	fun logout(memberId: String)
}
