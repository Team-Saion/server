package com.unicorn.server.infrastructure.adapter.`in`.web.member.dto

import com.unicorn.server.domain.member.port.dto.SocialLoginResult

data class KakaoLoginResponse(
	val accessToken: String,
	val refreshToken: String,
	val isNewMember: Boolean,
) {
	companion object {
		fun from(result: SocialLoginResult): KakaoLoginResponse = KakaoLoginResponse(
			accessToken = result.tokenPair.accessToken,
			refreshToken = result.tokenPair.refreshToken,
			isNewMember = result.isNewMember,
		)
	}
}
