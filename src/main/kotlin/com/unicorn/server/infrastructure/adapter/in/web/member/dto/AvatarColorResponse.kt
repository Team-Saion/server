package com.unicorn.server.infrastructure.adapter.`in`.web.member.dto

import com.unicorn.server.domain.member.enums.AvatarColor

data class AvatarColorResponse(
	val code: String,
	val hex: String,
) {
	companion object {
		fun from(avatarColor: AvatarColor): AvatarColorResponse = AvatarColorResponse(
			code = avatarColor.code,
			hex = avatarColor.hex,
		)
	}
}
