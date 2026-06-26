package com.unicorn.server.domain.member.port.dto

import com.unicorn.server.domain.member.enums.AvatarColor

data class OnboardingInfoResult(
	val socialNickname: String?,
	val socialProfileImageUrl: String?,
	val avatarColor: AvatarColor,
)
