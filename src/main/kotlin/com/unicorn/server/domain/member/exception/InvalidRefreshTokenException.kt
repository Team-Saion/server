package com.unicorn.server.domain.member.exception

import com.unicorn.server.common.exception.BusinessException

// InvalidRefreshTokenException - refresh token 검증 또는 활성 상태 확인 실패 시 사용한다.
class InvalidRefreshTokenException(detail: String? = null) :
	BusinessException(MemberErrorCode.INVALID_REFRESH_TOKEN, detail)
