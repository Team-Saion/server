package com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.customizer

import com.unicorn.server.common.exception.ErrorCode
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiErrorCodeExample
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiErrorCodeExamples
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiSuccessCodeExample
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.NoErrorCode
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.examples.Example
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import org.springdoc.core.customizers.OperationCustomizer
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.util.ClassUtils
import org.springframework.web.method.HandlerMethod
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.primaryConstructor

@Component
class ExampleResponseCustomizer : OperationCustomizer {

	override fun customize(operation: Operation, handlerMethod: HandlerMethod): Operation {
		handleErrorExamples(operation, handlerMethod)
		handleSuccessExample(operation, handlerMethod)
		return operation
	}

	private fun handleErrorExamples(operation: Operation, handlerMethod: HandlerMethod) {
		val examples = buildList {
			findAnnotation(handlerMethod, ApiErrorCodeExample::class.java)?.let(::add)
			findAnnotation(handlerMethod, ApiErrorCodeExamples::class.java)?.value?.let(::addAll)
		}

		examples
			.mapNotNull(::resolveErrorEntry)
			.groupBy { it.status.toString() }
			.forEach { (statusCode, errorEntries) ->
				val responseExamples = errorEntries.associate { errorEntry ->
					errorEntry.exampleName to Example().apply {
						summary = errorEntry.message
						value = buildErrorBody(errorEntry)
					}
				}

				addExamples(
					response = getOrCreateResponse(operation, statusCode, statusDescription(statusCode)),
					examples = responseExamples,
				)
			}
	}

	private fun handleSuccessExample(operation: Operation, handlerMethod: HandlerMethod) {
		val annotation = findAnnotation(handlerMethod, ApiSuccessCodeExample::class.java) ?: return
		val statusCode = findSuccessStatusCode(operation)
		val exampleData = generateExample(annotation.value)
		val example = Example().apply {
			summary = "성공 응답"
			value = buildSuccessBody(exampleData)
		}

		addExamples(
			response = getOrCreateResponse(operation, statusCode, "성공"),
			examples = mapOf("success" to example),
		)
	}

	private fun resolveErrorEntry(example: ApiErrorCodeExample): ErrorEntry? {
		if (example.codeType != NoErrorCode::class) {
			val errorCode = example.codeType.java.enumConstants
				.orEmpty()
				.firstOrNull { it.name == example.code } as? ErrorCode
				?: throw IllegalArgumentException(
					"Invalid API error code example: ${example.codeType.simpleName}.${example.code}",
				)

			return ErrorEntry(
				status = errorCode.httpStatus.value(),
				message = errorCode.message,
				code = errorCode.code,
				exampleName = example.exampleName.ifBlank { example.code },
			)
		}

		if (example.status <= 0) {
			return null
		}

		val code = example.code.ifBlank { "ERROR_${example.status}" }
		val exampleName = example.exampleName.ifBlank { code }
		return ErrorEntry(
			status = example.status,
			message = example.message.ifBlank { "에러" },
			code = code,
			exampleName = exampleName,
		)
	}

	private fun buildErrorBody(errorEntry: ErrorEntry): Map<String, Any?> =
		mapOf(
			"isSuccess" to false,
			"data" to null,
			"errorCode" to errorEntry.code,
			"message" to errorEntry.message,
			"timestamp" to EXAMPLE_TIMESTAMP,
		)

	private fun buildSuccessBody(data: Any?): Map<String, Any?> =
		mapOf(
			"isSuccess" to true,
			"data" to data,
			"errorCode" to null,
			"message" to null,
			"timestamp" to EXAMPLE_TIMESTAMP,
		)

	private fun generateExample(kClass: KClass<*>): Any? {
		if (kClass == Unit::class || kClass == Void::class) {
			return null
		}

		return sampleObject(kClass, depth = 0)
	}

	private fun sampleObject(kClass: KClass<*>, depth: Int): Any? {
		if (kClass.java.isEnum) {
			return (kClass.java.enumConstants.firstOrNull() as? Enum<*>)?.name
		}

		if (depth >= MAX_SAMPLE_DEPTH) {
			return emptyMap<String, Any?>()
		}

		val primaryConstructor = kClass.primaryConstructor ?: return emptyMap<String, Any?>()
		return primaryConstructor.parameters.associate { parameter ->
			(parameter.name ?: "value") to sampleValue(parameter.type, parameter.name, depth + 1)
		}
	}

