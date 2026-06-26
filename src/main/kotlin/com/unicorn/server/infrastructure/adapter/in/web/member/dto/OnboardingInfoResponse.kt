package com.unicorn.server.infrastructure.adapter.`in`.web.member.dto

import com.unicorn.server.domain.member.port.dto.OnboardingInfoResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "온보딩 사전정보 응답")
data class OnboardingInfoResponse(
	@field:Schema(description = "소셜 플랫폼 프로필 닉네임", example = "홍길동", nullable = true)
	val socialNickname: String?,

	@field:Schema(description = "소셜 플랫폼 프로필 이미지 URL", example = "https://example.com/profile.png", nullable = true)
	val socialProfileImageUrl: String?,

	@field:Schema(description = "멤버 아바타 기본 색상")
	val avatarColor: AvatarColorResponse,
) {
	companion object {
		fun from(result: OnboardingInfoResult): OnboardingInfoResponse = OnboardingInfoResponse(
			socialNickname = result.socialNickname,
			socialProfileImageUrl = result.socialProfileImageUrl,
			avatarColor = AvatarColorResponse.from(result.avatarColor),
		)
	}
}
