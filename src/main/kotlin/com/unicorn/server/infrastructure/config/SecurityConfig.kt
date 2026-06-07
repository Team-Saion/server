package com.unicorn.server.infrastructure.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.unicorn.server.common.exception.CommonErrorCode
import com.unicorn.server.infrastructure.adapter.`in`.web.common.dto.ApiResponse
import com.unicorn.server.infrastructure.security.JwtAuthenticationFilter
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

// SecurityConfig - Stateless JWT 기반 보안 설정을 구성한다.
@Configuration
@EnableWebSecurity
class SecurityConfig(
	private val jwtAuthenticationFilter: JwtAuthenticationFilter,
	private val objectMapper: ObjectMapper,
) {

	@Bean
	fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
		http
			.csrf { it.disable() }
			.formLogin { it.disable() }
			.httpBasic { it.disable() }
			.sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
			.exceptionHandling { exception ->
				exception
					.authenticationEntryPoint { _, response, _ ->
						writeErrorResponse(response, CommonErrorCode.UNAUTHORIZED)
					}
					.accessDeniedHandler { _, response, _ ->
						writeErrorResponse(response, CommonErrorCode.FORBIDDEN)
					}
			}
			.authorizeHttpRequests { auth ->
				auth
					.requestMatchers(*PERMIT_ALL_ENDPOINTS).permitAll()
					.requestMatchers(*ADMIN_ENDPOINTS).hasRole("ADMIN")
					.anyRequest().authenticated()
			}
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
			.build()

	// Spring Security 예외를 공통 ApiResponse 포맷으로 직렬화한다.
	private fun writeErrorResponse(response: HttpServletResponse, errorCode: CommonErrorCode) {
		response.status = errorCode.httpStatus.value()
		response.contentType = MediaType.APPLICATION_JSON_VALUE
		response.characterEncoding = Charsets.UTF_8.name()
		objectMapper.writeValue(response.writer, ApiResponse.error<Unit>(errorCode).body)
	}

	companion object {
		private val PERMIT_ALL_ENDPOINTS = arrayOf(
			"/api/v1/auth/**",
			"/api/swagger-ui/**",
			"/swagger-ui/**",
			"/api/swagger-ui.html",
			"/api/api-specs/**",
			"/actuator/**",
			"/error",
		)

		private val ADMIN_ENDPOINTS = arrayOf(
			"/api/v1/admin/**",
		)
	}
}
