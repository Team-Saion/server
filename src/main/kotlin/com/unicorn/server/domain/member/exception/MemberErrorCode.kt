package com.unicorn.server.domain.member.exception

import com.unicorn.server.common.exception.ErrorCode
import org.springframework.http.HttpStatus

// MemberErrorCode - 멤버 도메인에서 발생하는 비즈니스 오류 코드를 정의한다.
enum class MemberErrorCode(
	override val code: String,
	override val message: String,
	override val httpStatus: HttpStatus,
) : ErrorCode {
	// Not Found
	MEMBER_NOT_FOUND("M404_1", "Member not found", HttpStatus.NOT_FOUND),

	// Unauthorized
	INVALID_SOCIAL_TOKEN("M401_1", "Invalid social login token", HttpStatus.UNAUTHORIZED),
	INVALID_REFRESH_TOKEN("M401_2", "Invalid or expired refresh token", HttpStatus.UNAUTHORIZED),

	// Conflict
	DUPLICATE_EMAIL("M409_1", "Email already exists", HttpStatus.CONFLICT),

	// Bad Request
	INVALID_NICKNAME(
		"M400_1",
		"Nickname must be 2-10 characters using Korean, English, or numbers only",
		HttpStatus.BAD_REQUEST,
	),

	// Gone
	MEMBER_ALREADY_DELETED("M410_1", "Member is already deleted", HttpStatus.GONE),
	WITHDRAWN_MEMBER("M410_2", "Member has withdrawn", HttpStatus.GONE),
}
