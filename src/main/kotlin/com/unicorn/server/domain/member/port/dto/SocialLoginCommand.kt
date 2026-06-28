package com.unicorn.server.domain.member.port.dto

import com.unicorn.server.domain.member.enums.SocialProvider

// SocialLoginCommand - 검증된 소셜 사용자 정보로 로그인을 요청하는 입력 DTO다.
data class SocialLoginCommand(
	val provider: SocialProvider,
	val providerId: String,
	val email: String?,
	val name: String?,
	val kakaoNickname: String? = null,
	val kakaoProfileImageUrl: String? = null,
)
