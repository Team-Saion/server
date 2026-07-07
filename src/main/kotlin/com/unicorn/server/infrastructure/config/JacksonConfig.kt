package com.unicorn.server.infrastructure.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer as Jackson2LocalTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer as Jackson2LocalTimeSerializer
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.ext.javatime.deser.LocalTimeDeserializer as Jackson3LocalTimeDeserializer
import tools.jackson.databind.ext.javatime.ser.LocalTimeSerializer as Jackson3LocalTimeSerializer
import tools.jackson.databind.module.SimpleModule
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// JacksonConfig - 애플리케이션 전역 JSON 직렬화 설정을 구성한다.
@Configuration
class JacksonConfig {

	// HTTP 메시지 직렬화는 Jackson 3(tools.jackson) JsonMapper가 담당하므로
	// API 명세의 시간 포맷(HH:mm)을 여기서 맞춘다.
	@Bean
	fun timeFormatJsonMapperCustomizer(): JsonMapperBuilderCustomizer =
		JsonMapperBuilderCustomizer { builder ->
			builder.addModule(
				SimpleModule("time-format").apply {
					addSerializer(LocalTime::class.java, Jackson3LocalTimeSerializer(TIME_FORMAT))
					addDeserializer(LocalTime::class.java, Jackson3LocalTimeDeserializer(TIME_FORMAT))
				},
			)
		}

	@Bean
	fun objectMapper(): ObjectMapper =
		JsonMapper.builder()
			.addModule(javaTimeModule())
			.addModule(KotlinModule.Builder().build())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
			.build()

	private fun javaTimeModule(): JavaTimeModule =
		JavaTimeModule().apply {
			addSerializer(LocalTime::class.java, Jackson2LocalTimeSerializer(TIME_FORMAT))
			addDeserializer(LocalTime::class.java, Jackson2LocalTimeDeserializer(TIME_FORMAT))
		}

	companion object {
		private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
	}
}
