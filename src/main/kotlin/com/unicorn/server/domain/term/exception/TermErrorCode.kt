package com.unicorn.server.domain.term.exception

import com.unicorn.server.common.exception.ErrorCode
import org.springframework.http.HttpStatus

// TermErrorCode - 약관(Term) 도메인에서 발생하는 비즈니스 오류 코드를 정의한다.
enum class TermErrorCode(
	override val code: String,
	override val message: String,
	override val httpStatus: HttpStatus,
) : ErrorCode {
	// Client Error
	REQUIRED_TERMS_NOT_AGREED("T400_1", "Required terms must be agreed", HttpStatus.BAD_REQUEST),
	INVALID_TERM_ID("T400_2", "One or more term IDs are invalid", HttpStatus.BAD_REQUEST),

	// Server Error
	INVALID_TERM_DATA("T500_1", "Stored term data is invalid or corrupted", HttpStatus.INTERNAL_SERVER_ERROR),
}
