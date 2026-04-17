package com.unicorn.server.common.annotation

import org.springframework.core.annotation.AliasFor
import org.springframework.stereotype.Component

// 영속성 처리를 담당한다는 것을 명시한다.
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Component
annotation class PersistenceAdapter(
	@get:AliasFor(annotation = Component::class)
	val value: String = "",
)
