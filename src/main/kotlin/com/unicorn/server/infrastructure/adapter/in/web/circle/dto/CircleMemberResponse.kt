package com.unicorn.server.infrastructure.adapter.`in`.web.circle.dto

import com.unicorn.server.domain.home.port.dto.HomeMemberDto

data class CircleMemberResponse(
	val memberId: String,
	val nickname: String,
	val avatarColor: String,
	val kakaoNickname: String?,
	val isMe: Boolean,
	val role: String,
) {
	companion object {
		fun from(view: HomeMemberDto): CircleMemberResponse = CircleMemberResponse(
			memberId = view.memberId,
			nickname = view.nickname,
			avatarColor = view.avatarColor,
			kakaoNickname = view.kakaoNickname,
			isMe = view.isMe,
			role = view.role,
		)
	}
}
