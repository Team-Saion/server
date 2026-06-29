package com.unicorn.server.infrastructure.adapter.`in`.web.member.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "온보딩 완료 요청")
data class CompleteOnboardingRequest(
	@field:Schema(
		description = "온보딩에서 설정할 닉네임. 서버에서 앞뒤 공백 제거 후 2자 이상 10자 이하, 한글/영문/숫자만 허용한다.",
		example = "홍길동",
	)
	val nickname: String,
)
