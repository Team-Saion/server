package com.unicorn.server.domain.circle.port.dto

data class CircleMemberDto(
	val memberId: String,
	val nickname: String,
	val role: String,
	val active: Boolean,
)
