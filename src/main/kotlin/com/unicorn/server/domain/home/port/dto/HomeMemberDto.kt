package com.unicorn.server.domain.home.port.dto

data class HomeMemberDto(
	val memberId: String,
	val nickname: String,
	val avatarColor: String,
	val kakaoNickname: String?,
	val isMe: Boolean,
	val role: String,
)
