package com.unicorn.server.domain.schedule.port.dto

import com.unicorn.server.domain.member.enums.AvatarColor

data class TodoMemberResult(
	val memberId: String,
	val nickname: String,
	val avatarColor: AvatarColor,
	val checked: Boolean,
)
