package com.unicorn.server.domain.member.port.dto

// KakaoUserInfo - 카카오 ID Token에서 추출한 사용자 정보를 담는다.
data class KakaoUserInfo(
	val providerId: String,
	val email: String,
	val name: String,
)
