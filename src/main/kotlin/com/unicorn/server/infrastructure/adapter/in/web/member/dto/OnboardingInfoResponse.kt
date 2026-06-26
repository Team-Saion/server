package com.unicorn.server.infrastructure.adapter.`in`.web.member.dto

import com.unicorn.server.domain.member.port.dto.OnboardingInfoResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "온보딩 사전정보 응답")
data class OnboardingInfoResponse(
	@field:Schema(description = "카카오 프로필 닉네임", example = "홍길동", nullable = true)
	val kakaoNickname: String?,

	@field:Schema(description = "카카오 프로필 이미지 URL", example = "https://example.com/profile.png", nullable = true)
	val kakaoProfileImageUrl: String?,

	@field:Schema(description = "멤버 아바타 기본 색상", example = "#A1B2C3")
	val avatarColor: String,
) {
	companion object {
		fun from(result: OnboardingInfoResult): OnboardingInfoResponse = OnboardingInfoResponse(
			kakaoNickname = result.kakaoNickname,
			kakaoProfileImageUrl = result.kakaoProfileImageUrl,
			avatarColor = result.avatarColor,
		)
	}
}
