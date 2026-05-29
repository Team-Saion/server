package com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation

import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ApiErrorCodeExample(
	val codeType: KClass<out Enum<*>> = NoErrorCode::class,
	val code: String = "",
	val status: Int = 0,
	val message: String = "",
	val exampleName: String = "",
)

enum class NoErrorCode
