package com.unicorn.server.infrastructure.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// JacksonConfig - 애플리케이션 전역 JSON 직렬화 설정을 구성한다.
@Configuration
class JacksonConfig {

	@Bean
	fun objectMapper(): ObjectMapper =
		JsonMapper.builder()
			.addModule(JavaTimeModule())
			.addModule(KotlinModule.Builder().build())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
			.build()

}
