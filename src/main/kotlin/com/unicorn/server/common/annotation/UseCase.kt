package com.unicorn.server.common.annotation

import org.springframework.core.annotation.AliasFor
import org.springframework.stereotype.Service

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Service
annotation class UseCase(
	@get:AliasFor(annotation = Service::class)
	val value: String = "",
)
