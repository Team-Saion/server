package com.unicorn.server.infrastructure.aop

import org.aspectj.lang.annotation.Pointcut

class Pointcuts {
	@Pointcut("execution(* com.unicorn.server..*Controller.*(..))")
	fun allController() {
	}
}
