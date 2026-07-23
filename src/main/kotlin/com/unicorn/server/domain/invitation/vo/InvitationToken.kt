package com.unicorn.server.domain.invitation.vo

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.domain.invitation.exception.InvitationErrorCode

@JvmInline
value class InvitationToken(val value: String) {
	init {
		if (value.length != LENGTH || !TOKEN_PATTERN.matches(value)) {
			throw BusinessException(InvitationErrorCode.INVITATION_TOKEN_INVALID)
		}
	}

	companion object {
		const val LENGTH = 6
		private val TOKEN_PATTERN = Regex("^[A-Za-z0-9_-]+$")
	}
}
