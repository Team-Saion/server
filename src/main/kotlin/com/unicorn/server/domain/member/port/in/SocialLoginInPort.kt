package com.unicorn.server.domain.member.port.`in`

import com.unicorn.server.domain.member.port.dto.SocialLoginCommand
import com.unicorn.server.domain.member.port.dto.TokenPair

// SocialLoginInPort - 플랫폼별 토큰 검증 이후 공통 소셜 로그인 처리를 정의한다.
// 현재 HTTP 진입점은 KakaoLoginInPort이며, 이 포트는 테스트와 플랫폼 확장 시 공통 흐름 검증에 사용한다.
interface SocialLoginInPort {
	// 검증된 소셜 로그인 명령을 처리하고 서비스 토큰을 발급한다.
	fun login(command: SocialLoginCommand): TokenPair
}
