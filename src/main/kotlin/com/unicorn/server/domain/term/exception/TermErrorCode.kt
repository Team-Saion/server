package com.unicorn.server.domain.term.exception

import com.unicorn.server.common.exception.ErrorCode
import org.springframework.http.HttpStatus

/**
약관(Term) 도메인에서 발생하는 비즈니스 오류 코드를 정의한다.

현재는 조회 전용 API만 존재해서 사용자 입력에서 발생하는 오류는 없고, 저장된 데이터
자체가 비정상인 경우(예: term_code가 TermCode enum에 없는 값, 영속화 전 엔티티를
도메인으로 변환 시도)만 다룬다.
*/
enum class TermErrorCode(
	override val code: String,
	override val message: String,
	override val httpStatus: HttpStatus,
) : ErrorCode {
	// Client Error
	REQUIRED_TERMS_NOT_AGREED("T400_1", "Required terms must be agreed", HttpStatus.BAD_REQUEST),

	// Server Error
	INVALID_TERM_DATA("T500_1", "Stored term data is invalid or corrupted", HttpStatus.INTERNAL_SERVER_ERROR),
}
