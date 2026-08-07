package com.unicorn.server.domain.member.port.dto

// AppleUserInfo - 애플 ID Token에서 추출한 사용자 정보를 담는다.
data class AppleUserInfo(
	val providerId: String,
	val email: String?,
)
