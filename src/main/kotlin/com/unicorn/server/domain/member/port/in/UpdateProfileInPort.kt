package com.unicorn.server.domain.member.port.`in`

import com.unicorn.server.domain.member.Member
import com.unicorn.server.domain.member.port.dto.UpdateProfileCommand

// UpdateProfileInPort - 멤버 프로필 변경 유스케이스 진입점을 정의한다.
interface UpdateProfileInPort {
	// 멤버 식별자와 변경 명령으로 프로필을 갱신한다.
	fun updateProfile(memberId: String, command: UpdateProfileCommand): Member
}
