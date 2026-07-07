package com.unicorn.server.infrastructure.adapter.`in`.web.circle

import com.unicorn.server.domain.circle.port.dto.CreateCircleCommand
import com.unicorn.server.domain.circle.port.`in`.CircleInPort
import com.unicorn.server.domain.circle.port.`in`.CircleMemberInPort
import com.unicorn.server.domain.member.Member
import com.unicorn.server.domain.member.enums.Role
import com.unicorn.server.domain.member.port.out.MemberOutPort
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("CircleController 통합 테스트")
class CircleControllerTest(
	@param:Autowired private val context: WebApplicationContext,
	@param:Autowired private val jwtProvider: JwtProvider,
	@param:Autowired private val memberOutPort: MemberOutPort,
	@param:Autowired private val circleInPort: CircleInPort,
	@param:Autowired private val circleMemberInPort: CircleMemberInPort,
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
	@DisplayName("방장이 같은 써클의 다른 활성 구성원에게 권한을 위임할 수 있다")
	fun transferInitiator_success() {
		val owner = memberOutPort.save(member("OwnerA", "ownerA"))
		val target = memberOutPort.save(member("TargetA", "targetA"))
		val circle = circleInPort.create(owner.id.toString(), CreateCircleCommand("위임API테스트"))
		circleMemberInPort.join(circle.id, target.id.toString())
		val accessToken = jwtProvider.issue(owner.id.toString(), Role.MEMBER).accessToken

		mockMvc.perform(
			patch("/api/v1/circles/${circle.id}/initiator")
				.header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""{"targetMemberId":"${target.id}"}"""),
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.ownerId").value(target.id.toString()))

		val members = circleMemberInPort.getCircleMembers(circle.id).associateBy { it.memberId }
		assertThat(members[owner.id.toString()]?.role).isEqualTo("MEMBER")
		assertThat(members[target.id.toString()]?.role).isEqualTo("INITIATOR")
	}

	@Test
	@DisplayName("방장이 아닌 구성원이 권한 위임 API를 호출하면 403을 반환한다")
	fun transferInitiator_nonInitiator_returnsForbidden() {
		val owner = memberOutPort.save(member("OwnerB", "ownerB"))
		val requester = memberOutPort.save(member("Requester", "requestr"))
		val target = memberOutPort.save(member("TargetB", "targetB"))
		val circle = circleInPort.create(owner.id.toString(), CreateCircleCommand("위임권한없음"))
		circleMemberInPort.join(circle.id, requester.id.toString())
		circleMemberInPort.join(circle.id, target.id.toString())
		val accessToken = jwtProvider.issue(requester.id.toString(), Role.MEMBER).accessToken

		mockMvc.perform(
			patch("/api/v1/circles/${circle.id}/initiator")
				.header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""{"targetMemberId":"${target.id}"}"""),
		)
			.andExpect(status().isForbidden)
			.andExpect(jsonPath("$.errorCode").value("C403_2"))
	}

	private fun member(name: String, nickname: String): Member =
		Member.create(null, name, nickname, role = Role.MEMBER)
}
