package com.unicorn.server.infrastructure.aop

import jakarta.servlet.http.HttpServletRequest
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * Controller API 호출을 AOP로 로깅한다.
 *
 * 성공 로그 예시:
 * [API] POST /api/v1/users -> 201 | UserController.register() | params={} | time=32ms
 *
 * 예외 로그 예시:
 * [API ERROR] GET /api/v1/users/abc | UserController.getUser() | params={} | time=5ms | message=...
 */
@Aspect
@Order(1)
@Component
@Profile("!test")
class ApiLoggingAspect {
	@Around("com.unicorn.server.infrastructure.aop.Pointcuts.allController()")
	fun logApiCall(joinPoint: ProceedingJoinPoint): Any? {
		val request = currentRequest()
		val startTime = System.currentTimeMillis()

		return try {
			val result = joinPoint.proceed()
			val elapsedTime = System.currentTimeMillis() - startTime

			if (request != null) {
				log.info(
					"[API] {} {} -> {} | {}.{}() | params={} | time={}ms",
					request.method,
					request.decodedRequestUri(),
					result.statusCodeValue(),
					joinPoint.signature.declaringType.simpleName,
					joinPoint.signature.name,
					request.params(),
					elapsedTime,
				)
			}

			result
		} catch (ex: Exception) {
			val elapsedTime = System.currentTimeMillis() - startTime

			if (request != null) {
				log.error(
					"[API ERROR] {} {} | {}.{}() | params={} | time={}ms | message={}",
					request.method,
					request.decodedRequestUri(),
					joinPoint.signature.declaringType.simpleName,
					joinPoint.signature.name,
					request.params(),
					elapsedTime,
					ex.message,
					ex,
				)
			}

			throw ex
		}
	}

	private fun currentRequest(): HttpServletRequest? {
		val attributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
		return attributes?.request
	}

	private fun HttpServletRequest.decodedRequestUri(): String =
		runCatching {
			URLDecoder.decode(requestURI, StandardCharsets.UTF_8)
		}.getOrElse {
			requestURI
		}

	private fun HttpServletRequest.params(): Map<String, Any> =
		parameterMap.mapKeys { (key, _) -> key.replace(".", "-") }
			.mapValues { (_, values) ->
				when {
					values.isNullOrEmpty() -> ""
					values.size == 1 -> values[0]
					else -> values.toList()
				}
			}

	private fun Any?.statusCodeValue(): Int =
		(this as? ResponseEntity<*>)?.statusCode?.value() ?: 200

	companion object {
		private val log = LoggerFactory.getLogger(ApiLoggingAspect::class.java)
	}
}
