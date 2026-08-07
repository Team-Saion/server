package com.unicorn.server.infrastructure.adapter.out.apple

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.sun.net.httpserver.HttpServer
import com.unicorn.server.domain.member.exception.InvalidSocialTokenException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.net.InetSocketAddress
import java.time.Instant
import java.util.Date

@DisplayName("AppleAuthAdapter 단위 테스트")
class AppleAuthAdapterTest {

	private lateinit var httpServer: HttpServer
	private lateinit var rsaKey: RSAKey
	private lateinit var jwksUri: String
	private val issuer = "https://appleid.apple.com"
	private val clientId = "com.unicorn.saion"

	@BeforeEach
	fun setUp() {
		rsaKey = RSAKeyGenerator(2048).keyID("test-key-id").generate()
		val jwkSetJson = JWKSet(rsaKey.toPublicJWK()).toString()

		httpServer = HttpServer.create(InetSocketAddress("localhost", 0), 0)
		httpServer.createContext("/keys") { exchange ->
			val body = jwkSetJson.toByteArray()
			exchange.responseHeaders.add("Content-Type", "application/json")
			exchange.sendResponseHeaders(200, body.size.toLong())
			exchange.responseBody.use { it.write(body) }
		}
		httpServer.start()
		jwksUri = "http://localhost:${httpServer.address.port}/keys"
	}

	@AfterEach
	fun tearDown() {
		httpServer.stop(0)
	}

	private fun signToken(
		subject: String = "apple-user-001",
		email: String? = "user@example.com",
		audience: String = clientId,
		tokenIssuer: String = issuer,
		expiresAt: Instant = Instant.now().plusSeconds(3600),
		signingKey: RSAKey = rsaKey,
	): String {
		val claimsBuilder = JWTClaimsSet.Builder()
			.subject(subject)
			.issuer(tokenIssuer)
			.audience(audience)
			.expirationTime(Date.from(expiresAt))
			.issueTime(Date.from(Instant.now()))
		email?.let { claimsBuilder.claim("email", it) }

		val header = JWSHeader.Builder(JWSAlgorithm.RS256)
			.type(JOSEObjectType.JWT)
			.keyID(signingKey.keyID)
			.build()

		val signedJWT = SignedJWT(header, claimsBuilder.build())
		signedJWT.sign(RSASSASigner(signingKey))
		return signedJWT.serialize()
	}

	@Test
	@DisplayName("verify 호출 시 유효한 ID Token이면 AppleUserInfo를 반환한다")
	fun verify_withValidIdToken_returnsAppleUserInfo() {
		val adapter = AppleAuthAdapter(clientId, jwksUri, issuer)
		val token = signToken(subject = "apple-user-001", email = "user@example.com")

		val result = adapter.verify(token)

		assertThat(result.providerId).isEqualTo("apple-user-001")
		assertThat(result.email).isEqualTo("user@example.com")
	}

	@Test
	@DisplayName("verify 호출 시 email 클레임이 없어도 예외 없이 email null로 반환한다")
	fun verify_withoutEmailClaim_returnsUserInfoWithNullEmail() {
		val adapter = AppleAuthAdapter(clientId, jwksUri, issuer)
		val token = signToken(email = null)

		val result = adapter.verify(token)

		assertThat(result.email).isNull()
	}

	@Test
	@DisplayName("verify 호출 시 aud가 client-id와 다르면 InvalidSocialTokenException이 발생한다")
	fun verify_withWrongAudience_throwsInvalidSocialTokenException() {
		val adapter = AppleAuthAdapter(clientId, jwksUri, issuer)
		val token = signToken(audience = "other.app.id")

		assertThatThrownBy { adapter.verify(token) }
			.isInstanceOf(InvalidSocialTokenException::class.java)
	}

	@Test
	@DisplayName("verify 호출 시 iss가 애플이 아니면 InvalidSocialTokenException이 발생한다")
	fun verify_withWrongIssuer_throwsInvalidSocialTokenException() {
		val adapter = AppleAuthAdapter(clientId, jwksUri, issuer)
		val token = signToken(tokenIssuer = "https://evil.example.com")

		assertThatThrownBy { adapter.verify(token) }
			.isInstanceOf(InvalidSocialTokenException::class.java)
	}

	@Test
	@DisplayName("verify 호출 시 만료된 토큰이면 InvalidSocialTokenException이 발생한다")
	fun verify_withExpiredToken_throwsInvalidSocialTokenException() {
		val adapter = AppleAuthAdapter(clientId, jwksUri, issuer)
		val token = signToken(expiresAt = Instant.now().minusSeconds(60))

		assertThatThrownBy { adapter.verify(token) }
			.isInstanceOf(InvalidSocialTokenException::class.java)
	}

	@Test
	@DisplayName("verify 호출 시 JWKS에 없는 키로 서명된 토큰이면 InvalidSocialTokenException이 발생한다")
	fun verify_withUnknownSigningKey_throwsInvalidSocialTokenException() {
		val adapter = AppleAuthAdapter(clientId, jwksUri, issuer)
		val otherKey = RSAKeyGenerator(2048).keyID("test-key-id").generate()
		val token = signToken(signingKey = otherKey)

		assertThatThrownBy { adapter.verify(token) }
			.isInstanceOf(InvalidSocialTokenException::class.java)
	}
}
