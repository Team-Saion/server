package com.unicorn.server.domain.member.exception

import com.unicorn.server.common.exception.BusinessException

// InvalidSocialTokenException - 소셜 ID Token 검증 실패 시 사용한다.
class InvalidSocialTokenException(detail: String? = null) :
	BusinessException(MemberErrorCode.INVALID_SOCIAL_TOKEN, detail)
