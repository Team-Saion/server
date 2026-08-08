package com.unicorn.server.infrastructure.adapter.out.oidc

import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.jwt.JwtClaimValidator
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder

// OidcJwtDecoderFactory - JWKS 기반 OIDC ID Token 검증용 NimbusJwtDecoder를 issuer/audience 검증과 함께 생성한다.
object OidcJwtDecoderFactory {
	fun create(jwksUri: String, issuer: String, audience: String): NimbusJwtDecoder =
		NimbusJwtDecoder
			.withJwkSetUri(jwksUri)
			.build()
			.also { decoder ->
				decoder.setJwtValidator(
					DelegatingOAuth2TokenValidator(
						JwtValidators.createDefaultWithIssuer(issuer),
						JwtClaimValidator<Any>("aud") { aud ->
							when (aud) {
								is String -> aud == audience
								is List<*> -> aud.contains(audience)
								else -> false
							}
						},
					),
				)
			}
}
