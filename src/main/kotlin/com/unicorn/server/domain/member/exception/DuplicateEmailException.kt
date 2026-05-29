package com.unicorn.server.domain.member.exception

import com.unicorn.server.common.exception.BusinessException

// DuplicateEmailException - 서비스 내 이메일 중복이 감지될 때 사용한다.
class DuplicateEmailException(email: String) :
	BusinessException(MemberErrorCode.DUPLICATE_EMAIL, "email=$email")
