package com.unicorn.server.domain.user.event

import com.unicorn.server.common.domain.Event

data class UserWithdrawnEvent(
	val userId: String,
) : Event()
