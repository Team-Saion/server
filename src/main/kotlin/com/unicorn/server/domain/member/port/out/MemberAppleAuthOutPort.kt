package com.unicorn.server.domain.member.port.out

import com.unicorn.server.domain.member.port.dto.AppleUserInfo

// MemberAppleAuthOutPort - 애플 ID Token을 검증하고 사용자 정보를 반환하는 포트를 정의한다.
interface MemberAppleAuthOutPort {
	fun verify(idToken: String): AppleUserInfo
}
