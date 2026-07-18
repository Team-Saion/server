package com.unicorn.server.domain.invitation.exception

import com.unicorn.server.common.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class InvitationErrorCode(
	override val code: String,
	override val message: String,
	override val httpStatus: HttpStatus,
) : ErrorCode {
	INVITATION_TARGET_INVALID("I400_1", "잘못된 초대 대상입니다.", HttpStatus.BAD_REQUEST),
	INVITATION_TOKEN_INVALID("I400_4", "유효하지 않은 초대 토큰입니다.", HttpStatus.BAD_REQUEST),
	INVITATION_NOT_AUTHORIZED("I403_1", "초대장을 발급할 권한이 없습니다.", HttpStatus.FORBIDDEN),
	INVITATION_SELF_APPROVAL_FORBIDDEN("I403_2", "자신이 발급한 초대장은 수락할 수 없습니다.", HttpStatus.FORBIDDEN),
	INVITATION_NOT_FOUND("I404_1", "초대장을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
	INVITATION_EXPIRED("I410_1", "만료된 초대장이에요. 초대자에게 다시 요청해주세요.", HttpStatus.GONE),
}
