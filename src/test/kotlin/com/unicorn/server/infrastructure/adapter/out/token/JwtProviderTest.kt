package com.unicorn.server.infrastructure.adapter.out.token

import com.unicorn.server.domain.member.enums.Role
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("JwtProvider 단위 테스트")
class JwtProviderTest {

	@Test
	@DisplayName("정상 refresh token을 파싱하면 멤버 ID를 반환한다")
	fun parseRefreshToken_withValidRefreshToken_returnsMemberId() {
		val jwtProvider = jwtProvider()
		val tokenPair = jwtProvider.issue(MEMBER_ID, Role.MEMBER)

		val result = jwtProvider.parseRefreshToken(tokenPair.refreshToken)

		assertThat(result).isEqualTo(MEMBER_ID)
	}

	@Test
	@DisplayName("access token을 refresh token으로 파싱하면 null을 반환한다")
	fun parseRefreshToken_withAccessToken_returnsNull() {
		val jwtProvider = jwtProvider()
		val tokenPair = jwtProvider.issue(MEMBER_ID, Role.MEMBER)

		val result = jwtProvider.parseRefreshToken(tokenPair.accessToken)

		assertThat(result).isNull()
	}

	@Test
	@DisplayName("위조된 refresh token을 파싱하면 null을 반환한다")
	fun parseRefreshToken_withForgedRefreshToken_returnsNull() {
		val jwtProvider = jwtProvider()
		val refreshToken = jwtProvider.issue(MEMBER_ID, Role.MEMBER).refreshToken
		val forgedToken = refreshToken.dropLast(1) + if (refreshToken.last() == 'a') "b" else "a"

		val result = jwtProvider.parseRefreshToken(forgedToken)

		assertThat(result).isNull()
	}

	@Test
	@DisplayName("만료된 refresh token을 파싱하면 null을 반환한다")
	fun parseRefreshToken_withExpiredRefreshToken_returnsNull() {
		val jwtProvider = jwtProvider(refreshTokenExpiration = -1)
		val refreshToken = jwtProvider.issue(MEMBER_ID, Role.MEMBER).refreshToken

		val result = jwtProvider.parseRefreshToken(refreshToken)

		assertThat(result).isNull()
	}

	private fun jwtProvider(refreshTokenExpiration: Long = 3600): JwtProvider =
		JwtProvider(
			secretKey = SECRET_KEY,
			accessTokenExpiration = 3600,
			refreshTokenExpiration = refreshTokenExpiration,
		)

	companion object {
		private const val MEMBER_ID = "MB20260101000000001"
		private const val SECRET_KEY = "test-secret-key-for-jwt-signing-must-be-at-least-32-bytes!!"
	}
}
