package com.unicorn.server.infrastructure.config

import com.unicorn.server.domain.member.enums.Role
import com.unicorn.server.infrastructure.adapter.out.token.JwtProvider
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("SecurityConfig 통합 테스트")
class SecurityConfigTest(
	@param:Autowired private val context: WebApplicationContext,
	@param:Autowired private val jwtProvider: JwtProvider,
) {
	private lateinit var mockMvc: MockMvc

	@BeforeEach
	fun setUp() {
		mockMvc = MockMvcBuilders
			.webAppContextSetup(context)
			.apply<DefaultMockMvcBuilder>(springSecurity())
			.build()
	}

	@Test
	@DisplayName("카카오 로그인 API는 인증 없이 접근할 수 있다")
	fun kakaoLogin_withoutAuthentication_passesSecurityFilterChain() {
		mockMvc.perform(
			post("/v1/auth/kakao")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""{"idToken":""}"""),
		)
			.andExpect(status().isBadRequest)
	}

	@Test
	@DisplayName("토큰 재발급 API는 인증 없이 접근할 수 있다")
	fun refresh_withoutAuthentication_passesSecurityFilterChain() {
		mockMvc.perform(
			post("/v1/auth/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""{"refreshToken":""}"""),
		)
			.andExpect(status().isBadRequest)
	}

	@Test
	@DisplayName("멤버 API는 인증 없이 접근하면 401을 반환한다")
	fun memberApi_withoutAuthentication_returnsUnauthorized() {
		mockMvc.perform(get("/v1/members/me"))
			.andExpect(status().isUnauthorized)
	}

	@Test
	@DisplayName("일반 멤버가 관리자 API에 접근하면 403을 반환한다")
	fun adminApi_withMemberRole_returnsForbidden() {
		val accessToken = jwtProvider.issue(MEMBER_ID, Role.MEMBER).accessToken

		mockMvc.perform(
			get("/v1/admin/test")
				.header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken"),
		)
			.andExpect(status().isForbidden)
	}

	companion object {
		private const val MEMBER_ID = "00000000-0000-0000-0000-000000000001"
	}
}
