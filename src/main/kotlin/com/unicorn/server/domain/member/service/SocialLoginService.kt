package com.unicorn.server.domain.member.service

import com.unicorn.server.domain.member.enums.SocialProvider
import com.unicorn.server.domain.member.port.`in`.KakaoLoginInPort
import com.unicorn.server.domain.member.port.`in`.SocialLoginInPort
import com.unicorn.server.domain.member.port.dto.SocialLoginCommand
import com.unicorn.server.domain.member.port.dto.SocialLoginResult
import com.unicorn.server.domain.member.port.out.KakaoAuthPort
import org.springframework.stereotype.Service

// SocialLoginService - 소셜 플랫폼 토큰 검증 후 공통 소셜 로그인 유스케이스로 위임한다.
@Service
class SocialLoginService(
	private val kakaoAuthPort: KakaoAuthPort,
	private val socialLoginInPort: SocialLoginInPort,
) : KakaoLoginInPort {

	// 카카오 ID Token을 검증하고 공통 소셜 로그인 유스케이스로 넘긴다.
	override fun kakaoLogin(idToken: String): SocialLoginResult {
		val userInfo = kakaoAuthPort.verify(idToken)
		return socialLoginInPort.login(
			SocialLoginCommand(
				provider = SocialProvider.KAKAO,
				providerId = userInfo.providerId,
				email = userInfo.email,
				name = null,
				kakaoNickname = userInfo.name,
				kakaoProfileImageUrl = userInfo.profileImageUrl,
			),
		)
	}
}
