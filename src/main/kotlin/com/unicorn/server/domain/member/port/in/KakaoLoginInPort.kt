package com.unicorn.server.domain.member.port.`in`

import com.unicorn.server.domain.member.port.dto.SocialLoginResult

// KakaoLoginInPort - 카카오 ID Token 기반 로그인 유스케이스 진입점을 정의한다.
interface KakaoLoginInPort {
	fun kakaoLogin(idToken: String): SocialLoginResult
}