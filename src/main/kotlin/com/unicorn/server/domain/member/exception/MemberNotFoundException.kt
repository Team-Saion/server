package com.unicorn.server.domain.member.exception

import com.unicorn.server.common.exception.BusinessException

// MemberNotFoundException - 요청한 멤버를 찾을 수 없을 때 사용한다.
class MemberNotFoundException(memberId: String) :
	BusinessException(MemberErrorCode.MEMBER_NOT_FOUND, "memberId=$memberId")
