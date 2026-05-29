package com.unicorn.server.domain.member.exception

import com.unicorn.server.common.exception.BusinessException

// WithdrawnMemberException - 탈퇴한 멤버가 접근을 시도할 때 사용한다.
class WithdrawnMemberException(memberId: String) :
	BusinessException(MemberErrorCode.WITHDRAWN_MEMBER, "memberId=$memberId")
