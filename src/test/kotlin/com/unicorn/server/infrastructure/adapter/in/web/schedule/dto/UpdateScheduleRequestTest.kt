package com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("UpdateScheduleRequest 역직렬화 테스트")
class UpdateScheduleRequestTest {

	private val objectMapper = JsonMapper.builder()
		.addModule(JavaTimeModule())
		.addModule(KotlinModule.Builder().build())
		.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
		.build()

	@Test
	@DisplayName("startTime, endTime, memo를 null로 명시하면 제공 여부가 true로 세팅된다")
	fun deserialize_withExplicitNullFields_marksFieldsProvided() {
		val request = objectMapper.readValue<UpdateScheduleRequest>(
			"""
			{
			  "startTime": null,
			  "endTime": null,
			  "needConfirm": false,
			  "memo": null
			}
			""".trimIndent(),
		)

		assertThat(request.startTime).isNull()
		assertThat(request.endTime).isNull()
		assertThat(request.memo).isNull()
		assertThat(request.startTimeProvided).isTrue()
		assertThat(request.endTimeProvided).isTrue()
		assertThat(request.memoProvided).isTrue()
	}

	@Test
	@DisplayName("startTime, endTime, memo를 생략하면 제공 여부가 false로 유지된다")
	fun deserialize_withOmittedFields_keepsFieldsNotProvided() {
		val request = objectMapper.readValue<UpdateScheduleRequest>(
			"""
			{
			  "needConfirm": true
			}
			""".trimIndent(),
		)

		assertThat(request.startTime).isNull()
		assertThat(request.endTime).isNull()
		assertThat(request.memo).isNull()
		assertThat(request.startTimeProvided).isFalse()
		assertThat(request.endTimeProvided).isFalse()
		assertThat(request.memoProvided).isFalse()
	}
}
