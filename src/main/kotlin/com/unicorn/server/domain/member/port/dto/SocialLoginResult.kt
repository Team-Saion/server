package com.unicorn.server.domain.member.port.dto

data class SocialLoginResult(
	val tokenPair: TokenPair,
	val isNewMember: Boolean,
)
