package com.unicorn.server.domain.member.event

import com.unicorn.server.common.domain.Event

// MemberWithdrawnEvent - 멤버 탈퇴 완료 사실을 후속 처리 어댑터에 전달한다.
class MemberWithdrawnEvent(
	val memberId: String,
) : Event()
