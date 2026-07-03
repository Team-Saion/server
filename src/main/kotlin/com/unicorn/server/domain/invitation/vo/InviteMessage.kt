package com.unicorn.server.domain.invitation.vo

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.domain.invitation.exception.InvitationErrorCode

@JvmInline
value class InviteMessage(val value: String) {
	init {
		val trimmed = value.trim()
		if (trimmed.isBlank() || trimmed.length > 50) {
			throw BusinessException(InvitationErrorCode.INVITE_MESSAGE_INVALID)
		}
	}
}