	private fun sampleValue(type: KType, name: String?, depth: Int): Any? =
		when (val classifier = type.classifier) {
			String::class -> sampleString(name)
			Int::class -> 1
			Long::class -> 1L
			Double::class -> 1.0
			Float::class -> 1.0f
			Boolean::class -> true
			BigDecimal::class -> BigDecimal("1000.00")
			BigInteger::class -> BigInteger("1000")
			LocalDate::class -> LocalDate.parse(EXAMPLE_DATE)
			LocalDateTime::class -> LocalDateTime.parse(EXAMPLE_TIMESTAMP)
			LocalTime::class -> LocalTime.parse(EXAMPLE_TIME)
			Instant::class -> Instant.parse("${EXAMPLE_TIMESTAMP}Z")
			OffsetDateTime::class -> OffsetDateTime.parse("${EXAMPLE_TIMESTAMP}+09:00")
			ZonedDateTime::class -> ZonedDateTime.parse("${EXAMPLE_TIMESTAMP}+09:00[Asia/Seoul]")
			UUID::class -> UUID.fromString("11111111-1111-1111-1111-111111111111")
			List::class, Collection::class, Set::class -> sampleCollection(type, name, depth)
			Map::class -> emptyMap<String, Any?>()
			else -> sampleNestedObject(classifier, depth)
		}

	private fun sampleCollection(type: KType, name: String?, depth: Int): List<Any?> {
		val elementType = type.arguments.firstOrNull()?.type ?: return emptyList()
		return listOf(sampleValue(elementType, name, depth + 1))
	}

	private fun sampleNestedObject(classifier: Any?, depth: Int): Any? {
		val kClass = classifier as? KClass<*> ?: return null
		return sampleObject(kClass, depth)
	}

	private fun sampleString(name: String?): String =
		when (name) {
			"memberId" -> "MB20260101000000001"
			"circleId", "targetId" -> "CC20260101000000001"
			"invitationId" -> "IV20260101000000001"
			"id" -> "MB20260101000000001"
			"userId" -> "user-1"
			"email" -> "user@example.com"
			"username" -> "unicorn"
			"password" -> "password123"
			else -> name ?: "value"
		}

	private fun addExamples(response: ApiResponse, examples: Map<String, Example>) {
		val content = response.content ?: Content().also { response.content = it }
		val mediaType = content[APPLICATION_JSON_VALUE]
			?: MediaType().also { content.addMediaType(APPLICATION_JSON_VALUE, it) }
		val existingExamples = mediaType.examples ?: linkedMapOf<String, Example>().also { mediaType.examples = it }

		examples.forEach { (name, example) ->
			existingExamples[name] = example
		}
	}

	private fun getOrCreateResponse(operation: Operation, statusCode: String, description: String): ApiResponse {
		val responses = operation.responses ?: ApiResponses().also { operation.responses = it }
		val response = responses[statusCode]
			?: ApiResponse().description(description).also { responses.addApiResponse(statusCode, it) }

		if (response.description.isNullOrBlank()) {
			response.description = description
		}

		return response
	}

	private fun findSuccessStatusCode(operation: Operation): String =
		operation.responses
			?.keys
			?.firstOrNull { it.startsWith("2") }
			?: "200"

	private fun statusDescription(statusCode: String): String =
		statusCode.toIntOrNull()
			?.let { runCatching { HttpStatus.valueOf(it).reasonPhrase }.getOrNull() }
			?: "Error"

	private fun <A : Annotation> findAnnotation(handlerMethod: HandlerMethod, annotationType: Class<A>): A? {
		AnnotatedElementUtils.findMergedAnnotation(handlerMethod.method, annotationType)?.let { return it }

		return ClassUtils.getAllInterfacesForClass(handlerMethod.beanType).firstNotNullOfOrNull { interfaceType ->
			findAnnotationOnInterface(interfaceType, handlerMethod, annotationType)
		}
	}

	private fun <A : Annotation> findAnnotationOnInterface(
		interfaceType: Class<*>,
		handlerMethod: HandlerMethod,
		annotationType: Class<A>,
	): A? {
		val annotation = runCatching {
			interfaceType.getMethod(handlerMethod.method.name, *handlerMethod.method.parameterTypes)
		}.getOrNull()?.let { method ->
			AnnotatedElementUtils.findMergedAnnotation(method, annotationType)
		}

		return annotation ?: interfaceType.interfaces.firstNotNullOfOrNull { parentInterface ->
			findAnnotationOnInterface(parentInterface, handlerMethod, annotationType)
		}
	}

	private companion object {
		private const val APPLICATION_JSON_VALUE = "application/json"
		private const val EXAMPLE_DATE = "2024-01-01"
		private const val EXAMPLE_TIME = "00:00:00"
		private const val EXAMPLE_TIMESTAMP = "2024-01-01T00:00:00"
		private const val MAX_SAMPLE_DEPTH = 2
	}

	private data class ErrorEntry(
		val status: Int,
		val message: String,
		val code: String,
		val exampleName: String,
	)
}
