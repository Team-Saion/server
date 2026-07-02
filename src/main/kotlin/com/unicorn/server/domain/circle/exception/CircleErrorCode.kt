package com.unicorn.server.domain.circle.exception

import com.unicorn.server.common.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class CircleErrorCode(
	override val code: String,
	override val message: String,
	override val httpStatus: HttpStatus,
) : ErrorCode {
	CIRCLE_NAME_BLANK("C400_1", "써클 이름을 입력해주세요.", HttpStatus.BAD_REQUEST),
	CIRCLE_NAME_TOO_LONG("C400_2", "20자 이내로 입력해주세요.", HttpStatus.BAD_REQUEST),
	CIRCLE_NAME_INVALID_CHARSET("C400_3", "한글, 영문, 숫자만 사용할 수 있어요.", HttpStatus.BAD_REQUEST),
	CIRCLE_NICKNAME_INVALID("C400_4", "유효하지 않은 닉네임입니다.", HttpStatus.BAD_REQUEST),
	INITIATOR_CANNOT_LEAVE("C400_5", "생성자는 탈퇴할 수 없습니다.", HttpStatus.BAD_REQUEST),
	ALREADY_JOINED("C400_6", "이미 참여한 써클입니다.", HttpStatus.BAD_REQUEST),
	CIRCLE_ACCESS_DENIED("C403_1", "써클에 접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
	CIRCLE_NOT_FOUND("C404_1", "써클을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
}
