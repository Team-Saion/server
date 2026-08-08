package com.unicorn.server.domain.member.service

import com.unicorn.server.domain.member.enums.SocialProvider
import com.unicorn.server.domain.member.port.`in`.MemberSocialLoginInPort
import com.unicorn.server.domain.member.port.dto.AppleUserInfo
import com.unicorn.server.domain.member.port.dto.KakaoUserInfo
import com.unicorn.server.domain.member.port.dto.SocialLoginCommand
import com.unicorn.server.domain.member.port.dto.SocialLoginResult
import com.unicorn.server.domain.member.port.dto.TokenPair
import com.unicorn.server.domain.member.port.out.MemberAppleAuthOutPort
import com.unicorn.server.domain.member.port.out.MemberKakaoAuthOutPort
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("SocialLoginService 단위 테스트")
class SocialLoginServiceTest {

	private val kakaoAuthPort = FakeKakaoAuthPort()
	private val appleAuthPort = FakeAppleAuthPort()
	private val socialLoginInPort = RecordingSocialLoginInPort()
	private val socialLoginService = SocialLoginService(kakaoAuthPort, appleAuthPort, socialLoginInPort)

	@Test
	@DisplayName("kakaoLogin 호출 시 카카오 토큰 검증 후 공통 소셜 로그인으로 위임한다")
	fun kakaoLogin_delegatesToSocialLogin() {
		val result = socialLoginService.kakaoLogin("id-token")

		assertThat(result.tokenPair.accessToken).isEqualTo("access-token")
		assertThat(result.tokenPair.refreshToken).isEqualTo("refresh-token")
		assertThat(result.isNewMember).isFalse()
		assertThat(socialLoginInPort.command).isEqualTo(
			SocialLoginCommand(
				provider = SocialProvider.KAKAO,
				providerId = "fake-kakao-id",
				email = "fake@example.com",
				name = null,
				socialNickname = "가짜유저",
				socialProfileImageUrl = "https://example.com/profile.png",
			),
		)
	}

	@Test
	@DisplayName("appleLogin 호출 시 애플 토큰 검증 후 공통 소셜 로그인으로 위임한다")
	fun appleLogin_delegatesToSocialLogin() {
		val result = socialLoginService.appleLogin("apple-id-token")

		assertThat(result.tokenPair.accessToken).isEqualTo("access-token")
		assertThat(result.tokenPair.refreshToken).isEqualTo("refresh-token")
		assertThat(result.isNewMember).isFalse()
		assertThat(socialLoginInPort.command).isEqualTo(
			SocialLoginCommand(
				provider = SocialProvider.APPLE,
				providerId = "fake-apple-id",
				email = "fake-apple@example.com",
				name = null,
				socialNickname = null,
				socialProfileImageUrl = null,
			),
		)
	}

	private class FakeKakaoAuthPort : MemberKakaoAuthOutPort {
		override fun verify(idToken: String): KakaoUserInfo =
			KakaoUserInfo(
				providerId = "fake-kakao-id",
				email = "fake@example.com",
				name = "가짜유저",
				profileImageUrl = "https://example.com/profile.png",
			)
	}

	private class FakeAppleAuthPort : MemberAppleAuthOutPort {
		override fun verify(idToken: String): AppleUserInfo =
			AppleUserInfo(
				providerId = "fake-apple-id",
				email = "fake-apple@example.com",
			)
	}

	private class RecordingSocialLoginInPort : MemberSocialLoginInPort {
		var command: SocialLoginCommand? = null

		override fun login(command: SocialLoginCommand): SocialLoginResult {
			this.command = command
			return SocialLoginResult(TokenPair("access-token", "refresh-token"), isNewMember = false)
		}
	}
}
