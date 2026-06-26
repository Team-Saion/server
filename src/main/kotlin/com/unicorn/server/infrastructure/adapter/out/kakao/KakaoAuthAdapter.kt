package com.unicorn.server.infrastructure.adapter.out.kakao

import com.unicorn.server.domain.member.exception.InvalidSocialTokenException
import com.unicorn.server.domain.member.port.dto.KakaoUserInfo
import com.unicorn.server.domain.member.port.out.KakaoAuthPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.jwt.JwtClaimValidator
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.stereotype.Component

// KakaoAuthAdapter - 카카오 JWKS를 이용해 ID Token 서명, 만료, iss, aud를 검증한다.
@Component
class KakaoAuthAdapter(
	@param:Value("\${app.kakao.app-key}") private val appKey: String,
	@param:Value("\${app.kakao.jwks-uri}") private val jwksUri: String,
	@param:Value("\${app.kakao.issuer}") private val issuer: String,
) : KakaoAuthPort {

	private val jwtDecoder: NimbusJwtDecoder = NimbusJwtDecoder
		.withJwkSetUri(jwksUri)
		.build()
		.also { decoder ->
			decoder.setJwtValidator(
				DelegatingOAuth2TokenValidator(
					JwtValidators.createDefaultWithIssuer(issuer),
					JwtClaimValidator<Any>("aud") { aud ->
						when (aud) {
							is String -> aud == appKey
							is List<*> -> aud.contains(appKey)
							else -> false
						}
					},
				),
			)
		}

	// 카카오 ID Token을 검증하고 서비스 로그인에 필요한 사용자 정보를 추출한다.
	override fun verify(idToken: String): KakaoUserInfo {
		val jwt = try {
			jwtDecoder.decode(idToken)
		} catch (e: JwtException) {
			throw InvalidSocialTokenException(e.message)
		}

		val providerId = jwt.subject ?: throw InvalidSocialTokenException("Missing sub claim")
		val email = jwt.getClaimAsString("email")
		val name = jwt.getClaimAsString("nickname")
		val profileImageUrl = jwt.getClaimAsString("picture")

		return KakaoUserInfo(providerId = providerId, email = email, name = name, profileImageUrl = profileImageUrl)
	}
}
