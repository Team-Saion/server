package com.unicorn.server.domain.member.port.`in`

import com.unicorn.server.domain.member.port.dto.SocialLoginCommand
import com.unicorn.server.domain.member.port.dto.TokenPair

// SocialLoginInPort - 소셜 로그인 유스케이스 진입점을 정의한다.
interface SocialLoginInPort {
	// 검증된 소셜 로그인 명령을 처리하고 서비스 토큰을 발급한다.
	fun login(command: SocialLoginCommand): TokenPair
}
