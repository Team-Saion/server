package com.unicorn.server.domain.member.port.dto

// UpdateProfileCommand - 멤버 프로필 변경 요청 값을 담는다.
data class UpdateProfileCommand(
	val nickname: String,
)
