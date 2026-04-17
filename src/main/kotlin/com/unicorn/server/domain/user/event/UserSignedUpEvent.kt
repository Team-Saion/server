package com.unicorn.server.domain.user.event

import com.unicorn.server.common.domain.Event

data class UserSignedUpEvent(
	val userId: String,
	val email: String,
) : Event()
