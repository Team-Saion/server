package com.unicorn.server.domain.member.service

import com.unicorn.server.domain.member.enums.SocialProvider
import com.unicorn.server.domain.member.port.`in`.SocialLoginInPort
import com.unicorn.server.domain.member.port.dto.KakaoUserInfo
import com.unicorn.server.domain.member.port.dto.SocialLoginCommand
import com.unicorn.server.domain.member.port.dto.TokenPair
import com.unicorn.server.domain.member.port.out.KakaoAuthPort
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("SocialLoginService 단위 테스트")
class SocialLoginServiceTest {

	private val kakaoAuthPort = FakeKakaoAuthPort()
	private val socialLoginInPort = RecordingSocialLoginInPort()
	private val socialLoginService = SocialLoginService(kakaoAuthPort, socialLoginInPort)

	@Test
	@DisplayName("kakaoLogin 호출 시 카카오 토큰 검증 후 공통 소셜 로그인으로 위임한다")
	fun kakaoLogin_delegatesToSocialLogin() {
		val result = socialLoginService.kakaoLogin("id-token")

		assertThat(result.accessToken).isEqualTo("access-token")
		assertThat(result.refreshToken).isEqualTo("refresh-token")
		assertThat(socialLoginInPort.command).isEqualTo(
			SocialLoginCommand(
				provider = SocialProvider.KAKAO,
				providerId = "fake-kakao-id",
				email = "fake@example.com",
				name = "가짜유저",
			),
		)
	}

	private class FakeKakaoAuthPort : KakaoAuthPort {
		override fun verify(idToken: String): KakaoUserInfo =
			KakaoUserInfo(
				providerId = "fake-kakao-id",
				email = "fake@example.com",
				name = "가짜유저",
			)
	}

	private class RecordingSocialLoginInPort : SocialLoginInPort {
		var command: SocialLoginCommand? = null

		override fun login(command: SocialLoginCommand): TokenPair {
			this.command = command
			return TokenPair("access-token", "refresh-token")
		}
	}
}
