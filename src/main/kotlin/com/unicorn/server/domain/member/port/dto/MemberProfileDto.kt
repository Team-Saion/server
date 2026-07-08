package com.unicorn.server.domain.member.port.dto

data class MemberProfileDto(
	val memberId: String,
	val nickname: String,
	val avatarColor: String,
	val kakaoNickname: String?,
	val active: Boolean,
)
