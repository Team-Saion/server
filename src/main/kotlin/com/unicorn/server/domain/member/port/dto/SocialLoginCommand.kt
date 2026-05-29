package com.unicorn.server.domain.member.port.dto

import com.unicorn.server.domain.member.enums.SocialProvider

// SocialLoginCommand - 검증된 소셜 사용자 정보로 로그인을 요청하는 입력 DTO다.
// Step 4 구현 시 최초 가입의 초기 nickname은 command.name으로 세팅한다.
data class SocialLoginCommand(
	val provider: SocialProvider,
	val providerId: String,
	val email: String,
	val name: String,
)
