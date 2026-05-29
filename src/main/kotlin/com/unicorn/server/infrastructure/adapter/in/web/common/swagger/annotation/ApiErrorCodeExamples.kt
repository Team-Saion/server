package com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ApiErrorCodeExamples(
	vararg val value: ApiErrorCodeExample,
)
