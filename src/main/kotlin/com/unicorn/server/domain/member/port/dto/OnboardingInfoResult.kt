package com.unicorn.server.domain.member.port.dto

data class OnboardingInfoResult(
	val kakaoNickname: String?,
	val kakaoProfileImageUrl: String?,
	val avatarColor: String,
)
