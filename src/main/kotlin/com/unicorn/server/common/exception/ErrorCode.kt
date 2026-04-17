package com.unicorn.server.common.exception

import org.springframework.http.HttpStatus

interface ErrorCode {
	val code: String
	val message: String
	val httpStatus: HttpStatus
}
