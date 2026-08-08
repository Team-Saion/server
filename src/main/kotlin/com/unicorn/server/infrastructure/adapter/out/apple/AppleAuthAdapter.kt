package com.unicorn.server.infrastructure.adapter.out.apple

import com.unicorn.server.domain.member.exception.InvalidSocialTokenException
import com.unicorn.server.domain.member.port.dto.AppleUserInfo
import com.unicorn.server.domain.member.port.out.MemberAppleAuthOutPort
import com.unicorn.server.infrastructure.adapter.out.oidc.OidcJwtDecoderFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.stereotype.Component

// AppleAuthAdapter - 애플 JWKS를 이용해 ID Token 서명, 만료, iss, aud를 검증한다.
@Component
class AppleAuthAdapter(
	@param:Value("\${app.apple.client-id}") private val clientId: String,
	@param:Value("\${app.apple.jwks-uri}") private val jwksUri: String,
	@param:Value("\${app.apple.issuer}") private val issuer: String,
) : MemberAppleAuthOutPort {

	private val jwtDecoder: NimbusJwtDecoder = OidcJwtDecoderFactory.create(jwksUri, issuer, clientId)

	// 애플 ID Token을 검증하고 서비스 로그인에 필요한 사용자 정보를 추출한다.
	override fun verify(idToken: String): AppleUserInfo {
		val jwt = try {
			jwtDecoder.decode(idToken)
		} catch (e: JwtException) {
			throw InvalidSocialTokenException("provider=apple, reason=${e.message ?: e::class.simpleName}")
		}

		val providerId = jwt.subject ?: throw InvalidSocialTokenException("provider=apple, missing claim=sub")
		val email = jwt.getClaimAsString("email")

		return AppleUserInfo(providerId = providerId, email = email)
	}
}
