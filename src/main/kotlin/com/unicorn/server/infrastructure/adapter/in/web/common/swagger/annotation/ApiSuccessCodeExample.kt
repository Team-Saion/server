package com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation

import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ApiSuccessCodeExample(
	val value: KClass<*> = Unit::class,
)
