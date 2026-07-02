package com.unicorn.server.domain.invitation.exception

import com.unicorn.server.common.exception.BusinessException

class InvitationExpiredException(invitationId: String) :
	BusinessException(InvitationErrorCode.INVITATION_EXPIRED, "invitationId=$invitationId")
