package com.unicorn.server.infrastructure.adapter.`in`.web.term

import com.unicorn.server.domain.member.enums.Role
import com.unicorn.server.domain.term.enums.TermCode
import com.unicorn.server.infrastructure.adapter.out.persistence.term.MemberTermJpaRepository
import com.unicorn.server.infrastructure.adapter.out.persistence.term.TermJpaRepository
import com.unicorn.server.infrastructure.adapter.out.persistence.term.entity.TermEntity
import com.unicorn.server.infrastructure.adapter.out.token.JwtProvider
import org.assertj.core.api.Assertions.assertThat
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.time.LocalDateTime

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("TermController 통합 테스트")
class TermControllerTest(
	@param:Autowired private val context: WebApplicationContext,
	@param:Autowired private val jwtProvider: JwtProvider,
	@param:Autowired private val termJpaRepository: TermJpaRepository,
	@param:Autowired private val memberTermJpaRepository: MemberTermJpaRepository,
) {
	private lateinit var mockMvc: MockMvc

	@BeforeEach
	fun setUp() {
		mockMvc = MockMvcBuilders
			.webAppContextSetup(context)
			.apply<DefaultMockMvcBuilder>(springSecurity())
			.build()
		memberTermJpaRepository.deleteAll()
		termJpaRepository.deleteAll()
	}

	@Test
	@DisplayName("PENDING 멤버가 필수 약관에 모두 동의하면 동의 내역을 저장한다")
	fun agreeTerms_withPendingRole_savesAgreements() {
		val requiredTermId = saveTerm(TermCode.SERVICE_USE, requiredYn = "Y")
		val optionalTermId = saveTerm(TermCode.MARKETING, requiredYn = "N")
		val accessToken = jwtProvider.issue(MEMBER_ID, Role.PENDING).accessToken

		mockMvc.perform(
			post("/api/v1/terms/agree")
				.header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""{"termIds":[$requiredTermId,$optionalTermId]}"""),
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.success").value(true))

		val agreements = memberTermJpaRepository.findAll()
		assertThat(agreements).hasSize(2)
		assertThat(agreements.map { it.memberId }).containsOnly(MEMBER_ID)
		assertThat(agreements.map { it.termId }).containsExactlyInAnyOrder(requiredTermId, optionalTermId)
	}

	@Test
	@DisplayName("MEMBER 멤버도 약관 동의 API에 접근할 수 있다")
	fun agreeTerms_withMemberRole_returnsOk() {
		val requiredTermId = saveTerm(TermCode.SERVICE_USE, requiredYn = "Y")
		val accessToken = jwtProvider.issue(MEMBER_ID, Role.MEMBER).accessToken

		mockMvc.perform(
			post("/api/v1/terms/agree")
				.header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""{"termIds":[$requiredTermId]}"""),
		)
			.andExpect(status().isOk)
	}

	@Test
	@DisplayName("미인증 사용자가 약관 동의 API에 접근하면 401을 반환한다")
	fun agreeTerms_withoutAuthentication_returnsUnauthorized() {
		mockMvc.perform(
			post("/api/v1/terms/agree")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""{"termIds":[1]}"""),
		)
			.andExpect(status().isUnauthorized)
	}

	@Test
	@DisplayName("필수 약관을 누락하면 400을 반환한다")
	fun agreeTerms_missingRequiredTerms_returnsBadRequest() {
		saveTerm(TermCode.SERVICE_USE, requiredYn = "Y")
		val optionalTermId = saveTerm(TermCode.MARKETING, requiredYn = "N")
		val accessToken = jwtProvider.issue(MEMBER_ID, Role.PENDING).accessToken

		mockMvc.perform(
			post("/api/v1/terms/agree")
				.header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""{"termIds":[$optionalTermId]}"""),
		)
			.andExpect(status().isBadRequest)
			.andExpect(jsonPath("$.errorCode").value("T400_1"))

		assertThat(memberTermJpaRepository.findAll()).isEmpty()
	}

	private fun saveTerm(termCode: TermCode, requiredYn: String): Long {
		val now = LocalDateTime.now()
		val entity = TermEntity(
			termCode = termCode,
			title = termCode.name,
			contentUrl = null,
			version = 1,
			requiredYn = requiredYn,
			effectiveAt = now.minusDays(1),
		)
		return requireNotNull(termJpaRepository.save(entity).id)
	}

	companion object {
		private const val MEMBER_ID = "MB20260703000000001"
	}
}
