package com.unicorn.server.domain.invitation.vo

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.domain.invitation.exception.InvitationErrorCode

@JvmInline
value class InvitationToken(val value: String) {
	init {
		if (value.length !in 32..64 || !TOKEN_PATTERN.matches(value)) {
			throw BusinessException(InvitationErrorCode.INVITATION_TOKEN_INVALID)
		}
	}

	companion object {
		private val TOKEN_PATTERN = Regex("^[A-Za-z0-9_-]+$")
	}
}
