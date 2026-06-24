package com.unicorn.server.infrastructure.adapter.out.token

import com.unicorn.server.domain.member.enums.Role
import com.unicorn.server.domain.member.port.dto.TokenPair
import com.unicorn.server.domain.member.port.out.TokenIssuer
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

// JwtProvider - JWT 기반 토큰 발급 및 검증을 담당한다.
@Component
class JwtProvider(
	@Value("\${app.jwt.secret}") secretKey: String,
	@param:Value("\${app.jwt.access-token-expiration}") private val accessTokenExpiration: Long,
	@param:Value("\${app.jwt.refresh-token-expiration}") private val refreshTokenExpiration: Long,
) : TokenIssuer {

	private val key: SecretKey = Keys.hmacShaKeyFor(secretKey.toByteArray(Charsets.UTF_8))

	// 멤버 식별자와 역할을 기반으로 access/refresh token 쌍을 발급한다.
	override fun issue(memberId: String, role: Role): TokenPair {
		val accessToken = buildToken(memberId, listOf(role), ACCESS_TOKEN_TYPE, accessTokenExpiration)
		val refreshToken = buildToken(memberId, emptyList(), REFRESH_TOKEN_TYPE, refreshTokenExpiration)
		return TokenPair(accessToken, refreshToken)
	}

	// refresh token의 서명, 만료, 타입을 검증하고 멤버 식별자를 반환한다.
	override fun parseRefreshToken(refreshToken: String): String? =
		runCatching {
			val claims = getClaims(refreshToken)
			claims.subject.takeIf {
				claims.get(TOKEN_TYPE_CLAIM, String::class.java) == REFRESH_TOKEN_TYPE
			}
		}.getOrNull()

	// 토큰 서명, 만료, access token 타입을 검증한다.
	fun validate(token: String): Boolean =
		runCatching { getClaims(token).get(TOKEN_TYPE_CLAIM, String::class.java) == ACCESS_TOKEN_TYPE }
			.getOrDefault(false)

	// 토큰 subject에서 멤버 식별자를 추출한다.
	fun extractMemberId(token: String): String? =
		runCatching { getClaims(token).subject }.getOrNull()

	// 토큰 roles claim에서 멤버 권한 목록을 추출한다.
	fun extractRoles(token: String): List<String> =
		runCatching { getClaims(token)[ROLES_CLAIM] as? List<*> }
			.getOrNull()
			?.mapNotNull { it as? String }
			?: emptyList()

	// 공통 JWT claim과 만료 시간을 조립해 서명한다.
	private fun buildToken(subject: String, roles: List<Role>, tokenType: String, expirationSeconds: Long): String {
		val now = Date()
		return Jwts.builder()
			.subject(subject)
			.claim(TOKEN_TYPE_CLAIM, tokenType)
			.apply {
				if (roles.isNotEmpty()) {
					claim(ROLES_CLAIM, roles.map { it.name })
				}
			}
			.issuedAt(now)
			.expiration(Date(now.time + expirationSeconds * 1000))
			.signWith(key)
			.compact()
	}

	// 서명 검증을 통과한 JWT claims를 반환한다.
	private fun getClaims(token: String): Claims =
		Jwts.parser()
			.verifyWith(key)
			.build()
			.parseSignedClaims(token)
			.payload

	companion object {
		private const val TOKEN_TYPE_CLAIM = "type"
		private const val ROLES_CLAIM = "roles"
		private const val ACCESS_TOKEN_TYPE = "access"
		private const val REFRESH_TOKEN_TYPE = "refresh"
	}
}
