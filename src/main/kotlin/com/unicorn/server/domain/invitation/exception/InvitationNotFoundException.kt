package com.unicorn.server.domain.invitation.exception

import com.unicorn.server.common.exception.BusinessException

class InvitationNotFoundException(token: String) :
	BusinessException(InvitationErrorCode.INVITATION_NOT_FOUND, "token=$token")
