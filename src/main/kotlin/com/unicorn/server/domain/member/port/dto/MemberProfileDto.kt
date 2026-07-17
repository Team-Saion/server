package com.unicorn.server.domain.member.port.dto

import com.unicorn.server.domain.member.enums.AvatarColor

data class MemberProfileDto(
	val memberId: String,
	val nickname: String,
	val avatarColor: AvatarColor,
	val profileImageKey: String?,
	val kakaoNickname: String?,
	val active: Boolean,
)
