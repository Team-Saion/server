package com.unicorn.server.infrastructure.adapter.`in`.web.schedule

import com.fasterxml.jackson.databind.ObjectMapper
import com.unicorn.server.domain.member.enums.Role
import com.unicorn.server.domain.schedule.port.out.CircleAccessOutPort
import com.unicorn.server.infrastructure.adapter.out.persistence.schedule.ScheduleConfirmationJpaRepository
import com.unicorn.server.infrastructure.adapter.out.persistence.schedule.ScheduleJpaRepository
import com.unicorn.server.infrastructure.adapter.out.token.JwtProvider
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.time.LocalDate
import java.time.ZoneId

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("ScheduleController 통합 테스트")
class ScheduleControllerTest(
	@param:Autowired private val context: WebApplicationContext,
	@param:Autowired private val jwtProvider: JwtProvider,
	@param:Autowired private val objectMapper: ObjectMapper,
	@param:Autowired private val scheduleJpaRepository: ScheduleJpaRepository,
	@param:Autowired private val scheduleConfirmationJpaRepository: ScheduleConfirmationJpaRepository,
) {
	private lateinit var mockMvc: MockMvc

	private val today: LocalDate = LocalDate.now(ZoneId.of("Asia/Seoul"))

	@TestConfiguration
	class CircleAccessTestConfig {
		@Bean
		@Primary
		fun fakeCircleAccessOutPort(): CircleAccessOutPort = object : CircleAccessOutPort {
			override fun existsById(circleId: String): Boolean = circleId == CIRCLE_ID

			override fun isMember(circleId: String, memberId: String): Boolean =
				circleId == CIRCLE_ID && memberId in setOf(AUTHOR_ID, INITIATOR_ID, OTHER_MEMBER_ID)

			override fun isInitiator(circleId: String, memberId: String): Boolean =
				circleId == CIRCLE_ID && memberId == INITIATOR_ID
		}
	}

	@BeforeEach
	fun setUp() {
		mockMvc = MockMvcBuilders
			.webAppContextSetup(context)
			.apply<DefaultMockMvcBuilder>(springSecurity())
			.build()
		scheduleConfirmationJpaRepository.deleteAll()
		scheduleJpaRepository.deleteAll()
	}

	@Test
	@DisplayName("일정을 생성하면 201과 함께 저장된 일정을 상세 조회할 수 있다")
	fun createSchedule_withValidRequest_returns201AndSchedulePersists() {
		val token = memberToken(AUTHOR_ID)

		val scheduleId = createSchedule(
			token,
			createRequestJson(
				title = "제주도 여행",
				startDate = today.plusDays(10),
				endDate = today.plusDays(12),
				needConfirm = true,
				memo = "숙소 체크인 15시",
			),
		)

		mockMvc.perform(
			get("$BASE_URL/$scheduleId")
				.header(HttpHeaders.AUTHORIZATION, "Bearer $token"),
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.title").value("제주도 여행"))
			.andExpect(jsonPath("$.data.startDate").value(today.plusDays(10).toString()))
			.andExpect(jsonPath("$.data.endDate").value(today.plusDays(12).toString()))
			.andExpect(jsonPath("$.data.startTime").value("09:00"))
			.andExpect(jsonPath("$.data.endTime").value("18:00"))
			.andExpect(jsonPath("$.data.isAllDay").value(false))
			.andExpect(jsonPath("$.data.needConfirm").value(true))
			.andExpect(jsonPath("$.data.memo").value("숙소 체크인 15시"))
			.andExpect(jsonPath("$.data.status").value("UPCOMING"))
			.andExpect(jsonPath("$.data.dDay").value(10))
			.andExpect(jsonPath("$.data.progressRate").value(0))
			.andExpect(jsonPath("$.data.confirmations").isEmpty)
			.andExpect(jsonPath("$.data.myConfirmationType").doesNotExist())
			.andExpect(jsonPath("$.data.createdBy").value(AUTHOR_ID))
	}

	@Test
	@DisplayName("시작시간만 있고 종료시간이 없으면 S400_8을 반환한다")
	fun createSchedule_withStartTimeOnly_returnsMissingEndTimeError() {
		mockMvc.perform(
			post(BASE_URL)
				.header(HttpHeaders.AUTHORIZATION, "Bearer ${memberToken(AUTHOR_ID)}")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					createRequestJson(startDate = today.plusDays(1), startTime = "09:00", endTime = null),
				),
		)
			.andExpect(status().isBadRequest)
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.errorCode").value("S400_8"))
	}

	@Test
	@DisplayName("needConfirm 없이 일정을 생성하면 400을 반환한다")
	fun createSchedule_withoutNeedConfirm_returns400() {
		mockMvc.perform(
			post(BASE_URL)
				.header(HttpHeaders.AUTHORIZATION, "Bearer ${memberToken(AUTHOR_ID)}")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					"""
					{
						"title": "제목",
						"startDate": "${today.plusDays(1)}",
						"endDate": "${today.plusDays(1)}"
					}
					""".trimIndent(),
				),
		)
			.andExpect(status().isBadRequest)
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.errorCode").value("G400"))
	}

	@Test
	@DisplayName("액세스 토큰 없이 요청하면 401과 G401을 반환한다")
	fun createSchedule_withoutAccessToken_returns401() {
		mockMvc.perform(
			post(BASE_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(createRequestJson(startDate = today.plusDays(1))),
		)
			.andExpect(status().isUnauthorized)
			.andExpect(jsonPath("$.errorCode").value("G401"))
	}

	@Test
	@DisplayName("써클 구성원이 아니면 S403_1을 반환한다")
	fun createSchedule_byNonCircleMember_returnsAccessDenied() {
		mockMvc.perform(
			post(BASE_URL)
				.header(HttpHeaders.AUTHORIZATION, "Bearer ${memberToken(OUTSIDER_ID)}")
				.contentType(MediaType.APPLICATION_JSON)
				.content(createRequestJson(startDate = today.plusDays(1))),
		)
			.andExpect(status().isForbidden)
			.andExpect(jsonPath("$.errorCode").value("S403_1"))
	}

	@Test
	@DisplayName("startTime과 endTime을 명시적으로 null로 수정하면 종일 일정으로 변경된다")
	fun updateSchedule_withExplicitNullTimes_becomesAllDay() {
		val token = memberToken(AUTHOR_ID)
		val scheduleId = createSchedule(token, createRequestJson(startDate = today.plusDays(3)))

		mockMvc.perform(
			patch("$BASE_URL/$scheduleId")
				.header(HttpHeaders.AUTHORIZATION, "Bearer $token")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""{"startTime":null,"endTime":null,"needConfirm":false}"""),
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.success").value(true))

		mockMvc.perform(
			get("$BASE_URL/$scheduleId")
				.header(HttpHeaders.AUTHORIZATION, "Bearer $token"),
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.data.isAllDay").value(true))
			.andExpect(jsonPath("$.data.startTime").doesNotExist())
			.andExpect(jsonPath("$.data.endTime").doesNotExist())
	}

	@Test
	@DisplayName("memo를 생략하고 수정하면 기존 memo가 유지된다")
	fun updateSchedule_withOmittedMemo_keepsMemo() {
		val token = memberToken(AUTHOR_ID)
		val scheduleId = createSchedule(
			token,
			createRequestJson(startDate = today.plusDays(3), memo = "기존 메모"),
		)

		mockMvc.perform(
			patch("$BASE_URL/$scheduleId")
				.header(HttpHeaders.AUTHORIZATION, "Bearer $token")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""{"title":"수정된 제목","needConfirm":false}"""),
		)
			.andExpect(status().isOk)

		mockMvc.perform(
			get("$BASE_URL/$scheduleId")
				.header(HttpHeaders.AUTHORIZATION, "Bearer $token"),
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.data.title").value("수정된 제목"))
			.andExpect(jsonPath("$.data.memo").value("기존 메모"))
	}

	@Test
	@DisplayName("memo를 명시적으로 null로 수정하면 memo가 삭제된다")
	fun updateSchedule_withExplicitNullMemo_clearsMemo() {
		val token = memberToken(AUTHOR_ID)
		val scheduleId = createSchedule(
			token,
			createRequestJson(startDate = today.plusDays(3), memo = "기존 메모"),
		)

		mockMvc.perform(
			patch("$BASE_URL/$scheduleId")
				.header(HttpHeaders.AUTHORIZATION, "Bearer $token")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""{"memo":null,"needConfirm":false}"""),
		)
			.andExpect(status().isOk)

		mockMvc.perform(
			get("$BASE_URL/$scheduleId")
				.header(HttpHeaders.AUTHORIZATION, "Bearer $token"),
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.data.memo").doesNotExist())
	}

	@Test
	@DisplayName("작성자도 initiator도 아닌 구성원이 수정하면 S403_2를 반환한다")
	fun updateSchedule_byNonAuthorMember_returnsModificationDenied() {
		val scheduleId = createSchedule(memberToken(AUTHOR_ID), createRequestJson(startDate = today.plusDays(3)))

		mockMvc.perform(
			patch("$BASE_URL/$scheduleId")
				.header(HttpHeaders.AUTHORIZATION, "Bearer ${memberToken(OTHER_MEMBER_ID)}")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""{"title":"다른 멤버 수정","needConfirm":false}"""),
		)
			.andExpect(status().isForbidden)
			.andExpect(jsonPath("$.errorCode").value("S403_2"))
	}

	@Test
	@DisplayName("작성자가 아니어도 써클 initiator는 일정을 수정할 수 있다")
	fun updateSchedule_byInitiator_updatesTitle() {
		val scheduleId = createSchedule(memberToken(AUTHOR_ID), createRequestJson(startDate = today.plusDays(3)))

		mockMvc.perform(
			patch("$BASE_URL/$scheduleId")
				.header(HttpHeaders.AUTHORIZATION, "Bearer ${memberToken(INITIATOR_ID)}")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""{"title":"운영진 수정","needConfirm":false}"""),
		)
			.andExpect(status().isOk)

		mockMvc.perform(
			get("$BASE_URL/$scheduleId")
				.header(HttpHeaders.AUTHORIZATION, "Bearer ${memberToken(AUTHOR_ID)}"),
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.data.title").value("운영진 수정"))
	}

	@Test
	@DisplayName("일정 삭제 후 상세 조회와 재삭제는 S404_2를 반환한다")
	fun deleteSchedule_softDeletesSchedule() {
		val token = memberToken(AUTHOR_ID)
		val scheduleId = createSchedule(token, createRequestJson(startDate = today.plusDays(3)))

		mockMvc.perform(
			delete("$BASE_URL/$scheduleId")
				.header(HttpHeaders.AUTHORIZATION, "Bearer $token"),
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.success").value(true))

		mockMvc.perform(
			get("$BASE_URL/$scheduleId")
				.header(HttpHeaders.AUTHORIZATION, "Bearer $token"),
		)
			.andExpect(status().isNotFound)
			.andExpect(jsonPath("$.errorCode").value("S404_2"))

		mockMvc.perform(
			delete("$BASE_URL/$scheduleId")
				.header(HttpHeaders.AUTHORIZATION, "Bearer $token"),
		)
			.andExpect(status().isNotFound)
			.andExpect(jsonPath("$.errorCode").value("S404_2"))
	}

	@Test
	@DisplayName("일정 목록을 커서 기반으로 시작일 순서대로 페이지네이션한다")
	fun getScheduleList_withCursor_paginatesInOrder() {
		val token = memberToken(AUTHOR_ID)
		createSchedule(token, createRequestJson(title = "일정1", startDate = today.plusDays(1)))
		createSchedule(
			token,
			createRequestJson(title = "일정2", startDate = today.plusDays(2), startTime = null, endTime = null),
		)
		createSchedule(token, createRequestJson(title = "일정3", startDate = today.plusDays(3)))

		val firstPage = mockMvc.perform(
			get(BASE_URL)
				.header(HttpHeaders.AUTHORIZATION, "Bearer $token")
				.param("size", "2"),
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.data.schedules.length()").value(2))
			.andExpect(jsonPath("$.data.schedules[0].title").value("일정1"))
			.andExpect(jsonPath("$.data.schedules[1].title").value("일정2"))
			.andExpect(jsonPath("$.data.schedules[1].isAllDay").value(true))
			.andExpect(jsonPath("$.data.hasNext").value(true))
			.andExpect(jsonPath("$.data.nextCursor").isNotEmpty)
			.andReturn()
			.response.contentAsString
		val nextCursor = objectMapper.readTree(firstPage).path("data").path("nextCursor").asText()

		mockMvc.perform(
			get(BASE_URL)
				.header(HttpHeaders.AUTHORIZATION, "Bearer $token")
				.param("cursor", nextCursor)
				.param("size", "2"),
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.data.schedules.length()").value(1))
			.andExpect(jsonPath("$.data.schedules[0].title").value("일정3"))
			.andExpect(jsonPath("$.data.hasNext").value(false))
			.andExpect(jsonPath("$.data.nextCursor").doesNotExist())
	}

	@Test
	@DisplayName("확인하기는 멤버당 1건으로 유지되고 재등록은 멱등하다")
	fun registerConfirmation_registersAndKeepsSingleRowPerMember() {
		val token = memberToken(AUTHOR_ID)
		val scheduleId = createSchedule(
			token,
			createRequestJson(startDate = today.plusDays(3), needConfirm = true),
		)

		repeat(2) {
			mockMvc.perform(
				post("$BASE_URL/$scheduleId/confirmations")
					.header(HttpHeaders.AUTHORIZATION, "Bearer $token")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""{"confirmationType":"CONFIRMED"}"""),
			)
				.andExpect(status().isOk)
				.andExpect(jsonPath("$.data.confirmationType").value("CONFIRMED"))
		}

		mockMvc.perform(
			get("$BASE_URL/$scheduleId")
				.header(HttpHeaders.AUTHORIZATION, "Bearer $token"),
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.data.confirmations.length()").value(1))
			.andExpect(jsonPath("$.data.confirmations[0].type").value("CONFIRMED"))
			.andExpect(jsonPath("$.data.confirmations[0].count").value(1))
			.andExpect(jsonPath("$.data.myConfirmationType").value("CONFIRMED"))
	}

	@Test
	@DisplayName("needConfirm=false인 일정에 확인하기를 등록하면 S400_11을 반환한다")
	fun registerConfirmation_onScheduleWithoutConfirm_returnsNotSupported() {
		val token = memberToken(AUTHOR_ID)
		val scheduleId = createSchedule(
			token,
			createRequestJson(startDate = today.plusDays(3), needConfirm = false),
		)

		mockMvc.perform(
			post("$BASE_URL/$scheduleId/confirmations")
				.header(HttpHeaders.AUTHORIZATION, "Bearer $token")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""{"confirmationType":"CONFIRMED"}"""),
		)
			.andExpect(status().isBadRequest)
			.andExpect(jsonPath("$.errorCode").value("S400_11"))
	}

	private fun memberToken(memberId: String): String =
		jwtProvider.issue(memberId, Role.MEMBER).accessToken

	private fun createSchedule(token: String, requestJson: String): Long {
		val response = mockMvc.perform(
			post(BASE_URL)
				.header(HttpHeaders.AUTHORIZATION, "Bearer $token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson),
		)
			.andExpect(status().isCreated)
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.scheduleId").isNumber)
			.andReturn()
			.response.contentAsString
		return objectMapper.readTree(response).path("data").path("scheduleId").asLong()
	}

	private fun createRequestJson(
		title: String = "제주도 여행",
		startDate: LocalDate,
		endDate: LocalDate = startDate,
		startTime: String? = "09:00",
		endTime: String? = "18:00",
		needConfirm: Boolean = false,
		memo: String? = null,
	): String = objectMapper.writeValueAsString(
		mapOf(
			"title" to title,
			"startDate" to startDate.toString(),
			"endDate" to endDate.toString(),
			"startTime" to startTime,
			"endTime" to endTime,
			"needConfirm" to needConfirm,
			"memo" to memo,
		),
	)

	companion object {
		private const val CIRCLE_ID = "CC202506010000000001"
		private const val BASE_URL = "/api/v1/circles/$CIRCLE_ID/schedules"
		private const val AUTHOR_ID = "00000000-0000-0000-0000-000000000001"
		private const val INITIATOR_ID = "00000000-0000-0000-0000-000000000002"
		private const val OTHER_MEMBER_ID = "00000000-0000-0000-0000-000000000003"
		private const val OUTSIDER_ID = "00000000-0000-0000-0000-000000000009"
	}
}
