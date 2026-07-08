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
	CIRCLE_NAME_INVALID_CHARSET("C400_3", "사용할 수 없는 문자가 포함되어 있어요.", HttpStatus.BAD_REQUEST),
	CIRCLE_NICKNAME_INVALID("C400_4", "유효하지 않은 닉네임입니다.", HttpStatus.BAD_REQUEST),
	INITIATOR_CANNOT_LEAVE("C400_5", "생성자는 탈퇴할 수 없습니다.", HttpStatus.BAD_REQUEST),
	ALREADY_JOINED("C400_6", "이미 참여한 써클입니다.", HttpStatus.BAD_REQUEST),
	ALREADY_HAS_ACTIVE_CIRCLE("C400_7", "현재 참여 중인 써클이 있어 새로 생성하거나 참여할 수 없습니다.", HttpStatus.BAD_REQUEST),
	CIRCLE_MEMBER_LIMIT_EXCEEDED("C400_8", "써클은 최대 10명까지 참여할 수 있습니다.", HttpStatus.BAD_REQUEST),
	INITIATOR_DELEGATION_SELF_FORBIDDEN("C400_9", "자기 자신에게는 권한을 위임할 수 없습니다.", HttpStatus.BAD_REQUEST),
	INITIATOR_DELEGATION_TARGET_INVALID("C400_10", "같은 써클의 다른 활성 구성원에게만 권한을 위임할 수 있습니다.", HttpStatus.BAD_REQUEST),
	CIRCLE_ACCESS_DENIED("C403_1", "써클에 접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
	INITIATOR_DELEGATION_FORBIDDEN("C403_2", "써클 방장만 권한을 위임할 수 있습니다.", HttpStatus.FORBIDDEN),
	CIRCLE_NOT_FOUND("C404_1", "써클을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
}
